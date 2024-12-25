package dev.hossain.weatheralert.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AlertScreen(viewModel: AlertViewModel = hiltViewModel()) {
    val tiles = viewModel.tiles.collectAsState(initial = emptyList())
    PreviewScreen(tiles = tiles.value)
}
