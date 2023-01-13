package com.ikami.encryptionsample.utils

import android.os.Environment
import java.io.*

class Utils {

    @Throws(IOException::class)
   open fun getBytes(inputStream: InputStream): ByteArray? {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    open fun saveFile(filename: String?, content: ByteArray?): String? {
        println( "Save file is called!!!!!");
        val bytes = ByteArrayOutputStream()
        val wallpaperDirectory= File(Environment.getExternalStorageDirectory() , DOCUMENT_FILE_DIRECTORY)
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }
        return try {
            val f = File(wallpaperDirectory, filename)
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(content)
            fo.close()
            f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
            ""
        }
    }
    
    companion object{


        const val DOCUMENT_FILE_DIRECTORY = "/POC/EncryptionFiles"

    }
}