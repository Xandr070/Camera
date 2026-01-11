package com.example.camera.fragment

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.example.camera.viewmodel.GalleryViewModel
import com.example.camera.viewmodel.Resource

@Composable
fun GalleryFragment(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier,
    paddings: PaddingValues = PaddingValues.Zero
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    val mediaItems = remember { mutableStateListOf<Resource>() }

    var selectedItem by remember { mutableStateOf<Resource?>(null) }

    LaunchedEffect(Unit) {
        if (mediaItems.isEmpty()) {
            mediaItems.addAll(viewModel.getResources(context))
        }
    }

    BackHandler(enabled = selectedItem != null) {
        selectedItem = null
    }

    AnimatedContent(
        targetState = selectedItem,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { item ->
        when (item) {
            is Resource.Image -> ImageViewer(
                modifier = modifier,
                image = item,
                paddings = paddings,
                onClose = { selectedItem = null },
                onDelete = {
                    viewModel.deleteResource(context, item.uri)
                    mediaItems.remove(item)
                    selectedItem = null
                }
            )

            is Resource.Video -> VideoViewer(
                modifier = modifier,
                video = item,
                paddings = paddings,
                onClose = { selectedItem = null },
                onDelete = {
                    viewModel.deleteResource(context, item.uri)
                    mediaItems.remove(item)
                    selectedItem = null
                }
            )

            null -> MediaGrid(
                modifier = modifier,
                state = gridState,
                items = mediaItems,
                onItemClick = { selectedItem = it }
            )
        }
    }
}

@Composable
private fun ImageViewer(
    modifier: Modifier,
    image: Resource.Image,
    paddings: PaddingValues,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    Box(modifier = modifier) {
        AsyncImage(
            modifier = Modifier
                .padding(bottom = paddings.calculateBottomPadding())
                .fillMaxSize(),
            model = image.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onDelete
        ) {
            Icon(
                contentDescription = null,
                imageVector = Icons.Rounded.Delete,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun VideoViewer(
    modifier: Modifier,
    video: Resource.Video,
    paddings: PaddingValues,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    Box(modifier = modifier) {
        VideoPlayer(
            modifier = Modifier
                .padding(bottom = paddings.calculateBottomPadding())
                .fillMaxSize(),
            controller = true,
            uri = video.uri
        )
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onDelete
        ) {
            Icon(
                contentDescription = null,
                imageVector = Icons.Rounded.Delete,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun MediaGrid(
    modifier: Modifier,
    state: androidx.compose.foundation.lazy.grid.LazyGridState,
    items: List<Resource>,
    onItemClick: (Resource) -> Unit
) {
    LazyVerticalGrid(
        modifier = modifier,
        state = state,
        columns = GridCells.Adaptive(128.dp)
    ) {
        items(
            items.size,
            key = { i -> items[i].uri }
        ) { index ->
            when (val item = items[index]) {
                is Resource.Image -> MediaThumbnail(
                    imageUri = item.uri.toString(),
                    dateText = item.date,
                    onClick = { onItemClick(item) }
                )

                is Resource.Video -> VideoThumbnail(
                    videoUri = item.uri,
                    dateText = item.date,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun MediaThumbnail(
    imageUri: String,
    dateText: String,
    onClick: () -> Unit
) {
    Box {
        AsyncImage(
            modifier = Modifier
                .aspectRatio(0.75f)
                .clickable(onClick = onClick),
            model = imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Text(
            modifier = Modifier.align(Alignment.BottomCenter),
            text = dateText
        )
    }
}

@Composable
private fun VideoThumbnail(
    videoUri: Uri,
    dateText: String,
    onClick: () -> Unit
) {
    Box {
        VideoPlayer(
            modifier = Modifier
                .aspectRatio(0.75f)
                .clickable(onClick = onClick),
            uri = videoUri,
            initialVolume = 0f
        )
        Text(
            modifier = Modifier.align(Alignment.BottomCenter),
            text = dateText
        )
    }
}

@Composable
private fun VideoPlayer(
    uri: Uri,
    modifier: Modifier = Modifier,
    initialVolume: Float = 1f,
    controller: Boolean = false
) {
    val context = LocalContext.current

    if (controller) {
        val player = remember(uri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                volume = initialVolume
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = true
                prepare()
            }
        }

        DisposableEffect(uri) {
            onDispose { player.release() }
        }

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                }
            },
            update = { view -> view.player = player }
        )
    } else {
        Box {
            AsyncImage(
                modifier = modifier,
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
            Icon(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.Center),
                contentDescription = null,
                imageVector = Icons.Rounded.PlayArrow
            )
        }
    }
}


