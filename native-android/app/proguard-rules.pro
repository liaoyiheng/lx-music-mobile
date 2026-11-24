# QuickJS JNI bindings must not be obfuscated
-keep class com.whl.quickjs.** { *; }
-keep interface com.whl.quickjs.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class cn.toside.music.liquid.**$$serializer { *; }
-keepclassmembers class cn.toside.music.liquid.** {
    *** Companion;
}
-keepclasseswithmembers class cn.toside.music.liquid.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Media3
-keep class androidx.media3.** { *; }
