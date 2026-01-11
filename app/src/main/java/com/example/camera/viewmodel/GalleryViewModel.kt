package com.example.camera.viewmodel

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class GalleryViewModel : ViewModel() {

    fun getResources(context: Context): List<Resource> {
        val videoQuery = context.contentResolver.cursor(
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DISPLAY_NAME,
            ),
            selection = "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?",
            selectionArgs = arrayOf("%Movies/CameraX-App/%"),
            sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC",
        )
        val imageQuery = context.contentResolver.cursor(
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DISPLAY_NAME,
            ),
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?",
            selectionArgs = arrayOf("%Pictures/CameraX-App/%"),
            sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC",
        )

        val videos = videoQuery?.use { cursor ->
            with(cursor) {
                val idColumn = getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val sizeColumn = getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateColumn = getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val nameColumn = getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)


                List(count) { index ->
                    moveToPosition(index)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        getLong(idColumn),
                    )
                    val instant = Instant.ofEpochSecond(getLong(dateColumn))
                    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .withLocale(Locale.getDefault())

                    Resource.Video(
                        uri = contentUri,
                        size = getInt(sizeColumn),
                        name = getString(nameColumn),
                        duration = getInt(durationColumn),
                        date = formatter.format(instant),
                    )
                }
            }
        } ?: emptyList()
        val images = imageQuery?.use { cursor ->
            with(cursor) {
                val idColumn = getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val sizeColumn = getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateColumn = getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val nameColumn = getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)


                List(count) { index ->
                    moveToPosition(index)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        getLong(idColumn),
                    )
                    val instant = Instant.ofEpochSecond(getLong(dateColumn))
                    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .withLocale(Locale.getDefault())

                    Resource.Image(
                        uri = contentUri,
                        size = getInt(sizeColumn),
                        name = getString(nameColumn),
                        date = formatter.format(instant),
                    )
                }
            }
        } ?: emptyList()


        return (videos + images).sortedByDescending { resource -> resource.date }
    }

    fun deleteResource(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null)
    }

    private fun ContentResolver.cursor(
        collection: Uri,
        selection: String? = null,
        sortOrder: String? = null,
        projection: Array<String>? = null,
        selectionArgs: Array<String>? = null,
    ): Cursor? = query(
        collection,
        projection,
        selection,
        selectionArgs,
        sortOrder,
    )

}

sealed interface Resource {

    val uri: Uri
    val size: Int
    val name: String
    val date: String

    data class Video(
        override val uri: Uri,
        override val size: Int,
        override val name: String,
        override val date: String,
        val duration: Int,
    ) : Resource

    data class Image(
        override val uri: Uri,
        override val size: Int,
        override val name: String,
        override val date: String,
    ) : Resource
}

