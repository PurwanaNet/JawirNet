package id.my.kkgi.jawirnet;

import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class P2PServer {
    private static final String TAG = "P2PServer";
    private static final int PORT = 8888;
    private Map<String, InetAddress> peerMap = new HashMap<>();

    public void startServer() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                Log.d(TAG, "P2P Server started on port " + PORT);
                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String receivedData = new String(packet.getData(), 0, packet.getLength());
                    Log.d(TAG, "Received: " + receivedData);

                    processIncomingData(receivedData, packet.getAddress());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in P2P Server", e);
            }
        }).start();
    }

    private void processIncomingData(String data, InetAddress senderAddress) {
        String[] parts = data.split(":");
        if (parts.length < 2) return;

        String peerId = parts[0];
        String message = parts[1];

        peerMap.put(peerId, senderAddress);
        Log.d(TAG, "Peer " + peerId + " registered with IP: " + senderAddress.getHostAddress());

        // Example: Broadcast message to all connected peers
        broadcastMessage(peerId, message);
    }

    public void sendMessage(String peerId, String message) {
        InetAddress recipientAddress = peerMap.get(peerId);
        if (recipientAddress != null) {
            new Thread(() -> {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    byte[] data = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, recipientAddress, PORT);
                    socket.send(packet);
                    socket.close();
                    Log.d(TAG, "Message sent to " + peerId);
                } catch (Exception e) {
                    Log.e(TAG, "Error sending message to " + peerId, e);
                }
            }).start();
        } else {
            Log.e(TAG, "Peer ID not found: " + peerId);
        }
    }

    private void broadcastMessage(String senderId, String message) {
        for (Map.Entry<String, InetAddress> entry : peerMap.entrySet()) {
            String peerId = entry.getKey();
            if (!peerId.equals(senderId)) {
                sendMessage(peerId, "From " + senderId + ": " + message);
            }
        }
    }
}
