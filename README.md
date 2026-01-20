# Second Brain Lite

A privacy-first, offline Android app for capturing thoughts, lessons, and decisions with emotional retention focus.

## Features

- 📝 **Offline-First**: All data stored locally with Room database
- 🎨 **Beautiful Dark UI**: Material Design with custom color palette
- 📌 **Pin Important Thoughts**: Keep your most important thoughts at the top
- 🔍 **Smart Search**: Find thoughts by text or category
- 🎯 **Categories**: Organize thoughts as Decisions, Lessons, or Reflections
- 💰 **Monetization**: AdMob integration with "Remove Ads" IAP
- 🔒 **Privacy**: No cloud, no login, no tracking

## Technical Stack

- **Language**: Kotlin
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Database**: Room (SQLite)
- **Architecture**: MVVM with coroutines and Flow
- **Monetization**: Google AdMob + Google Play Billing
- **UI**: Material Design 3

## Project Structure

```
app/src/main/java/com/secondbrain/lite/
├── MainActivity.kt              # Home screen with timeline
├── AddThoughtActivity.kt        # Add/edit thoughts
├── ViewThoughtActivity.kt       # View full thought
├── OnboardingActivity.kt        # First-time user onboarding
├── database/
│   ├── Thought.kt              # Room entity
│   ├── ThoughtDao.kt           # Database access object
│   └── AppDatabase.kt          # Database singleton
├── adapters/
│   └── ThoughtAdapter.kt       # RecyclerView adapter
└── utils/
    ├── PreferenceManager.kt    # SharedPreferences wrapper
    ├── AdManager.kt            # AdMob integration
    └── BillingManager.kt       # Google Play Billing
```

## Setup Instructions

### 1. Prerequisites
- Android Studio (latest version recommended)
- JDK 8 or higher
- Android SDK with API 34

### 2. Clone and Open
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `SecondBrainLite` folder
4. Wait for Gradle sync to complete

### 3. Configure AdMob (IMPORTANT)
Before publishing, replace test Ad Unit IDs in `AdManager.kt`:
```kotlin
// REPLACE THESE WITH YOUR REAL AD UNIT IDs
const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
```

Also update the AdMob App ID in `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="YOUR-ADMOB-APP-ID"/>
```

### 4. Configure In-App Purchase
1. Create a product in Google Play Console with ID: `remove_ads`
2. Set the price to $1.99
3. The product ID is already configured in `BillingManager.kt`

### 5. Build & Run
- **Debug APK**: `./gradlew assembleDebug`
- **Release APK**: `./gradlew assembleRelease`
- **Install on device**: `./gradlew installDebug`

## Build APK

### Windows:
```powershell
.\gradlew assembleDebug
```

### Linux/Mac:
```bash
./gradlew assembleDebug
```

The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

## Features Implementation

### Thoughts Management
- Create, read, update, delete thoughts
- Optional title + required main text
- Auto-assigned timestamp
- Category selection (Decision/Lesson/Reflection)

### Pin System
- Pin up to 5 thoughts by default
- Watch rewarded ad to unlock additional pin slot
- Pinned thoughts appear at top of timeline

### Search
- Real-time search as you type
- Searches both title and main text
- Partial match support

### Monetization
- **Banner Ads**: Persistent at bottom of timeline
- **Interstitial Ads**: Show after every 5 saved thoughts
- **Rewarded Ads**: Unlock additional pin slot
- **IAP**: One-time purchase to remove all ads ($1.99)

### Onboarding
- 3-screen walkthrough for first-time users
- Explains app purpose, how to add thoughts, and how to interact
- Skip button to bypass onboarding

## App Permissions

- `INTERNET`: Required for AdMob (even though app is offline)
- `ACCESS_NETWORK_STATE`: For AdMob network checks
- `VIBRATE`: For haptic feedback on interactions

## Privacy & Data

- **100% Offline**: No internet required except for ads
- **Local Storage**: All thoughts stored in local SQLite database
- **No Backup**: Database excluded from cloud backups for privacy
- **No Analytics**: No user tracking or analytics

## Color Palette

- Background: `#0F172A` (Dark Navy)
- Primary: `#38BDF8` (Soft Blue)
- Secondary: `#22C55E` (Success Green)
- Text: `#E5E7EB` (Off-White)
- Delete: `#EF4444` (Red)

## Category Colors
- Decision: `#F59E0B` (Orange)
- Lesson: `#8B5CF6` (Purple)
- Reflection: `#EC4899` (Pink)

## Testing

### Test AdMob Integration
The app uses Google's test ad unit IDs by default. You should see test ads appearing with "Test Ad" label.

### Test Billing
Use Google Play's test credit cards to test the IAP flow without actual charges.

### Manual Testing Checklist
- [ ] Add a new thought
- [ ] Edit an existing thought
- [ ] Pin a thought (verify it moves to top)
- [ ] Unpin a thought
- [ ] Delete a thought (verify confirmation dialog)
- [ ] Search for thoughts
- [ ] Add 5 thoughts (verify interstitial ad appears)
- [ ] Pin 5 thoughts (verify limit reached)
- [ ] Watch rewarded ad (verify additional pin unlocked)
- [ ] Purchase "Remove Ads" (verify ads disappear)
- [ ] First launch (verify onboarding appears)

## Known Limitations

- No cloud sync (by design for privacy)
- No data export/import yet
- No backup/restore (database excluded for privacy)
- Rewarded ad pins reset on app uninstall

## Future Enhancements (Optional)

- Export thoughts to text file
- Dark/light theme toggle
- Custom categories
- Thought templates
- Reminders/notifications
- Statistics dashboard

## License

This project is private and proprietary.

## Support

For issues or questions, please refer to the documentation or contact the developer.

---

**Built with ❤️ for privacy and reflection**
