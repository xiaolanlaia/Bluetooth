package win.lioil.bluetooth.ble.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import win.lioil.bluetooth.MyApplication
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

    private var mBluetoothGatt: BluetoothGatt? = null

    // 连接蓝牙设备
    fun connect(context : Context, device : BluetoothDevice){

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

}