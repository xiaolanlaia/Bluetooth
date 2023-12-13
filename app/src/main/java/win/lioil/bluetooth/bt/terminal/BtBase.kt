package win.lioil.bluetooth.bt.terminal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Environment
import win.lioil.bluetooth.MyApplication
import win.lioil.bluetooth.bt.callback.BlueCallback
import win.lioil.bluetooth.util.BlueUtils
import win.lioil.bluetooth.util.CONNECTED
import win.lioil.bluetooth.util.DISCONNECTED
import win.lioil.bluetooth.util.FLAG_FILE
import win.lioil.bluetooth.util.FLAG_MSG
import win.lioil.bluetooth.util.MSG
import win.lioil.bluetooth.util.Util
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
@SuppressLint("MissingPermission")
open class BtBase internal constructor(private var blueCallback: BlueCallback?) {
    private var mSocket: BluetoothSocket? = null
    private var isRead = false


    companion object {
        private val FILE_PATH = Environment.getExternalStorageDirectory().absolutePath + "/bluetooth/"
    }
    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    fun loopRead(isServer : Boolean) {
        mSocket = BlueUtils.instance.getBluetoothSocket(isServer)
        try {

            notifyUI(CONNECTED, mSocket!!.remoteDevice)
            val dataInputStream = DataInputStream(mSocket!!.inputStream)
            isRead = true
            //死循环读取
            while (isRead) {
                when (dataInputStream.readInt()) {
                    FLAG_MSG -> {
                        val msg = dataInputStream.readUTF()
                        notifyUI(MSG, "接收短消息：$msg")
                    }

                    FLAG_FILE -> {
                        Util.mkdirs(FILE_PATH)
                        //文件名
                        val fileName = dataInputStream.readUTF()
                        //文件长度
                        val fileLen = dataInputStream.readLong()
                        // 读取文件内容
                        var len: Long = 0
                        var r: Int
                        val b = ByteArray(4 * 1024)
                        val out = FileOutputStream(FILE_PATH + fileName)
                        notifyUI(MSG, "正在接收文件($fileName),请稍后...")
                        while ((dataInputStream.read(b).also { r = it }) != -1) {
                            out.write(b, 0, r)
                            len += r.toLong()
                            if (len >= fileLen) {
                                break
                            }
                        }
                        notifyUI(MSG, "文件接收完成(存放在:" + FILE_PATH + ")")
                    }
                }
            }
        } catch (e: Throwable) {
            close()
        }
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    fun release() {
        blueCallback = null
    }

    /**
     * 关闭Socket连接
     */
    open fun close() {
        try {
            isRead = false
            BlueUtils.instance.close()
            notifyUI(DISCONNECTED, null)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }



    // ============================================通知UI===========================================================


    private fun notifyUI(state: Int, obj: Any?) {
        blueCallback!!.socketNotify(state, obj)
    }
}