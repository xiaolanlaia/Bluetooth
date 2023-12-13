package win.lioil.bluetooth.util

import android.os.Environment
import java.util.UUID

/**
 * @Description
 * @Author WuJianFeng
 * @Date 2023/12/12 13:42
 *
 */



const val DISCONNECTED = 0
const val CONNECTED = 1
const val MSG = 2

//消息标记
const val FLAG_MSG = 0
//文件标记
const val FLAG_FILE = 1



object Constant {

    val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    val FILE_PATH = Environment.getExternalStorageDirectory().absolutePath + "/bluetooth/"

}