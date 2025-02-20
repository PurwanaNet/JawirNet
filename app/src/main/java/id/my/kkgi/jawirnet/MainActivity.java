package id.my.kkgi.jawirnet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    
    private WifiManagerHelper wifiManagerHelper;
    private NFCHandler nfcHandler;
    private P2PServer p2pServer;
    private P2PClient p2pClient;
    
    private TextView statusView;
    private String deviceId;
    private Map<String, String> peerDevices = new HashMap<>(); // Simpan ID perangkat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusView = findViewById(R.id.statusView);
        
        // Inisialisasi WiFi, NFC, dan P2P
        wifiManagerHelper = new WifiManagerHelper(this);
        nfcHandler = new NFCHandler();
        p2pServer = new P2PServer();
        p2pClient = new P2PClient();

        // Pastikan WiFi aktif
        wifiManagerHelper.enableWifi();
        
        // Coba sambungkan ke SSID atau buat hotspot jika tidak tersedia
        wifiManagerHelper.connectOrCreateHotspot();

        // Mulai server UDP untuk komunikasi P2P
        new Thread(() -> p2pServer.startServer(peerDevices)).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcHandler.enableNFC(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag != null) {
            // Konversi ID NFC menjadi string heksadesimal
            deviceId = Utils.bytesToHex(tag.getId());
            statusView.setText("Device ID: " + deviceId);
            Log.d(TAG, "NFC Detected: " + deviceId);
        }
    }

    /**
     * Kirim pesan ke perangkat lain menggunakan ID NFC.
     */
    private void sendMessage(String message, String targetDeviceId) {
        if (peerDevices.containsKey(targetDeviceId)) {
            String targetIp = peerDevices.get(targetDeviceId);
            p2pClient.sendMessage(deviceId, message, targetIp);
        } else {
            Log.d(TAG, "Target device ID not found in network.");
        }
    }
}
