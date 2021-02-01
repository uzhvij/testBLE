package com.blogspot.uzhvij.testble;

import android.bluetooth.BluetoothDevice;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.uzhvij.testble.databinding.ActivityDeviceBinding;

import java.util.List;

public class DeviceActivity extends AppCompatActivity implements View.OnClickListener, InterfaceUpdater {
    ActivityDeviceBinding binding;

    private Connector connector;
    BluetoothDevice device;
    private static final String TAG = "myLogs";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.connect.setOnClickListener(this);
        binding.disconnect.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        device = bundle.getParcelable(BluetoothDevice.class.getSimpleName());
        connector = new Connector(this, device);
        Log.d(TAG, "onCreate: " + device.getAddress());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connector.disconnect();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.connect){
            Log.d(TAG, "onClick: connect");
            connector.connect();
        }else if(id == R.id.disconnect){
            Log.d(TAG, "onClick: disconnect");
            connector.disconnect();
        }
    }

    @Override
    public void updateInterface(String data) {
        binding.deviceInfo.setTextColor(Color.GREEN);
        binding.deviceInfo.append("\n" + data);
    }
}
