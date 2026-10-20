# SwipeIt

Offline Android photo cleaner: swipe left to trash, right/up to keep. No internet permission, all data stays on the device.

## Requirements
- JDK 17+ (the JBR bundled with Android Studio works)
- Android SDK with platform 37 installed
- `local.properties` with `sdk.dir=...` (Android Studio creates it; otherwise set `ANDROID_HOME`)

## Build
```sh
./gradlew assembleDebug          # APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest      # JVM unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests (device/emulator)
```
On Windows use `gradlew.bat`. If `java` is not on PATH, point `JAVA_HOME` at Android Studio's JBR, e.g.
`C:\Program Files\Android\Android Studio\jbr`.

## Run
With a device connected (USB debugging on) or an emulator running:
```sh
./gradlew installDebug
adb shell am start -n com.swipeit.app/.MainActivity
```
