package com.psspl.autoreply.data.network

import com.psspl.autoreply.data.network.model.AiConfigResponse
import com.psspl.autoreply.data.network.model.AiProviderMeta
import com.psspl.autoreply.data.network.model.AiReplyRequest
import com.psspl.autoreply.data.network.model.AiReplyResponse
import com.psspl.autoreply.data.network.model.BackendApp
import com.psspl.autoreply.data.network.model.ContactRequest
import com.psspl.autoreply.data.network.model.ContactResponse
import com.psspl.autoreply.data.network.model.CreatePromptRequest
import com.psspl.autoreply.data.network.model.DeleteAccountResponse
import com.psspl.autoreply.data.network.model.PromptBackupResponse
import com.psspl.autoreply.data.network.model.RestorePromptsRequest
import com.psspl.autoreply.data.network.model.TrainingPromptItem
import com.psspl.autoreply.data.network.model.UpdatePromptRequest
import com.psspl.autoreply.data.network.model.UpsertAiConfigRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // ── Apps ──────────────────────────────────────────────────────────────────

    @GET("apps/active")
    suspend fun getActiveApps(): Response<List<BackendApp>>

    // ── AI Providers ──────────────────────────────────────────────────────────

    @GET("ai-config/providers")
    suspend fun getAiProviders(): Response<List<AiProviderMeta>>

    // ── AI Config ─────────────────────────────────────────────────────────────

    @GET("ai-config/{appId}")
    suspend fun getAiConfig(
        @Path("appId") appId: Int,
    ): Response<AiConfigResponse>

    @PUT("ai-config/{appId}")
    suspend fun upsertAiConfig(
        @Path("appId") appId: Int,
        @Body request: UpsertAiConfigRequest,
    ): Response<AiConfigResponse>

    @DELETE("ai-config/{appId}")
    suspend fun deleteAiConfig(
        @Path("appId") appId: Int,
    ): Response<Unit>

    // ── AI Reply ──────────────────────────────────────────────────────────────

    @POST("ai/reply")
    suspend fun getAiReply(
        @Body request: AiReplyRequest,
    ): Response<AiReplyResponse>

    // ── AI History ────────────────────────────────────────────────────────────

    @DELETE("ai/history/{appId}")
    suspend fun clearAiHistory(
        @Path("appId") appId: Int,
    ): Response<Map<String, Int>>

    // ── Training Prompts ──────────────────────────────────────────────────────

    @GET("ai/training-prompts/{appId}")
    suspend fun listTrainingPrompts(
        @Path("appId") appId: Int,
    ): Response<List<TrainingPromptItem>>

    @POST("ai/training-prompts/{appId}")
    suspend fun createTrainingPrompt(
        @Path("appId") appId: Int,
        @Body request: CreatePromptRequest,
    ): Response<TrainingPromptItem>

    @PUT("ai/training-prompts/{appId}/{promptId}")
    suspend fun updateTrainingPrompt(
        @Path("appId") appId: Int,
        @Path("promptId") promptId: String,
        @Body request: UpdatePromptRequest,
    ): Response<TrainingPromptItem>

    @DELETE("ai/training-prompts/{appId}/{promptId}")
    suspend fun deleteTrainingPrompt(
        @Path("appId") appId: Int,
        @Path("promptId") promptId: String,
    ): Response<Unit>

    @DELETE("ai/training-prompts/{appId}")
    suspend fun deleteAllTrainingPrompts(
        @Path("appId") appId: Int,
    ): Response<Unit>

    // ── Support ───────────────────────────────────────────────────────────────

    @POST("support/contact")
    suspend fun submitContactRequest(
        @Body request: ContactRequest,
    ): Response<ContactResponse>

    // ── Account ───────────────────────────────────────────────────────────────

    @DELETE("auth/account")
    suspend fun deleteAccount(): Response<DeleteAccountResponse>

    @GET("ai/training-prompts/{appId}/backup")
    suspend fun backupTrainingPrompts(
        @Path("appId") appId: Int,
    ): Response<PromptBackupResponse>

    @POST("ai/training-prompts/{appId}/restore")
    suspend fun restoreTrainingPrompts(
        @Path("appId") appId: Int,
        @Body request: RestorePromptsRequest,
    ): Response<List<TrainingPromptItem>>
}
