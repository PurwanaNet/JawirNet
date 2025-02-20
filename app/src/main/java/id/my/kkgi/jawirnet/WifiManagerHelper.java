package id.my.kkgi.jawirnet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.List;

public class WifiManagerHelper {
    private static final String TAG = "WifiManagerHelper";
    private static final String SSID_NAME = "kkgi.my.id/JawirNet";
    private static final String SSID_PASSWORD = "Jawir12345";
    
    private WifiManager wifiManager;
    private Context context;

    public WifiManagerHelper(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public boolean connectToSSID() {
        if (wifiManager == null) {
            Log.e(TAG, "WiFi Manager not available");
            return false;
        }

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configuredNetworks) {
            if (config.SSID.equals("\"" + SSID_NAME + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(config.networkId, true);
                wifiManager.reconnect();
                Log.d(TAG, "Connected to existing SSID: " + SSID_NAME);
                return true;
            }
        }

        return false;
    }

    public boolean isConnectedToSSID() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        }
        return false;
    }

    public boolean createHotspot() {
        if (wifiManager == null) {
            Log.e(TAG, "WiFi Manager not available");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e(TAG, "Creating a hotspot is not supported in Android 8+ without system permissions.");
            return false;
        }

        try {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = SSID_NAME;
            wifiConfig.preSharedKey = SSID_PASSWORD;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean success = (boolean) setWifiApMethod.invoke(wifiManager, wifiConfig, true);

            Log.d(TAG, "Hotspot creation: " + (success ? "Success" : "Failed"));
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Failed to create hotspot", e);
            return false;
        }
    }
}
