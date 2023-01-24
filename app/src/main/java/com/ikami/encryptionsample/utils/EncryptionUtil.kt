package com.ikami.encryptionsample.utils

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionUtil {
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val iv: GCMParameterSpec = generateIV(Constants.IV.toByteArray())
    private val ALGORITHM = "AES"


    open fun encryptVideo(inputStream: InputStream, key: String, videoFileCount: Int) {
        val util = FileUtil()
        Log.e("encryptVideo is called ===>>", "")

        val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
            val inputBytes = ByteArray(inputStream.available())
            inputStream.read(inputBytes)
            val outputBytes = cipher.doFinal(inputBytes)
            util.saveFile("encryptedVideoFile$videoFileCount.txt", outputBytes, true)
            inputStream.close()
        } catch (ex: NoSuchPaddingException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: NoSuchAlgorithmException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: InvalidKeyException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: BadPaddingException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: IllegalBlockSizeException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: IOException) {
            throw Exception("Error encrypting/decrypting file", ex)
        }

    }

    fun decryptVideo(inputStream: InputStream, key: String, decryptedVideoFileCount: Int) {
        val util = FileUtil()
        Log.e("decryptVideo is called ===>>", "")
        val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)

            val inputBytes = ByteArray(inputStream.available())
            inputStream.read(inputBytes)
            val outputBytes = cipher.doFinal(inputBytes)

            util.saveFile("decryptedVideoFile$decryptedVideoFileCount.mp4", outputBytes)
            inputStream.close()
        } catch (ex: NoSuchPaddingException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: NoSuchAlgorithmException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: InvalidKeyException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: BadPaddingException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: IllegalBlockSizeException) {
            throw Exception("Error encrypting/decrypting file", ex)
        } catch (ex: IOException) {
            throw Exception("Error encrypting/decrypting file", ex)
        }
    }

    fun generateIV(ivByte: ByteArray): GCMParameterSpec {
        return GCMParameterSpec(128, ivByte)
    }



}