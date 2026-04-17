# Add project specific ProGuard rules here.
-keep class com.jcraft.jsch.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
