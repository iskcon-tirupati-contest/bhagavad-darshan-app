package com.iskcon.bhagavaddarshan.data

data class AppUpdateInfo(
    val version: String,
    val versionCode: Int,
    val apkUrl: String,
    val mandatory: Boolean,
    val releaseNotes: String
)
