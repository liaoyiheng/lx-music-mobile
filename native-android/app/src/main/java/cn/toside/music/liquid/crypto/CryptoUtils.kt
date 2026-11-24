package cn.toside.music.liquid.crypto

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(input.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(input.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun btoa(latin1: String): String {
        val bytes = latin1.toByteArray(StandardCharsets.ISO_8859_1)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun atob(b64: String): String {
        val bytes = Base64.decode(b64, Base64.DEFAULT)
        return String(bytes, StandardCharsets.ISO_8859_1)
    }

    fun aesEncrypt(dataB64: String, keyB64: String, ivB64: String, mode: String): String {
        val data = Base64.decode(dataB64, Base64.DEFAULT)
        val key = Base64.decode(keyB64, Base64.DEFAULT)
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance(resolveAesTransformation(mode))
        if (ivB64.isNotEmpty() && !mode.equals("AES", ignoreCase = true) && !mode.contains("ECB", ignoreCase = true)) {
            val iv = Base64.decode(ivB64, Base64.DEFAULT)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        }
        return Base64.encodeToString(cipher.doFinal(data), Base64.NO_WRAP)
    }

    fun aesDecrypt(dataB64: String, keyB64: String, ivB64: String, mode: String): String {
        val data = Base64.decode(dataB64, Base64.DEFAULT)
        val key = Base64.decode(keyB64, Base64.DEFAULT)
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance(resolveAesTransformation(mode))
        if (ivB64.isNotEmpty() && !mode.equals("AES", ignoreCase = true) && !mode.contains("ECB", ignoreCase = true)) {
            val iv = Base64.decode(ivB64, Base64.DEFAULT)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
        }
        return Base64.encodeToString(cipher.doFinal(data), Base64.NO_WRAP)
    }

    fun aesEncryptUtf8(plainText: String, keyB64: String, ivB64: String = "", mode: String = "AES/ECB/PKCS7Padding"): String {
        val dataB64 = Base64.encodeToString(plainText.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        return aesEncrypt(dataB64, keyB64, ivB64, mode)
    }

    fun aesDecryptUtf8(cipherB64: String, keyB64: String, ivB64: String = "", mode: String = "AES/ECB/PKCS7Padding"): String {
        val outB64 = aesDecrypt(cipherB64, keyB64, ivB64, mode)
        return String(Base64.decode(outB64, Base64.DEFAULT), StandardCharsets.UTF_8)
    }

    fun rsaEncrypt(dataB64: String, publicKeyPemOrB64: String, padding: String): String {
        val cleanKey = stripPem(publicKeyPemOrB64)
        val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
        val pubKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
        val cipher = Cipher.getInstance(resolveRsaTransformation(padding))
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        val input = Base64.decode(dataB64, Base64.DEFAULT)
        return Base64.encodeToString(cipher.doFinal(input), Base64.NO_WRAP)
    }

    fun rsaDecrypt(dataB64: String, privateKeyPemOrB64: String, padding: String): String {
        val cleanKey = stripPem(privateKeyPemOrB64)
        val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
        val privKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
        val cipher = Cipher.getInstance(resolveRsaTransformation(padding))
        cipher.init(Cipher.DECRYPT_MODE, privKey)
        val input = Base64.decode(dataB64, Base64.DEFAULT)
        return Base64.encodeToString(cipher.doFinal(input), Base64.NO_WRAP)
    }

    fun generateRsaKeyPair(): Pair<String, String> {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val pair = kpg.generateKeyPair()
        val pub = Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP)
        val priv = Base64.encodeToString(pair.private.encoded, Base64.NO_WRAP)
        return pub to priv
    }

    private fun stripPem(key: String): String = key
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace("\\s+".toRegex(), "")

    private fun resolveAesTransformation(mode: String): String = when (mode) {
        "AES", "AES/ECB/PKCS5Padding", "aes-128-ecb" -> "AES/ECB/PKCS5Padding"
        "AES/ECB/PKCS7Padding" -> "AES/ECB/PKCS5Padding"
        "AES/CBC/PKCS7Padding", "aes-128-cbc" -> "AES/CBC/PKCS5Padding"
        else -> mode.ifBlank { "AES/CBC/PKCS5Padding" }
    }

    private fun resolveRsaTransformation(padding: String): String = when (padding) {
        "RSA/ECB/NoPadding", "NoPadding" -> "RSA/ECB/NoPadding"
        "RSA/ECB/OAEPWithSHA1AndMGF1Padding", "OAEPWithSHA1AndMGF1Padding" -> "RSA/ECB/OAEPWithSHA1AndMGF1Padding"
        "RSA/ECB/PKCS1Padding", "PKCS1Padding" -> "RSA/ECB/PKCS1Padding"
        else -> padding.ifBlank { "RSA/ECB/OAEPWithSHA1AndMGF1Padding" }
    }
}
