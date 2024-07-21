package com.ruyomi.dev.utils.rexfile.file.impl

import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.ruyomi.dev.utils.rexfile.file.RexFile
import com.ruyomi.dev.utils.rexfile.file.RexFileConfig
import com.ruyomi.dev.utils.rexfile.file.documentAbsolutePath
import com.ruyomi.dev.utils.rexfile.file.documentPathToUri
import java.io.File

class DocFile : RexFile {

    private val context = RexFileConfig.instance.context
    internal var docFile: DocumentFile? = DocumentFile.fromTreeUri(context, path.documentPathToUri())

    constructor(path: String) : super(path)
    constructor(file: RexFile, child: String) : super(file, child)

    override fun createNewFile(): Boolean {
        if (exists()) return true
        val parent = getParentFile()
        if (!parent.exists()) return false
        return (parent.docFile?.createFile("*/*", name).apply { docFile = this }) != null
    }

    override fun createNewFileAnd(): Boolean {
        if (exists()) return true
        val parent = getParentFile()
        parent.mkdirs()
        if (!parent.exists()) return false
        return (parent.docFile?.createFile("*/*", name).apply { docFile = this }) != null
    }

    override fun canRead(): Boolean = docFile?.canRead() ?: false

    override fun canWrite(): Boolean = docFile?.canWrite() ?: false

    override fun delete(): Boolean = docFile?.delete() ?: false

    override fun deleteAnd(): Boolean = delete()

    override fun exists(): Boolean = docFile?.exists() ?: false

    override fun getAbsolutePath(): String = docFile?.uri?.documentAbsolutePath() ?: ""

    override fun getParent(): String = file.parent ?: ""

    override fun getParentFile(): DocFile = DocFile(getParent())

    override fun isDirectory(): Boolean = docFile?.isDirectory ?: false

    override fun isFile(): Boolean = docFile?.isFile ?: false

    override fun lastModified(): Long = docFile?.lastModified() ?: -1

    override fun length(): Long = docFile?.length() ?: 0

    override fun lengthAnd(): Long = docFile?.listFiles()?.sumOf { it.length() } ?: length()

    override fun list(): Array<String> = docFile?.listFiles()?.mapNotNull {
        "${path}${File.separator}${it.name}"
    }?.toTypedArray() ?: arrayOf()

    override fun list(filter: (String) -> Boolean): Array<String> =
        docFile?.listFiles()?.mapNotNull {
            it.name?.takeIf(filter)?.let { name -> "${path}${File.separator}$name" }
        }?.toTypedArray() ?: arrayOf()

    override fun listFiles(): Array<RexFile> = docFile?.listFiles()?.mapNotNull {
        DocFile(it.uri.documentAbsolutePath())
    }?.toTypedArray() ?: arrayOf()

    override fun listFiles(filter: (RexFile) -> Boolean): Array<RexFile> =
        docFile?.listFiles()?.mapNotNull {
            val newFile = DocFile(it.uri.documentAbsolutePath())
            newFile.takeIf(filter)
        }?.toTypedArray() ?: arrayOf()

    override fun mkdirs(): Boolean {
        if (exists()) return true
        val parent = getParentFile()
        if (!parent.exists()) parent.mkdirs()
        parent.docFile?.createDirectory(name)
        return exists()
    }

    override fun renameTo(dest: String): Boolean = docFile?.renameTo(dest) ?: false

    fun move(target: String) = move(DocFile(target))

    fun move(target: DocFile) = DocumentsContract.moveDocument(
        context.contentResolver,
        docFile?.uri ?: path.documentPathToUri(),
        docFile?.uri ?: path.documentPathToUri(),
        target.docFile?.uri ?: target.path.documentPathToUri()
    ) != null

}