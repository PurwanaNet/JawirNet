package java.id.my.kkgi.jawirnet;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int PORT = 8888;
    private static final String SSID_NAME = "kkgi.my.id/JawirNet";
    private WifiManager wifiManager;
    private NfcAdapter nfcAdapter;
    private String deviceId;
    private TextView statusView;
    private Map<String, InetAddress> peerDevices = new HashMap<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusView = findViewById(R.id.statusView);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        
        connectToSSID();
    }
    
    private void connectToSSID() {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", SSID_NAME);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        int netId = wifiManager.addNetwork(wifiConfig);
        
        if (netId != -1) {
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        } else {
            startHotspot();
        }
    }
    
    private void startHotspot() {
        Log.d("Jawir", "Starting Hotspot...");
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = SSID_NAME;
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        
        try {
            wifiManager.addNetwork(wifiConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, null, null, null);
        }
    }
    
    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            deviceId = new String(tag.getId(), Charset.forName("UTF-8"));
            statusView.setText("Device ID: " + deviceId);
            new Thread(this::startUDPServer).start();
        }
    }
    
    private void startUDPServer() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            while (true) {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                String senderId = received.split(":")[0];
                String message = received.split(":")[1];
                
                peerDevices.put(senderId, packet.getAddress());
                Log.d("Jawir", "Received from " + senderId + ": " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendUDPMessage(String message, String targetDeviceId) {
        new Thread(() -> {
            try {
                if (peerDevices.containsKey(targetDeviceId)) {
                    InetAddress address = peerDevices.get(targetDeviceId);
                    DatagramSocket socket = new DatagramSocket();
                    String data = deviceId + ":" + message;
                    byte[] buffer = data.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
                    socket.send(packet);
                    socket.close();
                } else {
                    Log.d("Jawir", "Target device ID not found in network.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
