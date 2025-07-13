package com.example.mrflashyflashapp.ui.screens

import android.R.attr.contentDescription
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.contentValuesOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.mrflashyflashapp.data.manager.PermissionManager
import com.google.accompanist.permissions.shouldShowRationale
import com.example.mrflashyflashapp.ui.viewmodel.FlashlightViewModel
import androidx.compose.ui.res.painterResource
import com.example.mrflashyflashapp.R
import com.example.mrflashyflashapp.domain.model.FlashlightMode
import kotlinx.serialization.builtins.MapSerializer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FlashlightScreen() {

    val context = LocalContext.current
    val permissionManager = remember { PermissionManager() }

    //This creates a state that automatically handles permission request

    val cameraPermissionState = rememberPermissionState(
        permission = PermissionManager.CAMERA_PERMISSION
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            cameraPermissionState.status.isGranted -> {
                //permission granted - show main UI

                FlashlightContent()
            }

            cameraPermissionState.status.shouldShowRationale -> {
                //User previously  denied , show explanation
                PermissionRequestContent(
                    onRequestPermission = {
                        cameraPermissionState.launchPermissionRequest()
                    }
                )
            }

            else -> {
                //permission not granted - show permission request
                PermissionRequestContent(
                    onRequestPermission = {
                        cameraPermissionState.launchPermissionRequest()
                    }
                )
            }
        }
    }
}

@Composable
fun FlashlightContent() {
    val context = LocalContext.current
    val viewModel: FlashlightViewModel = remember { FlashlightViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(16.dp)
    ) {

        //This is Where main flashlight Ui will go

        Text(
            text = "Flashlight",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        ModeSelectionRow(
            currentMode = uiState.currentMode,
            onModeSelected = { mode -> viewModel.setMode(mode) },
            enabled = uiState.isFlashlightAvailable
        )

        //Error Message if any
        uiState.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        //Main toggle button

        FlashlightToggleButton(
            isOn = uiState.isFlashlightOn,
            isLoading = uiState.isLoading,
            isAvailable = uiState.isFlashlightAvailable,
            isPatternActive = uiState.isPatternActive,
            onToggle = { viewModel.toggleFlashlight() }

        )

        //Status text

        StatusText(
            isFlashlightOn = uiState.isFlashlightOn,
            isFlashlightAvailable = uiState.isFlashlightAvailable,
            isLoading = uiState.isLoading,
            currentMode = uiState.currentMode,
            isPatternActive = uiState.isPatternActive
        )
    }
}

@Composable
fun ModeSelectionRow(
    currentMode: FlashlightMode,
    onModeSelected: (FlashlightMode) -> Unit,
    enabled: Boolean
) {
    val haptic = LocalHapticFeedback.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        FlashlightMode.values().forEach { mode ->
            FilterChip(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onModeSelected(mode)
                },
                label = {
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                selected = currentMode == mode,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            )

        }
    }
}

@Composable
fun FlashlightToggleButton(
    isOn: Boolean,
    isLoading: Boolean,
    isAvailable: Boolean,
    isPatternActive: Boolean,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isOn) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_Scale"
    )
    val buttonColor by animateColorAsState(
        targetValue = when {
            isPatternActive -> MaterialTheme.colorScheme.secondary
            isOn -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(300),
        label = "button_color"
    )
    Button(
        onClick = {
            haptic.performHapticFeedback(
                if (isOn) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
            )
            onToggle()
        },
        enabled = isAvailable && !isLoading,
        modifier = Modifier
            .size(140.dp)
            .scale(scale),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isOn) 8.dp else 4.dp,
            pressedElevation = 12.dp
        )
    )
    {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith  fadeOut(animationSpec = tween(300))
            },
            label = "button_content"
        ) { loading ->

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                )
                {
                    Icon(
                        painter = painterResource(
                            id = if (isOn)
                                R.drawable.ic_flashlight_on_svg
                            else
                                R.drawable.ic_flashlight_off_svg
                        ),
                        contentDescription = if (isOn) "Turn off the light" else "Turn on Flashlight",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                    AnimatedVisibility(
                        visible = isPatternActive,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Text(
                            text = "●●●",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}@Composable
fun StatusText(
    isFlashlightOn: Boolean,
    isFlashlightAvailable: Boolean,
    isLoading: Boolean,
    currentMode: FlashlightMode,
    isPatternActive: Boolean
) {
    val statusText = when {
        !isFlashlightAvailable -> "Flashlight not Available"
        isLoading -> "Please Wait..."
        !isFlashlightOn -> "Tap to Turn On ${currentMode.displayName.lowercase()} mode"
        isPatternActive -> "${currentMode.displayName}Pattern active"
        else -> "${currentMode.displayName} Mode is ON"
    }

    val statusColor = when {
        !isFlashlightAvailable -> MaterialTheme.colorScheme.error
        isFlashlightOn -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = statusText,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = statusColor
    )

}


@Composable
fun PermissionRequestContent(
    onRequestPermission: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Text(
            text = "This app needs camera Permission to control the flashlight",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Grant Permission"
            )
        }
    }
}


