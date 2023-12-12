package win.lioil.bluetooth.bt.terminal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Environment
import win.lioil.bluetooth.MyApplication
import win.lioil.bluetooth.bt.callback.BlueCallback
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
    private var dataOutputStream: DataOutputStream? = null
    private var isRead = false
    private var isSending = false


    companion object {
        private val FILE_PATH = Environment.getExternalStorageDirectory().absolutePath + "/bluetooth/"
    }
    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    fun loopRead(socket: BluetoothSocket?) {
        mSocket = socket
        try {
            if (!mSocket!!.isConnected) {
                mSocket!!.connect()
            }
            notifyUI(CONNECTED, mSocket!!.remoteDevice)
            dataOutputStream = DataOutputStream(mSocket!!.outputStream)
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
     * 发送短消息
     */
    fun sendMsg(msg: String) {
        if (checkSend()) {
            return
        }
        isSending = true
        try {
            //消息标记
            dataOutputStream!!.writeInt(FLAG_MSG)
            dataOutputStream!!.writeUTF(msg)
            dataOutputStream!!.flush()
            notifyUI(MSG, "发送短消息：$msg")
        } catch (e: Throwable) {
            close()
        }
        isSending = false
    }

    /**
     * 发送文件
     */
    fun sendFile(filePath: String) {
        if (checkSend()) {
            return
        }
        isSending = true
        Util.EXECUTOR.execute(Runnable {
            try {
                val fileInputStream = FileInputStream(filePath)
                val file = File(filePath)
                //文件标记
                dataOutputStream!!.writeInt(FLAG_FILE)
                //文件名
                dataOutputStream!!.writeUTF(file.name)
                //文件长度
                dataOutputStream!!.writeLong(file.length())
                var r: Int
                val b = ByteArray(4 * 1024)
                notifyUI(MSG, "正在发送文件($filePath),请稍后...")
                while ((fileInputStream.read(b).also { r = it }) != -1) {
                    dataOutputStream!!.write(b, 0, r)
                }
                dataOutputStream!!.flush()
                notifyUI(MSG, "文件发送完成.")
            } catch (e: Throwable) {
                close()
            }
            isSending = false
        })
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    fun unListener() {
        blueCallback = null
    }

    /**
     * 关闭Socket连接
     */
    open fun close() {
        try {
            isRead = false
            mSocket!!.close()
            notifyUI(DISCONNECTED, null)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 当前设备与指定设备是否连接
     */
    fun isConnected(dev: BluetoothDevice?): Boolean {
        val connected = (mSocket != null && mSocket!!.isConnected)
        return if (dev == null) {
            connected
        } else {
            connected && (mSocket!!.remoteDevice == dev)
        }
    }

    // ============================================通知UI===========================================================
    private fun checkSend(): Boolean {
        if (isSending) {
            MyApplication.toast("正在发送其它数据,请稍后再发...", 0)
            return true
        }
        return false
    }

    private fun notifyUI(state: Int, obj: Any?) {
        blueCallback!!.socketNotify(state, obj)
    }
}