package win.lioil.bluetooth.ble.utils

/**
 * @Description
 * @Author WuJianFeng
 * @Date 2023/12/14 16:02
 *
 */

class BleUtils {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BleUtils() }
    }
}