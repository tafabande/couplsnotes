# Add project specific ProGuard rules here.

# Keep data models for Firestore serialization to prevent runtime crashes
-keep class com.example.noteshare.data.model.** { *; }

# Workaround for Jetpack Compose Animation R8 stripping issue (NoSuchMethodError: at)
-keep class androidx.compose.animation.core.** { *; }
-keepclassmembers class androidx.compose.animation.core.** { *; }

# Keep Dagger/Hilt generated code safely
-keep,allowobfuscation,allowshrinking class dagger.hilt.** { *; }
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.** { *; }
