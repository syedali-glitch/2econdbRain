# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Room database classes
-keep class com.secondbrain.lite.database.** { *; }

# Keep AdMob classes
-keep class com.google.android.gms.ads.** { *; }

# Keep Billing classes
-keep class com.android.billingclient.** { *; }
