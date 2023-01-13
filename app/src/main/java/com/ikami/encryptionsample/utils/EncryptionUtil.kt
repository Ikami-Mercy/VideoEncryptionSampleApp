package com.ikami.encryptionsample.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

class EncryptionUtil {

    open fun encryptFile(encryptionKey: String, base64Video: String): String {
        var encryptedVideoString: String=""
         try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            val secureRandom = SecureRandom()
            val keyBitSize = 256
            keyGenerator.init(keyBitSize, secureRandom)
            val secretKey = keyGenerator.generateKey()

            val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val cipherText = cipher.doFinal(base64Video.toByteArray())
            encryptedVideoString = Base64.encodeToString(cipherText, Base64.DEFAULT)
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
        return encryptedVideoString
    }
}