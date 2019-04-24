package com.young.lineloginsample

import android.annotation.SuppressLint
import android.util.Log
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/*
 * Use SHA256, cryptographic hash function to on-way key
 * Use AES256, block cipher
 *
 */
class TokenUtil {

    // lazy instance
    companion object {

        // make encryption SHA256 key with AES256, 16 byte
        fun encrypt (plain: String, key: String): String? {
            try {
                val iv = ByteArray(16)
                SecureRandom().nextBytes(iv)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(charset("utf-8")), "AES"), IvParameterSpec(iv))
                val cipherText = cipher.doFinal(plain.toByteArray(charset("utf-8")))
                val ivAndCipherText = getCombinedArray(iv, cipherText)
                return String(Base64.encodeBase64(ivAndCipherText))
            } catch (e: Exception) {
                Log.e(">>>>>> ERROR >>>>>>",e.message)
                return null
            }

        }

        // decrypt token with SHA256 key
        fun decrypt (encoded: ByteArray, key: String): String? {
            try {
                val ivAndCipherText = Base64.decodeBase64(encoded)
                val iv = Arrays.copyOfRange(ivAndCipherText, 0, 16)
                val cipherText = Arrays.copyOfRange(ivAndCipherText, 16, ivAndCipherText.size)

                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES"), IvParameterSpec(iv))
                return String(cipher.doFinal(cipherText), charset("UTF-8"))
            } catch (e: Exception) {
                Log.e(">>>>>> ERROR >>>>>>",e.message)
                return null
            }

        }

        @SuppressLint("LongLogTag")
        // make MD5
        fun getCSRF(targetText: String): String {
            // base64 encoding
            val bytes = Base64.encodeBase64(targetText.toByteArray(charset("UTF-8")))
            // make digest
            val messageDigest = MessageDigest.getInstance("MD5")
//            DigestUtils.md5Hex(bytes)
            // slice to 16 byte
            val digest = messageDigest.digest(bytes)
            Log.d(">>>>> MD5 hash key size >>>>> ", ""+"%02x".format(digest.size))
            return digest.fold("", { dummyText, it -> dummyText+"%02x".format(it) })
//            return DigestUtils.md5Hex(targetText)
        }

        @SuppressLint("LongLogTag")
        // make SHA256 hash key
        fun hasher (targetText: String): String {
            // base64 encoding
            val bytes = Base64.encodeBase64(targetText.toByteArray(charset("UTF-8")))
            // make digest
            val messageDigest = MessageDigest.getInstance("SHA256")
            // slice to 16 byte
            val digest = Arrays.copyOfRange(messageDigest.digest(bytes), 0, 16)
            Log.d(">>>>> SHA256 hash key size >>>>> ", ""+"%02x".format(digest.size))
            return digest.fold("", { dummyText, it -> dummyText+"%02x".format(it) })
        }

        // verify received token with SHA256 key
        fun verifyToken (token: String, key:String, kidName:String): Boolean {
            val cecryptedkidName = decrypt(token.toByteArray(charset("utf-8")), key)
            if (cecryptedkidName == kidName) return true
            return false
        }

        // make initial vector 16 byte
        private fun getCombinedArray(one: ByteArray, two: ByteArray): ByteArray {
            val combined = ByteArray(one.size + two.size)
            for (i in combined.indices) {
                combined[i] = if (i < one.size) one[i] else two[i - one.size]
            }
            return combined
        }
    }
}