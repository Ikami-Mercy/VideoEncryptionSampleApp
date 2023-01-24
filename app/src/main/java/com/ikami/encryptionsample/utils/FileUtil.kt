package com.ikami.encryptionsample.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.*


class FileUtil {


    open fun saveFile(filename: String?, content: ByteArray?, isEncryption: Boolean = false): String? {

        Log.e("Save file is called!!!!! ======", "TRUE")

        var storageDirectory = if(isEncryption){
            DOCUMENT_ENCRYPTION_DIRECTORY
        } else{
            DOCUMENT_DECRYPTION_DIRECTORY

        }

        val wallpaperDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            storageDirectory
        )

        return try {
            Log.e("Writing file in the local storage called!!!!!", "");

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

    open fun saveFileInternally(context: Context, filename: String, content: ByteArray,directoryName: String){
        Log.e(this::class.java.simpleName, "saveFileInternally called")
        val videoFileDirectory = context.getDir(directoryName, Context.MODE_PRIVATE)

        try {

            val fileObject = File(videoFileDirectory, filename)
            fileObject.createNewFile()


            fileObject.createNewFile()
            val fo = FileOutputStream(fileObject)
            fo.write(content)
            fo.close()
            fileObject.absolutePath

            Log.e(this::class.java.simpleName, "saveFileInternally successful")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(this::class.java.simpleName, "saveFileInternally: $e")
        }
    }

    companion object {


        const val DOCUMENT_DECRYPTION_DIRECTORY = "/POC/DecryptionFiles"
        const val DOCUMENT_ENCRYPTION_DIRECTORY = "/POC/EncryptionFiles"

    }
}