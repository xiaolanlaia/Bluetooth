package win.lioil.bluetooth;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

public class MyApplication extends Application {
    private static Toast sToast; // 单例Toast,避免重复创建，显示时间过长
    public static MyApplication instance;

    @SuppressLint("ShowToast")
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        sToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    public static void toast(String txt, int duration) {
        sToast.setText(txt);
        sToast.setDuration(duration);
        sToast.show();
    }
}
