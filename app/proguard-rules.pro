# Optimasi kode dan penghapusan kode yang tidak digunakan
-dontoptimize
-dontshrink
-dontobfuscate

# Simpan semua class model dan interface untuk komunikasi NFC & WiFi
-keep class java.id.my.kkgi.jawirnet.** { *; }

# Simpan class yang digunakan dalam refleksi
-keepattributes *Annotation*
-keepattributes InnerClasses

# Simpan class Android yang diperlukan
-keep class android.** { *; }

# Simpan class dan method JNI (Java Native Interface)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Simpan semua class yang terkait dengan NFC
-keep class android.nfc.** { *; }

# Simpan semua class yang terkait dengan WiFi
-keep class android.net.wifi.** { *; }

# Simpan semua class dari library eksternal (misalnya jika menggunakan Retrofit, Gson, dll.)
-dontwarn okhttp3.**
-dontwarn com.google.gson.**
-dontwarn retrofit2.**

# Simpan metode yang digunakan untuk log debugging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
