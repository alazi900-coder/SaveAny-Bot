package com.krau.saveany.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SaveAnyApi {
    @GET("health")
    suspend fun health(): HealthResponse

    @GET("api/v1/storages")
    suspend fun listStorages(): StoragesResponse

    @GET("api/v1/task-types")
    suspend fun listTaskTypes(): TaskTypesResponse

    @GET("api/v1/tasks/")
    suspend fun listTasks(): TasksList

    @GET("api/v1/tasks/{id}")
    suspend fun getTask(@Path("id") id: String): TaskInfo

    @POST("api/v1/tasks")
    suspend fun createTask(@Body req: CreateTaskRequest): CreateTaskResponse

    @DELETE("api/v1/tasks/{id}")
    suspend fun cancelTask(@Path("id") id: String): Map<String, String>
}
