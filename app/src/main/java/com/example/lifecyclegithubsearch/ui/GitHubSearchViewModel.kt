package com.example.lifecyclegithubsearch.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifecyclegithubsearch.api.GitHubService
import com.example.lifecyclegithubsearch.data.GitHubRepo
import com.example.lifecyclegithubsearch.data.GitHubReposRepository
import com.example.lifecyclegithubsearch.data.LoadingStatus
import kotlinx.coroutines.launch

class GitHubSearchViewModel: ViewModel() {
    private val repository = GitHubReposRepository(GitHubService.create())

    private val _searchResults = MutableLiveData<List<GitHubRepo>?>(null)
    val searchResults: LiveData<List<GitHubRepo>?> = _searchResults

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _loadingStatus = MutableLiveData<LoadingStatus>(LoadingStatus.SUCCESS)
    val loadingStatus: LiveData<LoadingStatus> = _loadingStatus

    fun loadSearchResults(query: String) {
        viewModelScope.launch {
            _loadingStatus.value = LoadingStatus.LOADING
            val result = repository.loadRepositoriesSearch(query)
            _searchResults.value = result.getOrNull()
            _errorMessage.value = result.exceptionOrNull()?.message
            _loadingStatus.value = when(result.isSuccess) {
                true -> LoadingStatus.SUCCESS
                false -> LoadingStatus.ERROR
            }
        }
    }
}