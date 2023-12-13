package win.lioil.bluetooth.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import win.lioil.bluetooth.MyApplication
import win.lioil.bluetooth.bt.BtClientActivity
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream

/**
 * @Description
 * @Author WuJianFeng
 * @Date 2023/12/12 17:53
 *
 */

@SuppressLint("MissingPermission")
class BlueUtils {
    companion object{
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BlueUtils() }
    }

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var connectedDevice : BluetoothDevice? = null
    private var bluetoothSocket : BluetoothSocket? = null
    private var bluetoothServerSocket : BluetoothServerSocket? = null

    //是否正在发送消息
    private var isSending = false

    private var dataOutputStream: DataOutputStream? = null


    //设备是否支持蓝牙
    fun isSupport() : Boolean{
        return bluetoothAdapter != null
    }

    //是否开启
    fun isEnabled() : Boolean{
       return bluetoothAdapter.isEnabled
    }

    //开启蓝牙
    fun enable(){
        try {
            bluetoothAdapter.enable()
        } catch (e: Exception) {
            ExceptionUtils.instance.getCashHandler().uncaughtException(Thread.currentThread(),e.fillInStackTrace())
        }
    }

    //扫描蓝牙
    fun scan() : Boolean{
        if (!isEnabled()) return false

        if (bluetoothAdapter.isDiscovering){
            bluetoothAdapter.cancelDiscovery()
        }
        //此方法是个异步操作，一般搜索12秒
        return bluetoothAdapter.startDiscovery()
    }

    //当前设备与指定设备是否连接
    fun isConnected(dev: BluetoothDevice?): Boolean {
        val connected = (bluetoothSocket != null && bluetoothSocket!!.isConnected)
        return if (dev == null) {
            connected
        } else {
            connected && (bluetoothSocket!!.remoteDevice == dev)
        }
    }

    //创建BluetoothSocket，建立连接
    fun bluetoothSocket(device: BluetoothDevice) {
        //device.createRfcommSocketToServiceRecord(SPP_UUID) //加密传输，Android系统强制配对，弹窗显示配对码

        //明文传输(不安全)，无需配对
        bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(Constant.SPP_UUID)
        if (!bluetoothSocket!!.isConnected) {
            bluetoothSocket!!.connect()
        }
        connectedDevice = device
    }

    //创建 bluetoothServerSocket，建立连接
    fun bluetoothServerSocket() : BluetoothServerSocket{
        //            mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码
        //明文传输(不安全)，无需配对
        bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(this.javaClass.simpleName, Constant.SPP_UUID)

        return bluetoothServerSocket!!
    }

    fun getBluetoothSocket(isServer : Boolean) : BluetoothSocket?{
        return when(isServer){

            true -> {
                bluetoothServerSocket?.accept()
            }

            else ->{
                dataOutputStream = DataOutputStream(bluetoothSocket!!.outputStream)
                bluetoothSocket
            }
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
            BtClientActivity.blueCallback?.socketNotify(MSG, "发送短消息：$msg")
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
                BtClientActivity.blueCallback?.socketNotify(MSG, "正在发送文件($filePath),请稍后...")
                while ((fileInputStream.read(b).also { r = it }) != -1) {
                    dataOutputStream!!.write(b, 0, r)
                }
                dataOutputStream!!.flush()
                BtClientActivity.blueCallback?.socketNotify(MSG, "文件发送完成.")
            } catch (e: Throwable) {
                close()
            }
            isSending = false
        })
    }

    private fun checkSend(): Boolean {
        if (isSending) {
            MyApplication.toast("正在发送其它数据,请稍后再发...", 0)
            return true
        }
        return false
    }



    /**
     * 关闭Socket连接
     */
    fun close() {
        try {
            bluetoothSocket?.close()
            bluetoothServerSocket?.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}




















