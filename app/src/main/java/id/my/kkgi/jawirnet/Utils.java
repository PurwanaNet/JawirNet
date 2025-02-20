package id.my.kkgi.jawirnet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.nio.charset.StandardCharsets;

public class Utils {
    private static final String TAG = "Utils";
    public static final String SSID_NAME = "kkgi.my.id/JawirNet";

    /**
     * Konversi byte array ke string heksadesimal untuk ID NFC
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    /**
     * Konversi string heksadesimal ke byte array
     */
    public static byte[] hexToBytes(String hexString) {
        int length = hexString.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Konversi byte array ke string berbasis UTF-8
     */
    public static String bytesToUtf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Periksa apakah perangkat saat ini terhubung ke jaringan
     */
    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    /**
     * Log status koneksi jaringan
     */
    public static void logNetworkStatus(Context context) {
        if (isConnectedToNetwork(context)) {
            Log.d(TAG, "Terhubung ke jaringan: " + SSID_NAME);
        } else {
            Log.d(TAG, "Tidak terhubung ke jaringan.");
        }
    }
}
