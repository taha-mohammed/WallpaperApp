package com.wallpaper.wallpaper.ui

import android.app.Activity
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.glide.GlideImage
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.adsbase.AutoInterstitialPreferences
import com.startapp.sdk.adsbase.StartAppAd
import com.wallpaper.wallpaper.R
import com.wallpaper.wallpaper.data.ConnectionState
import com.wallpaper.wallpaper.data.category.Category
import com.wallpaper.wallpaper.data.connectivityState
import com.wallpaper.wallpaper.viewmodel.CategoryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    navToWallpaper: (Category) -> Unit,
    navToFavourite: () -> Unit
) {
    var exitState by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(true) }
    ExitAppDialog(exitState) { exitState = it }
    BackHandler {
        exitState = true
    }
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = { CategoryTopBar(navToFavourite) },
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
            Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val data = viewModel.state
            SwipeRefresh(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = rememberSwipeRefreshState(isRefreshing = data.value.isLoading),
                onRefresh = {
                    scope.launch {
                        viewModel.refreshDate()
                    }
                }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(data.value.categories, key = { category -> category.id }) { category ->
                        CategoryItem(category, showError, { showError = it }) {
                            navToWallpaper(category)
                            StartAppAd.showAd(context)
                            StartAppAd.setAutoInterstitialPreferences(
                                AutoInterstitialPreferences()
                                    .setSecondsBetweenAds(60)
                            )
                        }
                    }
                }
            }
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    Banner(
                        context as Activity
                    ).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryTopBar(navToFavourite: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.h5
            )
        },
        navigationIcon =
        {
            Icon(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Application Icon"
            )
        },
        actions = {
            IconButton(onClick = { navToFavourite() }) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "navigate to favourites page",
                    tint = Color.Red
                )
            }
            IconButton(onClick = { showMenu = showMenu.not() }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "")
            }
            WallpaperMenu(showMenu = showMenu, onDismiss = { showMenu = it })
        }
    )
}

@Composable
fun CategoryItem(
    category: Category,
    showError: Boolean,
    onShowError: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(120.dp)
            .clickable { onClick() },
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            val context = LocalContext.current
            GlideImage(
                imageModel = "https://drive.google.com/uc?id=" + category.background,
                requestBuilder = {
                    Glide.with(LocalContext.current)
                        .asDrawable()
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .thumbnail(0.6f)
                },
                contentScale = ContentScale.Crop,
                contentDescription = category.name,
                circularReveal = CircularReveal(),
                shimmerParams = ShimmerParams(
                    baseColor = MaterialTheme.colors.background,
                    highlightColor = MaterialTheme.colors.onBackground,
                    durationMillis = 350,
                    dropOff = 0.65f,
                    tilt = 20f
                ),
                failure = {
                    if (showError) {
                        Toast.makeText(
                            context,
                            stringResource(R.string.load_image_error),
                            Toast.LENGTH_LONG
                        ).show()
                        onShowError(false)
                    }
                }
            )
//            val imageLoader = ImageLoader.Builder(context)
//                .components {
//                    if (SDK_INT >= 28) {
//                        add(ImageDecoderDecoder.Factory())
//                    } else {
//                        add(GifDecoder.Factory())
//                    }
//                }.crossfade(600)
//                .build()
//            //GIF Image
//            SubcomposeAsyncImage(
//                model = "https://drive.google.com/uc?id=" + category.background,
//                modifier = Modifier.fillMaxSize(),
//                imageLoader = imageLoader,
//                contentDescription = category.name,
//                contentScale = ContentScale.Crop
//            ) {
//                val state = painter.state
//                if (state is AsyncImagePainter.State.Loading) {
//                    Box(
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                } else if (state is AsyncImagePainter.State.Error && showError) {
//                    Toast.makeText(
//                        context,
//                        stringResource(R.string.load_image_error),
//                        Toast.LENGTH_LONG
//                    ).show()
//                    onShowError(false)
//                } else {
//                    SubcomposeAsyncImageContent()
//                }
//            }
            //Category Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background.copy(alpha = 0.4F)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h4
                )
            }
        }
    }
}

@Preview
@Composable
fun TopBarPreview() {
    CategoryTopBar {

    }
}
