# Play Installer

**Install APKs as if they were installed from the Play Store.**

## 📱 Overview

**Play Installer** is a root-required Android app that installs APKs using the Play Store's installer signature. This can help with scenarios where certain apps (like accessibility services) don't function correctly if installed externally.

## ⚖️ Motivation

I was using the `BetterKnownInstalled` module but faced issues enabling accessibility for externally installed apps. This app solves that by installing APKs with the Play Store as the installer, making the system treat them as if they were installed directly from Google Play.

## ⚙️ How It Works

The app runs the following root command:

```bash
pm install -i "com.android.vending" -r /path/to/apk
```

This sets the installer to `com.android.vending` (the Play Store).

## 🚀 Usage Instructions

1. **Root Required:** Make sure your device is rooted and grant superuser permissions to the app.
2. **Recommended Setup:**

   * Install the debug version of Play Installer.
   * Use it to install the release (main) version of the app.
   * Uninstall the debug version if you wish.
3. **How to Use:**

   * Open any APK file with Play Installer.
   * The installation will proceed with the correct installer signature.

## ⚡ Notes

* Superuser (root) permission is mandatory.
* The app simply provides a front-end for the `pm install` command with a custom installer argument.
* No launcher icon if you’ve excluded it in your build.
