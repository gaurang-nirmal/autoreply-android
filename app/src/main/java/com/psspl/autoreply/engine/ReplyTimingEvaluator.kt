package com.psspl.autoreply.engine

import com.psspl.autoreply.repository.AppSettingsRepository
import com.psspl.autoreply.repository.ReplyTimingRepository
import com.psspl.autoreply.ui.screens.replytiming.ReplyMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized execution-time evaluator.
 *
 * Resolves the active reply type from [AppSettingsRepository], loads the matching
 * [com.psspl.autoreply.database.entity.ReplyTimingConfigEntity], and decides whether
 * to allow, delay, or block a reply for a given contact.
 *
 * All four [ReplyMode] variants and per-contact reply limits are handled here so the
 * service layer stays thin.
 */
@Singleton
class ReplyTimingEvaluator @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val replyTimingRepository: ReplyTimingRepository,
) {

    /**
     * Returns the [TimingDecision] that governs whether a reply should be sent to
     * [contactKey] right now.
     *
     * The [contactKey] must uniquely identify a sender within a specific app; the
     * recommended format is `"$appPackage:${senderName.trim().lowercase()}"`.
     */
    suspend fun evaluate(contactKey: String): TimingDecision {
        // 1. Derive timing key from the active reply type (uppercase enum → lowercase key)
        val settings = appSettingsRepository.get() ?: return TimingDecision.Allow
        val timingKey = settings.replyType.lowercase() // e.g. "keyword", "menu", "custom"

        // 2. Load the saved config — if none exists yet, use default (allow everything)
        val config = replyTimingRepository.getConfigOnce(timingKey)
            ?: return TimingDecision.Allow

        // 3. Reply limit gate (checked before mode to avoid unnecessary work)
        if (config.replyLimitEnabled) {
            val withinLimit = replyTimingRepository.isReplyAllowed(
                replyType = timingKey,
                contactKey = contactKey,
                replyLimitEnabled = true,
                maxReplies = config.maxReplies,
            )
            if (!withinLimit) return TimingDecision.Block
        }

        // 4. Reply mode gate
        return when (config.replyMode) {

            ReplyMode.EVERY_TIME.name -> TimingDecision.Allow

            ReplyMode.REPLY_AND_WAIT.name -> {
                // Block if the contact was replied to recently (within waitSeconds)
                val tracking = replyTimingRepository.getTracking(timingKey, contactKey)
                if (tracking == null) {
                    TimingDecision.Allow
                } else {
                    val elapsed = System.currentTimeMillis() - tracking.lastReplyAt
                    if (elapsed >= config.waitSeconds * 1_000L) {
                        TimingDecision.Allow
                    } else {
                        TimingDecision.Block
                    }
                }
            }

            ReplyMode.REPLY_AFTER_DELAY.name ->
                // Reply will be sent, but only after delaySeconds
                TimingDecision.Delay(config.delaySeconds)

            ReplyMode.REPLY_ONCE.name -> {
                // Allow only if no prior reply has been sent to this contact
                val tracking = replyTimingRepository.getTracking(timingKey, contactKey)
                if (tracking == null || tracking.replyCount == 0) {
                    TimingDecision.Allow
                } else {
                    TimingDecision.Block
                }
            }

            else -> TimingDecision.Allow
        }
    }

    /**
     * Records that a reply was successfully sent to [contactKey] so that
     * [ReplyMode.REPLY_AND_WAIT], [ReplyMode.REPLY_ONCE], and limit tracking
     * remain accurate on the next call.
     */
    suspend fun recordReply(contactKey: String) {
        val settings = appSettingsRepository.get() ?: return
        replyTimingRepository.incrementReplyCount(settings.replyType.lowercase(), contactKey)
    }
}

// ─── Decision type ────────────────────────────────────────────────────────────

/** The outcome of [ReplyTimingEvaluator.evaluate]. */
sealed class TimingDecision {
    /** Send the reply immediately. */
    object Allow : TimingDecision()

    /** Wait [seconds] seconds before sending. */
    data class Delay(val seconds: Int) : TimingDecision()

    /** Do not send a reply at all (limit reached or still in wait period). */
    object Block : TimingDecision()
}
