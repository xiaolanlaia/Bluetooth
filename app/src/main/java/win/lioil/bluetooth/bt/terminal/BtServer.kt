package win.lioil.bluetooth.bt.terminal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import win.lioil.bluetooth.bt.callback.BlueCallback
import win.lioil.bluetooth.util.BlueUtils
import win.lioil.bluetooth.util.Constant.SPP_UUID
import win.lioil.bluetooth.util.ThreadPoolUtils.cachedThreadPool

/**
 * 服务端监听和连接线程，只连接一个设备
 */
@SuppressLint("MissingPermission")
class BtServer(blueCallback: BlueCallback?) : BtBase(blueCallback) {

    init {
        listen()
    }

    /**
     * 监听客户端发起的连接
     */
    fun listen() {
        try {
            BlueUtils.instance.bluetoothServerSocket()
            // 开启子线程
            cachedThreadPool.execute {
                try {
                    // 循环读取
                    loopRead(true)
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
            BlueUtils.instance.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}