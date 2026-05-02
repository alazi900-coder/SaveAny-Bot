-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.krau.saveany.**$$serializer { *; }
-keepclassmembers class com.krau.saveany.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class kotlin.reflect.jvm.internal.impl.** { *; }
