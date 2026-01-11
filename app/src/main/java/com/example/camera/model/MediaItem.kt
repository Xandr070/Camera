package com.example.camera.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val type: MediaType,
    val dateCreated: Long,
    val displayName: String,
    val size: Long
)

enum class MediaType {
    PHOTO,
    VIDEO
}

