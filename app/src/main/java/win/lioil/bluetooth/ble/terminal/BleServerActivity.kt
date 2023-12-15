package win.lioil.bluetooth.ble.terminal

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import win.lioil.bluetooth.MyApplication
import win.lioil.bluetooth.R
import win.lioil.bluetooth.ble.callback.BleCallback
import win.lioil.bluetooth.ble.callback.BleCallbackImpl
import win.lioil.bluetooth.ble.utils.BleDev
import win.lioil.bluetooth.ble.utils.BleUtils
import win.lioil.bluetooth.util.ToastUtils

/**
 * BLE服务端(从机/外围设备/peripheral)
 */
@SuppressLint("MissingPermission")
class BleServerActivity : Activity(), BleCallback {

    private var mTips: TextView? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bleserver)
        BleCallbackImpl.setGattCallback(this)
        mTips = findViewById(R.id.tv_tips)
        BleUtils.instance.bleServiceSetting()
    }

    override fun onDestroy() {
        super.onDestroy()
        BleUtils.instance.bleServiceClose()
    }

    override fun connected() {

    }

    override fun disConnected() {

    }

    override fun scanning(bleDev: BleDev) {

    }

    override fun notify(msg: String) {
        runOnUiThread {
            ToastUtils.show(msg)
            mTips!!.append("\n\n$msg".trimIndent())
        }
    }
}