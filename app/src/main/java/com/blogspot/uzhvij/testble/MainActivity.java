package com.blogspot.uzhvij.testble;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.blogspot.uzhvij.testble.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, InterfaceUpdater {

    private static final int ACCESS_COARSE_LOCATION_REQUEST = 13;
    private ActivityMainBinding binding;
    private Scanner scanner;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> list;
    private ArrayList<BluetoothDevice> devices = null;
    private boolean scanning = false;
    private static final String TAG = "myLogs";
    private Connector connector;
    private AdapterView.OnItemClickListener clickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        binding.progressBar.setVisibility(View.GONE);
        setContentView(view);
        binding.button.setOnClickListener(this);

        list = new ArrayList<>();
        devices = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        binding.listItem.setAdapter(adapter);
        clickListener = (adapterView, view1, i, l) -> {
            scanner.stopScan();
            Intent intent = new Intent(getApplication(), DeviceActivity.class);
            intent.putExtra(BluetoothDevice.class.getSimpleName(), devices.get(i));
            startActivity(intent);
        };
        binding.listItem.setOnItemClickListener(clickListener);
        hasPermissions();
        scanner = new Scanner(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        scanner.stopScan();
    }

    private void hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION }, ACCESS_COARSE_LOCATION_REQUEST);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(scanning){
            scanner.stopScan();
            scanner.showScanResult();
            setProgressBarVisible(false);
            binding.button.setText(R.string.scanButtonText);
            scanning = false;
            binding.listItem.setOnItemClickListener(clickListener);
        }else {
            setProgressBarVisible(true);
            binding.listItem.setOnItemClickListener(null);
            if(Scanner.SCANNING_TIME == 0){
                scanning = true;
                binding.button.setText(R.string.stopScanButtonText);
            }else {
                binding.button.setEnabled(false);
            }
            scanner.scan();
        }
    }

    @Override
    public void updateInterface(List<String> list, List<BluetoothDevice> devices) {
        this.list.clear();
        this.list.addAll(list);
        this.devices.clear();
        this.devices.addAll(devices);
        adapter.notifyDataSetChanged();
        if(Scanner.SCANNING_TIME != 0){
            setProgressBarVisible(false);
            binding.button.setEnabled(true);
            binding.listItem.setOnItemClickListener(clickListener);
        }
    }

    void setProgressBarVisible(boolean visible){
        if (visible) {
            binding.progressBar.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.progressBar.getLayoutParams().height = 0;
        }
    }
}