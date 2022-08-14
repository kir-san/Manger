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

-dontwarn org.codehaus.mojo.animal_sniffer.*

-keeppackagenames org.jsoup.nodes
-keep public class org.jsoup.** {
    public *;
}

# Allow R8 to optimize away the FastServiceLoader.
# Together with ServiceLoader optimization in R8
# this results in direct instantiation when loading Dispatchers.Main
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatcherLoader {
    boolean FAST_SERVICE_LOADER_ENABLED return false;
}

-assumenosideeffects class kotlinx.coroutines.internal.FastServiceLoaderKt {
    boolean ANDROID_DETECTED return true;
}

-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {*;}

# Disable support for "Missing Main Dispatcher", since we always have Android main dispatcher
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatchersKt {
    boolean SUPPORT_MISSING return false;
}

# Statically turn off all debugging facilities and assertions
-assumenosideeffects class kotlinx.coroutines.DebugKt {
    boolean getASSERTIONS_ENABLED() return false;
    boolean getDEBUG() return false;
    boolean getRECOVER_STACK_TRACES() return false;
}

-keepnames !abstract class com.customername.android.injection.*

#Keeping the members of that have static vars
-keepclassmembers public class com.customername.android.** {
        	public static * ;
        	public *;
        }

# Below will be classes you want to explicity keep AND obfuscate - you shouldn't need to do this unless your class is only referenced at runtime and not compile time (IE injected via annotation or reflection)
#-keep,allowobfuscation class com.customername.android.** { *; }

#Things you don't want to obfuscate and you don't want to be shrunk usually GSON pojos. Add your domain/JSON below here
-keep class com.customername.android.model.** { *; }

-dontwarn okio.**
-dontwarn org.simpleframework.**
-keep class com.google.common.** { *; }


-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation**
