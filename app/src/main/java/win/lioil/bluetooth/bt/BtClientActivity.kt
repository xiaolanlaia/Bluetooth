package win.lioil.bluetooth.bt

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import win.lioil.bluetooth.MyApplication
import win.lioil.bluetooth.R
import win.lioil.bluetooth.bt.callback.BlueCallback
import win.lioil.bluetooth.bt.receiver.BlueReceiver
import win.lioil.bluetooth.util.BlueUtils
import win.lioil.bluetooth.util.CONNECTED
import win.lioil.bluetooth.util.DISCONNECTED
import win.lioil.bluetooth.util.MSG

@SuppressLint("MissingPermission")
class BtClientActivity : Activity(), BlueCallback, BtDevAdapter.Listener {

    companion object {
        var blueCallback: BlueCallback? = null
    }

    private var mTips: TextView? = null
    private var mInputMsg: EditText? = null
    private var mInputFile: EditText? = null
    private var mLogs: TextView? = null
    private var mBlueReceiver: BlueReceiver? = null
    private val mBtDevAdapter = BtDevAdapter(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btclient)
        blueCallback = this
        val rv = findViewById<RecyclerView>(R.id.rv_bt)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = mBtDevAdapter
        mTips = findViewById(R.id.tv_tips)
        mInputMsg = findViewById(R.id.input_msg)
        mInputFile = findViewById(R.id.input_file)
        mLogs = findViewById(R.id.tv_log)
        //注册蓝牙广播
        mBlueReceiver = BlueReceiver(this, this)
        BlueUtils.instance.scan()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBlueReceiver)
        BlueUtils.instance.close()
        socketNotify(DISCONNECTED, null)
    }

    override fun onItemClick(dev: BluetoothDevice) {

        BlueUtils.instance.bluetoothSocket(dev)

        MyApplication.toast("正在连接...", 0)
        mTips!!.text = "正在连接..."
    }

    // 重新扫描
    fun reScan(view: View?) {
        mBtDevAdapter.reScan()
    }

    fun sendMsg(view: View?) {
        BlueUtils.instance.sendMsg(mInputMsg!!.text.toString(),false)
    }

    fun sendFile(view: View?) {
        BlueUtils.instance.sendFile(mInputFile!!.text.toString(),false)
    }

    override fun socketNotify(state: Int, obj: Any?) {
        if (isDestroyed) {
            return
        }
        var msg: String? = null
        when (state) {
            CONNECTED -> {
                runOnUiThread {
                    val dev = obj as BluetoothDevice?
                    msg = String.format("与%s(%s)连接成功", dev?.name, dev?.address)
                    mTips!!.text = msg
                }
            }

            DISCONNECTED -> {
                runOnUiThread {
                    msg = "连接断开"
                    mTips!!.text = msg
                }
            }

            MSG -> {
                runOnUiThread {

                    msg = String.format("\n%s", obj)
                    mLogs!!.append(msg)
                }
            }
        }
        runOnUiThread {
            MyApplication.toast(msg, 0)

        }
    }

    override fun scanStarted() {}
    override fun scanFinished() {}
    override fun scanning(device: BluetoothDevice) {
        mBtDevAdapter.add(device)
    }

    override fun bondRequest() {}
    override fun bondFail() {}
    override fun bonding() {}
    override fun bondSuccess() {}
    override fun connected() {}
    override fun disconnected() {}

}