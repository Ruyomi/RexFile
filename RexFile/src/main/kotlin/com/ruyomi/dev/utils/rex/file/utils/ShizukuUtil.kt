package com.ruyomi.dev.utils.rex.file.utils

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.ruyomi.dev.utils.rex.file.BuildConfig
import com.ruyomi.dev.utils.rex.file.IShizukuFileService
import com.ruyomi.dev.utils.rex.file.RexFileConfig
import com.ruyomi.dev.utils.rex.file.impl.ShizukuFileService
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener

internal object ShizukuUtil {

    private lateinit var requestPermissionResultListener: OnRequestPermissionResultListener
    private lateinit var iShizukuFileService: IShizukuFileService

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            iShizukuFileService = IShizukuFileService.Stub.asInterface(iBinder)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }

    private val userServiceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(
                RexFileConfig.instance.context.packageName,
                ShizukuFileService::class.java.name
            )
        )
            .daemon(false)
            .debuggable(BuildConfig.DEBUG)
            .processNameSuffix("file_explorer_service")
            .version(1)
    }

    fun bindService(): Boolean {
        return try {
            if (Shizuku.getVersion() < 10) {
                throw Exception("requires Shizuku API 10.")
            }
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun unbindService(): Boolean {
        return try {
            if (Shizuku.getVersion() < 10) {
                throw Exception("requires Shizuku API 10.")
            }
            Shizuku.unbindUserService(userServiceArgs, serviceConnection, true)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun peekService(): Boolean {
        return try {
            Shizuku.getVersion() > 11 && Shizuku.peekUserService(
                userServiceArgs,
                serviceConnection
            ) != -1
        } catch (_: Exception) {
            false
        }
    }

    fun addRequestPermissionResultListener(
        onRequestPermissionResultListener: OnRequestPermissionResultListener
    ): Boolean {
        return try {
            requestPermissionResultListener = onRequestPermissionResultListener
            Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun removeRequestPermissionResultListener(): Boolean {
        return try {
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean =
        try {
            if (Shizuku.isPreV11()) false else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }

    fun requestPermission(requestCode: Int): Boolean {
        return try {
            if (hasPermission()) {
                return true
            }
            Shizuku.requestPermission(requestCode)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getShizukuFileService(): IShizukuFileService {
        if (!ShizukuUtil::iShizukuFileService.isInitialized || !peekService()) {
            bindService()
        }
        return iShizukuFileService
    }
}