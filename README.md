# Liquid TV Launcher

A tvOS-style home launcher for Android TV / Google TV, built with Kotlin + Jetpack Compose.

- Drifting colour wallpaper that takes on the focused app's colour
- Glass top bar (clock, date, Settings)
- Hero shelf that crossfades to the focused app
- App grid: spring-scale focus, lift, glow shadow, floating icon layer, specular sheen
- OK = open app, hold OK = app info, Back is swallowed (it's a launcher)

## Build
1. Open this folder in Android Studio (Ladybug or newer). It creates the Gradle wrapper and syncs.
2. Run on an Android TV emulator or device (`app` configuration).
   Or: `gradle assembleDebug` then `adb install app/build/outputs/apk/debug/app-debug.apk`

## Use as your default Home
Press Home on the TV -> choose "Liquid TV" -> Always.
Some TVs (Google TV, Sony, etc.) hide the chooser. Use:
`adb shell cmd package set-home-activity com.liquidtv.launcher/.MainActivity`
