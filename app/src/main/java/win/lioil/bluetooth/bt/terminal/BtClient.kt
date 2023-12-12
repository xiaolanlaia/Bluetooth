package win.lioil.bluetooth.bt.terminal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import win.lioil.bluetooth.bt.callback.BlueCallback
import win.lioil.bluetooth.util.Constant.SPP_UUID
import win.lioil.bluetooth.util.ThreadPoolUtils

/**
 * 客户端，与服务端建立长连接
 */
@SuppressLint("MissingPermission")
class BtClient internal constructor(blueCallback: BlueCallback) : BtBase(blueCallback) {
    /**
     * 与远端设备建立长连接
     *
     * @param dev 远端设备
     */
    fun connect(dev: BluetoothDevice) {
        close()
        try {
//             final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
            val socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID) //明文传输(不安全)，无需配对
            // 开启子线程
            ThreadPoolUtils.cachedThreadPool.execute {
                loopRead(socket) //循环读取
            }
        } catch (e: Throwable) {
            close()
        }
    }
}