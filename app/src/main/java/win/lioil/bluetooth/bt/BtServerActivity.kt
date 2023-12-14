package win.lioil.bluetooth.bt

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import win.lioil.bluetooth.MyApplication
import win.lioil.bluetooth.R
import win.lioil.bluetooth.bt.callback.BlueCallback
import win.lioil.bluetooth.util.BlueUtils
import win.lioil.bluetooth.util.BLUE_CONNECTED
import win.lioil.bluetooth.util.BLUE_DISCONNECTED
import win.lioil.bluetooth.util.BLUE_MSG

@SuppressLint("MissingPermission")
class BtServerActivity : Activity(), BlueCallback {
    companion object {
        var blueCallback: BlueCallback? = null
    }

    private var mTips: TextView? = null
    private var mInputMsg: EditText? = null
    private var mInputFile: EditText? = null
    private var mLogs: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btserver)
        blueCallback = this
        mTips = findViewById(R.id.tv_tips)
        mInputMsg = findViewById(R.id.input_msg)
        mInputFile = findViewById(R.id.input_file)
        mLogs = findViewById(R.id.tv_log)
        BlueUtils.instance.bluetoothServerSocket()
    }

    override fun onDestroy() {
        super.onDestroy()
        BlueUtils.instance.close()
        socketNotify(BLUE_DISCONNECTED, null)
    }

    fun sendMsg(view: View?) {
        BlueUtils.instance.sendMsg(mInputMsg!!.text.toString(),true)
    }

    fun sendFile(view: View?) {
        BlueUtils.instance.sendFile(mInputFile!!.text.toString(),true)
    }

    override fun socketNotify(state: Int, obj: Any?) {
        runOnUiThread(Runnable {
            if (isDestroyed) {
                return@Runnable
            }
            var msg: String? = null
            when (state) {
                BLUE_CONNECTED -> {
                    runOnUiThread {
                        val dev = obj as BluetoothDevice?
                        msg = String.format("与%s(%s)连接成功", dev!!.name, dev.address)
                        mTips!!.text = msg
                    }
                }

                BLUE_DISCONNECTED -> {
                    runOnUiThread {
                        BlueUtils.instance.bluetoothServerSocket()
                        msg = "连接断开,正在重新监听..."
                        mTips!!.text = msg
                    }
                }
                BLUE_MSG -> {
                    runOnUiThread {
                        msg = String.format("\n%s", obj)
                        mLogs!!.append(msg)
                    }
                }
            }
            runOnUiThread {
                MyApplication.toast(msg, 0)
            }
        })
    }

    override fun scanStarted() {}
    override fun scanFinished() {}
    override fun scanning(device: BluetoothDevice) {}
    override fun bondRequest() {}
    override fun bondFail() {}
    override fun bonding() {}
    override fun bondSuccess() {}
    override fun connected() {}
    override fun disconnected() {}


}