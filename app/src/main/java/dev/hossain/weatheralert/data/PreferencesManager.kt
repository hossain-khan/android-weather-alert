package dev.hossain.weatheralert.data

import android.content.Context
import dev.hossain.weatheralert.di.ApplicationContext
import javax.inject.Inject

/**
 * Manages user preferences using DataStore.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/datastore">DataStore</a>
 */
class PreferencesManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun doNothing() {
            // Do nothing for now
        }
    }
