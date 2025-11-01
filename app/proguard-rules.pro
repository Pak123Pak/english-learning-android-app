# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

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

# =================
# SECURITY & PRIVACY
# =================

# Remove all logging in release builds for privacy and security
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}

# Remove System.out and System.err printing
-assumenosideeffects class java.io.PrintStream {
    public void println(%);
    public void println(**);
    public void print(%);
    public void print(**);
}

-assumenosideeffects class java.lang.System {
    public static void out.println(...);
    public static void out.print(...);
    public static void err.println(...);
    public static void err.print(...);
}

# Remove printStackTrace for security (prevents stack trace leaks)
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
    public void printStackTrace(**);
}

# =================
# APP DATA MODELS
# =================

# Keep all data models and API response classes
-keep class com.example.englishlearningandroidapp.data.api.** { *; }
-keep class com.example.englishlearningandroidapp.data.database.** { *; }
-keepclassmembers class com.example.englishlearningandroidapp.data.** { *; }

# =================
# ROOM DATABASE
# =================

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <methods>;
}
-dontwarn androidx.room.paging.**

# Keep DAO methods
-keepclassmembers interface * {
    @androidx.room.* <methods>;
}

# =================
# RETROFIT & GSON
# =================

# Retrofit does reflection on generic parameters
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep Retrofit interfaces
-keep,allowobfuscation interface retrofit2.Call
-keep,allowobfuscation interface retrofit2.http.*
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Gson uses generic type information stored in a class file when working with fields
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# =================
# ANDROIDX & KOTLIN
# =================

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Lifecycle
-keep class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
-keep class * implements androidx.lifecycle.GeneratedAdapter {
    <init>(...);
}
-keepclassmembers class ** {
    @androidx.lifecycle.OnLifecycleEvent *;
}

# =================
# GENERAL ANDROID
# =================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep views
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# Keep Activity classes for proper back stack
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep view binding classes
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(***);
    public static *** inflate(***);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =================
# OPTIMIZATION
# =================

# Optimize and obfuscate (but keep crash reports useful)
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''