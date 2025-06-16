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

## 🔌 Alternative: Wireless ADB Without Root

If you don't have root, you can still achieve similar results by running ADB commands directly from your phone using Termux.

### Steps:

1. **Install Termux:**

   ```bash
   pkg install android-tools
   ```

2. **Enable Wireless Debugging:**

   * Go to **Developer Options**.
   * Enable **USB Debugging** and **Wireless Debugging**.

3. **Pair the Device:**

   * In Wireless Debugging, select **Pair device with pairing code**.
   * Note the IP and port.
   * Run in Termux:

     ```bash
     adb pair localhost:PORT
     ```
   * Enter the pairing code when prompted.

4. **Connect via ADB:**

   * After pairing, note the port listed under **Wireless Debugging** main screen.
   * Run in Termux:

     ```bash
     adb connect localhost:PORT
     ```

5. **Run the Install Command:**

   ```bash
   adb shell pm install -i "com.android.vending" -r /path/to/apk
   ```

### Advantages:

* No root required.
* Can run directly from the phone without a PC.
* Great for making quick changes without reconnecting to a computer.

This method was shared by the community and works reliably for ADB operations over Wi-Fi directly from the phone.

## 🛠️ Alternative: ADB from PC (Easiest Method)

If you have access to a PC, you can directly run ADB commands to install APKs with the Play Store installer signature.

### Steps:

1. **Enable USB Debugging:**

   * Go to **Developer Options** on your phone.
   * Enable **USB Debugging**.

2. **Connect Phone to PC:**

   * Use a USB cable to connect your device.

3. **Verify ADB Connection:**

   * On your PC, run:

     ```bash
     adb devices
     ```
   * Allow the USB Debugging prompt on your phone.

4. **Run the Install Command:**

   ```bash
   adb shell pm install -i "com.android.vending" -r /path/to/apk
   ```

### Advantages:

* Fastest and most reliable method.
* No need to pair devices or set up wireless debugging.
* Minimal setup required.
