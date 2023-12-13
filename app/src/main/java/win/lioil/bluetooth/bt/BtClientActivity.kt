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
import win.lioil.bluetooth.util.Constant
import win.lioil.bluetooth.util.DISCONNECTED
import win.lioil.bluetooth.util.FLAG_FILE
import win.lioil.bluetooth.util.FLAG_MSG
import win.lioil.bluetooth.util.MSG
import win.lioil.bluetooth.util.ThreadPoolUtils
import win.lioil.bluetooth.util.Util
import java.io.DataInputStream
import java.io.FileOutputStream

@SuppressLint("MissingPermission")
class BtClientActivity : Activity(), BlueCallback, BtDevAdapter.Listener {

    companion object {
        var blueCallback: BlueCallback? = null
    }
    private var isRead = false

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
        close()
    }

    override fun onItemClick(dev: BluetoothDevice) {
        if (BlueUtils.instance.isConnected(dev)) {
            MyApplication.toast("已经连接了", 0)
            return
        }
        BlueUtils.instance.bluetoothSocket(dev)
        connect()
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

    fun close(){

        isRead = false
        BlueUtils.instance.close()
        socketNotify(DISCONNECTED, null)
    }

    fun connect() {
        try {
            // 开启子线程
            ThreadPoolUtils.cachedThreadPool.execute {
                loopRead() //循环读取
            }
        } catch (e: Throwable) {
            close()
        }
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    fun loopRead() {
        val mSocket = BlueUtils.instance.getBluetoothSocket()
        try {

            socketNotify(CONNECTED, mSocket!!.remoteDevice)
            val dataInputStream = DataInputStream(mSocket!!.inputStream)
            isRead = true
            //死循环读取
            while (isRead) {
                when (dataInputStream.readInt()) {
                    FLAG_MSG -> {
                        val msg = dataInputStream.readUTF()
                        socketNotify(MSG, "接收短消息：$msg")
                    }

                    FLAG_FILE -> {
                        Util.mkdirs(Constant.FILE_PATH)
                        //文件名
                        val fileName = dataInputStream.readUTF()
                        //文件长度
                        val fileLen = dataInputStream.readLong()
                        // 读取文件内容
                        var len: Long = 0
                        var r: Int
                        val b = ByteArray(4 * 1024)
                        val out = FileOutputStream(Constant.FILE_PATH + fileName)
                        socketNotify(MSG, "正在接收文件($fileName),请稍后...")
                        while ((dataInputStream.read(b).also { r = it }) != -1) {
                            out.write(b, 0, r)
                            len += r.toLong()
                            if (len >= fileLen) {
                                break
                            }
                        }
                        socketNotify(MSG, "文件接收完成(存放在:" + Constant.FILE_PATH + ")")
                    }
                }
            }
        } catch (e: Throwable) {
            close()
        }
    }

}