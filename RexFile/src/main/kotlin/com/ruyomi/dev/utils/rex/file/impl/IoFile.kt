package com.ruyomi.dev.utils.rex.file.impl

import com.ruyomi.dev.utils.rex.file.RexFile
import java.io.File

class IoFile : RexFile {

    constructor(path: String) : super(path)
    constructor(file: RexFile, child: String) : super(file, child)

    override fun createNewFile(): Boolean = file.createNewFile()

    override fun createNewFileAnd(): Boolean {
        getParentFile().mkdirs()
        return createNewFile()
    }

    override fun canRead(): Boolean = file.canRead()

    override fun canWrite(): Boolean = file.canWrite()

    override fun delete(): Boolean = file.delete()

    override fun deleteAnd(): Boolean = file.deleteRecursively()

    override fun exists(): Boolean = file.exists()

    override fun getAbsolutePath(): String = file.absolutePath

    override fun getParent(): String = file.parent ?: ""

    override fun getParentFile(): IoFile = IoFile(getParent())

    override fun isDirectory(): Boolean = file.isDirectory

    override fun isFile(): Boolean = file.isFile

    override fun lastModified(): Long = file.lastModified()

    override fun length(): Long = file.length()

    override fun lengthAnd(): Long = file.listFiles()?.sumOf { it.length() } ?: length()

    override fun list(): Array<String> = file.list()?.mapNotNull {
        "${path}${File.separator}$it"
    }?.toTypedArray() ?: arrayOf()

    override fun list(filter: (String) -> Boolean): Array<String> =
        file.list { _, name -> filter(name) }?.mapNotNull {
            "${path}${File.separator}$it"
        }?.toTypedArray() ?: arrayOf()

    override fun listFiles(): Array<RexFile> = file.listFiles()?.mapNotNull {
        IoFile(it.path)
    }?.toTypedArray() ?: arrayOf()

    override fun listFiles(filter: (RexFile) -> Boolean): Array<RexFile> =
        file.listFiles { file -> filter(IoFile(file.absolutePath)) }?.mapNotNull {
            IoFile(it.path)
        }?.toTypedArray() ?: arrayOf()

    override fun mkdirs(): Boolean = file.mkdirs()

    override fun renameTo(dest: String): Boolean = file.renameTo(File(dest))
}
