package dev.hossain.weatheralert.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun AlertScreen(viewModel: AlertViewModel) {
    val tiles = viewModel.tiles.collectAsState(initial = emptyList())
    PreviewScreen(tiles = tiles.value)
}
