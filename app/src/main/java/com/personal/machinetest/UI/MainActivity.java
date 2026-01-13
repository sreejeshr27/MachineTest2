package com.personal.machinetest.UI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.personal.machinetest.UI.Adapter.DeviceAdapter;
import com.personal.machinetest.data.database.AppDatabase;
import com.personal.machinetest.data.database.Device;
import com.personal.machinetest.data.database.DeviceDao;
import com.personal.machinetest.databinding.ActivityMainBinding;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private static final String SERVICE_TYPE = "_airplay._tcp.";
    ActivityMainBinding binding;
    DeviceAdapter deviceAdapter;
    WifiManager.MulticastLock multicastLock;
    private AppDatabase appDatabase;
    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appDatabase = AppDatabase.getInstance(this);
        wifiCheckUp();
        deviceAdapter = new DeviceAdapter(device -> {
            Intent intent=new Intent(this,DetailsActivity.class);
            intent.putExtra("ipAddress",device.ipAddress);
            startActivity(intent);
        });
        binding.rvDeviceList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDeviceList.setAdapter(deviceAdapter);

        appDatabase.deviceDao().getAllDevices().observe(this, devices -> {
            deviceAdapter.setDeviceList(devices);

        });
        executorService.execute(() -> appDatabase.deviceDao().setAllOffline());
        if (checkPermissions()) {
            startDiscovery();
        }
        Device fakeDevice = new Device("192.168.1.99", "Test Device", true);
        executorService.execute(() -> appDatabase.deviceDao().insertDevice(fakeDevice));
    }

    public void wifiCheckUp() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            multicastLock = wifiManager.createMulticastLock("multicastLock");
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
        }

    }

    private void startDiscovery() {
        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.e("mDns", "onDiscoveryStarted");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.e("mDns", "onDiscoveryStopped " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.e("mDns", "onServiceFound " + serviceInfo);
                nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.e("mDns", "onResolveFailed " + errorCode);
                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        Log.e("mDns", "onServiceResolved " + serviceInfo);
                        saveDevice(serviceInfo);
                    }
                });
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.e("mDns", "onServiceLost " + serviceInfo);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("mDns", "onStartDiscoveryFailed " + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("mDns", "onStopDiscoveryFailed " + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDevice(NsdServiceInfo serviceInfo) {
        InetAddress inetAddresses = serviceInfo.getHost();
        String ipAddress = inetAddresses.getHostAddress();
        String hostName = serviceInfo.getServiceName();
        assert ipAddress != null;
        Device device = new Device(ipAddress, hostName, true);
        executorService.execute(() -> appDatabase.deviceDao().insertDevice(device));


    }

    @Override
    protected void onDestroy() {
        if (nsdManager != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (multicastLock != null) {
            multicastLock.release();
        }
        super.onDestroy();
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.NEARBY_WIFI_DEVICES}, 100);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDiscovery();
        } else {
            Toast.makeText(this, "Permission required to find devices", Toast.LENGTH_SHORT).show();
        }
    }
}