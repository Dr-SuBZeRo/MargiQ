package com.margiq.data.updater

import com.margiq.domain.updater.GitHubRelease
import com.margiq.domain.updater.GitHubReleaseAsset
import com.margiq.domain.updater.IGitHubUpdateService
import com.margiq.domain.updater.GitHubUpdateResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.HttpsURLConnection

class DefaultGitHubUpdateService(
    private val endpointUrl: String = "https://api.github.com/repos/Dr-SuBZeRo/MargiQ/releases/latest",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : IGitHubUpdateService {

    override suspend fun fetchLatestRelease(): GitHubUpdateResult = withContext(ioDispatcher) {
        var connection: HttpsURLConnection? = null
        try {
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "MargiQ-Android-App")
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                parseReleaseJson(jsonText)
            } else if (responseCode == 404) {
                GitHubUpdateResult.HttpError(404, "No published releases found on GitHub.")
            } else if (responseCode == 403) {
                GitHubUpdateResult.HttpError(403, "GitHub API rate limit exceeded or access forbidden. Please try again later.")
            } else {
                GitHubUpdateResult.HttpError(responseCode, "GitHub request failed with code $responseCode.")
            }
        } catch (_: UnknownHostException) {
            GitHubUpdateResult.NetworkError(isOffline = true, message = "Unable to connect. Please check your internet connection.")
        } catch (_: ConnectException) {
            GitHubUpdateResult.NetworkError(isOffline = true, message = "Unable to connect to GitHub. Please check your network connection.")
        } catch (_: SocketTimeoutException) {
            GitHubUpdateResult.NetworkError(isOffline = false, message = "Connection timed out. GitHub may be temporarily unreachable.")
        } catch (e: Exception) {
            GitHubUpdateResult.NetworkError(isOffline = false, message = e.localizedMessage ?: "Network error occurred")
        } finally {
            connection?.disconnect()
        }
    }

    companion object {
        fun parseReleaseJson(jsonString: String): GitHubUpdateResult {
            return try {
                val root = JSONObject(jsonString)
                val tagName = root.optString("tag_name", "").trim()
                if (tagName.isEmpty()) {
                    return GitHubUpdateResult.ParseError("Release tag_name is missing or empty.")
                }

                val name = root.optString("name", tagName)
                val body = root.optString("body", "")
                val htmlUrl = root.optString("html_url", "https://github.com/Dr-SuBZeRo/MargiQ/releases")
                val isDraft = root.optBoolean("draft", false)
                val isPrerelease = root.optBoolean("prerelease", false)

                val assetsArray = root.optJSONArray("assets")
                val assets = mutableListOf<GitHubReleaseAsset>()
                if (assetsArray != null) {
                    for (i in 0 until assetsArray.length()) {
                        val assetObj = assetsArray.getJSONObject(i)
                        assets.add(
                            GitHubReleaseAsset(
                                name = assetObj.optString("name", ""),
                                sizeBytes = assetObj.optLong("size", 0L),
                                browserDownloadUrl = assetObj.optString("browser_download_url", ""),
                                contentType = assetObj.optString("content_type", "")
                            )
                        )
                    }
                }

                GitHubUpdateResult.Success(
                    GitHubRelease(
                        tagName = tagName,
                        name = name,
                        body = body,
                        htmlUrl = htmlUrl,
                        isDraft = isDraft,
                        isPrerelease = isPrerelease,
                        assets = assets
                    )
                )
            } catch (e: Exception) {
                GitHubUpdateResult.ParseError("Malformed response: ${e.localizedMessage}")
            }
        }
    }
}

