package com.ikami.encryptionsample.utils

import android.util.Base64
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.SecureRandom
import java.util.Base64.getEncoder
import java.util.Base64.getMimeEncoder
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

class EncryptionUtil {

    open fun encryptFile(encryptionKey: String, base64Video: ByteArray){
        Log.e("encryptFile key is  ======", "$encryptionKey")
        val util = FileUtil()
         try {

            val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val cipherText = cipher.doFinal(base64Video)
            var encryptedVideoString = Base64.encode(cipherText,Base64.NO_PADDING)
            util.saveFile("encryptedBase64VideoFile.txt", encryptedVideoString, true)

        } catch (e: java.security.NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: javax.crypto.NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: java.security.InvalidKeyException) {
            e.printStackTrace()
        } catch (e: javax.crypto.BadPaddingException) {
            e.printStackTrace()
        } catch (e: javax.crypto.IllegalBlockSizeException) {
            e.printStackTrace()
        }
    }

    open fun decryptFile(encryptionKey: String, encryptedBase64Video: String): String {

        println("decryptFile called ======")
        var decryptedVideoString = ""
        try {

            val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            val decodedValue = Base64.decode(encryptedBase64Video.toByteArray(), Base64.NO_PADDING)
            val values = cipher.doFinal(decodedValue)
             decryptedVideoString = String(values)

        } catch (e: java.security.NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: javax.crypto.NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: java.security.InvalidKeyException) {
            e.printStackTrace()
        } catch (e: javax.crypto.BadPaddingException) {
            e.printStackTrace()
        } catch (e: javax.crypto.IllegalBlockSizeException) {
            e.printStackTrace()
        }
        println("decryptedVideoString is = ${decryptedVideoString.toCharArray().size}")
        return decryptedVideoString

    }



    open fun encryptVideo(inputStream: InputStream, key: String) {
        val util = FileUtil()
        Log.e("encryptVideo is called ===>>", "")

        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val inputBytes = ByteArray(inputStream.available())
        inputStream.read(inputBytes)
        val outputBytes = cipher.doFinal(inputBytes)
        util.saveFile("encryptedVideoFile.txt",outputBytes, true)
        inputStream.close()

    }

    fun decryptVideo(inputStream: InputStream, key: String) {
        val util = FileUtil()
        Log.e("decryptVideo is called ===>>", "")
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        val inputBytes = ByteArray(inputStream.available())
        inputStream.read(inputBytes)
        val outputBytes = cipher.doFinal(inputBytes)

        util.saveFile("decryptedVideoFile.mp4",outputBytes)
        inputStream.close()
    }
}