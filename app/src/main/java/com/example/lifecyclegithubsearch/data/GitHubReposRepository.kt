package com.example.lifecyclegithubsearch.data

import com.example.lifecyclegithubsearch.api.GitHubService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitHubReposRepository(
    private val service: GitHubService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun loadRepositoriesSearch(query: String): Result<List<GitHubRepo>> =
        withContext(dispatcher) {
            try {
                val response = service.searchRepositories(query)
                if (response.isSuccessful) {
                    Result.success(response.body()?.items ?: listOf())
                } else {
                    Result.failure(Exception(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}