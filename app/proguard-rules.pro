# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\MyInstallProgramms\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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

#-keep class com.san.kir.manger.** { *; }
#-dontwarn com.san.kir.manger.**
#
#-keep class rx.internal.util.unsafe.** { *; }
#-dontwarn rx.internal.util.unsafe.**
-dontobfuscate

-keepattributes Signature
-keepattributes *Annotation*

#### from okhhp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase


##########
# Kotlin
##########
-dontwarn kotlin.**
-dontnote kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

##### from coroutines
-keepnames class kotlinx.** { *; }

-dontwarn com.squareup.okhttp.**

-keeppackagenames org.jsoup.nodes
-keep public class org.jsoup.** {
    public *;
}
