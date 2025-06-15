    package com.seeker.playinstaller

    import android.app.AlertDialog
    import android.content.ComponentName
    import android.content.Intent
    import android.content.pm.ActivityInfo
    import android.content.pm.PackageManager
    import android.content.pm.ResolveInfo
    import android.net.Uri
    import android.os.Bundle
    import androidx.appcompat.app.AppCompatActivity
    import java.io.File

    class MainActivity : AppCompatActivity() {

        private var apkPath: String? = null
        private var installedPackage: String? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            apkPath = intent?.data?.let { getRealPathFromURI(it) }

            if (apkPath == null) {
                showMessage("Error: No APK file received.")
                finish()
                return
            }

            confirmInstall(apkPath!!)
        }

        private fun confirmInstall(path: String) {
            val file = File(path)
            if (!file.exists()) {
                showMessage("Error: File not found.")
                finish()
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
                    .setMessage("Do you want to install this app?")
                    .setPositiveButton("Yes") { _, _ -> installApk(path) }
                    .setNegativeButton("No") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()

            } ?: run {
                showMessage("Error: Failed to read APK information.")
                finish()
            }
        }

        private fun installApk(path: String) {
            Thread {
                runCommand("cp \"$path\" \"/data/local/tmp/temp_install.apk\"")
                val installResult = runCommand("pm install -i \"com.android.vending\" -r \"/data/local/tmp/temp_install.apk\"")
                runCommand("rm \"/data/local/tmp/temp_install.apk\"")

                runOnUiThread {
                    if (installResult.contains("Success", true)) {
                        showInstalledPopup()
                    } else {
                        showMessage("Installation failed.")
                        finish()
                    }
                }
            }.start()
        }

        private fun showInstalledPopup() {
            AlertDialog.Builder(this)
                .setTitle("App Installed")
                .setMessage("The app was installed successfully.")
                .setPositiveButton("Open") { _, _ -> launchApp() }
                .setNegativeButton("Done") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }

        private fun launchApp() {
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

                startActivity(launchIntent)
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

        private fun getRealPathFromURI(uri: Uri): String? {
            return contentResolver.openInputStream(uri)?.use { input ->
                val temp = File(cacheDir, "temp_install.apk")
                temp.outputStream().use { output -> input.copyTo(output) }
                temp.absolutePath
            }
        }

        private fun showMessage(message: String) {
            AlertDialog.Builder(this)
                .setTitle("Play Installer")
                .setMessage(message)
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }
