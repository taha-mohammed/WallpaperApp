package com.wallpaper.wallpaper.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.wallpaper.wallpaper.R
import com.wallpaper.wallpaper.data.ConnectionState
import com.wallpaper.wallpaper.data.connectivityState
import com.wallpaper.wallpaper.data.picture.Picture
import com.wallpaper.wallpaper.util.mirror
import com.wallpaper.wallpaper.viewmodel.WallpaperViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun WallpaperScreen(
    viewModel: WallpaperViewModel,
    navToPicture: (String, String, String) -> Unit,
    onBack: () -> Unit,
    navToFavourite: (() -> Unit)? = null
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    var showError by remember { mutableStateOf(true) }
    Scaffold(
        topBar = {
            WallpaperTopBar(
                title = viewModel.categoryName,
                onBack = onBack,
                navToFavourite = navToFavourite
            )
        },
        scaffoldState = scaffoldState
    ) {
        val connection by connectivityState()
        val context = LocalContext.current

        LaunchedEffect(connection) {
            if (connection === ConnectionState.Unavailable) {
                scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.network_error))
                return@LaunchedEffect
            }
            viewModel.refreshDate()
        }
        Column(
            Modifier.padding(it)
        ) {
            val data = viewModel.state
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = data.value.isLoading),
                onRefresh = {
                    scope.launch {
                        viewModel.refreshDate()
                    }
                }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(data.value.wallpapers, key = { picture -> picture.id }) { picture ->
                        WallpaperItem(picture = picture, showError, { showError = it }) {
                            navToPicture(picture.id, viewModel.categoryId, viewModel.categoryName)
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun WallpaperTopBar(title: String, onBack: () -> Unit, navToFavourite: (() -> Unit)?) {
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(
                text = title.takeIf { it != "Favourite" } ?: stringResource(R.string.favourite),
                style = MaterialTheme.typography.h5
            )
        },
        navigationIcon =
        {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    modifier = Modifier.mirror(),
                    contentDescription = "Back to Wallpaper Screen"
                )
            }
        },
        actions = {
            if (navToFavourite != null) {
                IconButton(
                    onClick = navToFavourite
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "navigate to favourites page",
                        tint = Color.Red
                    )
                }
            }
            IconButton(onClick = { showMenu = showMenu.not() }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "")
            }
            WallpaperMenu(showMenu = showMenu, onDismiss = { showMenu = it })
        }
    )
}

@Composable
fun WallpaperItem(
    picture: Picture,
    showError: Boolean,
    onShowError: (Boolean) -> Unit,
    onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .height(120.dp)
            .clickable { onClick() }
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data("https://drive.google.com/uc?id=" + picture.id)
                .crossfade(500)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state is AsyncImagePainter.State.Error && showError) {
                Toast.makeText(context, stringResource(R.string.load_image_error), Toast.LENGTH_LONG).show()
                onShowError(false)
            } else {
                SubcomposeAsyncImageContent()
            }
        }
    }
}

@Preview
@Composable
fun WallpaperTopBarPreview() {
    WallpaperTopBar(title = "Cars", {}) {

    }
}
