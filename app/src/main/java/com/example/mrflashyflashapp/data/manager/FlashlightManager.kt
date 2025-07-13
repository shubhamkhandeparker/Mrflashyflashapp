package com.example.mrflashyflashapp.data.manager

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.mrflashyflashapp.domain.model.FlashlightMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch



class FlashlightManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    private var patternJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())


    //State Management for flashlight
    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()
    private val _isAvailable = MutableStateFlow(true)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _isPatternActive = MutableStateFlow(false)
    val isPatternActive: StateFlow<Boolean> = _isPatternActive.asStateFlow()

    private val _currentMode=MutableStateFlow(FlashlightMode.STEADY)
    val currentMode: StateFlow<FlashlightMode> = _currentMode.asStateFlow()

    companion object {
        private const val TAG = "FlashlightManager"
        private const val STROBE_INTERVAL = 200L //200ms interval
        private const val SOS_DOT = 200L //short Flash
        private const val SOS_DASH = 600L //Long Flash
        private const val SOS_GAP = 200L //Gap between
        private const val SOS_LETTER_GAP = 600L //Gap between letters
        private const val SOS_WORD_GAP = 1400L //Gap between repetitions
    }

    init {
        initializeCamera()
    }

    private fun initializeCamera() {
        try {
            //find camera with flash
            cameraId = findCameraWithFlash()
            if (cameraId == null) {
                Log.e(TAG, "No Camera with flash found")
                _isAvailable.value = false
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Camera access exception during initialization", e)
            _isAvailable.value = false
        }
    }

    private fun findCameraWithFlash(): String? {
        return try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)

                val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)

                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)


                //we want the back camera with flash

                if (flashAvailable == true && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return id
                }
            }
            null
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error Finding Camera with flash ", e)
            null
        }
    }

    fun setMode(mode: FlashlightMode) {
        if (_currentMode.value == mode) return

        //Step current Pattern

        stopPattern()
        _currentMode.value = mode

        //if flashlight was on restart with new mode

        if (_isFlashlightOn.value) {
            startFlashlight()
        }
    }


    fun toggleFlashlight() {
        if (_isFlashlightOn.value) {
            stopFlashlight()
        } else {
            startFlashlight()
        }
    }

    private fun startFlashlight() {
        if (!_isAvailable.value || cameraId == null) {
            Log.w(TAG, "Flashlight not available ")
            return
        }

        _isFlashlightOn.value = true

        when (_currentMode.value) {
            FlashlightMode.STEADY -> startSteadyMode()
            FlashlightMode.STROBE -> startStrobeMode()
            FlashlightMode.SOS -> startSOSMode()

        }

    }


    private fun stopFlashlight() {
        stopPattern()
        setTorchMode(false)
        _isFlashlightOn.value = false
        _isPatternActive.value = false

    }

    private fun startSteadyMode() {
        setTorchMode(true)
    }

    private fun startStrobeMode() {
        _isPatternActive.value = true
        patternJob = scope.launch {
            while (isActive && _isFlashlightOn.value) {
                setTorchMode(true)
                delay(STROBE_INTERVAL)
                setTorchMode(false)
                delay(STROBE_INTERVAL)
            }
        }
    }


    private fun startSOSMode() {

        _isPatternActive.value = true
        patternJob = scope.launch {
            while (isActive && _isFlashlightOn.value) {
                //s(...)
                repeat(3) {
                    setTorchMode(true)
                    delay(SOS_DOT)
                    setTorchMode(false)
                    delay(SOS_GAP)

                }
                delay(SOS_LETTER_GAP)

                //O(---)
                repeat(3) {
                    setTorchMode(true)
                    delay(SOS_DASH)
                    setTorchMode(false)
                    delay(SOS_GAP)
                }


                delay(SOS_LETTER_GAP)

                //s(...)
                repeat(3) {
                    setTorchMode(true)
                    delay(SOS_DOT)
                    setTorchMode(false)
                    delay(SOS_GAP)
                }
                //wait before repeating

                delay(SOS_WORD_GAP)
            }
        }
    }

    private fun stopPattern(){
        patternJob?.cancel()
        patternJob=null
        _isPatternActive.value=false
    }

    private fun setTorchMode(enabled: Boolean){
        try{
            cameraManager.setTorchMode(cameraId!!,enabled)
        }catch (e: CameraAccessException){
            Log.e(TAG,"Failed to set torch Mode",e)
            _isFlashlightOn.value=false
            _isPatternActive.value=false
        }
    }

    fun turnOff(){
        stopFlashlight()
    }

    fun  isFlashlightAvailable(): Boolean=_isAvailable.value

    fun cleanup(){
        stopPattern()
        scope.cancel()
    }
}