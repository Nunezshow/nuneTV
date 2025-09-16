# NuneTV – Android TV IPTV Client

NuneTV is a Kotlin-based Android TV application built with the Leanback library to deliver an IPTV experience optimised for remote navigation. It supports XStream Codes portals, M3U playlists, XMLTV EPG data, and ExoPlayer playback for HLS, MPEG-TS, and MP4 streams.

## Features

- Leanback home screen with Live TV, Movies, Series, and Favorites rows
- Channel search, guided channel details, and in-app EPG grid
- Multiple provider management with encrypted credential storage
- ExoPlayer-based playback with buffering indicators and error handling
- Optional playlist and EPG ingestion via M3U and XMLTV sources

## Getting Started

1. Clone the repository and open it in Android Studio Giraffe (2022.3.1) or newer.
2. Allow Gradle to sync. Install Android SDK Platform 34 and Android TV emulator images if prompted.
3. Build a debug APK via **Build > Build APK(s)** or from the command line:
   ```bash
   ./gradlew assembleDebug
   ```
  4. Install the APK on an Android TV device or emulator:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

   You can also run `scripts/build_apk.sh` to execute the appropriate Gradle command and echo the APK location.

> **Note:** If the Gradle wrapper JAR is missing, run `./gradlew wrapper` before building. Android Studio can regenerate the wrapper automatically during sync.

## Provider Management

- Press the **Menu** or **Settings** button on the Android TV remote while on the home screen to launch the provider settings screen.
- Enter the XStream Codes portal URL, username, and password. Optional M3U and EPG URLs can be supplied to augment the catalogue.
- Use **Test Connection** to validate credentials, then **Save Provider** and **Activate Provider** to refresh the content library.
- Multiple provider profiles can be stored securely and switched on demand.

## Using the App

- Browse Live TV, Movies, Series, and Favorites rows using the D-pad.
- Click a channel to open the guided details dialog, where you can **Play now**, toggle favorites, or view the EPG.
- Press the search button in the Leanback UI to find channels across all categories.
- The EPG grid updates in real time when channels are highlighted or the guide action is selected.

## Troubleshooting

- **Login failures** – Confirm the portal URL, username, and password. Use the settings screen to test the connection.
- **Empty playlist** – Ensure the M3U URL is reachable and returns channel entries. The app surfaces an error when parsing fails.
- **No EPG data** – Validate the XMLTV feed URL and refresh the provider after updating the source.
- **Playback errors** – ExoPlayer displays error messages and stops playback. Test the stream URL outside the app if issues persist.

## Documentation

- [Setup Instructions](docs/SETUP.md)
- [Architecture Overview](docs/ARCHITECTURE.md)

## License

This project is released under the [MIT License](LICENSE).
