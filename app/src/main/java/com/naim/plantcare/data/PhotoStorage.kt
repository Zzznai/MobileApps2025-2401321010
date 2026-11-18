package com.naim.plantcare.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

interface PhotoFiles {
    fun newPhotoFile(): File
    fun copyFrom(source: Uri): File?
    fun delete(path: String?)
}

class PhotoStorage(context: Context) : PhotoFiles {
    private val appContext = context.applicationContext

    private val directory: File
        get() = File(appContext.filesDir, DIRECTORY).apply { mkdirs() }

    override fun newPhotoFile(): File = File(directory, "plant_${UUID.randomUUID()}.jpg")

    fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)

    override fun copyFrom(source: Uri): File? {
        val target = newPhotoFile()
        val input = appContext.contentResolver.openInputStream(source) ?: return null
        input.use { stream -> target.outputStream().use { stream.copyTo(it) } }
        return target
    }

    override fun delete(path: String?) {
        val file = path?.let(::File) ?: return
        if (file.parentFile == directory) file.delete()
    }

    fun deleteUnused(usedPaths: Set<String>) {
        directory.listFiles()?.filter { it.absolutePath !in usedPaths }?.forEach { it.delete() }
    }

    companion object {
        const val DIRECTORY = "photos"
    }
}
