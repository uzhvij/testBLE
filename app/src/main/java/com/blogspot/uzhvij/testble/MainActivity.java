package com.blogspot.uzhvij.testble;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.blogspot.uzhvij.testble.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, InterfaceUpdater {

    private static final int ACCESS_COARSE_LOCATION_REQUEST = 13;
    private ActivityMainBinding binding;
    private Scanner scanner;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> list;
    private static final String TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.button.setOnClickListener(this);

        list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(String.valueOf(i));
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        binding.listItem.setAdapter(adapter);

        hasPermissions();
        scanner = new Scanner(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        scanner.stopScan();
    }

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION }, ACCESS_COARSE_LOCATION_REQUEST);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        scanner.scan();
        Log.d(TAG, "onClick: ");
    }

    @Override
    public void updateInterface(ArrayList<String> list) {
        this.list.clear();
        this.list.addAll(list);
        adapter.notifyDataSetChanged();
    }
}