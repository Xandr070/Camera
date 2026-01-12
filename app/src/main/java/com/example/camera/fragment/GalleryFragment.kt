package com.example.camera.fragment

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import com.example.camera.ui.theme.icons.CameraIcons
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.camera.ui.components.BackButton
import com.example.camera.ui.components.DeleteButton
import com.example.camera.ui.theme.CameraXAppTheme
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
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    val mediaItems = remember { mutableStateListOf<Resource>() }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        if (mediaItems.isEmpty()) {
            mediaItems.addAll(viewModel.getResources(context))
        }
    }

    BackHandler(enabled = selectedIndex != null) {
        selectedIndex = null
    }

    AnimatedContent(
        targetState = selectedIndex,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { index ->
        when (index) {
            null -> Box(modifier = modifier) {
                MediaGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                        .navigationBarsPadding(),
                    state = gridState,
                    items = mediaItems,
                    onItemClick = { item ->
                        selectedIndex = mediaItems.indexOf(item)
                    }
                )
                BackButton(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 16.dp),
                    onClick = {
                        if (selectedIndex != null) {
                            selectedIndex = null
                        } else {
                            onBack()
                        }
                    }
                )
            }
            else -> {
                if (index in mediaItems.indices) {
                    key(mediaItems.size, index) {
                        MediaPager(
                            modifier = modifier,
                            items = mediaItems,
                            initialIndex = index,
                            paddings = PaddingValues(),
                            onClose = { selectedIndex = null },
                            onDelete = { item ->
                                val itemIndex = mediaItems.indexOf(item)
                                val currentPage = if (itemIndex < index) index - 1 else index
                                viewModel.deleteResource(context, item.uri)
                                mediaItems.remove(item)
                                if (mediaItems.isEmpty()) {
                                    selectedIndex = null
                                } else {
                                    selectedIndex = minOf(currentPage, mediaItems.size - 1).coerceAtLeast(0)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageViewer(
    modifier: Modifier,
    image: Resource.Image,
    paddings: PaddingValues
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            modifier = Modifier
                .padding(bottom = paddings.calculateBottomPadding())
                .fillMaxSize(),
            model = image.uri,
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun VideoViewer(
    modifier: Modifier,
    video: Resource.Video,
    paddings: PaddingValues
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VideoPlayer(
            modifier = Modifier
                .padding(bottom = paddings.calculateBottomPadding())
                .fillMaxSize(),
            controller = true,
            uri = video.uri
        )
    }
}

@Composable
private fun MediaPager(
    modifier: Modifier,
    items: List<Resource>,
    initialIndex: Int,
    paddings: PaddingValues,
    onClose: () -> Unit,
    onDelete: (Resource) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { items.size }
    )

    LaunchedEffect(initialIndex) {
        if (initialIndex in items.indices) {
            pagerState.animateScrollToPage(initialIndex)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page in items.indices) {
                when (val item = items[page]) {
                    is Resource.Image -> ImageViewer(
                        modifier = Modifier.fillMaxSize(),
                        image = item,
                        paddings = paddings
                    )
                    is Resource.Video -> VideoViewer(
                        modifier = Modifier.fillMaxSize(),
                        video = item,
                        paddings = paddings
                    )
                }
            }
        }
        
        BackButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp),
            onClick = onClose
        )
        DeleteButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp),
            onClick = {
                val currentPage = pagerState.currentPage
                if (currentPage in items.indices) {
                    onDelete(items[currentPage])
                }
            }
        )
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
                imageVector = CameraIcons.PlayArrow
            )
        }
    }
}

@Preview(showBackground = true, name = "Gallery Fragment")
@Composable
private fun GalleryFragmentPreview() {
    CameraXAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                state = rememberLazyGridState(),
                columns = GridCells.Adaptive(128.dp)
            ) {
                items(6) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(0.75f)
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

