package win.lioil.bluetooth.bt;

import static win.lioil.bluetooth.util.ConstantKt.CONNECTED;
import static win.lioil.bluetooth.util.ConstantKt.DISCONNECTED;
import static win.lioil.bluetooth.util.ConstantKt.MSG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import win.lioil.bluetooth.bt.callback.BlueCallback;

import java.io.File;

import win.lioil.bluetooth.MyApplication;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bt.receiver.BlueReceiver;
import win.lioil.bluetooth.bt.terminal.BtClient;

@SuppressLint("MissingPermission")
public class BtClientActivity extends Activity implements BlueCallback, BtDevAdapter.Listener {
    private TextView mTips;
    private EditText mInputMsg;
    private EditText mInputFile;
    private TextView mLogs;
    private BlueReceiver mBlueReceiver;
    private final BtDevAdapter mBtDevAdapter = new BtDevAdapter(this);

    public static BlueCallback blueCallback;
    private final BtClient mClient = new BtClient(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btclient);
        blueCallback = this;
        RecyclerView rv = findViewById(R.id.rv_bt);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mBtDevAdapter);
        mTips = findViewById(R.id.tv_tips);
        mInputMsg = findViewById(R.id.input_msg);
        mInputFile = findViewById(R.id.input_file);
        mLogs = findViewById(R.id.tv_log);
        //注册蓝牙广播
        mBlueReceiver = new BlueReceiver(this, this);
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBlueReceiver);
        mClient.unListener();
        mClient.close();
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {
        if (mClient.isConnected(dev)) {
            MyApplication.toast("已经连接了", 0);
            return;
        }
        mClient.connect(dev);
        MyApplication.toast("正在连接...", 0);
        mTips.setText("正在连接...");
    }

    // 重新扫描
    public void reScan(View view) {
        mBtDevAdapter.reScan();
    }

    public void sendMsg(View view) {
        if (mClient.isConnected(null)) {
            String msg = mInputMsg.getText().toString();
            if (TextUtils.isEmpty(msg)) {
                MyApplication.toast("消息不能空", 0);
            } else {
                mClient.sendMsg(msg);
            }
        } else {
            MyApplication.toast("没有连接", 0);
        }
    }

    public void sendFile(View view) {
        if (mClient.isConnected(null)) {
            String filePath = mInputFile.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).isFile()) {
                MyApplication.toast("文件无效", 0);
            } else {
                mClient.sendFile(filePath);
            }
        } else {
            MyApplication.toast("没有连接", 0);
        }
    }

    @Override
    public void socketNotify(int state, final Object obj) {
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
                msg = "连接断开";
                mTips.setText(msg);
                break;
            case MSG:
                msg = String.format("\n%s", obj);
                mLogs.append(msg);
                break;
        }
        MyApplication.toast(msg, 0);
    }

    @Override
    public void scanStarted() {

    }

    @Override
    public void scanFinished() {

    }

    @Override
    public void scanning(@NonNull BluetoothDevice device) {
        mBtDevAdapter.add(device);

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