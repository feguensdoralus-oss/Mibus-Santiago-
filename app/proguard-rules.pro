# © 2026 MiBus Santiago by Feguens Doralus
# ============================================================================
# Reglas de Optimización y Ofuscación ProGuard / R8
# ============================================================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Preservar atributos de depuración esenciales
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# ============================================================================
# Firebase (Authentication, Firestore, Common SDKs)
# ============================================================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Mantener clases POJO / Modelos de datos para Firestore
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
    @com.google.firebase.firestore.ServerTimestamp <methods>;
}

# Firebase Auth & Google Credential Manager
-keep class com.google.android.gms.auth.api.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# ============================================================================
# Modelos de Datos del Proyecto MiBus Santiago
# ============================================================================
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** {
    <fields>;
    <methods>;
}
-keep class com.example.data.local.** { *; }
-keepclassmembers class com.example.data.local.** {
    <fields>;
    <methods>;
}

# ============================================================================
# Retrofit 2, OkHttp 3 & Gson
# ============================================================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ============================================================================
# Coroutines & Jetpack Compose
# ============================================================================
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**
-keep class androidx.compose.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

