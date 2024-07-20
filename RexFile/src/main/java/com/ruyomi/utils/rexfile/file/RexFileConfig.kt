package com.ruyomi.utils.rexfile.file

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.ruyomi.utils.rexfile.shell.ShizukuUtil

enum class RexFileModel {
    FILE, DOCUMENT, SHIZUKU, ROOT
}

class RexFileConfig {

    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance = RexFileConfig()
    }

    internal lateinit var context: Context
    var fileModel = RexFileModel.FILE

    fun init(context: Context, fileModel: RexFileModel? = null) {
        this.context = context
        fileModel?.let { this.fileModel = it }
        if (fileModel == RexFileModel.SHIZUKU) ShizukuUtil.addResultListener()
    }

    fun destroy() {
        if (fileModel == RexFileModel.SHIZUKU) ShizukuUtil.removeResultListener()
    }
}