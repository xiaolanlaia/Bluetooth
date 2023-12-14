package win.lioil.bluetooth.ble.callback

import win.lioil.bluetooth.util.ExceptionUtils
import java.lang.NullPointerException

/**
 * @Description
 * @Author WuJianFeng
 * @Date 2023/12/14 17:42
 *
 */

object GattCallbackImpl{
    var mGattCallback : GattCallback? = null
    fun setGattCallback(gattCallback : GattCallback) : GattCallback{
        mGattCallback = gattCallback
        return gattCallback
    }

    fun getGattCallback() : GattCallback{
        if (mGattCallback == null){
            ExceptionUtils.instance.getCashHandler().uncaughtException(Thread.currentThread(),Throwable(NullPointerException()))
        }
        return mGattCallback!!
    }
}
interface GattCallback {

    fun connected()
    fun disConnected()
}