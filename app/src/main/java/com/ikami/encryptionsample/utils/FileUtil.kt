package com.ikami.encryptionsample.utils

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


    companion object {


        const val DOCUMENT_DECRYPTION_DIRECTORY = "/POC/DecryptionFiles"
        const val DOCUMENT_ENCRYPTION_DIRECTORY = "/POC/EncryptionFiles"

    }
}