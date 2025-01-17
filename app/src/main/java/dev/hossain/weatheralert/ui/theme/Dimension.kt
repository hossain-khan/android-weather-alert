package dev.hossain.weatheralert.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass

// For background, see:
// - https://github.com/hossain-khan/android-weather-alert/issues/126
// - https://bsky.app/profile/hossain.dev/post/3lflhafgn622p

/**
 * Data class to hold dimension values for padding and screen spacing.
 */
data class Dimensions(
    val smallPadding: Dp,
    val mediumPadding: Dp,
    val largePadding: Dp,
    val horizontalScreenPadding: Dp,
    val verticalScreenPadding: Dp,
)

/**
 * CompositionLocal to provide the current Dimensions instance.
 */
val LocalDimensions =
    staticCompositionLocalOf {
        Dimensions(
            smallPadding = 16.dp,
            mediumPadding = 24.dp,
            largePadding = 32.dp,
            horizontalScreenPadding = 16.dp,
            verticalScreenPadding = 16.dp,
        )
    }

/**
 * Extension property to access the current Dimensions instance from MaterialTheme.
 */
val MaterialTheme.dimensions: Dimensions
    @Composable
    get() = LocalDimensions.current

/**
 * Extension function to get Dimensions based on the WindowWidthSizeClass.
 *
 * @receiver WindowWidthSizeClass The current window size class.
 * @return Dimensions The corresponding Dimensions instance.
 */
internal fun WindowWidthSizeClass.dimensions(): Dimensions =
    when (this) {
        WindowWidthSizeClass.COMPACT ->
            Dimensions(
                smallPadding = 8.dp,
                mediumPadding = 16.dp,
                largePadding = 24.dp,
                horizontalScreenPadding = 16.dp,
                verticalScreenPadding = 16.dp,
            )
        WindowWidthSizeClass.MEDIUM ->
            Dimensions(
                smallPadding = 16.dp,
                mediumPadding = 24.dp,
                largePadding = 32.dp,
                horizontalScreenPadding = 48.dp,
                verticalScreenPadding = 24.dp,
            )
        WindowWidthSizeClass.EXPANDED ->
            Dimensions(
                smallPadding = 24.dp,
                mediumPadding = 32.dp,
                largePadding = 40.dp,
                horizontalScreenPadding = 64.dp,
                verticalScreenPadding = 24.dp,
            )
        else ->
            Dimensions(
                smallPadding = 16.dp,
                mediumPadding = 24.dp,
                largePadding = 32.dp,
                horizontalScreenPadding = 16.dp,
                verticalScreenPadding = 16.dp,
            )
    }
