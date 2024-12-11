package com.dragosstahie.heartratemonitor.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dragosstahie.heartratemonitor.ble.BLEDeviceConnection
import com.dragosstahie.heartratemonitor.ble.BLEScanner
import com.dragosstahie.heartratemonitor.ui.common.HeartRateChart
import com.dragosstahie.heartratemonitor.ui.theme.largeActionCallTitle
import com.dragosstahie.heartratemonitor.ui.theme.mediumActionCallTitle
import com.dragosstahie.heartratemonitor.ui.theme.smallActionCallTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
class HomeScreenState(
    private val bleScanner: BLEScanner,
    private val coroutineScope: CoroutineScope
) {
    var isBottomSheetVisible by mutableStateOf(false)
        private set

    var isScanning by mutableStateOf(false)
        private set

    var isSearchingForHeartRateService by mutableStateOf(false)
        private set

    var devicesList by mutableStateOf<List<BluetoothDevice>>(emptyList())
        private set

    var selectedDevice by mutableStateOf<BluetoothDevice?>(null)
        private set

    var heartRateReading by mutableStateOf<Int?>(null)
        private set

    private var bleConnection = MutableStateFlow<BLEDeviceConnection?>(null)

    init {
        coroutineScope.launch {
            bleScanner.foundDevices.collect { devices ->
                devicesList = devices
            }
        }
        coroutineScope.launch {
            bleScanner.isScanning.collect {
                isScanning = it
            }
        }
        coroutineScope.launch {
            bleConnection.collect { connection ->
                connection?.listenForConnectionChange()
                connection?.listenForServices()
                connection?.listenForHeartRate()
            }
        }
    }

    fun onOpenDeviceList() {
        isBottomSheetVisible = true
        bleScanner.startScanning()
    }

    fun onCloseDeviceList() {
        isBottomSheetVisible = false
        bleScanner.stopScanning()
    }

    fun onDeviceSelected(context: Context, deviceName: String) {
        selectedDevice = devicesList.find { it.name == deviceName }
        bleConnection.value = selectedDevice?.run { BLEDeviceConnection(context, this) }
        isSearchingForHeartRateService = true
        bleConnection.value?.connect()
    }

    private fun BLEDeviceConnection.listenForConnectionChange() {
        coroutineScope.launch {
            isConnected.collect { isConnected ->
                if (isConnected) {
                    discoverServices()
                }
            }
        }
    }

    private fun BLEDeviceConnection.listenForServices() {
        coroutineScope.launch {
            services.collect { services ->
                if (services.isNotEmpty()) {
                    readHeartRate()
                } else {
                    Log.e(selectedDevice?.name, "Services not found!")
                }
            }
        }
    }

    private fun BLEDeviceConnection.listenForHeartRate() {
        coroutineScope.launch {
            heartRate.collect { hr ->
                heartRateReading = hr
                if (services.value.isNotEmpty() && heartRateReading != null) {
                    isSearchingForHeartRateService = false
                }
            }
        }
    }
}

@Composable
fun rememberHomeScreenState(
    bleScanner: BLEScanner = koinInject(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember {
    HomeScreenState(
        bleScanner = bleScanner,
        coroutineScope = coroutineScope
    )
}

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    state: HomeScreenState,
    modifier: Modifier = Modifier,
) {
    val deviceNames by remember(state.devicesList) {
        derivedStateOf {
            state.devicesList.mapNotNull { it.name }
        }
    }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { state.onOpenDeviceList() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.selectedDevice?.name ?: "No selected device",
                    fontSize = 20.sp,
                )
                if (state.isSearchingForHeartRateService) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    Text(
                        text = "${state.heartRateReading ?: "--"} BPM",
                        fontSize = 20.sp,
                    )
                }
            }
        }
        HeartRateChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
        )
    }
    AddShoppingListBottomSheet(
        deviceNameList = deviceNames,
        isScanning = state.isScanning,
        isVisible = state.isBottomSheetVisible,
        onDismiss = { state.onCloseDeviceList() },
        onDeviceSelected = { deviceName -> state.onDeviceSelected(context, deviceName) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddShoppingListBottomSheet(
    deviceNameList: List<String>,
    isScanning: Boolean,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDeviceSelected: (deviceName: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(isVisible) {
        if (isVisible) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
    ) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            dragHandle = null,
            modifier = Modifier
                .defaultMinSize(minHeight = 500.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Select a device",
                    style = largeActionCallTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FilledIconButton(
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = {
                        onDismiss()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    )
                }
            }
            if (deviceNameList.isEmpty()) {
                if (isScanning) {
                    Text(
                        text = "Scanning for devices...",
                        textAlign = TextAlign.Center,
                        style = mediumActionCallTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 200.dp),
                    )
                } else {
                    Text(
                        text = "No devices in range..",
                        textAlign = TextAlign.Center,
                        style = mediumActionCallTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(deviceNameList) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable {
                                    onDeviceSelected(item)
                                    onDismiss()
                                },
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(
                                    text = item,
                                    style = smallActionCallTitle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        state = rememberHomeScreenState(),
        modifier = Modifier.fillMaxSize(),
    )
}