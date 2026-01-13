package com.personal.machinetest.UI;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.personal.machinetest.Util.HttpClient;
import com.personal.machinetest.databinding.ActivityDetailsBinding;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailsActivity extends AppCompatActivity {
    ActivityDetailsBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    String localIpAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        localIpAddress = getIntent().getStringExtra("ipAddress");
        binding.tvLocalIp.setText("Local IP : " + (localIpAddress != null ? localIpAddress : "Not Found"));

        fetchPublicIpAndGeoData();
    }

    private void fetchPublicIpAndGeoData() {
        executorService.execute(() -> {
            try {
                String ipResponse = HttpClient.get("https://api.ipify.org?format=json");
                JSONObject ipObject = new JSONObject(ipResponse);
                String publicIp = ipObject.getString("ip");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvPublicIp.setText("Public IP : " + publicIp);
                    }
                });

                String geoUrl = "https://ipinfo.io/" + publicIp + "/geo";
                String geoResponse = HttpClient.get(geoUrl);
                JSONObject geoObject = new JSONObject(geoResponse);

                String city = geoObject.optString("city", "N/A");
                String region = geoObject.optString("region", "N/A");
                String country = geoObject.optString("country", "N/A");
                String org = geoObject.optString("org", "N/A");

                String geoData = "City: " + city + "\nRegion: " + region + "\nCountry: " + country + "\nOrganization: " + org;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvGeoData.setText(geoData);
                    }
                });


            } catch (Exception e) {
                Log.e("DetailsActivity", "Error fetching public IP and Geo data");
            }


        });
    }
}