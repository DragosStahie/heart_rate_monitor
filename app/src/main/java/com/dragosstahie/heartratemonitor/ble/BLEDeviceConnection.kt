package com.dragosstahie.heartratemonitor.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.ByteBuffer

val HEART_RATE_MEASUREMENT_CHAR_UUID = 0x2A37.toUUID()
val ENABLE_NOTIFICATIONS_DESCRIPTOR_UUID = 0x2902.toUUID()

@Suppress("DEPRECATION")
class BLEDeviceConnection @RequiresPermission("PERMISSION_BLUETOOTH_CONNECT") constructor(
    private val context: Context,
    private val bluetoothDevice: BluetoothDevice
) {
    val isConnected = MutableStateFlow(false)
    val heartRate = MutableStateFlow<Int?>(null)
    val services = MutableStateFlow<List<BluetoothGattService>>(emptyList())

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val connected = newState == BluetoothGatt.STATE_CONNECTED
            if (connected) {
                //read the list of services
                services.value = gatt.services
            }
            isConnected.value = connected
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            services.value = gatt.services
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID) {
                heartRate.value = characteristic.value.getHeartRate()
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }
    }

    private var gatt: BluetoothGatt? = null

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun connect() {
        gatt = bluetoothDevice.connectGatt(context, false, callback)
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun discoverServices() {
        gatt?.discoverServices()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun readHeartRate() {
        val service = gatt?.getService(HEART_RATE_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
        if (characteristic != null) {
            gatt?.setCharacteristicNotification(characteristic, true)

            val desc = characteristic.getDescriptor(ENABLE_NOTIFICATIONS_DESCRIPTOR_UUID)
            desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            gatt?.writeDescriptor(desc)
        }
    }


    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun stopReadingHeartRate() {
        val service = gatt?.getService(HEART_RATE_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
        if (characteristic != null) {
            val desc = characteristic.getDescriptor(ENABLE_NOTIFICATIONS_DESCRIPTOR_UUID)
            desc?.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
            gatt?.writeDescriptor(desc)

            gatt?.setCharacteristicNotification(characteristic, false)
        }
    }

    private fun ByteArray.getHeartRate(): Int {
        // first byte of heart rate record denotes flags
        var hr = -1
        val flags: Byte = this[0]

        var offset = 1

        val hrc2 = (flags.toInt() and 1) == 1
        if (hrc2) { // this means the BPM is un uint16
            hr = ByteBuffer.wrap(this, offset, 2).short.toInt()
            offset += 2
        } else { // BPM is uint8
            hr = this[offset].toInt()
            offset += 1
        }

        // see if EE is available
        // if so, pull 2 bytes
        val ee = (flags.toInt() and (1 shl 3)) != 0
        if (ee) {
            offset += 2
        }

        // see if RR is present
        // if so, the number of RR values is total bytes left / 2 (size of uint16)
        val rr = (flags.toInt() and (1 shl 4)) != 0
        if (rr) {
            val count = (this.size - offset) / 2
            for (i in 0 until count) {
                // each existence of these values means an R-Wave was already detected
                // the ushort means the time (1/1024 seconds) since last r-wave
                val value: UShort = ByteBuffer.wrap(this, offset, 2).short.toUShort()

                val intervalLengthInSeconds = value.toDouble() / 1024.0
                offset += 2
            }
        }

        return hr
    }
}