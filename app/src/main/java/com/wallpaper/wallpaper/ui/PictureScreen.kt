package com.wallpaper.wallpaper.ui

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context.DOWNLOAD_SERVICE
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.wallpaper.wallpaper.R
import com.wallpaper.wallpaper.util.PictureEvent
import com.wallpaper.wallpaper.util.mirror
import com.wallpaper.wallpaper.viewmodel.PictureViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun PictureScreen(
    viewModel: PictureViewModel,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.eventFlow.collectLatest {
            when (it) {
                is PictureViewModel.UiEvent.ShowToast -> {
                    Toast.makeText(context, it.resId, Toast.LENGTH_LONG).show()
                }
                is PictureViewModel.UiEvent.StartIntent -> {
                    context.startActivity(it.intent)
                }
                is PictureViewModel.UiEvent.ChangePage -> {
                    pagerState.scrollToPage(it.page)
                }
                is PictureViewModel.UiEvent.Back -> onBack()
            }
        }
    }
    ModalBottomSheetLayout(
        sheetContent = { SheetContent(sheetState) { viewModel.onEvent(it) } },
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topEnd = 30.dp, topStart = 30.dp)
    ) {
        val pictures = viewModel.pictures
        val isBarVisible = remember {
            mutableStateOf(false)
        }
        Box {
            if (pictures.value.isNotEmpty()) {
                //full Screen Image
                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    count = pictures.value.size,
                    state = pagerState,
                    key = { index ->
                        if (index >= pictures.value.size)
                            return@HorizontalPager ""
                        pictures.value[index].id
                    }
                ) { index ->
                    PictureItem(
                        pictureId = pictures.value[index].id,
                        onClick = { isBarVisible.value = !isBarVisible.value }
                    )
                }
                val density = LocalDensity.current
                val currentState = viewModel.state
                //TopAppBar
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.TopCenter),
                    visible = isBarVisible.value,
                    enter = slideInVertically { with(density) { -80.dp.roundToPx() } },
                    exit = slideOutVertically { with(density) { -80.dp.roundToPx() } },
                ) {
                    PictureTopBar {
                        onBack()
                    }
                }
                //BottomAppBar
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = isBarVisible.value,
                    enter = slideInVertically { with(density) { +80.dp.roundToPx() } },
                    exit = slideOutVertically { with(density) { +80.dp.roundToPx() } },
                ) {
                    PictureOptions(
                        sheetState = sheetState,
                        isFavourite = pictures.value[currentState.value].isFavourite,
                        onEvent = { viewModel.onEvent(it) }
                    )
                }
                LaunchedEffect(true) {
                    scope.launch {
                        pagerState.scrollToPage(pictures.value.indexOfFirst { it.id == viewModel.initState })
                    }
                }
                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { page ->
                        viewModel.onEvent(PictureEvent.ChangeState(page))
                    }
                }
            }
        }
    }
}

@Composable
fun PictureTopBar(onBack: () -> Unit) {
    TopAppBar(backgroundColor = MaterialTheme.colors.background) {
        IconButton(onClick = { onBack() }) {
            Icon(
                Icons.Filled.ArrowBack,
                modifier = Modifier.mirror(),
                contentDescription = "Back to Wallpaper Screen"
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PictureOptions(
    sheetState: ModalBottomSheetState,
    isFavourite: Boolean,
    onEvent: (PictureEvent) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BottomAppBar(
        backgroundColor = MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Share Picture Button
            IconButton(onClick = {
                onEvent(PictureEvent.SharePicture(context))
            }) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "Share Picture"
                )
            }
            //Download Picture Button
            IconButton(onClick = {
                val dm = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                onEvent(PictureEvent.DownloadPicture(dm))
            }) {
                Icon(
                    Icons.Outlined.Download,
                    contentDescription = "Download Picture"
                )
            }
            //Set Wallpaper Button
            IconButton(onClick = {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    onEvent(
                        PictureEvent.SetAsWallpaper(
                            WallpaperManager.getInstance(context)
                        )
                    )
                    return@IconButton
                }
                scope.launch {
                    sheetState.show()
                }
            }) {
                Icon(
                    Icons.Outlined.Wallpaper,
                    contentDescription = "Set as Wallpaper"
                )
            }
            // Toggle Favourite Button
            IconToggleButton(
                checked = isFavourite,
                onCheckedChange = { onEvent(PictureEvent.ToggleFavourite) }) {
                if (isFavourite) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "UnFavourite"
                    )
                    return@IconToggleButton
                }
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favourite"
                )
            }
        }
    }
}

//This shown when Set Wallpaper button is clicked
@RequiresApi(Build.VERSION_CODES.N)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetContent(sheetState: ModalBottomSheetState, onEvent: (PictureEvent) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val wallpaperManager = WallpaperManager.getInstance(context)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Set as home screen button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 10.dp),
            onClick = {
                scope.launch {
                    onEvent(
                        PictureEvent.SetAsWallpaper(
                            wallpaperManager,
                            WallpaperManager.FLAG_SYSTEM
                        )
                    )
                    sheetState.hide()
                }
            }) {
            Text(text = stringResource(R.string.set_system_wallpaper))
        }
        //Set as lock screen button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            onClick = {
                scope.launch {
                    onEvent(
                        PictureEvent.SetAsWallpaper(
                            wallpaperManager,
                            WallpaperManager.FLAG_LOCK
                        )
                    )
                    sheetState.hide()
                }
            }) {
            Text(text = stringResource(R.string.set_lock_wallpaper))
        }
        //Set both button (home and lock screen)
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            onClick = {
                scope.launch {
                    onEvent(
                        PictureEvent.SetAsWallpaper(
                            wallpaperManager
                        )
                    )
                    sheetState.hide()
                }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary
            )
        ) {
            Text(text = stringResource(R.string.set_both))
        }
        //Crop image then set as wallpaper for both
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            onClick = {
                scope.launch {
                    onEvent(PictureEvent.CropWallpaper(context))
                    sheetState.hide()
                }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary
            )
        ) {
            Text(text = stringResource(R.string.crop_and_set))
        }
    }
}

@Composable
fun PictureItem(
    pictureId: String,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://drive.google.com/uc?id=$pictureId")
                .crossfade(500)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SubcomposeAsyncImageContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(locale = "ar")
@Composable
fun PictureTopBarPreview() {
    PictureOptions(
        sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
        isFavourite = true,
        onEvent = {})
}