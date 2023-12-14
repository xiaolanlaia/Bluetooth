package win.lioil.bluetooth.ble.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import win.lioil.bluetooth.MyApplication
import win.lioil.bluetooth.ble.callback.GattCallbackImpl
import win.lioil.bluetooth.ble.callback.GattCallbackUtils
import win.lioil.bluetooth.ble.terminal.BleServerActivity
import java.util.UUID

/**
 * @Description
 * @Author WuJianFeng
 * @Date 2023/12/14 16:02
 *
 */

@SuppressLint("MissingPermission")
class BleUtils private constructor(){

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BleUtils() }
    }

    private var isConnected = false
    private var isScanning = false

    private var mBluetoothGatt: BluetoothGatt? = null

    fun getScanState() : Boolean{
        return isScanning
    }

    // 连接蓝牙设备
    fun connect(context : Context, device : BluetoothDevice){
        closeConnect()

        mBluetoothGatt = device.connectGatt(context, false, GattCallbackUtils.instance.mBluetoothGattCallback)

    }

    // BLE中心设备连接外围设备的数量有限(大概2~7个)，在建立新连接之前必须释放旧连接资源，否则容易出现连接错误133
    fun closeConnect() {
        isConnected = false
        if (mBluetoothGatt != null) {
            mBluetoothGatt!!.disconnect()
            mBluetoothGatt!!.close()
        }
    }

    fun read(){
        val service = getGattService(BleServerActivity.UUID_SERVICE)
        if (service != null) {
            //通过UUID获取可读的Characteristic
            val characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ_NOTIFY)
            mBluetoothGatt!!.readCharacteristic(characteristic)
        }
    }

    fun write(content : String){
        val service = getGattService(BleServerActivity.UUID_SERVICE)
        if (service != null) {
            //通过UUID获取可写的Characteristic
            val characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_WRITE)
            //单次最多20个字节
            characteristic.value = content.toByteArray()
            mBluetoothGatt!!.writeCharacteristic(characteristic)
        }
    }

    fun setNotify(){

        val service = getGattService(BleServerActivity.UUID_SERVICE)
        if (service != null) {
            // 设置Characteristic通知   //通过UUID获取可通知的Characteristic
            val characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ_NOTIFY)
            mBluetoothGatt!!.setCharacteristicNotification(characteristic, true)

            // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
            val descriptor = characteristic.getDescriptor(BleServerActivity.UUID_DESC_NOTITY)
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            // 和通知类似,但服务端不主动发数据,只指示客户端读取数据
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }



    // 获取Gatt服务
    private fun getGattService(uuid: UUID): BluetoothGattService? {
        if (!isConnected) {
            MyApplication.toast("没有连接", 0)
            return null
        }
        val service = mBluetoothGatt!!.getService(uuid)
        if (service == null) {
            MyApplication.toast("没有找到服务UUID=$uuid", 0)
        }
        return service
    }

    fun setConnectState(){
        isConnected = true
    }


    private val mScanCallback: ScanCallback = object : ScanCallback() {
        // 扫描Callback
        override fun onScanResult(callbackType: Int, result: ScanResult) {

            GattCallbackImpl.getGattCallback().scanning(BleDev(result.device, result))
        }
    }



    // 扫描BLE蓝牙(不会扫描经典蓝牙)
    fun scanBle() {
        try {
            isScanning = true
            //        BluetoothAdapter bluetoothAdapter = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE).getDefaultAdapter();
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            // Android5.0新增的扫描API，扫描返回的结果更友好，比如BLE广播数据以前是byte[] scanRecord，而新API帮我们解析成ScanRecord类
            bluetoothLeScanner.startScan(mScanCallback)
            Handler().postDelayed({
                bluetoothLeScanner.stopScan(mScanCallback) //停止扫描
                isScanning = false
            }, 3000)
        } catch (e: Exception) {
        }
    }

}