package com.margiq.domain.updater

sealed class GitHubUpdateResult {
    data class Success(val release: GitHubRelease) : GitHubUpdateResult()
    data class HttpError(val statusCode: Int, val message: String) : GitHubUpdateResult()
    data class NetworkError(val isOffline: Boolean, val message: String) : GitHubUpdateResult()
    data class ParseError(val message: String) : GitHubUpdateResult()
}

interface IGitHubUpdateService {
    suspend fun fetchLatestRelease(): GitHubUpdateResult
}

