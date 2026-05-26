package com.auth.utils

import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

object KeyReader {
    fun getPublicKey(path: String): RSAPublicKey{
        val keyString = File(path).readText()
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(keyString)
        val spec = X509EncodedKeySpec(keyBytes)

        return KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
    }

    fun getPrivateKey(path: String): RSAPrivateKey {
        val keyString = File(path).readText()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(keyString)
        val spec = PKCS8EncodedKeySpec(keyBytes)

        return KeyFactory.getInstance("RSA").generatePrivate(spec) as RSAPrivateKey
    }
}