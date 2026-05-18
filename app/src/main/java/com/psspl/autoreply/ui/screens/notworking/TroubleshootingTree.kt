package com.psspl.autoreply.ui.screens.notworking

// ── Data model ────────────────────────────────────────────────────────────────

sealed class BotMessage {
    data class Text(val text: String) : BotMessage()

    /** Renders an inline fake-notification card visual inside the bubble. */
    object NotificationVisual : BotMessage()
}

data class BotOption(
    val label: String,
    val nextNodeId: String?,           // null = end of flow
    val action: OptionAction = OptionAction.None,
)

sealed class OptionAction {
    object None : OptionAction()
    object OpenNotificationSettings : OptionAction()
}

data class TroubleshootNode(
    val id: String,
    val messages: List<BotMessage>,
    val options: List<BotOption>,      // empty = terminal node
)

// ── Tree definition ───────────────────────────────────────────────────────────

object TroubleshootingTree {

    const val ROOT_NODE_ID = "root"

    private val nodes: Map<String, TroubleshootNode> = listOf(

        // ── Introduction + first question ─────────────────────────────────────
        TroubleshootNode(
            id = "root",
            messages = listOf(
                BotMessage.Text("👋 Hi! I'm your AutoReply troubleshooting assistant."),
                BotMessage.Text("I'll walk you through a quick checklist to figure out why auto-reply isn't working."),
                BotMessage.Text("First question: when someone messages you, does the notification show a REPLY button like this?"),
                BotMessage.NotificationVisual,
            ),
            options = listOf(
                BotOption("Yes, I see a REPLY button", nextNodeId = "check_notif_access"),
                BotOption("No, there's no REPLY button", nextNodeId = "fix_no_reply_btn"),
            ),
        ),

        // ── Check notification access ─────────────────────────────────────────
        TroubleshootNode(
            id = "check_notif_access",
            messages = listOf(
                BotMessage.Text("Great start! 🎯"),
                BotMessage.Text("Does AutoReply have Notification Access permission on your device?"),
                BotMessage.Text("You can check at:\nSettings → Apps → Special App Access → Notification Access"),
            ),
            options = listOf(
                BotOption("Yes, it's enabled", nextNodeId = "check_toggle"),
                BotOption("No, it's not enabled", nextNodeId = "fix_notif_access"),
                BotOption(
                    "I'm not sure — take me there", nextNodeId = "fix_notif_access",
                    action = OptionAction.OpenNotificationSettings
                ),
            ),
        ),

        // ── Fix: no notification access ───────────────────────────────────────
        TroubleshootNode(
            id = "fix_notif_access",
            messages = listOf(
                BotMessage.Text("Found the issue! ✅"),
                BotMessage.Text("AutoReply needs Notification Access to intercept incoming messages and fire replies."),
                BotMessage.Text("Please enable it:\nSettings → Apps → Special App Access → Notification Access → AutoReply → ON\n\nThen restart the app and try again."),
            ),
            options = listOf(
                BotOption("Done, I enabled it", nextNodeId = "done"),
            ),
        ),

        // ── Check dashboard toggle ────────────────────────────────────────────
        TroubleshootNode(
            id = "check_toggle",
            messages = listOf(
                BotMessage.Text("Good progress! Let's keep going."),
                BotMessage.Text("Is the auto-reply toggle turned ON on the Dashboard screen of this app?"),
            ),
            options = listOf(
                BotOption("Yes, it's ON", nextNodeId = "check_supported_app"),
                BotOption("No, it was OFF", nextNodeId = "fix_toggle_off"),
            ),
        ),

        // ── Fix: toggle was off ───────────────────────────────────────────────
        TroubleshootNode(
            id = "fix_toggle_off",
            messages = listOf(
                BotMessage.Text("That's it! 🎉"),
                BotMessage.Text("The auto-reply service is paused when the toggle is OFF. Head to the Dashboard and tap the toggle to activate it."),
            ),
            options = listOf(
                BotOption("Got it, I'll turn it ON", nextNodeId = "done"),
            ),
        ),

        // ── Check supported apps ──────────────────────────────────────────────
        TroubleshootNode(
            id = "check_supported_app",
            messages = listOf(
                BotMessage.Text("Almost there! One more check."),
                BotMessage.Text("Is the messaging app you're testing (e.g. WhatsApp, Telegram) added and enabled in your Supported Apps list?"),
            ),
            options = listOf(
                BotOption("Yes, it's in the list and enabled", nextNodeId = "check_battery"),
                BotOption("No, I haven't added it", nextNodeId = "fix_add_app"),
            ),
        ),

        // ── Fix: app not in supported list ────────────────────────────────────
        TroubleshootNode(
            id = "fix_add_app",
            messages = listOf(
                BotMessage.Text("Found it! ✅"),
                BotMessage.Text("AutoReply only processes notifications from apps you've explicitly added to the Supported Apps list."),
                BotMessage.Text("Go to the Supported Apps section, find your messaging app, and toggle it ON."),
            ),
            options = listOf(
                BotOption("Got it, adding it now", nextNodeId = "done"),
            ),
        ),

        // ── Check battery optimization ────────────────────────────────────────
        TroubleshootNode(
            id = "check_battery",
            messages = listOf(
                BotMessage.Text("Almost solved! Let's check one common culprit."),
                BotMessage.Text("Is battery optimization enabled for AutoReply on your device?"),
                BotMessage.Text("Some phones aggressively kill background services.\n\nCheck:\nSettings → Battery → Battery Optimization → AutoReply"),
            ),
            options = listOf(
                BotOption("Yes, it's being optimized", nextNodeId = "fix_battery"),
                BotOption("No, it's set to unrestricted", nextNodeId = "check_immutable"),
            ),
        ),

        // ── Fix: battery optimization ─────────────────────────────────────────
        TroubleshootNode(
            id = "fix_battery",
            messages = listOf(
                BotMessage.Text("That's likely the cause! ⚡"),
                BotMessage.Text("Battery optimization kills background services to save power, which stops AutoReply from receiving notification events."),
                BotMessage.Text("Set AutoReply to 'Don't optimize' or 'Unrestricted' in battery settings, then restart the app."),
            ),
            options = listOf(
                BotOption("Done, I disabled optimization", nextNodeId = "done"),
            ),
        ),

        // ── Check FLAG_IMMUTABLE ──────────────────────────────────────────────
        TroubleshootNode(
            id = "check_immutable",
            messages = listOf(
                BotMessage.Text("One last technical check."),
                BotMessage.Text("Are you trying to auto-reply in apps like Instagram, LinkedIn, or Twitter/X?"),
                BotMessage.Text("These apps use a security flag that blocks third-party reply injection."),
            ),
            options = listOf(
                BotOption("Yes, one of those apps", nextNodeId = "fix_immutable"),
                BotOption("No, a different app", nextNodeId = "fix_generic"),
            ),
        ),

        // ── Fix: FLAG_IMMUTABLE ───────────────────────────────────────────────
        TroubleshootNode(
            id = "fix_immutable",
            messages = listOf(
                BotMessage.Text("Identified the limitation. 🔒"),
                BotMessage.Text("This app uses FLAG_IMMUTABLE on its PendingIntent — a system-level security restriction that prevents any external app from injecting a reply into the notification."),
                BotMessage.Text("This is enforced by the OS and cannot be bypassed. An Accessibility Service integration would be required to support these apps, which is a planned feature."),
            ),
            options = listOf(
                BotOption("Understood", nextNodeId = "done"),
            ),
        ),

        // ── Fix: unknown / generic ────────────────────────────────────────────
        TroubleshootNode(
            id = "fix_generic",
            messages = listOf(
                BotMessage.Text("Everything looks correctly configured. 🤔"),
                BotMessage.Text("Here are a few extra steps to try:"),
                BotMessage.Text(
                    "• Force-close and reopen AutoReply\n" +
                            "• Revoke and re-grant Notification Access\n" +
                            "• Reboot your device\n" +
                            "• Check if Do Not Disturb mode is active\n" +
                            "• Ensure the messaging app sends individual message notifications (not grouped)"
                ),
                BotMessage.Text("If the issue persists, the messaging app may be using a locked notification format."),
            ),
            options = listOf(
                BotOption("Thanks, I'll try these", nextNodeId = "done"),
            ),
        ),

        // ── Fix: no reply button in notification ──────────────────────────────
        TroubleshootNode(
            id = "fix_no_reply_btn",
            messages = listOf(
                BotMessage.Text("That's the root cause. 📋"),
                BotMessage.Text("AutoReply works by programmatically pressing the REPLY button inside the notification. If there's no REPLY button, it has no way to send a response."),
                BotMessage.Text(
                    "Try these steps:\n" +
                            "• Open the messaging app's notification settings\n" +
                            "• Enable 'Show message preview' or 'Reply button in notifications'\n" +
                            "• Check if the app has a notification channel that includes reply actions"
                ),
            ),
            options = listOf(
                BotOption("Got it, I'll check the app settings", nextNodeId = "done"),
            ),
        ),

        // ── Done (terminal) ───────────────────────────────────────────────────
        TroubleshootNode(
            id = "done",
            messages = listOf(
                BotMessage.Text("Hope that helped! 🙌"),
                BotMessage.Text("If auto-reply is still not working after following the steps, you can restart this troubleshooter or reach out via the Help section."),
            ),
            options = emptyList(),
        ),

        ).associateBy { it.id }

    fun getNode(id: String): TroubleshootNode? = nodes[id]
}
