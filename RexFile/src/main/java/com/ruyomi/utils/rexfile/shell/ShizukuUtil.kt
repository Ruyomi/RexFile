package com.ruyomi.utils.rexfile.shell

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import java.io.DataOutputStream
import java.io.InputStreamReader

object ShizukuUtil {

    var resultListener = OnRequestPermissionResultListener { _, _ -> hasPermission() }

    fun addResultListener(): Boolean {
        return try {
            Shizuku.addRequestPermissionResultListener(resultListener)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun removeResultListener(): Boolean {
        return try {
            Shizuku.removeRequestPermissionResultListener(resultListener)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11()) false else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    fun requestPermission(requestCode: Int): Boolean {
        return try {
            Shizuku.requestPermission(requestCode)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun executeCommand(command: String): Pair<Int, String> {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh"), null, null)

            val outStream = process.outputStream
            val inStream = process.inputStream
            val errStream = process.errorStream

            val outputStream = DataOutputStream(outStream)
            val inputStream = InputStreamReader(inStream)
            val errorStream = InputStreamReader(errStream)

            outputStream.apply {
                writeBytes("$command\n")
                flush()
                writeBytes("exit\n")
                flush()
            }

            val output = StringBuilder()
            inputStream.forEachLine { output.append(it).append("\n") }
            errorStream.forEachLine { output.append(it).append("\n") }

            val exitValue = process.waitFor()

            errorStream.close()
            inputStream.close()
            outputStream.close()

            errStream.close()
            inStream.close()
            outStream.close()

            Pair(exitValue, output.toString())
        } catch (e: Exception) {
            Pair(-1, e.toString())
        }
    }
}