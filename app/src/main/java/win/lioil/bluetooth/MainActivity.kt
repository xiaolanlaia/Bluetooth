package win.lioil.bluetooth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import win.lioil.bluetooth.ble.BleClientActivity
import win.lioil.bluetooth.ble.BleServerActivity
import win.lioil.bluetooth.bt.terminal.BtClientActivity
import win.lioil.bluetooth.bt.terminal.BtServerActivity
import win.lioil.bluetooth.bt.utils.BlueUtils

class MainActivity : Activity() {
    private val REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BlueUtils.instance.requestPermission(this,REQUEST_CODE)


        if (!BlueUtils.instance.isSupport()) {
            MyApplication.toast("本机没有找到蓝牙硬件或驱动！", 0)
            finish()
            return
        } else {

            //直接开启蓝牙
            BlueUtils.instance.enable()
            //跳转到设置界面
            //startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 112);
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