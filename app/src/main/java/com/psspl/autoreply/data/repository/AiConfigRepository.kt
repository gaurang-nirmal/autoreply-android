package com.psspl.autoreply.data.repository

import com.psspl.autoreply.data.network.ApiService
import com.psspl.autoreply.data.network.model.AiConfigResponse
import com.psspl.autoreply.data.network.model.AiProviderMeta
import com.psspl.autoreply.data.network.model.BackupPromptItem
import com.psspl.autoreply.data.network.model.CreatePromptRequest
import com.psspl.autoreply.data.network.model.PromptBackupResponse
import com.psspl.autoreply.data.network.model.RestorePromptsRequest
import com.psspl.autoreply.data.network.model.TrainingPromptItem
import com.psspl.autoreply.data.network.model.UpdatePromptRequest
import com.psspl.autoreply.data.network.model.UpsertAiConfigRequest
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

@Singleton
class AiConfigRepository @Inject constructor(
    private val api: ApiService,
) {
    // ── AI Config ─────────────────────────────────────────────────────────────

    suspend fun getProviders(): Result<List<AiProviderMeta>> = try {
        val response = api.getAiProviders()
        if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
        else Result.Error("Failed to load providers: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    suspend fun getConfig(appId: Int): Result<AiConfigResponse> = try {
        val response = api.getAiConfig(appId)
        when {
            response.isSuccessful -> Result.Success(response.body()!!)
            response.code() == 404 -> Result.Error("NOT_FOUND")
            else -> Result.Error("Failed to load config: ${response.code()}")
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    suspend fun saveConfig(appId: Int, request: UpsertAiConfigRequest): Result<AiConfigResponse> =
        try {
            val response = api.upsertAiConfig(appId, request)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to save config: ${response.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }

    suspend fun deleteConfig(appId: Int): Result<Unit> = try {
        val response = api.deleteAiConfig(appId)
        if (response.isSuccessful) Result.Success(Unit)
        else Result.Error("Failed to delete config: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    suspend fun clearHistory(appId: Int): Result<Int> = try {
        val response = api.clearAiHistory(appId)
        if (response.isSuccessful) Result.Success(response.body()?.get("deleted") ?: 0)
        else Result.Error("Failed to clear history: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    // ── Training Prompts ──────────────────────────────────────────────────────

    suspend fun listPrompts(appId: Int): Result<List<TrainingPromptItem>> = try {
        val response = api.listTrainingPrompts(appId)
        if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
        else Result.Error("Failed to load prompts: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    suspend fun createPrompt(
        appId: Int,
        content: String,
        isEnabled: Boolean = true
    ): Result<TrainingPromptItem> = try {
        val response = api.createTrainingPrompt(
            appId,
            CreatePromptRequest(content = content, isEnabled = isEnabled)
        )
        if (response.isSuccessful) Result.Success(response.body()!!)
        else Result.Error("Failed to create prompt: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    suspend fun updatePrompt(
        appId: Int,
        promptId: String,
        content: String? = null,
        isEnabled: Boolean? = null,
    ): Result<TrainingPromptItem> = try {
        val response = api.updateTrainingPrompt(
            appId,
            promptId,
            UpdatePromptRequest(content = content, isEnabled = isEnabled)
        )
        if (response.isSuccessful) Result.Success(response.body()!!)
        else Result.Error("Failed to update prompt: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    suspend fun deletePrompt(appId: Int, promptId: String): Result<Unit> = try {
        val response = api.deleteTrainingPrompt(appId, promptId)
        if (response.isSuccessful) Result.Success(Unit)
        else Result.Error("Failed to delete prompt: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    suspend fun deleteAllPrompts(appId: Int): Result<Unit> = try {
        val response = api.deleteAllTrainingPrompts(appId)
        if (response.isSuccessful) Result.Success(Unit)
        else Result.Error("Failed to delete prompts: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    suspend fun backupPrompts(appId: Int): Result<PromptBackupResponse> = try {
        val response = api.backupTrainingPrompts(appId)
        if (response.isSuccessful) Result.Success(response.body()!!)
        else Result.Error("Failed to backup prompts: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    suspend fun restorePrompts(
        appId: Int,
        prompts: List<BackupPromptItem>
    ): Result<List<TrainingPromptItem>> = try {
        val response = api.restoreTrainingPrompts(appId, RestorePromptsRequest(prompts))
        if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
        else Result.Error("Failed to restore prompts: ${response.code()}")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }
}
