package dev.hossain.weatheralert.data.repository

import android.content.Context
import androidx.datastore.core.Serializer
import com.squareup.moshi.Json
import dev.hossain.weatheralert.domain.model.AlertConfig
import java.io.InputStream
import java.io.OutputStream

@Singleton
class AlertConfigRepository @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.createDataStore(
        name = "alert_configs",
        serializer = AlertConfigSerializer
    )

    suspend fun getAlertConfigs(): List<AlertConfig> {
        return dataStore.data.first().configs
    }

    suspend fun addAlertConfig(config: AlertConfig) {
        dataStore.updateData { prefs ->
            prefs.copy(configs = prefs.configs + config)
        }
    }

    suspend fun removeAlertConfig(configId: String) {
        dataStore.updateData { prefs ->
            prefs.copy(configs = prefs.configs.filterNot { it.id == configId })
        }
    }

    suspend fun updateAlertConfig(config: AlertConfig) {
        dataStore.updateData { prefs ->
            prefs.copy(
                configs = prefs.configs.map {
                    if (it.id == config.id) config else it
                }
            )
        }
    }
}

// Proto serializer for AlertConfig
@Serializable
data class AlertConfigsPreferences(
    val configs: List<AlertConfig> = emptyList()
)

object AlertConfigSerializer : Serializer<AlertConfigsPreferences> {
    override val defaultValue: AlertConfigsPreferences = AlertConfigsPreferences()

    override suspend fun readFrom(input: InputStream): AlertConfigsPreferences {
        return try {
            Json.decodeFromString(
                AlertConfigsPreferences.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: AlertConfigsPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(AlertConfigsPreferences.serializer(), t)
                .encodeToByteArray()
        )
    }
}