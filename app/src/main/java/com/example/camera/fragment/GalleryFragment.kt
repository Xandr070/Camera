package com.example.camera.fragment

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
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
    val resources = remember { mutableStateListOf<Resource>() }

    var targetResource by remember { mutableStateOf<Resource?>(null) }

    LaunchedEffect(Unit) {
        if (resources.isEmpty()) {
            resources.addAll(viewModel.getResources(context))
        }
    }

    BackHandler(enabled = targetResource != null) {
        targetResource = null
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = targetResource,
        ) { resource ->
            when (resource) {
                is Resource.Image -> Box(modifier = modifier) {
                    AsyncImage(
                        modifier = Modifier
                            .padding(bottom = paddings.calculateBottomPadding())
                            .sharedElement(
                                animatedVisibilityScope = this@AnimatedContent,
                                sharedContentState = rememberSharedContentState(key = resource.uri),
                            )
                            .fillMaxSize(),
                        model = resource.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                    IconButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = {
                            targetResource = null
                            viewModel.deleteResource(context, resource.uri)
                            resources.remove(resource)
                        }
                    ) {
                        Icon(
                            contentDescription = null,
                            imageVector = Icons.Rounded.Delete,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                is Resource.Video -> Box(modifier = modifier) {
                    VideoPlayer(
                        modifier = Modifier
                            .padding(bottom = paddings.calculateBottomPadding())
                            .sharedElement(
                                animatedVisibilityScope = this@AnimatedContent,
                                sharedContentState = rememberSharedContentState(key = resource.uri),
                            )
                            .fillMaxSize(),
                        controller = true,
                        uri = resource.uri,
                    )
                    IconButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = {
                            targetResource = null
                            viewModel.deleteResource(context, resource.uri)
                            resources.remove(resource)
                        }
                    ) {
                        Icon(
                            contentDescription = null,
                            imageVector = Icons.Rounded.Delete,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                null -> LazyVerticalGrid(
                    modifier = modifier,
                    state = gridState,
                    columns = GridCells.Adaptive(128.dp),
                ) {
                    items(
                        resources.size,
                        key = { i -> resources[i].uri },
                    ) { i ->
                        when (val item = resources[i]) {
                            is Resource.Image -> Box {
                                AsyncImage(
                                    modifier = Modifier
                                        .sharedElement(
                                            animatedVisibilityScope = this@AnimatedContent,
                                            sharedContentState = rememberSharedContentState(key = item.uri),
                                        )
                                        .aspectRatio(0.75f)
                                        .clickable(onClick = { targetResource = item }),
                                    model = item.uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )
                                Text(
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    text = item.date,
                                )
                            }

                            is Resource.Video -> Box {
                                VideoPlayer(
                                    modifier = Modifier
                                        .sharedElement(
                                            animatedVisibilityScope = this@AnimatedContent,
                                            sharedContentState = rememberSharedContentState(key = item.uri),
                                        )
                                        .aspectRatio(0.75f)
                                        .clickable(onClick = { targetResource = item }),
                                    uri = item.uri,
                                    initialVolume = 0f,
                                )
                                Text(
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    text = item.date,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(
    uri: Uri,
    modifier: Modifier = Modifier,
    initialVolume: Float = 1f,
    controller: Boolean = false,
) {
    val context = LocalContext.current

    if (controller) {
        val exoPlayer = remember(uri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                volume = initialVolume
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = true
                prepare()
            }
        }

        DisposableEffect(uri) {
            onDispose { exoPlayer.release() }
        }

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            }
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
                imageVector = Icons.Rounded.PlayArrow,
            )
        }
    }
}


