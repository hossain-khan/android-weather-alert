package dev.hossain.weatheralert.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.data.AlertTileData

@Composable
fun PreviewScreen(tiles: List<AlertTileData>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Weather Alerts",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        AlertTileGrid(tiles = tiles)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScreenPreview() {
    val sampleTiles = listOf(
        AlertTileData("Snowfall", "5 cm", "Tomorrow: 7 cm"),
        AlertTileData("Rainfall", "10 mm", "Tomorrow: 12 mm")
    )
    PreviewScreen(tiles = sampleTiles)
}
