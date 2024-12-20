package com.dragosstahie.heartratemonitor.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import androidx.annotation.RequiresPermission
import com.dragosstahie.heartratemonitor.common.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
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
    val heartRate =
        MutableSharedFlow<Int?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val services = MutableStateFlow<List<BluetoothGattService>>(emptyList())
    private val logger = Logger.getInstance()

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val connected = newState == BluetoothGatt.STATE_CONNECTED
            if (connected) {
                //read the list of services
                services.value = gatt.services
            }
            isConnected.value = connected
            logger.debug("onConnectionStateChange: connected = $connected")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            services.value = gatt.services
            logger.debug("onServicesDiscovered: ${gatt.services}")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID) {
                logger.debug("onCharacteristicChanged: found HR characteristic!")
                heartRate.tryEmit(characteristic.value.getHeartRate())
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
        logger.debug("disconnect")
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun connect() {
        gatt = bluetoothDevice.connectGatt(context, false, callback)
        logger.debug("connect")
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun discoverServices() {
        gatt?.discoverServices()
        logger.debug("discoverServices")
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
        logger.debug("readHeartRate - characteristic = $characteristic")
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
        logger.debug("stopReadHeartRate - characteristic = $characteristic")
    }

    private fun ByteArray.getHeartRate(): Int {
        logger.debug("getHeartRate - start")

        // first byte of heart rate record denotes flags
        val hr: Int
        val flags: Byte = this[0]

        var offset = 1

        val hrc2 = (flags.toInt() and 1) == 1
        logger.debug("getHeartRate - hrc2 = $hrc2")

        if (hrc2) { // this means the BPM is un uint16
            hr = byteArrayOf(this[offset], this[offset + 1]).fromUnsignedBytesToInt()
            logger.debug("getHeartRate - reading as uint16 BIG ENDIAN - hr = $hr from bytes ${this[offset]}${this[offset + 1]}")
            offset += 2
        } else { // BPM is uint8
            hr = byteArrayOf(this[offset]).fromUnsignedBytesToInt()
            logger.debug("getHeartRate - reading as uint8  - hr = $hr from byte ${this[offset]}")
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
                logger.debug("getHeartRate - reading respiration - rr = $intervalLengthInSeconds")
            }
        }

        return hr
    }

    private fun ByteArray.fromUnsignedBytesToInt(): Int {
        val bytes = 4
        val paddedArray = ByteArray(bytes)
        for (i in 0 until bytes - this.size) paddedArray[i] = 0
        for (i in bytes - this.size until paddedArray.size) paddedArray[i] =
            this[i - (bytes - this.size)]

        return (((paddedArray[0].toULong() and 0xFFu) shl 24) or
                ((paddedArray[1].toULong() and 0xFFu) shl 16) or
                ((paddedArray[2].toULong() and 0xFFu) shl 8) or
                (paddedArray[3].toULong() and 0xFFu)).toInt()
    }
}