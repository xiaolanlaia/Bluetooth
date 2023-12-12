package win.lioil.bluetooth.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter

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
}




















