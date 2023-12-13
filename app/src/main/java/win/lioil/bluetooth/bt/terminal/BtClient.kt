package win.lioil.bluetooth.bt.terminal

import android.annotation.SuppressLint
import win.lioil.bluetooth.bt.callback.BlueCallback
import win.lioil.bluetooth.util.BlueUtils
import win.lioil.bluetooth.util.ThreadPoolUtils

/**
 * 客户端，与服务端建立长连接
 */
@SuppressLint("MissingPermission")
class BtClient internal constructor(blueCallback: BlueCallback) : BtBase(blueCallback) {
    /**
     * 与远端设备建立长连接
     *
     * @param device 远端设备
     */
    fun connect() {
        try {
            // 开启子线程
            ThreadPoolUtils.cachedThreadPool.execute {
                loopRead(false) //循环读取
            }
        } catch (e: Throwable) {
            close()
        }
    }
}