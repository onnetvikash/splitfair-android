# SplitFair

A tiny, free Android app that splits a restaurant bill (tip included) fairly between friends.
No sign-up, no ads, no internet, no permissions, no tracking.

## Features
- Enter the bill, pick a tip (0-30%), choose how many people are splitting
- Every share is rounded **up**, so the group never comes up short
- Optional "round each share up to a whole number" for easy cash payments
- Shows the tip, total and what each person pays
- One tap to share the summary in any chat app
- Light and dark theme, works offline, uses your phone's currency format

## Requirements
- Android 8.0 (API 26) or newer

## Build and test
The project uses Gradle with the Android Gradle Plugin 8.5 and needs JDK 17.

Easiest: open the folder in **Android Studio** and press Run.

From the command line (with Gradle 8.7+ installed, or after running `gradle wrapper` once):

```bash
gradle testDebugUnitTest   # run unit tests
gradle assembleDebug       # build app/build/outputs/apk/debug/app-debug.apk
```

The bill-splitting logic lives in `SplitCalculator.java` (pure Java, unit tested in
`SplitCalculatorTest.java`).

## Continuous integration and releases
`.github/workflows/android.yml` runs the unit tests and builds a debug APK on every push and pull request.
Push a tag such as `v1.0.0` and the workflow also attaches the APK to a GitHub Release.

## Publishing to Google Play
1. Create a keystore once and keep it safe: `keytool -genkey -v -keystore splitfair.jks -keyalg RSA -keysize 2048 -validity 10000 -alias splitfair`
2. Build a signed bundle by setting `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`, then run `gradle bundleRelease`
3. Upload `app/build/outputs/bundle/release/app-release.aab` in the Play Console
4. Use the texts in [`docs/STORE_LISTING.md`](docs/STORE_LISTING.md) and add screenshots

## Privacy
SplitFair collects nothing. It has no internet access, requests no permissions and stores no data.

## License
MIT, see [LICENSE](LICENSE). Made by Devexis India.
