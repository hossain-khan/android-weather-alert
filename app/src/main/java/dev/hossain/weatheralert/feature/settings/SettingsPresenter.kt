package dev.hossain.weatheralert.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.hossain.weatheralert.core.data.AlertConfigDataStore
import dev.hossain.weatheralert.core.model.AlertCategory
import dev.hossain.weatheralert.core.model.AlertConfig
import dev.hossain.weatheralert.feature.settings.SettingsScreen.Event
import dev.hossain.weatheralert.feature.settings.SettingsScreen.State
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsPresenter @Inject constructor(
    private val dataStore: AlertConfigDataStore
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val configs by dataStore.getAlertConfigs().collectAsRetainedState(initial = emptyList())
        var snowThreshold by remember {
            mutableStateOf(
                configs.firstOrNull { it.category is AlertCategory.Snow }?.threshold?.toString()
                    ?: ""
            )
        }
        var rainThreshold by remember {
            mutableStateOf(
                configs.firstOrNull { it.category is AlertCategory.Rain }?.threshold?.toString()
                    ?: ""
            )
        }

        return State(
            snowThreshold = snowThreshold,
            rainThreshold = rainThreshold,
            eventSink = { event ->
                when (event) {
                    is Event.UpdateSnowThreshold -> snowThreshold = event.value
                    is Event.UpdateRainThreshold -> rainThreshold = event.value
                    is Event.SaveSnowThreshold -> {
                        coroutineScope.launch {
                            val value = event.value.toDoubleOrNull() ?: 0.0
                            dataStore.saveAlertConfig(AlertConfig(AlertCategory.Snow, value))
                        }
                    }

                    is Event.SaveRainThreshold -> {
                        coroutineScope.launch {
                            val value = event.value.toDoubleOrNull() ?: 0.0
                            dataStore.saveAlertConfig(AlertConfig(AlertCategory.Rain, value))
                        }
                    }
                }
            }
        )
    }
}