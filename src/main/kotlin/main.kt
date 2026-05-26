package com.auth

import com.auth.utils.DatabaseFactory
import com.auth.utils.KeyReader
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}


fun Application.module(){
    val publicKey = KeyReader.getPublicKey("/app/keys/public.pem")
    val privateKey = KeyReader.getPrivateKey("/app/keys/private.pem")
    val rsaAlgorithm = Algorithm.RSA256(publicKey, privateKey)
    val dbDir = System.getenv("DB_DIR")

    configureSerialization()
    DatabaseFactory.init(dbDir)

    configureRouting(rsaAlgorithm)
}
