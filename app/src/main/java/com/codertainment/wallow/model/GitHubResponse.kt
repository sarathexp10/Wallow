package com.codertainment.wallow.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GitHubResponse(var name: String,
                          var path: String,
                          var sha: String,
                          var size: Long,
                          var url: String,
                          @SerializedName("html_url")
                          var htmlUrl: String,
                          @SerializedName("git_url")
                          var gitUrl: String,
                          @SerializedName("download_url")
                          var downloadUrl: String,
                          var type: String) : Parcelable {
  fun isWallpaper() = type == "file" && (name.endsWith("png") || name.endsWith("jpg"))
}