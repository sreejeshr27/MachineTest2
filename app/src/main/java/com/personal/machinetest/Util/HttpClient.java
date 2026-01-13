package com.personal.machinetest.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {
    public static String get(String urlString) throws Exception {
        HttpURLConnection httpURLConnection = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();


        try {
            URL url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP error code: " + responseCode);
            }
            reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } finally {
            {
                if (httpURLConnection!=null){
                    httpURLConnection.disconnect();
                }
                if (reader!=null){
                    try {
                        reader.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
