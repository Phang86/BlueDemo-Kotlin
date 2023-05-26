package com.hnhy.bluedemo_kotlin

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hnhy.bluedemo_kotlin.callback.BleCallback
import com.hnhy.bluedemo_kotlin.databinding.ActivityDataExchangeBinding
import com.hnhy.bluedemo_kotlin.utils.BleHelper

class DataExchangeActivity : AppCompatActivity(),BleCallback.UiCallback{
    //绑定视图
    private lateinit var binding: ActivityDataExchangeBinding
    //Gatt
    private lateinit var gatt: BluetoothGatt
    //Ble回调
    private val bleCallback = BleCallback()
    //状态缓存
    private val stringBuffer = StringBuffer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataExchangeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    @SuppressLint("MissingPermission")
    private fun initView() {
        supportActionBar?.apply {
            title = "Data Exchange"
            setDisplayHomeAsUpEnabled(true)
        }
        val device = intent.getParcelableExtra<BluetoothDevice>("device")
        //gatt连接
        gatt = device!!.connectGatt(this,false,bleCallback)
        //发送指令
        binding.btnSendCommand.setOnClickListener {
            //底部弹框弹出来时，重新匹配窗口高度，保证软键盘保持在底部弹框下面

            val command = binding.etCommand.text.toString().trim()
            if (command.isEmpty()) {
                showMsg("请输入指令")
                return@setOnClickListener
            }
            //发送指令
            BleHelper.sendCommand(gatt, command, "010200" == command)
        }
        //Ble状态页面UI回调
        bleCallback.setUiCallback(this)
    }

    override fun state(state: String) = runOnUiThread {
        stringBuffer.append(state).append("\n")
        binding.tvDevice.text = stringBuffer.toString()
    }

    //页面返回
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home)  { onBackPressed();true } else false

    private fun showMsg(msg: CharSequence) = showMyToast(msg = msg, duration = Toast.LENGTH_SHORT)

    private fun showLongMsg(msg: CharSequence) = showMyToast(msg = msg, duration = Toast.LENGTH_LONG)

    private fun showMyToast(msg: CharSequence, duration: Int, gravity: Int = Gravity.CENTER) {
        val toast = Toast.makeText(this, msg, duration)
        toast.setGravity(gravity,0,0)
        toast.show()
    }


}

