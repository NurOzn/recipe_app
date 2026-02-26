# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,allowobfuscation,allowshrinking class com.melikenurozun.recipe_app.domain.model.** { *; }
-keep,allowobfuscation,allowshrinking class com.melikenurozun.recipe_app.data.remote.** { *; }

# Supabase / Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Hilt (Generally handled by plugin, but good practice)
-keep class com.melikenurozun.recipe_app.RecipeApp { *; }
-keep class dagger.hilt.** { *; }

# Android Components
-keep class * extends androidx.activity.ComponentActivity
-keep class * extends androidx.lifecycle.ViewModel

# Coil
-keep class coil.** { *; }