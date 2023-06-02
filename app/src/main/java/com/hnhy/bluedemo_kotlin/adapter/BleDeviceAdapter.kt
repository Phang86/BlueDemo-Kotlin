package com.hnhy.bluedemo_kotlin.adapter

import android.util.Log
import com.hnhy.bluedemo_kotlin.BleDevice
import com.hnhy.bluedemo_kotlin.databinding.ItemBluetoothBinding

class BleDeviceAdapter(data: MutableList<BleDevice> ?= null) :
    ViewBindingAdapter<ItemBluetoothBinding, BleDevice>(data){

    override fun convert(holder: ViewBindingHolder<ItemBluetoothBinding>, item: BleDevice) {
        val binding = holder.vb
        if ("Unkown".equals(item.name)) binding.tvDeviceName.text = "未知" else binding.tvDeviceName.text = item.name
        binding.tvMacAddress.text = item.device.address
        binding.tvRssi.text = "${item.rssi} dBm"
    }
}