package id.my.kkgi.jawirnet;

import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class P2PClient {
    private static final String TAG = "P2PClient";
    private static final int PORT = 8888;
    private String myDeviceId;

    public P2PClient(String deviceId) {
        this.myDeviceId = deviceId;
    }

    public void sendMessage(String recipientId, String message, String serverAddress) {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                InetAddress serverInetAddress = InetAddress.getByName(serverAddress);

                String formattedMessage = myDeviceId + ":" + recipientId + ":" + message;
                byte[] data = formattedMessage.getBytes();

                DatagramPacket packet = new DatagramPacket(data, data.length, serverInetAddress, PORT);
                socket.send(packet);
                socket.close();

                Log.d(TAG, "Message sent to " + recipientId + " via server at " + serverAddress);
            } catch (Exception e) {
                Log.e(TAG, "Error sending message", e);
            }
        }).start();
    }

    public void listenForMessages() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (true) {
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    Log.d(TAG, "Received message: " + receivedMessage);
                    processReceivedMessage(receivedMessage);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error receiving messages", e);
            }
        }).start();
    }

    private void processReceivedMessage(String message) {
        String[] parts = message.split(":");
        if (parts.length < 3) return;

        String senderId = parts[0];
        String recipientId = parts[1];
        String content = parts[2];

        if (recipientId.equals(myDeviceId)) {
            Log.d(TAG, "New message from " + senderId + ": " + content);
        }
    }
}
