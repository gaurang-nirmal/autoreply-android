package com.psspl.autoreply.backup

import android.content.Context
import android.net.Uri
import com.psspl.autoreply.database.entity.KeywordRuleEntity
import com.psspl.autoreply.repository.KeywordRuleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Foundation class for keyword rule backup and restore functionality.
 *
 * Current implementation: stub / scaffolding only.
 * Future implementation will:
 *  - Export all rules to a JSON file on the device storage
 *  - Import rules from a previously exported JSON file
 *  - Support Google Drive backup (optional)
 */
@Singleton
class KeywordRuleBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keywordRuleRepository: KeywordRuleRepository,
) {

    /**
     * Export all keyword rules to a JSON file at the given [uri].
     *
     * @return true on success, false on failure.
     */
    suspend fun exportRules(uri: Uri): Boolean {
        return try {
            val rules = keywordRuleRepository.allRules.first()
            val json = buildJsonArray(rules)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(json.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Import keyword rules from a JSON file at the given [uri].
     * Existing rules are preserved — duplicates (by keyword, case-insensitive) are skipped.
     *
     * @return number of rules imported, or -1 on failure.
     */
    suspend fun importRules(uri: Uri): Int {
        return try {
            val jsonText = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return -1

            // Build a set of existing keywords to skip duplicates
            val existingKeywords = keywordRuleRepository.allRules.first()
                .map { it.keyword.trim().lowercase() }
                .toHashSet()

            val jsonArray = org.json.JSONArray(jsonText)
            var imported = 0

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val keyword = obj.getString("keyword")

                // Skip if keyword already exists (case-insensitive)
                if (keyword.trim().lowercase() in existingKeywords) continue

                val rule = KeywordRuleEntity(
                    id = 0, // let Room auto-generate
                    keyword = keyword,
                    replyText = obj.getString("replyText"),
                    matchType = obj.optString("matchType", "EXACT"),
                    replyOptions = obj.optString("replyOptions", ""),
                    sendEmail = obj.optBoolean("sendEmail", false),
                    isActive = obj.optBoolean("isActive", true),
                    appPackage = obj.optString("appPackage", ""),
                    createdAt = System.currentTimeMillis(),
                )
                keywordRuleRepository.insert(rule)
                existingKeywords.add(keyword.trim().lowercase()) // guard same-file dupes
                imported++
            }
            imported
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun buildJsonArray(rules: List<KeywordRuleEntity>): String {
        val entries = rules.joinToString(",\n") { rule ->
            """
            {
              "id": ${rule.id},
              "keyword": ${rule.keyword.toJsonString()},
              "replyText": ${rule.replyText.toJsonString()},
              "matchType": "${rule.matchType}",
              "replyOptions": "${rule.replyOptions}",
              "sendEmail": ${rule.sendEmail},
              "isActive": ${rule.isActive},
              "appPackage": "${rule.appPackage}",
              "createdAt": ${rule.createdAt}
            }
            """.trimIndent()
        }
        return "[\n$entries\n]"
    }

    private fun String.toJsonString(): String =
        "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""
}
