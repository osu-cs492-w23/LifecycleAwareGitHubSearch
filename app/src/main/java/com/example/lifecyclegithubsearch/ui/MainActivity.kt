package com.example.lifecyclegithubsearch.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifecyclegithubsearch.R
import com.example.lifecyclegithubsearch.api.GitHubService
import com.example.lifecyclegithubsearch.data.GitHubRepo
import com.example.lifecyclegithubsearch.data.GitHubSearchResults
import com.google.android.material.progressindicator.CircularProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val gitHubService = GitHubService.create()
    private val repoListAdapter = GitHubRepoListAdapter(::onGitHubRepoClick)

    private lateinit var searchResultsListRV: RecyclerView
    private lateinit var searchErrorTV: TextView
    private lateinit var loadingIndicator: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchBoxET: EditText = findViewById(R.id.et_search_box)
        val searchBtn: Button = findViewById(R.id.btn_search)

        searchErrorTV = findViewById(R.id.tv_search_error)
        loadingIndicator = findViewById(R.id.loading_indicator)

        /*
         * Set up RecyclerView.
         */
        searchResultsListRV = findViewById(R.id.rv_search_results)
        searchResultsListRV.layoutManager = LinearLayoutManager(this)
        searchResultsListRV.setHasFixedSize(true)
        searchResultsListRV.adapter = repoListAdapter

        /*
         * Attach click listener to "search" button to perform repository search with GitHub API
         * using the search query entered by the user.
         */
        searchBtn.setOnClickListener {
            val query = searchBoxET.text.toString()
            if (!TextUtils.isEmpty(query)) {
                doRepoSearch(query)
                searchResultsListRV.scrollToPosition(0)
            }
        }
    }

    /**
     * This method executes a repository search with with the GitHub API.  It sends an HTTP request
     * to the GitHub API based on a provided search query and handles the response from the API.
     *
     * @param query The search query to be sent to the GitHub API
     */
    private fun doRepoSearch(query: String) {
        /*
         * Show loading indicator and hide other UI components while the search is in progress.
         */
        loadingIndicator.visibility = View.VISIBLE
        searchResultsListRV.visibility = View.INVISIBLE
        searchErrorTV.visibility = View.INVISIBLE

        /*
         * Asynchronously send HTTP request to the GitHub API using the GitHubService Retrofit
         * service.
         */
        gitHubService.searchRepositories(query)
            .enqueue(object : Callback<GitHubSearchResults> {
                /*
                 * onResponse() is the callback executed when a response is received from the API.
                 * The response may or may not indicate success.
                 */
                override fun onResponse(call: Call<GitHubSearchResults>, response: Response<GitHubSearchResults>) {
                    Log.d(
                        TAG,
                        "Response received for query '$query', status code: ${response.code()}"
                    )
                    loadingIndicator.visibility = View.INVISIBLE
                    if (response.isSuccessful) {
                        /*
                         * If response was successful, grab list of search results out of response
                         * body and plug them into the RecyclerView adapter.  Show RecyclerView.
                         */
                        repoListAdapter.updateRepoList(response.body()?.items)
                        searchResultsListRV.visibility = View.VISIBLE
                    } else {
                        /*
                         * If response was not successful, display error message to user.
                         */
                        searchErrorTV.visibility = View.VISIBLE
                        searchErrorTV.text = getString(
                            R.string.search_error,
                            response.errorBody()?.string() ?: "unknown error"
                        )
                    }
                }

                /*
                 * onFailure() is called when an API call can't be executed (i.e. the request can't
                 * be sent, or no response is received).
                 */
                override fun onFailure(call: Call<GitHubSearchResults>, t: Throwable) {
                    /*
                     * If an error occurred when executing the search query, display error message
                     * to the user.
                     */
                    Log.d(TAG, "Error executing query '$query': ${t.message}")
                    loadingIndicator.visibility = View.INVISIBLE
                    searchErrorTV.visibility = View.VISIBLE
                    searchErrorTV.text = getString(R.string.search_error, t.message)
                }
            })
    }

    /**
     * This method is passed into the RecyclerView adapter to handle clicks on individual items
     * in the list of GitHub repos.  When a repo is clicked, a new activity is launched to view
     * details about that repo.
     */
    private fun onGitHubRepoClick(repo: GitHubRepo) {
        val intent = Intent(this, RepoDetailActivity::class.java)
        intent.putExtra(EXTRA_GITHUB_REPO, repo)
        startActivity(intent)
    }
}