package win.lioil.bluetooth.bt;

import static win.lioil.bluetooth.util.ConstantKt.CONNECTED;
import static win.lioil.bluetooth.util.ConstantKt.DISCONNECTED;
import static win.lioil.bluetooth.util.ConstantKt.MSG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;

import win.lioil.bluetooth.MyApplication;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bt.callback.BlueCallback;
import win.lioil.bluetooth.bt.terminal.BtServer;

@SuppressLint("MissingPermission")
public class BtServerActivity extends Activity implements BlueCallback {
    private TextView mTips;
    private EditText mInputMsg;
    private EditText mInputFile;
    private TextView mLogs;
    private BtServer mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btserver);
        mTips = findViewById(R.id.tv_tips);
        mInputMsg = findViewById(R.id.input_msg);
        mInputFile = findViewById(R.id.input_file);
        mLogs = findViewById(R.id.tv_log);
        mServer = new BtServer(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServer.unListener();
        mServer.close();
    }

    public void sendMsg(View view) {
        if (mServer.isConnected(null)) {
            String msg = mInputMsg.getText().toString();
            if (TextUtils.isEmpty(msg)) {
                MyApplication.toast("消息不能空", 0);
            } else {
                mServer.sendMsg(msg);
            }
        } else {
            MyApplication.toast("没有连接", 0);
        }
    }

    public void sendFile(View view) {
        if (mServer.isConnected(null)) {
            String filePath = mInputFile.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).isFile()) {
                MyApplication.toast("文件无效", 0);
            } else {
                mServer.sendFile(filePath);
            }
        } else {
            MyApplication.toast("没有连接", 0);
        }
    }

    @Override
    public void socketNotify(int state, final Object obj) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (isDestroyed()) {
                    return;
                }
                String msg = null;
                switch (state) {
                    case CONNECTED:
                        BluetoothDevice dev = (BluetoothDevice) obj;
                        msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                        mTips.setText(msg);
                        break;
                    case DISCONNECTED:
                        mServer.listen();
                        msg = "连接断开,正在重新监听...";
                        mTips.setText(msg);
                        break;
                    case MSG:
                        msg = String.format("\n%s", obj);
                        mLogs.append(msg);
                        break;
                }
                MyApplication.toast(msg, 0);
            }
        });
    }

    @Override
    public void scanStarted() {

    }

    @Override
    public void scanFinished() {

    }

    @Override
    public void scanning(@NonNull BluetoothDevice device) {

    }

    @Override
    public void bondRequest() {

    }

    @Override
    public void bondFail() {

    }

    @Override
    public void bonding() {

    }

    @Override
    public void bondSuccess() {

    }

    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

    }
}