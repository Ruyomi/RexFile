package com.ruyomi.dev.utils.rex.file.impl

import android.os.ParcelFileDescriptor
import com.ruyomi.dev.utils.rex.file.IShizukuFileService

internal class ShizukuFileService : IShizukuFileService.Stub() {
    override fun createNewFile(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).createNewFile()
        } else {
            false
        }
    }

    override fun createNewFileAnd(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).createNewFileAnd()
        } else {
            false
        }
    }

    override fun canRead(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).canRead()
        } else {
            false
        }
    }

    override fun canWrite(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).canWrite()
        } else {
            false
        }
    }

    override fun delete(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).delete()
        } else {
            false
        }
    }

    override fun deleteAnd(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).deleteAnd()
        } else {
            false
        }
    }

    override fun exists(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).exists()
        } else {
            false
        }
    }

    override fun getAbsolutePath(path: String?): String {
        return if (path != null) {
            IoFile(path).getAbsolutePath()
        } else {
            ""
        }
    }

    override fun getParent(path: String?): String {
        return if (path != null) {
            IoFile(path).getParent()
        } else {
            ""
        }
    }

    override fun isDirectory(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).isDirectory()
        } else {
            false
        }
    }

    override fun isFile(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).isFile()
        } else {
            false
        }
    }

    override fun lastModified(path: String?): Long {
        return if (path != null) {
            IoFile(path).lastModified()
        } else {
            0L
        }
    }

    override fun length(path: String?): Long {
        return if (path != null) {
            IoFile(path).length()
        } else {
            0L
        }
    }

    override fun lengthAnd(path: String?): Long {
        return if (path != null) {
            IoFile(path).lengthAnd()
        } else {
            0L
        }
    }

    override fun list(path: String?): MutableList<String> {
        return if (path != null) {
            IoFile(path).list().map {
                IoFile(it).name
            }.toMutableList()
        } else {
            mutableListOf()
        }
    }

    override fun mkdirs(path: String?): Boolean {
        return if (path != null) {
            IoFile(path).mkdirs()
        } else {
            false
        }
    }

    override fun renameTo(path: String?, dest: String?): Boolean {
        return if (path != null && dest != null) {
            IoFile(path).renameTo(dest)
        } else {
            false
        }
    }

    override fun getParcelFileDescriptor(path: String?): ParcelFileDescriptor {
        if (path == null) {
            throw Exception("getParcelFileDescriptor path is not be null")
        }

        return ParcelFileDescriptor.open(
            IoFile(path).file,
            ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_READ_WRITE
        )
    }

}