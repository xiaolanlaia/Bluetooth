package win.lioil.bluetooth.ble.terminal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import win.lioil.bluetooth.R
import win.lioil.bluetooth.ble.utils.BleUtils
import win.lioil.bluetooth.util.ToastUtils
import java.util.Objects

@SuppressLint("MissingPermission")
class BleDevAdapter internal constructor() : RecyclerView.Adapter<BleDevAdapter.VH>() {
    private val mHandler = Handler()
    private val mDevices: MutableList<BleDev> = ArrayList()
    var isScanning = false
    private val mScanCallback: ScanCallback = object : ScanCallback() {
        // 扫描Callback
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val dev = BleDev(result.device, result)
            if (!mDevices.contains(dev)) {
                Log.d("__device-name", "" + result.device.name)
                mDevices.add(dev)
                notifyDataSetChanged()
            }
        }
    }

    init {
        scanBle()
    }

    // 重新扫描
    fun reScan() {
        mDevices.clear()
        notifyDataSetChanged()
        scanBle()
    }

    // 扫描BLE蓝牙(不会扫描经典蓝牙)
    private fun scanBle() {
        try {
            isScanning = true
            //        BluetoothAdapter bluetoothAdapter = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE).getDefaultAdapter();
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            // Android5.0新增的扫描API，扫描返回的结果更友好，比如BLE广播数据以前是byte[] scanRecord，而新API帮我们解析成ScanRecord类
            bluetoothLeScanner.startScan(mScanCallback)
            mHandler.postDelayed({
                bluetoothLeScanner.stopScan(mScanCallback) //停止扫描
                isScanning = false
            }, 3000)
        } catch (e: Exception) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dev, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val dev = mDevices[position]
        val name = dev.dev.name
        val address = dev.dev.address
        holder.name.text = String.format("%s, %s, Rssi=%s", name, address, dev.scanResult.rssi)
        holder.address.text = String.format("广播数据{%s}", dev.scanResult.scanRecord)
    }

    override fun getItemCount(): Int {
        return mDevices.size
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val name: TextView
        val address: TextView

        init {
            itemView.setOnClickListener(this)
            name = itemView.findViewById(R.id.name)
            address = itemView.findViewById(R.id.address)
        }

        override fun onClick(v: View) {
            val pos = adapterPosition
            Log.d(TAG, "onClick, getAdapterPosition=$pos")
            if (pos >= 0 && pos < mDevices.size) {

                BleUtils.instance.closeConnect()
                BleUtils.instance.connect(v.context,mDevices[pos].dev)
                ToastUtils.show(String.format("与[%s]开始连接............", mDevices[pos].dev))
            }
        }
    }

    interface Listener {
        fun onItemClick(dev: BluetoothDevice?)
    }

    class BleDev internal constructor(var dev: BluetoothDevice, var scanResult: ScanResult) {
        override fun equals(o: Any?): Boolean {
            if (this === o) {
                return true
            }
            if (o == null || javaClass != o.javaClass) {
                return false
            }
            val bleDev = o as BleDev
            return dev == bleDev.dev
        }

        override fun hashCode(): Int {
            return Objects.hash(dev)
        }
    }

    companion object {
        private val TAG = BleDevAdapter::class.java.simpleName
    }
}