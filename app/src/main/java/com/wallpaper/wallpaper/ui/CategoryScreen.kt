package com.wallpaper.wallpaper.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarRate
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
import androidx.compose.ui.window.Dialog
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.wallpaper.wallpaper.BuildConfig
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
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(data.value.categories, key = { category -> category.id }) { category ->
                        CategoryItem(category) {
                            navToWallpaper(category)
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun CategoryTopBar(navToFavourite: () -> Unit) {
    TopAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Application Icon"
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.h5
                )
            }

            IconButton(onClick = { navToFavourite() }) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "navigate to favourites page",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(120.dp)
            .clickable { onClick() },
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            val context = LocalContext.current
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    if (SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }.crossfade(600)
                .build()
            //GIF Image
            SubcomposeAsyncImage(
                model = "https://drive.google.com/uc?id=" + category.background,
                modifier = Modifier.fillMaxSize(),
                imageLoader = imageLoader,
                contentDescription = category.name,
                contentScale = ContentScale.Crop
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }else if (state is AsyncImagePainter.State.Error){
                    Toast.makeText(context, state.result.throwable.message, Toast.LENGTH_LONG).show()
                } else {
                    SubcomposeAsyncImageContent()
                }
            }
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

@Composable
fun ExitAppDialog(
    showDialog: Boolean,
    onDismiss: (Boolean) -> Unit) {
    val context = LocalContext.current
    if (showDialog) {
        Dialog(
            onDismissRequest = {
                onDismiss(false)
            }
        ){
            Surface(
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //App Logo
                    Image(
                        modifier = Modifier.size(100.dp),
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Logo"
                    )
                    //Share Button
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { shareApp(context) },
                        border = BorderStroke(2.dp, Color.Green)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                modifier = Modifier.align(Alignment.CenterStart),
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "share application",
                                tint = Color.Green
                            )
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = stringResource(R.string.share_button),
                                color = Color.Green
                            )
                        }
                    }
                    // Rate Us Button
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { rateApp(context) },
                        border = BorderStroke(2.dp, Color.Yellow)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                modifier = Modifier.align(Alignment.CenterStart),
                                imageVector = Icons.Outlined.StarRate,
                                contentDescription = "Rate Us",
                                tint = Color.Yellow
                            )
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = stringResource(R.string.rate_button),
                                color = Color.Yellow
                            )
                        }
                    }
                    // Exit Button
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { (context as Activity).finish() },
                        border = BorderStroke(2.dp, Color.Red)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                modifier = Modifier.align(Alignment.CenterStart),
                                imageVector = Icons.Outlined.ExitToApp,
                                contentDescription = "exit app",
                                tint = Color.Red
                            )
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = stringResource(R.string.exit_button),
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

fun rateApp(context: Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
        "market://details?id=" + BuildConfig.APPLICATION_ID
    )))
}

fun shareApp(context: Context) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Wallpaper App")
    shareIntent.putExtra(
        Intent.EXTRA_TEXT, context.getString(R.string.sharing_message) +"\n"+
                "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
    )
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_chooser_title)))
}

@Preview
@Composable
fun TopBarPreview() {
    CategoryTopBar {

    }
}
