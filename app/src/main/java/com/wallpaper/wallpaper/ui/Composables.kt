package com.wallpaper.wallpaper.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wallpaper.wallpaper.BuildConfig
import com.wallpaper.wallpaper.R


@Composable
fun WallpaperMenu(showMenu: Boolean, onDismiss: (Boolean) -> Unit) {
    var about by remember{ mutableStateOf(false) }
    val context = LocalContext.current
    DropdownMenu(expanded = showMenu, onDismissRequest = { onDismiss(false) }) {
        DropdownMenuItem(onClick = { about = about.not() }) {
            Icon(
                modifier = Modifier.padding(end = 8.dp),
                imageVector = Icons.Outlined.Info,
                contentDescription = ""
            )
            Text(text = stringResource(R.string.about_button))
        }
        DropdownMenuItem(onClick = { shareApp(context) }) {
            Icon(
                modifier = Modifier.padding(end = 8.dp),
                imageVector = Icons.Outlined.Share,
                contentDescription = "share application"
            )
            Text(text = stringResource(id = R.string.share_button))
        }
        DropdownMenuItem(onClick = { rateApp(context) }) {
            Icon(
                modifier = Modifier.padding(end = 8.dp),
                imageVector = Icons.Outlined.StarRate,
                contentDescription = "Rate Us"
            )
            Text(text = stringResource(id = R.string.rate_button))
        }
        DropdownMenuItem(onClick = { (context as Activity).finish() }) {
            Icon(
                modifier = Modifier.padding(end = 8.dp),
                imageVector = Icons.Outlined.ExitToApp,
                contentDescription = "exit app"
            )
            Text(text = stringResource(id = R.string.exit_button))
        }
    }
    About(showDialog = about, onDismiss = { about = it })
}

@Composable
fun ExitAppDialog(
    showDialog: Boolean,
    onDismiss: (Boolean) -> Unit
) {
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

@Composable
fun About(
    showDialog: Boolean,
    onDismiss: (Boolean) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss(false) },
            title = { Text(text = stringResource(id = R.string.about_button)) },
            text = { Text(stringResource(id = R.string.about_text), style = MaterialTheme.typography.body1)},
            confirmButton = {},
            shape = RoundedCornerShape(15.dp)
        )
    }
}

fun rateApp(context: Context) {
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW, Uri.parse(
        "market://details?id=" + BuildConfig.APPLICATION_ID
    ))
    )
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
