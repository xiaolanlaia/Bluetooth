package win.lioil.bluetooth.ble.callback

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.util.Log
import win.lioil.bluetooth.util.ToastUtils
import java.util.Arrays

/**
 * @Description
 * @Author WuJianFeng
 * @Date 2023/12/14 17:40
 *
 */

@SuppressLint("MissingPermission")
class GattCallbackUtils {

    companion object{
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { GattCallbackUtils () }
    }


    // 与服务端连接的Callback
    var mBluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val dev = gatt.device
            Log.i("__BleClient-1", String.format("onConnectionStateChange:%s,%s,%s,%s", dev.name, dev.address, status, newState))
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                GattCallbackImpl.getGattCallback().connected()
                gatt.discoverServices() //启动服务发现
            } else {
                GattCallbackImpl.getGattCallback().disConnected()
            }
            ToastUtils.show(String.format(if (status == 0) (if (newState == 2) "与[%s]连接成功" else "与[%s]连接断开") else "与[%s]连接出错,错误码:$status", dev))
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.i("__BleClient-2", String.format("onServicesDiscovered:%s,%s,%s", gatt.device.name, gatt.device.address, status))
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                // 遍历获取BLE服务Services/Characteristics/Descriptors的全部UUID
                for (service in gatt.services) {
                    val allUUIDs = StringBuilder(" UUIDs={S=${service.uuid}".trimIndent())
                    for (characteristic in service.characteristics) {
                        allUUIDs.append(",\nC=").append(characteristic.uuid)
                        for (descriptor in characteristic.descriptors) {
                            allUUIDs.append(",\nD=").append(descriptor.uuid)
                        }
                    }
                    allUUIDs.append("}")
                    Log.i("__BleClient-3", "onServicesDiscovered:$allUUIDs")
                    ToastUtils.show("发现服务$allUUIDs")
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val uuid = characteristic.uuid
            val valueStr = String(characteristic.value)
            Log.i("__BleClient-4", String.format("onCharacteristicRead:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            ToastUtils.show("读取Characteristic[$uuid]:\n$valueStr")
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val uuid = characteristic.uuid
            val valueStr = String(characteristic.value)
            Log.i("__BleClient-5", String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            ToastUtils.show("写入Characteristic[$uuid]:\n$valueStr")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val uuid = characteristic.uuid
            val valueStr = String(characteristic.value)
            Log.i("__BleClient-6", String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr))
            ToastUtils.show("通知Characteristic[$uuid]:\n$valueStr")
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            val uuid = descriptor.uuid
            val valueStr = Arrays.toString(descriptor.value)
            Log.i("__BleClient-7", String.format("onDescriptorRead:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            ToastUtils.show("读取Descriptor[$uuid]:\n$valueStr")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            val uuid = descriptor.uuid
            val valueStr = Arrays.toString(descriptor.value)
            Log.i("__BleClient-8", String.format("onDescriptorWrite:%s,%s,%s,%s,%s", gatt.device.name, gatt.device.address, uuid, valueStr, status))
            ToastUtils.show("写入Descriptor[$uuid]:\n$valueStr")
        }
    }

}