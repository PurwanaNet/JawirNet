package id.my.kkgi.jawirnet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final long TIMEOUT_MS = 30000; // 30 detik
    
    private WifiManagerHelper wifiManagerHelper;
    private NFCHandler nfcHandler;
    private P2PServer p2pServer;
    private P2PClient p2pClient;
    
    private TextView statusView;
    private String deviceId;
    private Map<String, String> peerDevices = new ConcurrentHashMap<>(); // ID perangkat dan IP
    private Map<String, Long> messageTimestamps = new ConcurrentHashMap<>();
    
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        statusView = findViewById(R.id.statusView);
        
        wifiManagerHelper = new WifiManagerHelper(this);
        nfcHandler = new NFCHandler();
        p2pServer = new P2PServer();
        p2pClient = new P2PClient();
        
        wifiManagerHelper.enableWifi();
        wifiManagerHelper.connectOrCreateHotspot();
        
        new Thread(() -> p2pServer.startServer(peerDevices, this::onMessageReceived)).start();
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
            deviceId = Utils.bytesToHex(tag.getId());
            statusView.setText("Device ID: " + deviceId);
            Log.d(TAG, "NFC Detected: " + deviceId);
        }
    }
    
    /**
     * Kirim pesan ke perangkat lain dengan zona dan waktu
     */
    private void sendMessage(String message, String targetDeviceId) {
        if (peerDevices.containsKey(targetDeviceId)) {
            String targetIp = peerDevices.get(targetDeviceId);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String bundleMessage = "[" + timestamp + "] " + message;
            
            messageTimestamps.put(targetDeviceId, System.currentTimeMillis());
            p2pClient.sendMessage(deviceId, bundleMessage, targetIp);
            
            startRepeater(targetDeviceId, bundleMessage);
        } else {
            Log.d(TAG, "Target device ID not found in network.");
        }
    }
    
    /**
     * Repeater untuk memastikan pesan diterima, berhenti setelah 30 detik jika tidak ada feedback.
     */
    private void startRepeater(String targetDeviceId, String message) {
        handler.postDelayed(() -> {
            long currentTime = System.currentTimeMillis();
            if (messageTimestamps.containsKey(targetDeviceId) && currentTime - messageTimestamps.get(targetDeviceId) < TIMEOUT_MS) {
                Log.d(TAG, "Repeating message to " + targetDeviceId);
                sendMessage(message, targetDeviceId);
                startRepeater(targetDeviceId, message);
            } else {
                Log.d(TAG, "Message timeout, stopping repeater for " + targetDeviceId);
                messageTimestamps.remove(targetDeviceId);
            }
        }, 5000); // Coba ulangi setiap 5 detik
    }
    
    /**
     * Callback ketika menerima pesan
     */
    private void onMessageReceived(String senderId, String message) {
        Log.d(TAG, "Received message from " + senderId + ": " + message);
        messageTimestamps.remove(senderId); // Hentikan repeater karena ada feedback
        
        // Kirim ulang pesan ke perangkat lain sebagai repeater
        for (String peerId : peerDevices.keySet()) {
            if (!peerId.equals(senderId)) {
                sendMessage(message, peerId);
            }
        }
    }
}
