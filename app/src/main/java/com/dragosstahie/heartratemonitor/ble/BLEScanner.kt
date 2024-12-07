package com.dragosstahie.heartratemonitor.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID


const val PERMISSION_BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN"
const val PERMISSION_BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"

val HEART_RATE_SERVICE_UUID = 0x180D.toUUID()

fun Int.toUUID(): UUID {
    val msb = 0x0000000000001000L
    val lsb = -0x7fffff7fa064cb05L
    val value = (this and -0x1).toLong()
    return UUID(msb or (value shl 32), lsb)
}

class BLEScanner(context: Context) {

    private val bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager
        ?: throw Exception("Bluetooth is not supported by this device")

    val isScanning = MutableStateFlow(false)

    val foundDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())

    private val scanner: BluetoothLeScanner
        get() = bluetooth.adapter.bluetoothLeScanner

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return

            if (!foundDevices.value.contains(result.device)) {
                foundDevices.update { it + result.device }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning.value = false
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    fun startScanning() {
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(HEART_RATE_SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0)
            .build()

        scanner.startScan(mutableListOf(filter), settings, scanCallback)
        isScanning.value = true
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    fun stopScanning() {
        scanner.stopScan(scanCallback)
        isScanning.value = false
    }


}