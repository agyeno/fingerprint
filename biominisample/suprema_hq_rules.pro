-dontshrink
-keepclasseswithmembernames interface com.suprema.IBioMiniDevice
-keepclasseswithmembernames interface com.suprema.IUsbEventHandler
-keepclasseswithmembernames class com.suprema.android.BioMiniJni
-keepclasseswithmembernames class com.suprema.android.BioMiniJniTest
-keepclasseswithmembernames class com.suprema.CaptureResponder

-keepclasseswithmembernames interface com.suprema.util.IBioMiniCallback
-keepclasseswithmembernames interface com.suprema.util.IBioMiniDeviceCallback

-keep public interface com.suprema.util.IBioMiniCallback {*;}
-keep public interface com.suprema.util.IBioMiniDeviceCallback {*;}

-keep public interface com.suprema.IBioMiniDevice {*;}
-keep public class com.suprema.IBioMiniDevice$** {*;}
-keep public interface com.suprema.IBioMiniDevice$** {*;}
-keep public interface com.suprema.IUsbEventHandler {*;}

-keep class com.suprema.devices.BioMiniOC4 {
	enum **;
	public final byte[] m_ImageLast;
	private boolean SetOC4IntegrationTime(int,int);
}
-keep class com.suprema.devices.*{
	enum **;
	public final byte[] m_ImageLast;
}
-keep class com.suprema.hid.*{
	enum **;
	public byte[] m_ImageLast;
}
-keep public enum com.suprema.IUsbEventHandler$** {
    **[] $VALUES;
    public *;
}
-keep class com.suprema.BioMiniFactory {
    public *;
    enum **;
}
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keep class com.suprema.CaptureResponder {
	public *;
}

-keepattributes InnerClasses

-keep class com.suprema.util.IBridgeCallback
-keep class com.suprema.util.IBridgeCallback{
	public *;
}
-keep class com.suprema.util.Security{
	public *;
}
-keep class com.suprema.util.Logger{
	public *;
}
-keep class com.android.biomini.BioMiniAndroid {
	public *;
	public enum **;
}

-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}