package com.example.playinstallerdebug

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var textDebug: TextView
    private lateinit var buttonClose: Button

    private var apkPath: String? = null
    private var installedPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textDebug = findViewById(R.id.textDebug)
        buttonClose = findViewById(R.id.buttonClose)

        buttonClose.setOnClickListener { finish() }

        apkPath = intent?.data?.let { getRealPathFromURI(it) }

        if (apkPath == null) {
            log("Error: No APK file received.")
            return
        }

        log("APK received:\n$apkPath")
        confirmInstall(apkPath!!)
    }

    private fun confirmInstall(path: String) {
        val file = File(path)
        if (!file.exists()) {
            log("Error: File not found.")
            return
        }

        val packageInfo = packageManager.getPackageArchiveInfo(path, 0)

        packageInfo?.applicationInfo?.let { appInfo ->
            appInfo.sourceDir = path
            appInfo.publicSourceDir = path

            val label = packageManager.getApplicationLabel(appInfo).toString()
            val packageName = packageInfo.packageName

            installedPackage = packageName

            AlertDialog.Builder(this)
                .setTitle("Install $label?")
                .setMessage("Do you want to install this app?\n\nApp: $label\nPackage: $packageName")
                .setPositiveButton("Yes") { _, _ -> installApk(path) }
                .setNegativeButton("No") { _, _ -> finish() }
                .setCancelable(false)
                .show()

        } ?: run {
            log("Error: Failed to read APK information.")
        }
    }

    private fun installApk(path: String) {
        Thread {
            log("Copying APK to /data/local/tmp...")
            val copyResult = runCommand("cp \"$path\" \"/data/local/tmp/temp_install.apk\"")
            log(copyResult)

            log("Installing APK...")
            val installResult = runCommand("pm install -i \"com.android.vending\" -r \"/data/local/tmp/temp_install.apk\"")
            log(installResult)

            runCommand("rm \"/data/local/tmp/temp_install.apk\"")

            if (installResult.contains("Success", true)) {
                log("Installation successful.")
                deleteCacheFile(path)
                runOnUiThread { askToOpenApp() }
            } else {
                log("Installation failed.")
            }
        }.start()
    }

    private fun askToOpenApp() {
        AlertDialog.Builder(this)
            .setTitle("Open Installed App?")
            .setMessage("Do you want to launch the installed app now?")
            .setPositiveButton("Yes") { _, _ -> launchApp() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun launchApp() {
        if (installedPackage == null) {
            log("Error: No installed package to launch.")
            return
        }

        log("Launching app: $installedPackage")

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.setPackage(installedPackage)

        val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)

        if (resolveInfos.isNotEmpty()) {
            val launchable: ResolveInfo = resolveInfos[0]
            val activity: ActivityInfo = launchable.activityInfo
            val name = ComponentName(activity.applicationInfo.packageName, activity.name)

            val launchIntent = Intent(Intent.ACTION_MAIN)
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            launchIntent.component = name

            log("Launching: ${launchIntent.component}")
            startActivity(launchIntent)
        } else {
            log("Error: Cannot find launcher activity for $installedPackage")
        }
    }

    private fun deleteCacheFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
                log("Deleted cached APK: $path")
            }
        } catch (e: Exception) {
            log("Error deleting cached APK: ${e.message}")
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        log("Full URI: $uri")
        log("URI Scheme: ${uri.scheme}")
        log("URI Authority: ${uri.authority}")
        log("URI Path: ${uri.path}")

        // Always copy to internal storage to avoid permissions issue
        return contentResolver.openInputStream(uri)?.use { input ->
            val temp = File(cacheDir, "temp_install.apk")
            temp.outputStream().use { output -> input.copyTo(output) }
            log("Copied content URI to: ${temp.absolutePath}")
            temp.absolutePath
        } ?: run {
            log("Error: Failed to open input stream from content URI.")
            null
        }
    }

    private fun runCommand(cmd: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val out = process.inputStream.bufferedReader().readText()
            val err = process.errorStream.bufferedReader().readText()
            process.waitFor()
            if (out.isNotBlank()) out else err
        } catch (e: Exception) {
            "Command error: ${e.message}"
        }
    }

    private fun log(msg: String) {
        runOnUiThread { textDebug.append("$msg\n\n") }
    }
}
