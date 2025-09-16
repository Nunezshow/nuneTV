# Project Setup

This guide walks you through configuring and launching the NuneTV Android TV IPTV app.

## Prerequisites

- Android Studio Giraffe (2022.3.1) or newer
- Android SDK Platform 34 with Android TV emulator image or a physical Android TV device
- JDK 17 (bundled with recent Android Studio releases)
- Git

## Repository Initialization

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-user>/android-tv-iptv-app.git
   cd android-tv-iptv-app
   ```
2. If the Gradle wrapper JAR is not present, regenerate it:
   ```bash
   ./gradlew wrapper
   ```
   Android Studio will also regenerate the wrapper automatically on project sync.

3. (Optional) Create the remote repository using the helper script once you update the placeholders:
   ```bash
   bash docs/init_repo.sh
   ```

## Android Studio

1. Open Android Studio and select **Open an Existing Project**.
2. Navigate to the cloned repository root and open it.
3. Allow Android Studio to sync Gradle and download dependencies.
4. If prompted, install the required Android TV SDK packages.

## Building the APK

- From Android Studio: click **Build > Make Project** and then **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
- From the command line:
  ```bash
  ./gradlew assembleDebug
  ```
  The resulting APK will be available at `app/build/outputs/apk/debug/app-debug.apk`.

## Installing on Android TV

1. Connect an Android TV device via ADB or start an Android TV emulator.
2. Install the debug APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. Launch the **NuneTV** app from the Android TV home screen.

## Provider Configuration

1. Open the app and press the **Menu** or **Settings** button on the remote to launch the provider settings screen.
2. Enter the XStream Codes portal URL, username, password, and optional M3U/EPG URLs.
3. Use **Test Connection** to validate the credentials.
4. Save the provider and activate it to refresh the content catalogue.

## Troubleshooting

- **Gradle Sync Issues** – Ensure that JDK 17 is configured in Android Studio and that the Android SDK Platform 34 is installed.
- **Missing Gradle Wrapper** – Run `./gradlew wrapper` from the repository root, or re-import the project into Android Studio.
- **ADB Connection Errors** – Confirm that `adb devices` lists your Android TV target before installing the APK.
