package win.lioil.bluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.widget.Toast
import win.lioil.bluetooth.util.ExceptionUtils
import win.lioil.bluetooth.util.ToastUtils

class MyApplication : Application() {
    @SuppressLint("ShowToast")
    override fun onCreate() {
        super.onCreate()
        instance = this
        sToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)

        ExceptionUtils.instance(object : ExceptionUtils.CrashHandler {
            override fun uncaughtException(t: Thread, e: Throwable) {
                ToastUtils.show("报错了")
                Log.d("__unCatchException-1", Log.getStackTraceString(e))
            }
        })
    }

    companion object {
        // 单例Toast,避免重复创建，显示时间过长
        private var sToast: Toast? = null
        lateinit var instance: MyApplication
        fun toast(txt: String?, duration: Int) {
            sToast!!.setText(txt)
            sToast!!.duration = duration
            sToast!!.show()
        }
    }
}