package com.example.mrflashyflashapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mrflashyflashapp.data.manager.FlashlightManager
import com.example.mrflashyflashapp.domain.model.FlashlightMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class FlashlightViewModel(context: Context) :
    ViewModel() {

    private val flashlightManager = FlashlightManager(context)

    //UI State
    private val _uiState = MutableStateFlow(FlashlightUiState())

    val uiState: StateFlow<FlashlightUiState> = _uiState.asStateFlow()

    init {
        //Observe flashlight manager States
        viewModelScope.launch {
            flashlightManager.isFlashlightOn.collect { isOn ->
                _uiState.value = _uiState.value.copy(
                    isFlashlightOn = isOn,
                    isLoading = false
                )
            }
        }


        viewModelScope.launch {
            flashlightManager.isAvailable.collect { isAvailable ->
                _uiState.value = _uiState.value.copy(
                    isFlashlightAvailable = isAvailable,
                    errorMessage = if (isAvailable) "Flashlight not Available on this device " else null
                )
            }

        }

        viewModelScope.launch {
            flashlightManager.currentMode.collect { mode ->
                _uiState.value = _uiState.value.copy(
                    currentMode = mode
                )
            }
        }

        viewModelScope.launch {
            flashlightManager.isPatternActive.collect { isActive ->
                _uiState.value = _uiState.value.copy(
                    isPatternActive = isActive
                )
            }
        }


    }


    fun toggleFlashlight() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        flashlightManager.toggleFlashlight()
    }

    fun setMode(mode: FlashlightMode) {
        flashlightManager.setMode(mode)
    }

    override fun onCleared() {
        super.onCleared()
        //Turn off flashlight when app closes
        flashlightManager.turnOff()
    }
}

data class FlashlightUiState(
    val isFlashlightOn: Boolean = false,
    val isFlashlightAvailable: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentMode: FlashlightMode= FlashlightMode.STEADY,
    val isPatternActive :Boolean =false
)
