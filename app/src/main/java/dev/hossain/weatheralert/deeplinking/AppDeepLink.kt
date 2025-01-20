package dev.hossain.weatheralert.deeplinking

import android.net.Uri
import androidx.core.net.toUri

internal const val DEEP_LINK_SCHEME = "weatheralert"
internal const val DEEP_LINK_HOST_VIEW_ALERT = "view_alert"

/**
 * Creates a deep link URI for the alert.
 *
 * For example: `weatheralert://view_alert/123`
 */
internal fun createViewAlertDeeplinkUri(alertId: Long): Uri = "$DEEP_LINK_SCHEME://$DEEP_LINK_HOST_VIEW_ALERT/$alertId".toUri()

/**
 * Extracts the alert id from the deep link URI.
 *
 * For example:
 * Get the alert id `123` from the path: `weatheralert://view_alert/123`
 */
internal fun getIdFromPath(dataUri: Uri): Long? = dataUri.pathSegments.firstOrNull()?.toLongOrNull()
