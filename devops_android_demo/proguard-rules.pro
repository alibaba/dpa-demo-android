# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-------------------update-main------------------------------
-keepclassmembers class * {
    @com.google.inject.Inject <init>(...);
}
# There's no way to keep all @Observes methods, so use the On*Event convention to identify event handlers
-keepclassmembers class * {
    void *(**On*Event);
}
-keepclassmembers class ** {
    public <init>(android.content.Context);
}
-keepclassmembernames class **.R$* {*;}
-keepclassmembernames class **.R {*;}
-keepclassmembers class **{
    public static final <fields>;
}

-keep  class com.taobao.update.apk.MainUpdateData{*;}
-keep  class com.taobao.update.apk.ApkUpdater{*;}

#-------------------update-common------------------------------
-keep class com.taobao.update.common.framework.**{*;}
-keep class com.taobao.update.common.utils.**{*;}
-keep  class com.taobao.update.common.dialog.**{*;}
-keep  class com.taobao.update.common.Config{*;}
-keep class com.taobao.update.common.dialog.CustomUpdateInfo{
    public <methods>;
}
-keep interface com.taobao.update.common.dialog.UpdateNotifyListener{*;}
