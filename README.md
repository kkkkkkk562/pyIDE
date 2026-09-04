# Kotlin Python IDE

Kotlin Python IDE is a native Android Studio project for editing and running Python on an Android device. It includes:

- A dark, touch-friendly Kotlin + Jetpack Compose IDE shell
- Local projects and files stored in the app sandbox
- A Python editor with save and run actions
- Per-project package folders, acting as an Android-safe equivalent of a virtual environment
- Runtime package installation for pure-Python packages through pip
- A built-in output console
- Android launcher icon and release build configuration
- Adaptive launcher icon with the KODEX code-brackets plus mark

## Open in Android Studio

1. Open the `kotlin-python-ide` folder in Android Studio Hedgehog or newer.
2. Allow Gradle sync to finish.
3. Connect an Android 8.0+ device or start an emulator.
4. Press **Run**.

The Python runtime is embedded with Chaquopy. Android does not support a desktop-style `python -m venv` directory, so each project gets an isolated `.packages` directory which is added to Python's import path at runtime. Packages with native desktop-only extensions may not install on Android.

## Build APK and AAB

From Android Studio, open the Gradle tool window and run:

- `app > Tasks > build > assembleRelease` for an APK
- `app > Tasks > build > bundleRelease` for an AAB

If you have Gradle installed locally, run these from the project directory:

```bash
gradle assembleRelease
gradle bundleRelease
```

Outputs:

- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

For a production release, configure signing in Android Studio or add a secure signing configuration to `app/build.gradle.kts`. Do not commit keystores or passwords.

## Build from an Android phone using the cloud

AndroidIDE is archived and is no longer maintained. The included `.github/workflows/android-build.yml` lets GitHub build the project remotely:

1. Create a private GitHub repository from your phone.
2. Upload the extracted `kotlin-python-ide` folder, including the `.github` folder.
3. Open the repository's **Actions** tab.
4. Select **Build Android APK and AAB**.
5. Tap **Run workflow**.
6. When it finishes, open the workflow run and download the `kotlin-python-ide-builds` artifact.

The cloud build produces a debug APK for direct phone testing and an unsigned release AAB. A Play Store upload requires a signed AAB, which should be configured with GitHub encrypted secrets or a mobile CI provider.