package com.hnhy.bluedemo_kotlin

import android.bluetooth.BluetoothDevice

data class BleDevice(var device: BluetoothDevice, var rssi: Int, var name: String)
