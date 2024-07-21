package com.ruyomi.dev.utils.rexfile.shell

import java.io.DataOutputStream
import java.io.InputStreamReader

object RootUtil {

    fun hasPermission(): Boolean {
        return try {
            Runtime.getRuntime().exec("su -c exit").waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun requestPermission(): Boolean {
        return executeCommand("").first == 0
    }

    fun executeCommand(command: String, useRoot: Boolean = true): Pair<Int, String> {
        return try {
            val process = Runtime.getRuntime().exec(if (useRoot) "su" else "sh")

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