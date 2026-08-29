# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    *;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-if @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
-keepclassmembers class kotlinx.serialization.internal.PluginGeneratedSerialDescriptor { *; }

# Ktor / OkHttp engine
-dontwarn kotlinx.serialization.**
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# Timber
-dontwarn org.jetbrains.annotations.**
