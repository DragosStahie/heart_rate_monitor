package com.dragosstahie.heartratemonitor.ui.common

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dragosstahie.heartratemonitor.ui.screens.HomeScreen
import kotlinx.parcelize.Parcelize


@Parcelize
sealed class Destination : Parcelable {
    data object HomeScreen : Destination()
}

enum class NavigationDirection {
    FORWARD, BACKWARD, UP, DOWN, NONE
}

class NavigationController(
    homeDestination: Destination,
) {

    var currentDestination by mutableStateOf(homeDestination)
        private set

    var navigationDirection by mutableStateOf(NavigationDirection.FORWARD)
        private set

    fun navigate(
        destination: Destination, direction: NavigationDirection
    ) {
        currentDestination = destination
        navigationDirection = direction
    }

    companion object {
        private const val CURRENT_DESTINATION = "CURRENT_DESTINATION"

        fun getSaver(): Saver<NavigationController, *> =
            mapSaver(save = { mapOf(CURRENT_DESTINATION to it.currentDestination) }, restore = {
                NavigationController(
                    it[CURRENT_DESTINATION] as Destination
                )
            })
    }
}

@Composable
fun rememberNavigationController(
    homeDestination: Destination,
): NavigationController = rememberSaveable(saver = NavigationController.getSaver()) {
    NavigationController(homeDestination)
}

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    navigationController: NavigationController = rememberNavigationController(homeDestination = Destination.HomeScreen),
) {

    val enterTransition: EnterTransition = remember(navigationController.navigationDirection) {
        when (navigationController.navigationDirection) {
            NavigationDirection.FORWARD -> slideInHorizontally(
                initialOffsetX = { it / 2 }, animationSpec = spring()
            )

            NavigationDirection.BACKWARD -> slideInHorizontally(
                initialOffsetX = { -it / 2 }, animationSpec = spring()
            )

            NavigationDirection.UP -> slideInVertically(
                initialOffsetY = { it / 2 }, animationSpec = spring()
            )

            NavigationDirection.DOWN -> slideInVertically(
                initialOffsetY = { -it / 2 }, animationSpec = spring()
            )

            NavigationDirection.NONE -> EnterTransition.None
        }
    }

    val exitTransition: ExitTransition = remember(navigationController.navigationDirection) {
        when (navigationController.navigationDirection) {
            NavigationDirection.FORWARD -> slideOutHorizontally(
                targetOffsetX = { -it / 2 }, animationSpec = spring()
            )

            NavigationDirection.BACKWARD -> slideOutHorizontally(
                targetOffsetX = { it / 2 }, animationSpec = spring()
            )

            NavigationDirection.UP -> slideOutVertically(
                targetOffsetY = { -it / 2 }, animationSpec = spring()
            )

            NavigationDirection.DOWN -> slideOutVertically(
                targetOffsetY = { it / 2 }, animationSpec = spring()
            )

            NavigationDirection.NONE -> ExitTransition.None
        }
    }

    AnimatedContent(
        targetState = navigationController.currentDestination,
        modifier = modifier,
        transitionSpec = { enterTransition.togetherWith(exitTransition) },
        label = "screen_animation"
    ) { destination ->
        when (destination) {
            is Destination.HomeScreen -> NavigationItem {
                HomeScreen()
            }
        }
    }
}

@Composable
fun NavigationItem(
    modifier: Modifier = Modifier,
    onBackPressed: (() -> Unit)? = null,
    composable: @Composable () -> Unit
) {
    Box(modifier) {
        composable.invoke()

        onBackPressed?.let {
            BackHandler {
                it.invoke()
            }
        }
    }
}