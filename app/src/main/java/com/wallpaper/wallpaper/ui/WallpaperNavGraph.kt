package com.wallpaper.wallpaper.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.wallpaper.wallpaper.data.category.Category
import com.wallpaper.wallpaper.viewmodel.CategoryViewModel
import com.wallpaper.wallpaper.viewmodel.PictureViewModel
import com.wallpaper.wallpaper.viewmodel.WallpaperViewModel


object MainDestinations {
    const val SPLASH_ROUTE = "splash"
    const val CATEGORY_ROUTE = "categories"
    const val WALLPAPER_ROUTE = "wallpapers"
    const val PICTURE_ROUTE = "pictures"
    const val FAVOURITE_ROUTE = "favourites"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WallpaperNavGraph(
    navController: NavHostController = rememberAnimatedNavController(),
    startDestination: String = MainDestinations.SPLASH_ROUTE
) {
    val actions = remember(navController) { MainActions(navController) }
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            MainDestinations.SPLASH_ROUTE,
            enterTransition = { fadeIn(tween(1000)) },
            exitTransition = { fadeOut(tween(1000)) },
        ) {
            SplashScreen {
                navController.navigate(MainDestinations.CATEGORY_ROUTE)
            }
        }
        composable(
            MainDestinations.CATEGORY_ROUTE,
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Start,
                    tween(500)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.End,
                    tween(500)
                )
            },
        ) {
            val viewModel = hiltViewModel<CategoryViewModel>()
            CategoryScreen(
                viewModel = viewModel,
                navToWallpaper = actions.navigateToWallpaper,
                navToFavourite = actions.navigateToFavourite
            )
        }
        composable(
            route = "${MainDestinations.CATEGORY_ROUTE}/{category_id}/" +
                    "${MainDestinations.WALLPAPER_ROUTE}?category_name={category_name}",
            arguments = listOf(navArgument("category_id") { type = NavType.StringType },
                navArgument("category_name") { type = NavType.StringType }),
        ) {
            val viewModel = hiltViewModel<WallpaperViewModel>()
            WallpaperScreen(
                viewModel = viewModel,
                navToPicture = actions.navigateToPicture,
                onBack = actions.upPress,
                navToFavourite = actions.navigateToFavourite
            )
        }
        composable(
            route = MainDestinations.FAVOURITE_ROUTE,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Up,
                    tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Down,
                    tween(500)
                )
            }
        ) {
            val viewModel = hiltViewModel<WallpaperViewModel>()
            WallpaperScreen(
                viewModel = viewModel,
                navToPicture = actions.navigateToPicture,
                onBack = actions.upPress
            )
        }
        composable(
            route = "${MainDestinations.WALLPAPER_ROUTE}/{picture_id}/${MainDestinations.PICTURE_ROUTE}" +
                    "?category_id={category_id},category_name={category_name}",
            arguments = listOf(navArgument("picture_id") { type = NavType.StringType },
                navArgument("category_id") { nullable = true },
                navArgument("category_name") { nullable = true }
            ),
            enterTransition = { expandIn(tween(1000)) },
            exitTransition = { shrinkOut(tween(1000)) },
        ) {
            val viewModel = hiltViewModel<PictureViewModel>()
            PictureScreen(
                viewModel,
                actions.upPress
            )
        }
    }
}

class MainActions(navController: NavHostController) {
    val navigateToWallpaper: (Category) -> Unit = { category: Category ->
        navController.navigate(
            "${MainDestinations.CATEGORY_ROUTE}/${category.id}/" +
                    "${MainDestinations.WALLPAPER_ROUTE}?category_name=${category.name}"
        )
    }
    val navigateToPicture: (String, String, String) -> Unit =
        { pictureId: String, categoryId: String, categoryName: String ->
            val destination =
                "${MainDestinations.WALLPAPER_ROUTE}/$pictureId/${MainDestinations.PICTURE_ROUTE}"
            navController.navigate(
                if (categoryId.isEmpty()) {
                    destination
                } else {
                    "$destination?category_id=$categoryId,category_name=$categoryName"
                }
            )
        }

    val navigateToFavourite: () -> Unit =
        { navController.navigate(MainDestinations.FAVOURITE_ROUTE) }

    val upPress: () -> Unit = {
        navController.navigateUp()
    }
}