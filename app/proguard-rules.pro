# Add project specific ProGuard rules here.
# By default, the flags in the AOSP rules file are used.
# See http://developer.android.com/tools/help/proguard.html for more details.

# Keep all Compose Composables
-keepnames class * extends androidx.compose.runtime.Composer

# Keep all Compose runtime classes
-keep class androidx.compose.runtime.** { *; }

# Keep all Compose UI classes
-keep class androidx.compose.ui.** { *; }

# Keep all Compose Material classes
-keep class androidx.compose.material.** { *; }

# Keep all Compose Material3 classes
-keep class androidx.compose.material3.** { *; }
