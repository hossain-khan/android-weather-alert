package dev.hossain.weatheralert.ui.alertslist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.ui.alertslist.CurrentWeatherAlertScreen.Event
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme

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
                text = { Text("View Onboarding") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.hiking_direction),
                        contentDescription = null,
                        modifier = Modifier.size(width = 60.dp, height = 45.dp),
                    )
                },
                onClick = {
                    expanded = false
                    eventSink(Event.ViewOnboardingClicked)
                },
            )

            DropdownMenuItem(
                text = { Text("Send Feedback") },
                leadingIcon = { Icon(painter = painterResource(R.drawable.github_logo), contentDescription = "Github Logo Icon") },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.open_in_new_24dp),
                        contentDescription = "External Link Icon",
                        modifier = Modifier.size(16.dp),
                    )
                },
                onClick = {
                    expanded = false
                    eventSink(Event.SendFeedbackClicked)
                },
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Credits") },
                leadingIcon = { Icon(painter = painterResource(R.drawable.book_letter_24dp), contentDescription = null) },
                onClick = {
                    expanded = false
                    eventSink(Event.CreditsClicked)
                },
            )

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

@Preview(showBackground = true, name = "Menu Collapsed - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Menu Collapsed - Dark")
@Composable
private fun AppMenuItemsPreviewCollapsed() {
    WeatherAlertAppTheme {
        AppMenuItems(eventSink = {})
    }
}

@Preview(showBackground = true, name = "Menu Expanded - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Menu Expanded - Dark")
@Composable
private fun AppMenuItemsPreviewExpanded() {
    WeatherAlertAppTheme {
        // Create a version with the menu always expanded for preview
        Box(
            modifier = Modifier.wrapContentSize(),
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
                expanded = true,
                onDismissRequest = { },
            ) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    onClick = { },
                )

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text("View Onboarding") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.hiking_direction),
                            contentDescription = null,
                            modifier = Modifier.size(width = 60.dp, height = 45.dp),
                        )
                    },
                    onClick = { },
                )

                DropdownMenuItem(
                    text = { Text("Send Feedback") },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.github_logo), contentDescription = "Github Logo Icon") },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.open_in_new_24dp),
                            contentDescription = "External Link Icon",
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    onClick = { },
                )

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text("Credits") },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.book_letter_24dp), contentDescription = null) },
                    onClick = { },
                )

                DropdownMenuItem(
                    text = { Text("About") },
                    leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    onClick = { },
                )
            }
        }
    }
}
