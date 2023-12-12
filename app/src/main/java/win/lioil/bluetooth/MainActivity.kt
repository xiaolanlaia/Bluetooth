package win.lioil.bluetooth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import win.lioil.bluetooth.ble.BleClientActivity
import win.lioil.bluetooth.ble.BleServerActivity
import win.lioil.bluetooth.bt.BtClientActivity
import win.lioil.bluetooth.bt.BtServerActivity
import win.lioil.bluetooth.util.BlueUtils
import win.lioil.bluetooth.util.PermissionUtil

class MainActivity : Activity() {
    private val REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val permissionList = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissionList.add(Manifest.permission.BLUETOOTH)
            permissionList.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissionList.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        PermissionUtil.requestPermissions(this,permissionList,REQUEST_CODE)


        if (BlueUtils.instance.isSupport()) {
            MyApplication.toast("本机没有找到蓝牙硬件或驱动！", 0)
            finish()
            return
        } else {
            if (!BlueUtils.instance.isEnabled()) {
                //直接开启蓝牙
                BlueUtils.instance.enable()
                //跳转到设置界面
                //startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 112);
            }
        }

        // 检查是否支持BLE蓝牙
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            MyApplication.toast("本机不支持低功耗蓝牙！", 0)
            finish()
            return
        }
    }

    fun btClient(view: View?) {
        startActivity(Intent(this, BtClientActivity::class.java))
    }

    fun btServer(view: View?) {
        startActivity(Intent(this, BtServerActivity::class.java))
    }

    fun bleClient(view: View?) {
        startActivity(Intent(this, BleClientActivity::class.java))
    }

    fun bleServer(view: View?) {
        startActivity(Intent(this, BleServerActivity::class.java))
    }
}