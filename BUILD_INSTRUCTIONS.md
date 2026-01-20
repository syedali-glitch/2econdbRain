# Build Instructions for Second Brain Lite

## Prerequisites

1. **Install Android Studio**: Download from https://developer.android.com/studio
2. **Install JDK 8 or higher**: Android Studio usually includes one
3. **Android SDK**: Install through Android Studio SDK Manager
   - SDK Platform: Android 14 (API 34)
   - Build Tools: Latest version
   - Android SDK Platform-Tools
   - Android SDK Command-line Tools

## Option 1: Build Using Android Studio (Recommended)

### Step 1: Open Project
1. Launch Android Studio
2. Click "Open" or "File > Open"
3. Navigate to `c:\Users\pc\Desktop\2nd Brain\SecondBrainLite`
4. Click "OK"

### Step 2: Sync Gradle
1. Android Studio will automatically start Gradle sync
2. Wait for sync to complete (may take a few minutes on first run)
3. If sync fails, click "Sync Project with Gradle Files" button

### Step 3: Build APK
1. Click "Build" in the menu bar
2. Select "Build Bundle(s) / APK(s)" > "Build APK(s)"
3. Wait for build to complete
4. Click "locate" in the notification to find the APK

### Step 4: Find Your APK
Location: `app\build\outputs\apk\debug\app-debug.apk`

## Option 2: Build Using Command Line

### Windows (PowerShell):

```powershell
# Navigate to project directory
cd "c:\Users\pc\Desktop\2nd Brain\SecondBrainLite"

# Make gradlew executable (first time only)
# Not needed on Windows

# Build debug APK
.\gradlew assembleDebug

# Build release APK (unsigned)
.\gradlew assembleRelease
```

### Linux/Mac:

```bash
# Navigate to project directory
cd "/path/to/SecondBrainLite"

# Make gradlew executable (first time only)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK (unsigned)
./gradlew assembleRelease
```

### Output Location:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Option 3: Install Directly to Device

### Using Android Studio:
1. Connect Android device via USB
2. Enable "Developer Options" and "USB Debugging" on device
3. Click the green "Run" button (▶) in Android Studio
4. Select your device from the list
5. App will build, install, and launch automatically

### Using Command Line:

```powershell
# Install debug APK to connected device
.\gradlew installDebug

# Or manually install with adb
adb install app\build\outputs\apk\debug\app-debug.apk
```

## Troubleshooting

### Gradle Sync Failed
- **Solution**: File > Invalidate Caches and Restart
- Check internet connection (needed to download dependencies)
- Ensure you have the correct SDK versions installed

### Build Error: "SDK not found"
- **Solution**: Set ANDROID_HOME environment variable
  ```powershell
  # Windows
  $env:ANDROID_HOME = "C:\Users\YourName\AppData\Local\Android\Sdk"
  ```

### "Missing Kotlin Plugin"
- **Solution**: Android Studio should prompt to install it
- Or manually: File > Settings > Plugins > Search "Kotlin" > Install

### Out of Memory During Build
- **Solution**: Add to `gradle.properties`:
  ```
  org.gradle.jvmargs=-Xmx2048m -XX:MaxPermSize=512m
  ```

### Clean Build
If you encounter persistent issues:
```powershell
.\gradlew clean
.\gradlew assembleDebug
```

## Building for Production

### 1. Create Keystore (first time only):

```powershell
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

### 2. Sign the APK:

Add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/my-release-key.jks")
            storePassword = "your-store-password"
            keyAlias = "my-key-alias"
            keyPassword = "your-key-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other settings
        }
    }
}
```

### 3. Build signed release APK:

```powershell
.\gradlew assembleRelease
```

### 4. Before Publishing:

⚠️ **IMPORTANT**: Replace test Ad Unit IDs in `AdManager.kt` with your real AdMob IDs:
- Banner Ad Unit ID
- Interstitial Ad Unit ID
- Rewarded Ad Unit ID

⚠️ **IMPORTANT**: Update AdMob App ID in `AndroidManifest.xml`

⚠️ **IMPORTANT**: Create IAP product `remove_ads` in Google Play Console

## APK Size

- Debug APK: ~10-15 MB
- Release APK (with ProGuard): ~8-12 MB

## Testing

### Install on Physical Device:
1. Enable "Unknown Sources" in device settings
2. Transfer APK to device
3. Open APK file and install
4. Grant required permissions

### Test Ads:
- Debug builds use Google's test ad unit IDs
- You should see "Test Ad" labels on all ads

## Next Steps After Building

1. Test all features (see README.md testing checklist)
2. Replace AdMob test IDs with production IDs
3. Create IAP product in Google Play Console
4. Sign APK with release keystore
5. Upload to Google Play Console (internal testing first)
6. Submit for review

## Support

For build issues, check:
1. Android Studio error log: View > Tool Windows > Build
2. Gradle console output
3. `build` directory for detailed error logs

---

**Ready to build your app! 🚀**
