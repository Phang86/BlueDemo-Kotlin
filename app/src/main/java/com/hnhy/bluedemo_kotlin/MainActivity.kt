package com.hnhy.bluedemo_kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hnhy.bluedemo_kotlin.adapter.BleDeviceAdapter
import com.hnhy.bluedemo_kotlin.databinding.ActivityMainBinding
import com.hnhy.bluedemo_kotlin.utils.EasyDate
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.runBlocking
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult


class MainActivity : AppCompatActivity() {

    //视图绑定
    private lateinit var binding: ActivityMainBinding

    //默认蓝牙适配器
    private var defalutAdapter = BluetoothAdapter.getDefaultAdapter()

    //低功耗蓝牙适配器
    private lateinit var bleAdapter : BleDeviceAdapter

    //蓝牙列表
    private var mList: MutableList<BleDevice> = ArrayList()

    //地址列表
    private var addressList: MutableList<String> = ArrayList()

    //当前是否扫描
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //检查版本
        checkAndroidVersion()
        //初始化页面
        initView()
    }

    private fun initView(){
        bleAdapter = BleDeviceAdapter(mList).apply {
            setOnItemClickListener { adapter, view, position ->
                stopScan()
                val device = mList[position].device
                //跳转页面
                startActivity(Intent(this@MainActivity,DataExchangeActivity::class.java)
                    .putExtra("device",device))
                Log.e("TAG", "startActivity: "+device)
            }
            animationEnable = true
            setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInRight)
        }
        binding.rvDevice.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = bleAdapter
        }
        //扫描蓝牙
        binding.fabAdd.setOnClickListener { if (isScanning) stopScan() else scan() }
    }

    /**
     * 扫描蓝牙
     */
    private fun scan() {
        if (!defalutAdapter.isEnabled){
            showMsg("蓝牙未打开")
            return
        }
        if (isScanning){
            showMsg("正在扫描中...")
            return
        }
        isScanning = true
        addressList.clear()
        mList.clear()
        BluetoothLeScannerCompat.getScanner().startScan(scanCallback)
        binding.progressBar.visibility = View.VISIBLE
        binding.fabAdd.text = "扫描中"
    }

    /**
     * 停止扫描
     */
    private fun stopScan() {
        if (!defalutAdapter.isEnabled) {
            showMsg("蓝牙未打开")
            return
        }
        if (isScanning){
            isScanning = false
            BluetoothLeScannerCompat.getScanner().stopScan(scanCallback)
            binding.progressBar.visibility = View.INVISIBLE
            binding.fabAdd.text = "开始扫描"
        }
    }

    /**
     * Android版本
     */
    private fun checkAndroidVersion() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPermission() else openBluetooth()

    /**
     * 动态权限请求
     */
    private fun requestPermission() =
        PermissionX.init(this).permissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .request { allGranted, _, _ -> if (allGranted) openBluetooth() else showMsg() }

    /**
     * 打开蓝牙
     */
    private fun openBluetooth() = defalutAdapter.let {
        if (it.isEnabled) showMsg("蓝牙已经打开，可以开始扫描设备了") else activityResult.launch(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        )
    }

    //注册开启蓝牙  注意在onCrate之前注册
    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) showMsg(if (defalutAdapter.isEnabled) "蓝牙已经打开" else "蓝牙未打开")
        }

    private fun showMsg(msg: CharSequence = "权限未通过") =
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()

    //扫描结果回调
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val address = result.device.address
            val name = result.device.name ?: "Unkown"
            if (addressList.size == 0) {
                addressList.add(address)
                addDeviceList(BleDevice(result.device, result.rssi, name))
            }else{
                //检查之前所添加的设备地址是否存在当前地址列表
                if (!addressList.contains(address)){
                    addressList.add(address)
                    addDeviceList(BleDevice(result.device, result.rssi, name))
                }
            }
        }
    }

    private fun addDeviceList(bleDevice: BleDevice) {
        mList.add(bleDevice)
        //无设备UI展示
        binding.layNoEquipment.visibility = if (mList.size > 0) View.GONE else View.VISIBLE
        //刷新列表适配器
        bleAdapter.notifyDataSetChanged()
    }



}



