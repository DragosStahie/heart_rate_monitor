package com.dragosstahie.heartratemonitor.ui.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dragosstahie.heartratemonitor.ble.BLEDeviceConnection
import com.dragosstahie.heartratemonitor.ble.BLEScanner
import com.dragosstahie.heartratemonitor.ble.PERMISSION_BLUETOOTH_CONNECT
import com.dragosstahie.heartratemonitor.ble.PERMISSION_BLUETOOTH_SCAN
import com.dragosstahie.heartratemonitor.data.repository.HeartRateRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalCoroutinesApi::class)
class BLEClientViewModel(
    private val application: Application,
    private val heartRateRepository: HeartRateRepository
) : AndroidViewModel(application) {

    private val bleScanner = BLEScanner(application)
    private var activeConnection = MutableStateFlow<BLEDeviceConnection?>(null)

    private val isDeviceConnected =
        activeConnection.flatMapLatest { it?.isConnected ?: flowOf(false) }
    private val isReadingHeartRate = MutableStateFlow(false)
    private val activeDeviceServices = activeConnection.flatMapLatest {
        it?.services ?: flowOf(emptyList())
    }
    private val activeDeviceHeartRate = activeConnection.flatMapLatest {
        it?.heartRate ?: flowOf(null)
    }

    private val _uiState = MutableStateFlow(BLEClientUIState())
    val uiState = combine(
        _uiState,
        isDeviceConnected,
        isReadingHeartRate,
        activeDeviceServices,
        activeDeviceHeartRate
    ) { state, isDeviceConnected, isReadingHeartRate, services, heartRate ->
        if (isReadingHeartRate && heartRate != null) {
            saveReadingToDb(heartRate)
        }

        state.copy(
            isDeviceConnected = isDeviceConnected,
            isReadingHeartRate = isReadingHeartRate,
            discoveredCharacteristics = services.associate { service ->
                Pair(
                    service.uuid.toString(),
                    service.characteristics.map { it.uuid.toString() })
            },
            heartRate = if (isReadingHeartRate) {
                heartRate
            } else {
                null
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BLEClientUIState())

    init {
        viewModelScope.launch {
            bleScanner.foundDevices.collect { devices ->
                _uiState.update { it.copy(foundDevices = devices.filter { it.name != null }) }
            }
        }
        viewModelScope.launch {
            bleScanner.isScanning.collect { isScanning ->
                _uiState.update { it.copy(isScanning = isScanning) }
            }
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    fun startScanning() {
        bleScanner.startScanning()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    fun stopScanning() {
        bleScanner.stopScanning()
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_CONNECT, PERMISSION_BLUETOOTH_SCAN])
    fun setActiveDevice(device: BluetoothDevice?) {
        activeConnection.value = device?.run { BLEDeviceConnection(application, device) }
        _uiState.update { it.copy(activeDevice = device) }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun connectActiveDevice() {
        activeConnection.value?.connect()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun disconnectActiveDevice() {
        activeConnection.value?.disconnect()
        isReadingHeartRate.value = false
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun discoverActiveDeviceServices() {
        activeConnection.value?.discoverServices()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun readHeartRateFromActiveDevice() {
        activeConnection.value?.let {
            clearDb()
            it.readHeartRate()
            isReadingHeartRate.value = true
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun stopReadingHeartRateFromActiveDevice() {
        activeConnection.value?.let {
            it.stopReadingHeartRate()
            isReadingHeartRate.value = false
        }
    }

    private fun saveReadingToDb(heartRate: Int) {
        viewModelScope.launch {
            heartRateRepository.insert(heartRate, System.currentTimeMillis())
        }
    }

    private fun clearDb() {
        viewModelScope.launch {
            heartRateRepository.deleteAll()
        }
    }

    override fun onCleared() {
        super.onCleared()

        //when the ViewModel dies, shut down the BLE client with it
        if (bleScanner.isScanning.value) {
            if (ActivityCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bleScanner.stopScanning()
                isReadingHeartRate.value = false
            }
        }
    }
}

data class BLEClientUIState(
    val isScanning: Boolean = false,
    val isReadingHeartRate: Boolean = false,
    val foundDevices: List<BluetoothDevice> = emptyList(),
    val activeDevice: BluetoothDevice? = null,
    val isDeviceConnected: Boolean = false,
    val discoveredCharacteristics: Map<String, List<String>> = emptyMap(),
    val heartRate: Int? = null,
)