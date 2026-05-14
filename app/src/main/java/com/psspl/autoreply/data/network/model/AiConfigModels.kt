package com.psspl.autoreply.data.network.model

import com.google.gson.annotations.SerializedName

// ── Provider metadata (GET /ai-config/providers) ─────────────────────────────

data class AiProviderMeta(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("defaultModel") val defaultModel: String,
    @SerializedName("availableModels") val availableModels: List<String>,
    @SerializedName("extraParams") val extraParams: List<AiParamSpec> = emptyList(),
)

data class AiParamSpec(
    @SerializedName("key") val key: String,
    @SerializedName("label") val label: String,
    @SerializedName("type") val type: String,
    @SerializedName("min") val min: Double? = null,
    @SerializedName("max") val max: Double? = null,
    @SerializedName("default") val default: Any? = null,
)

// ── Existing config (GET /ai-config/:appId) ───────────────────────────────────

data class AiConfigResponse(
    @SerializedName("id") val id: String,
    @SerializedName("provider") val provider: String,
    @SerializedName("config") val config: AiConfigBody,
    @SerializedName("hasApiKey") val hasApiKey: Boolean,
    @SerializedName("useHistory") val useHistory: Boolean,
    @SerializedName("historyTurns") val historyTurns: Int,
    @SerializedName("isEnabled") val isEnabled: Boolean,
)

data class AiConfigBody(
    @SerializedName("model") val model: String,
    @SerializedName("systemPrompt") val systemPrompt: String? = null,
    @SerializedName("temperature") val temperature: Double? = null,
    @SerializedName("maxTokens") val maxTokens: Int? = null,
    @SerializedName("extraParams") val extraParams: Map<String, Any>? = null,
)

// ── Training prompts ─────────────────────────────────────────────────────────

data class TrainingPromptItem(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("isEnabled") val isEnabled: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class CreatePromptRequest(
    @SerializedName("content") val content: String,
    @SerializedName("isEnabled") val isEnabled: Boolean = true,
    @SerializedName("sortOrder") val sortOrder: Int? = null,
)

data class UpdatePromptRequest(
    @SerializedName("content") val content: String? = null,
    @SerializedName("isEnabled") val isEnabled: Boolean? = null,
)

data class PromptBackupResponse(
    @SerializedName("prompts") val prompts: List<BackupPromptItem>,
)

data class BackupPromptItem(
    @SerializedName("content") val content: String,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("isEnabled") val isEnabled: Boolean,
)

data class RestorePromptsRequest(
    @SerializedName("prompts") val prompts: List<BackupPromptItem>,
)

// ── Backend App (GET /apps/active) ───────────────────────────────────────────

data class BackendApp(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("packageName") val packageName: String,
    @SerializedName("isActive") val isActive: Boolean,
)

// ── AI Reply (POST /ai/reply) ─────────────────────────────────────────────────

data class AiReplyRequest(
    @SerializedName("appId") val appId: Int,
    @SerializedName("contactId") val contactId: String,
    @SerializedName("contactName") val contactName: String? = null,
    @SerializedName("incomingMessage") val incomingMessage: String,
    @SerializedName("includeHistory") val includeHistory: Boolean = true,
)

data class AiReplyResponse(
    @SerializedName("reply") val reply: String,
)

// ── Save config (PUT /ai-config/:appId) ───────────────────────────────────────

data class UpsertAiConfigRequest(
    @SerializedName("provider") val provider: String,
    @SerializedName("apiKey") val apiKey: String? = null,
    @SerializedName("config") val config: AiConfigBody,
    @SerializedName("useHistory") val useHistory: Boolean? = null,
    @SerializedName("historyTurns") val historyTurns: Int? = null,
    @SerializedName("isEnabled") val isEnabled: Boolean? = null,
)
