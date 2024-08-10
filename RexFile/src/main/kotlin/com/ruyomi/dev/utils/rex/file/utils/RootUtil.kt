package com.ruyomi.dev.utils.rex.file.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.util.concurrent.locks.ReentrantLock

internal object RootUtil {

    private var process: Process? = null
    private var outputStream: DataOutputStream? = null
    private var inputStream: BufferedReader? = null

    private val lock = ReentrantLock()

    private fun newProcess(useRoot: Boolean = true) = try {
        lock.lockInterruptibly()
        process = Runtime.getRuntime().exec(if (useRoot) "su" else "sh")
        outputStream = DataOutputStream(process?.outputStream)
        inputStream = BufferedReader(InputStreamReader(process?.inputStream))
    } catch (_: Exception) {
    } finally {
        lock.unlock()
    }

    fun exit() = try {
        outputStream?.close()
        inputStream?.close()
        process?.destroy()
    } catch (_: Exception) {
    }

    fun hasPermission() = try {
        Runtime.getRuntime().exec("su -c exit").waitFor() == 0
    } catch (_: Exception) {
        false
    }

    fun requestPermission() = executeCommand("").first == 0

    fun executeCommand(command: String, useRoot: Boolean = true) = try {
        if (process == null) newProcess(useRoot)

        lock.lockInterruptibly()

        outputStream?.apply {
            writeBytes(command)
            writeBytes("\necho \"|<<exitCode:\$?|\"")
            writeBytes("\necho \"|<<exitWait|\"\n")
            flush()
        }

        val output = StringBuilder()
        var exitCode = -1
        while (true) {
            val line = inputStream?.readLine()
            if (line != null && line.startsWith("|<<exitCode:")) {
                exitCode = line.run {
                    substring(
                        startIndex = "exitCode:".let { indexOf(it) + it.length },
                        endIndex = lastIndexOf("|")
                    )
                }.toIntOrNull() ?: -1
            }
            if (line == null || line == "|<<exitWait|") break
            output.append(line).append("\n")
        }

        Pair(exitCode, output.toString())
    } catch (e: Exception) {
        Pair(-1, e.toString())
    } finally {
        lock.unlock()
    }
}