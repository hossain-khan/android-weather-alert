package dev.hossain.weatheralert.ui.alertslist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.ui.alertslist.CurrentWeatherAlertScreen.Event

/**
 * App menu items that shows settings, about etc.
 *
 * Dev Ref: https://developer.android.com/develop/ui/compose/components/menu
 */
@Composable
fun AppMenuItems(
    modifier: Modifier = Modifier,
    eventSink: (Event) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = modifier.wrapContentSize(),
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Settings") },
                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                onClick = {
                    expanded = false
                    eventSink(Event.SettingsClicked)
                },
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Send Feedback") },
                leadingIcon = { Icon(painter = painterResource(R.drawable.github_logo), contentDescription = "Github Logo Icon") },
                trailingIcon = { Icon(painter = painterResource(R.drawable.open_in_new_24dp), contentDescription = "External Link Icon") },
                onClick = {
                    expanded = false
                    // Take user to GitHub issues page to report issue or provide feedback.
                    uriHandler.openUri("https://github.com/hossain-khan/android-weather-alert/issues")
                },
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("About") },
                leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                onClick = {
                    expanded = false
                    eventSink(Event.AboutAppClicked)
                },
            )
        }
    }
}
