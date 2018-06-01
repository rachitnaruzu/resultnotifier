package com.resultnotifier.main;

import android.util.Base64;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CommonUtility {
    public static final String APP_NAME = "Result Notifier";
    public static final String DOMAIN = "<DOMAIN>";
    public static final String SECRET_KEY = "<SECRET_KEY>"; //must be same as that in backend

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static Map<String, String> getSecureParams() {
        Map<String, String> params = new HashMap<>();
        return addSecureParams(params);
    }

    private static Map<String, String> addSecureParams(Map<String, String> params) {
        String ts = getEncodedTimestamp();
        params.put("sec", ts);
        params.put("sig", encodeStringData(SECRET_KEY, ts));
        return params;
    }

    private static String getEncodedTimestamp() {
        Calendar currentTime = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String ts = dateFormat.format(currentTime.getTime());
        return Base64.encodeToString(ts.getBytes(), Base64.URL_SAFE);
    }

    private static String encodeStringData(String key, String value) {
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes());
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            Log.e("MainFragment", e.getMessage());
        }
        return null;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
