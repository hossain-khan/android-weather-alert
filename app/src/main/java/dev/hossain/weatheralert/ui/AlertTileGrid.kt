package dev.hossain.weatheralert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.data.AlertTileData

@Composable
fun AlertTileGrid(tiles: List<AlertTileData>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tiles) { tile ->
            AlertTile(data = tile, modifier = Modifier.fillMaxWidth())
        }
    }
}
