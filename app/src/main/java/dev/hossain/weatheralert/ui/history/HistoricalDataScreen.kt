package dev.hossain.weatheralert.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.data.HistoricalWeatherRepository
import dev.hossain.weatheralert.datamodel.HistoricalWeather
import dev.hossain.weatheralert.db.City
import dev.hossain.weatheralert.di.AppScope
import kotlinx.parcelize.Parcelize

@Parcelize
class HistoricalDataScreen(
    val cityId: Long,
    val cityName: String,
) : Screen {
    data class State(
        val city: City?,
        val historicalData: List<HistoricalWeather>?,
        val isLoading: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()
    }
}

class HistoricalDataPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: HistoricalDataScreen,
        private val historicalWeatherRepository: HistoricalWeatherRepository,
    ) : Presenter<HistoricalDataScreen.State> {
        @Composable
        override fun present(): HistoricalDataScreen.State {
            var isLoading by remember { mutableStateOf(true) }
            var historicalData by remember { mutableStateOf<List<HistoricalWeather>?>(null) }
            // TODO: Load city info if needed
            val city: City? = null

            LaunchedEffect(screen.cityId) {
                isLoading = true
                historicalWeatherRepository
                    .getHistoryForCity(
                        cityId = screen.cityId,
                        start = 0L, // TODO: Use actual range
                        end = System.currentTimeMillis(),
                    ).collect { data ->
                        historicalData = data
                        isLoading = false
                    }
            }

            return HistoricalDataScreen.State(
                city = city,
                historicalData = historicalData,
                isLoading = isLoading,
            ) { event ->
                when (event) {
                    HistoricalDataScreen.Event.GoBack -> navigator.pop()
                }
            }
        }

        @CircuitInject(HistoricalDataScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(
                navigator: Navigator,
                screen: HistoricalDataScreen,
            ): HistoricalDataPresenter
        }
    }

@CircuitInject(HistoricalDataScreen::class, AppScope::class)
@Composable
fun HistoricalDataScreen(
    state: HistoricalDataScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historical Data") },
                navigationIcon = { /* TODO: Add back button */ },
            )
        },
    ) { contentPadding ->
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        } else {
            LazyColumn(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.historicalData?.forEach { record ->
                    item {
                        Card(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Date: ${record.date}", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Snow: ${record.snow}")
                                Text(text = "Rain: ${record.rain}")
                                Text(text = "Source: ${record.forecastSourceService}")
                            }
                        }
                    }
                }
            }
        }
    }
}
