package com.wallpaper.wallpaper.ui

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wallpaper.wallpaper.R
import com.wallpaper.wallpaper.ui.theme.WallpaperTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navToCategory: () -> Unit
) {
    val scale = remember {
        Animatable(0f)
    }
    val visible = remember{mutableStateOf(false)}

    // AnimationEffect
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = {
                    OvershootInterpolator(4f).getInterpolation(it)
                })
        )
        visible.value = true
        delay(3000)
        navToCategory()
    }
    // Image
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // logo
        Image(
            modifier = Modifier
                .size(150.dp)
                .scale(scale.value),
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo"
        )
        //Welcome
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn(tween(500)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                text = stringResource(R.string.welcome),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            )
        }
    }


}

@Preview
@Composable
fun SplashPreview() {
    WallpaperTheme {
        SplashScreen {

        }
    }
}