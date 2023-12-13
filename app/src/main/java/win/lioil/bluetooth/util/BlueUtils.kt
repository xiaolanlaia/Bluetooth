package win.lioil.bluetooth.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.text.TextUtils
import win.lioil.bluetooth.MyApplication
import win.lioil.bluetooth.bt.BtClientActivity
import win.lioil.bluetooth.bt.BtServerActivity
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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
    private var bluetoothSocket : BluetoothSocket? = null
    private var bluetoothServerSocket : BluetoothServerSocket? = null

    //是否正在发送消息
    private var isSending = false
    private var isRead = false

    private var dataOutputStream: DataOutputStream? = null

    /**
     * 请求权限
     */
    fun requestPermission(activity: Activity, requestCode : Int){
        val permissionList = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionList.add(Manifest.permission.BLUETOOTH)
            permissionList.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissionList.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        PermissionUtil.requestPermissions(activity,permissionList,requestCode)
    }

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

        if (isEnabled()) return
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
        val connected = (getBluetoothSocket() != null && getBluetoothSocket()!!.isConnected)
        return if (dev == null) {
            connected
        } else {
            connected && (getBluetoothSocket()!!.remoteDevice == dev)
        }
    }

    //创建BluetoothSocket，建立连接
    fun bluetoothSocket(device: BluetoothDevice) {
        if (isConnected(device)) {
            MyApplication.toast("已经连接了", 0)
            return
        }
        //device.createRfcommSocketToServiceRecord(SPP_UUID) //加密传输，Android系统强制配对，弹窗显示配对码

        //明文传输(不安全)，无需配对
        bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(Constant.SPP_UUID)
        if (!bluetoothSocket!!.isConnected) {
            bluetoothSocket!!.connect()
        }
        dataOutputStream = DataOutputStream(bluetoothSocket?.outputStream)

        // 开启子线程
        ThreadPoolUtils.cachedThreadPool.execute {
            loopRead(false) //循环读取
        }
    }

    //创建 bluetoothServerSocket，建立连接
    fun bluetoothServerSocket() : BluetoothServerSocket{
        //            mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码
        //明文传输(不安全)，无需配对
        bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("TAG", Constant.SPP_UUID)

        Thread(Runnable {
            bluetoothSocket = bluetoothServerSocket?.accept()
            if (!bluetoothSocket!!.isConnected) {
                bluetoothSocket!!.connect()
            }
            dataOutputStream = DataOutputStream(bluetoothSocket?.outputStream)

            loopRead(true)
        }).start()
        return bluetoothServerSocket!!
    }

    fun getBluetoothSocket() : BluetoothSocket?{
        return bluetoothSocket
    }

    fun getConnectedDevice() : BluetoothDevice?{
        return bluetoothSocket?.remoteDevice
    }

    fun getBondDevice() : Set<BluetoothDevice>{
        return bluetoothAdapter.bondedDevices
    }



    /**
     * 发送短消息
     */
    fun sendMsg(msg: String,isServer: Boolean) {
        if (!isConnected(null)) {
            MyApplication.toast("没有连接", 0)
            return
        }


        if (TextUtils.isEmpty(msg)) {
            MyApplication.toast("消息不能空", 0)
            return
        }

        if (checkSend()) {
            return
        }
        isSending = true
        try {
            //消息标记
            dataOutputStream!!.writeInt(FLAG_MSG)
            dataOutputStream!!.writeUTF(msg)
            dataOutputStream!!.flush()
            if(isServer){
                BtServerActivity.blueCallback?.socketNotify(MSG, "发送短消息：$msg")
            }else{
                BtClientActivity.blueCallback?.socketNotify(MSG, "发送短消息：$msg")
            }
        } catch (e: Throwable) {
            close()
        }
        isSending = false
    }

    /**
     * 发送文件
     */
    fun sendFile(filePath: String, isServer : Boolean) {

        if (!isConnected(null)) {
            MyApplication.toast("没有连接", 0)
            return
        }

        if (TextUtils.isEmpty(filePath) || !File(filePath).isFile) {
            MyApplication.toast("文件无效", 0)
            return
        }

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
                if(isServer){
                    BtServerActivity.blueCallback?.socketNotify(MSG, "正在发送文件($filePath),请稍后...")
                }else{
                    BtClientActivity.blueCallback?.socketNotify(MSG, "正在发送文件($filePath),请稍后...")
                }
                while ((fileInputStream.read(b).also { r = it }) != -1) {
                    dataOutputStream!!.write(b, 0, r)
                }
                dataOutputStream!!.flush()

                if(isServer){
                    BtServerActivity.blueCallback?.socketNotify(MSG, "文件发送完成.")
                }else{
                    BtClientActivity.blueCallback?.socketNotify(MSG, "文件发送完成.")
                }
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
            dataOutputStream?.close()
            bluetoothSocket?.close()
            bluetoothServerSocket?.close()
            isRead = false
            isSending = false

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }



    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    fun loopRead(isServer: Boolean) {
        val mSocket = getBluetoothSocket()
        try {

            if(isServer){
                BtServerActivity.blueCallback?.socketNotify(CONNECTED, mSocket?.remoteDevice)
            }else{
                BtClientActivity.blueCallback?.socketNotify(CONNECTED, mSocket?.remoteDevice)
            }
            val dataInputStream = DataInputStream(mSocket?.inputStream)
            isRead = true
            //死循环读取
            while (isRead) {
                when (dataInputStream.readInt()) {
                    FLAG_MSG -> {
                        val msg = dataInputStream.readUTF()
                        if(isServer){
                            BtServerActivity.blueCallback?.socketNotify(MSG, "接收短消息：$msg")
                        }else{
                            BtClientActivity.blueCallback?.socketNotify(MSG, "接收短消息：$msg")
                        }
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

                        if(isServer){
                            BtServerActivity.blueCallback?.socketNotify(MSG, "正在接收文件($fileName),请稍后...")
                        }else{
                            BtClientActivity.blueCallback?.socketNotify(MSG, "正在接收文件($fileName),请稍后...")
                        }
                        while ((dataInputStream.read(b).also { r = it }) != -1) {
                            out.write(b, 0, r)
                            len += r.toLong()
                            if (len >= fileLen) {
                                break
                            }
                        }
                        if(isServer){
                            BtServerActivity.blueCallback?.socketNotify(MSG, "文件接收完成(存放在:" + Constant.FILE_PATH + ")")
                        }else{
                            BtClientActivity.blueCallback?.socketNotify(MSG, "文件接收完成(存放在:" + Constant.FILE_PATH + ")")
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            close()
        }
    }


}




















