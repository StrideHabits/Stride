# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================
# Retrofit & OkHttp Rules
# ============================================

# Retrofit interfaces
-keep interface com.mpieterse.stride.data.remote.SummitApiService { *; }

# Retrofit does reflection on generic parameters of RequestBody and ResponseBody
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retrofit annotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep annotation default values (e.g., retrofit2.http.Field.encoded)
-keepattributes AnnotationDefault

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available
-dontwarn okhttp3.**

# ============================================
# Gson Rules for DTOs
# ============================================

# Keep all DTOs used with Gson serialization
-keep class com.mpieterse.stride.data.dto.** { *; }

# Keep all remote models
-keep class com.mpieterse.stride.data.remote.models.** { *; }

# Gson uses generic type information stored in a class file when working with fields
-keepattributes Signature

# Gson specific classes (keep)
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================
# Room Database Rules
# ============================================

# Room entities
-keep class com.mpieterse.stride.data.local.entities.** { *; }

# Room DAOs
-keep interface com.mpieterse.stride.data.local.dao.** { *; }

# Room database
-keep class com.mpieterse.stride.data.local.db.AppDatabase { *; }
-keep class com.mpieterse.stride.data.local.db.Converters { *; }

# Room uses reflection to access constructors
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# Room migrations
-keep class com.mpieterse.stride.data.local.db.Migrations { *; }

# ============================================
# Kotlin Data Classes
# ============================================

# Keep Kotlin data classes for API responses
-keep class com.mpieterse.stride.core.models.** { *; }
-keep class com.mpieterse.stride.ui.layout.**.models.** { *; }

# Keep Kotlin metadata for reflection
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# ============================================
# Kotlin Coroutines
# ============================================

# Keep coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

-keepclassmembers class kotlinx.coroutines.internal.** {
    volatile <fields>;
}

# ============================================
# Firebase Rules
# ============================================

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============================================
# Hilt/Dagger Rules
# ============================================

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ============================================
# WorkManager Rules
# ============================================

# Keep WorkManager workers
-keep class com.mpieterse.stride.workers.** { *; }

# ============================================
# Keep Stride-specific models and services
# ============================================

# Keep service classes
-keep class com.mpieterse.stride.core.services.** { *; }

# Keep repository implementations
-keep class com.mpieterse.stride.data.repo.concrete.** { *; }

# Keep ViewModels (if needed for debugging)
-keep class com.mpieterse.stride.ui.layout.**.viewmodels.** { *; }