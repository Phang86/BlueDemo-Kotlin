package com.hnhy.bluedemo_kotlin.callback

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Build
import android.util.Log
import com.hnhy.bluedemo_kotlin.utils.BleConstant
import com.hnhy.bluedemo_kotlin.utils.BleHelper
import com.hnhy.bluedemo_kotlin.utils.ByteUtils
import java.util.*

class BleCallback : BluetoothGattCallback() {

    private val TAG = BleCallback::class.java.simpleName

    private lateinit var uiCallback: UiCallback

    fun setUiCallback(uiCallback: UiCallback){
        this.uiCallback = uiCallback
    }

    /**
     * 连接状态回调
     */
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS){
            Log.e(TAG, "onConnectionStateChange: $status")
            return
        }
        uiCallback.state(
            when(newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    //获取MtuSize
                    gatt?.requestMtu(512)
                    "连接成功"
                }
                BluetoothProfile.STATE_DISCONNECTED -> "断开连接"
                else -> "onConnectionStateChange: $status"
            }
        )
    }

    /**
     * 获取MtuSize回调
     */
    @SuppressLint("MissingPermission")
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        uiCallback.state("获取到MtuSize$mtu")
        //发现服务
        gatt?.discoverServices()
    }

    /**
     * 发现服务回调
     */
    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        uiCallback.state(if (!BleHelper.enableIndicateNotification(gatt!!)){
            gatt?.disconnect()
            "开启通知属性异常"
        } else "发现了服务")
    }

    /**
     * 特性改变回调
     */
    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        val content = ByteUtils.bytesToHexString(characteristic!!.value)
        uiCallback.state("特性改变：收到内容$content")
    }

    /**
     * 特性写入回调
     */
    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        val command = ByteUtils.bytesToHexString(characteristic!!.value)
        uiCallback.state("特性写入：${if (status == BluetoothGatt.GATT_SUCCESS) "写入成功" else "写入失败"}$command")
    }

    /**
     * 描述符写入回调
     */
    @SuppressLint("MissingPermission")
    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        if (BleConstant.DESCRIPTOR_UUID == descriptor.uuid.toString().lowercase(Locale.getDefault())) {
            uiCallback.state(if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        readPhy()
                    readDescriptor(descriptor)
                    readRemoteRssi() }
                "通知开启成功"
            } else "通知开启失败")
        }
    }

    /**
     * 读远程的设备信号强度回调
     */
    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) = uiCallback.state("onReadRemoteRssi：rssi：$rssi")

    /**
     * UI回调
    */
    interface UiCallback{
        //当前Ble状态信息
        fun state(state: String)
    }
}