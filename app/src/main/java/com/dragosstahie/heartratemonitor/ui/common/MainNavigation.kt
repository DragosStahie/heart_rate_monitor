package com.dragosstahie.heartratemonitor.ui.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dragosstahie.heartratemonitor.ui.screens.DeviceScreen
import com.dragosstahie.heartratemonitor.ui.screens.PermissionsRequiredScreen
import com.dragosstahie.heartratemonitor.ui.screens.ScanningScreen
import com.dragosstahie.heartratemonitor.ui.screens.haveAllPermissions

@SuppressLint("MissingPermission")
@Composable
fun MainNavigation(viewModel: BLEClientViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var allPermissionsGranted by remember {
        mutableStateOf(haveAllPermissions(context))
    }

    if (!allPermissionsGranted) {
        PermissionsRequiredScreen { allPermissionsGranted = true }
    } else if (uiState.activeDevice == null) {
        ScanningScreen(
            isScanning = uiState.isScanning,
            foundDevices = uiState.foundDevices,
            startScanning = viewModel::startScanning,
            stopScanning = viewModel::stopScanning,
            selectDevice = { device ->
                viewModel.stopScanning()
                viewModel.setActiveDevice(device)
            }
        )
    } else {
        DeviceScreen(
            unselectDevice = {
                viewModel.disconnectActiveDevice()
                viewModel.setActiveDevice(null)
            },
            isDeviceConnected = uiState.isDeviceConnected,
            isReadingHeartRate = uiState.isReadingHeartRate,
            discoveredCharacteristics = uiState.discoveredCharacteristics,
            heartRate = uiState.heartRate,
            connect = viewModel::connectActiveDevice,
            discoverServices = viewModel::discoverActiveDeviceServices,
            readHeartRate = viewModel::readHeartRateFromActiveDevice,
            stopReadingHeartRate = viewModel::stopReadingHeartRateFromActiveDevice,
        )
    }
}