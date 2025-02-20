package id.my.kkgi.jawirnet;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefMessage;
import android.nfc.tech.NdefRecord;
import android.os.Parcelable;
import android.util.Log;
import java.nio.charset.StandardCharsets;

public class NFCHandler {
    private static final String TAG = "NFCHandler";
    private NfcAdapter nfcAdapter;
    private Activity activity;
    private NFCListener listener;

    public interface NFCListener {
        void onNFCDetected(String deviceId);
    }

    public NFCHandler(Activity activity, NFCListener listener) {
        this.activity = activity;
        this.listener = listener;
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
    }

    public boolean isNfcSupported() {
        return nfcAdapter != null;
    }

    public boolean isNfcEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    public void processIntent(Intent intent) {
        if (intent == null || !NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            return;
        }

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            String deviceId = getTagId(tag);
            Log.d(TAG, "NFC Tag Detected: " + deviceId);
            if (listener != null) {
                listener.onNFCDetected(deviceId);
            }
        }
    }

    private String getTagId(Tag tag) {
        byte[] id = tag.getId();
        StringBuilder hexString = new StringBuilder();
        for (byte b : id) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
}
