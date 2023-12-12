package win.lioil.bluetooth.bt.terminal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import win.lioil.bluetooth.bt.callback.BlueCallback
import win.lioil.bluetooth.util.Constant.SPP_UUID
import win.lioil.bluetooth.util.ThreadPoolUtils.cachedThreadPool

/**
 * 服务端监听和连接线程，只连接一个设备
 */
@SuppressLint("MissingPermission")
class BtServer(blueCallback: BlueCallback?) : BtBase(blueCallback) {
    private lateinit var mSSocket: BluetoothServerSocket
    private val TAG = BtServer::class.java.simpleName

    init {
        listen()
    }

    /**
     * 监听客户端发起的连接
     */
    fun listen() {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            //            mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码
            //明文传输(不安全)，无需配对
            mSSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG, SPP_UUID)
            // 开启子线程
            cachedThreadPool.execute {
                try {
                    // 监听连接
                    val socket = mSSocket.accept()
                    // 关闭监听，只连接一个设备
                    mSSocket.close()
                    // 循环读取
                    loopRead(socket)
                } catch (e: Throwable) {
                    close()
                }
            }
        } catch (e: Throwable) {
            close()
        }
    }

    override fun close() {
        super.close()
        try {
            mSSocket.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}