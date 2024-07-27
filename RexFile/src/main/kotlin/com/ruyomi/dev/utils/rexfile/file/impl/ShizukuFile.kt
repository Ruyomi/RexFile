package com.ruyomi.dev.utils.rexfile.file.impl

import com.ruyomi.dev.utils.rexfile.file.RexFile
import com.ruyomi.dev.utils.rexfile.shell.ShizukuUtil
import java.io.File

class ShizukuFile : RexFile {

    constructor(path: String) : super(path)
    constructor(file: RexFile, child: String) : super(file, child)

    override fun createNewFile(): Boolean = ShizukuUtil.getShizukuFileService().createNewFile(path)

    override fun createNewFileAnd(): Boolean {
        getParentFile().mkdirs()
        return createNewFile()
    }

    override fun canRead(): Boolean = ShizukuUtil.getShizukuFileService().canRead(path)

    override fun canWrite(): Boolean = ShizukuUtil.getShizukuFileService().canWrite(path)

    override fun delete(): Boolean = ShizukuUtil.getShizukuFileService().delete(path)

    override fun deleteAnd(): Boolean = ShizukuUtil.getShizukuFileService().deleteAnd(path)

    override fun exists(): Boolean = ShizukuUtil.getShizukuFileService().exists(path)

    override fun getAbsolutePath(): String =
        ShizukuUtil.getShizukuFileService().getAbsolutePath(path)

    override fun getParent(): String = ShizukuUtil.getShizukuFileService().getParent(path)

    override fun getParentFile(): ShizukuFile = ShizukuFile(getParent())

    override fun isDirectory(): Boolean = ShizukuUtil.getShizukuFileService().isDirectory(path)

    override fun isFile(): Boolean = ShizukuUtil.getShizukuFileService().isFile(path)

    override fun lastModified(): Long = ShizukuUtil.getShizukuFileService().lastModified(path)

    override fun length(): Long = ShizukuUtil.getShizukuFileService().length(path)

    override fun lengthAnd(): Long = ShizukuUtil.getShizukuFileService().lengthAnd(path)

    override fun list(): Array<String> =
        ShizukuUtil.getShizukuFileService().list(path)?.mapNotNull {
            "${path}${File.separator}$it"
    }?.toTypedArray() ?: arrayOf()

    override fun list(filter: (String) -> Boolean): Array<String> =
        ShizukuUtil.getShizukuFileService().list(path)?.mapNotNull {
            it?.takeIf(filter)?.let { name -> "${path}${File.separator}$it" }
        }?.toTypedArray() ?: arrayOf()

    override fun listFiles(): Array<RexFile> = ShizukuUtil.getShizukuFileService().list(path)?.map {
        ShizukuFile("${path}${File.separator}$it")
    }?.toTypedArray() ?: arrayOf()

    override fun listFiles(filter: (RexFile) -> Boolean): Array<RexFile> =
        ShizukuUtil.getShizukuFileService().list(path)?.map {
            ShizukuFile("${path}${File.separator}$it").apply {
                takeIf(filter)
            }
        }?.toTypedArray() ?: arrayOf()

    override fun mkdirs(): Boolean = ShizukuUtil.getShizukuFileService().mkdirs(path)

    override fun renameTo(dest: String): Boolean =
        ShizukuUtil.getShizukuFileService().renameTo(path, dest)

}