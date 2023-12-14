package win.lioil.bluetooth.ble.terminal

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import win.lioil.bluetooth.MyApplication
import win.lioil.bluetooth.R
import win.lioil.bluetooth.ble.callback.GattCallback
import win.lioil.bluetooth.ble.callback.GattCallbackImpl
import win.lioil.bluetooth.ble.utils.BleDev
import win.lioil.bluetooth.ble.utils.BleUtils

/**
 * BLE客户端(主机/中心设备/Central)
 */
@SuppressLint("MissingPermission")
class BleClientActivity : Activity() , GattCallback {
    private var mWriteET: EditText? = null
    private var mTips: TextView? = null
    private var mBleDevAdapter: BleDevAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bleclient)
        GattCallbackImpl.setGattCallback(this)
        val rv = findViewById<RecyclerView>(R.id.rv_ble)
        mWriteET = findViewById(R.id.et_write)
        mTips = findViewById(R.id.tv_tips)
        rv.layoutManager = LinearLayoutManager(this)
        mBleDevAdapter = BleDevAdapter()
        rv.adapter = mBleDevAdapter
    }

    // 扫描BLE
    fun reScan(view: View?) {
        if (BleUtils.instance.getScanState()) {
            MyApplication.toast("正在扫描...", 0)
        } else {
            mBleDevAdapter!!.reScan()
        }
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 读取数据成功会回调->onCharacteristicChanged()
    fun read(view: View?) {
        BleUtils.instance.read()
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 写入数据成功会回调->onCharacteristicWrite()
    fun write(view: View?) {
        BleUtils.instance.write(mWriteET!!.text.toString())
    }

    // 设置通知Characteristic变化会回调->onCharacteristicChanged()
    fun setNotify(view: View?) {
        BleUtils.instance.setNotify()
    }

    override fun connected() {
        BleUtils.instance.setConnectState()

    }

    override fun disConnected() {
        BleUtils.instance.closeConnect()
    }

    override fun scanning(bleDev: BleDev) {
        mBleDevAdapter?.updateList(bleDev)
    }


    override fun onDestroy() {
        super.onDestroy()
        BleUtils.instance.closeConnect()
    }
}