package dev.sennix

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(preferenceManager: PreferenceManager) {
    val useImmichFrameDim by preferenceManager.useImmichFrameDim.collectAsState(initial = true)
    val motionSensorTimeout by preferenceManager.motionSensorTimeout.collectAsState(initial = 5)
    val acquireWakeLock by preferenceManager.acquireWakeLock.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // Get the current Context


    val timeoutOptions = listOf(1, 2, 5, 10, 15, 20, 30, 60, 120, 180)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        PreferenceItem(
            title = "Use Immich Frame Dim",
            subtitle = "Dims the screen when displaying Immich frames",
            checked = useImmichFrameDim,
            onCheckedChange = { checked ->
                coroutineScope.launch {
                    preferenceManager.setUseImmichFrameDim(checked)

                    val intent = Intent(context, ScreenControllerService::class.java)
                    intent.action = ScreenControllerService.ACTION_UPDATE_SETTINGS
                    context.startService(intent)
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        PreferenceItem(
            title = "Acquire Wake Lock",
            subtitle = "Prevents the screen from turning off",
            checked = acquireWakeLock,
            onCheckedChange = { checked ->
                coroutineScope.launch {
                    preferenceManager.setAcquireWakeLock(checked)

                    val intent = Intent(context, ScreenControllerService::class.java)
                    intent.action = ScreenControllerService.ACTION_UPDATE_SETTINGS
                    context.startService(intent)
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Dropdown for Motion Sensor Timeout
        var expanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Motion Sensor Timeout", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Sets the timeout for the motion sensor (in minutes)", style = MaterialTheme.typography.bodySmall, color = LocalContentColor.current.copy(alpha = 0.6f))
            }

            Box {
                // Exposing the dropdown menu
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        // Display the currently selected value
                        value = "$motionSensorTimeout minutes",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )

                    // The actual dropdown menu
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        timeoutOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.toString()) },
                                onClick = {
                                    coroutineScope.launch {
                                        preferenceManager.setMotionSensorTimeout(selectionOption)
                                    }
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = LocalContentColor.current.copy(alpha = 0.6f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}