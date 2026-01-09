# Add project specific ProGuard rules here.
-keep class * extends android.webkit.WebChromeClient { *; }
-keep class * implements android.webkit.WebViewClient { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
