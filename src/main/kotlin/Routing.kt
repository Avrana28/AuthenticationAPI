package com.auth

import com.auth.utils.DatabaseFactory
import com.auth.utils.LoginRequest
import com.auth.utils.MessageResponse
import com.auth.utils.RegisterRequest
import com.auth.utils.TokenResponse
import com.auth.utils.Users
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

fun Application.configureRouting(rsaAlgorithm: Algorithm) {
    routing {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()

                if (request.username.isBlank() || request.password.length < 6) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Invalid username or password (The password has to have at list 7 characters)")
                    )
                    return@post
                }

                val userExists = DatabaseFactory.dbQuery {
                    Users.selectAll().where {
                        Users.username eq request.username
                    }.count() > 0
                }

                if (userExists) {
                    call.respond(HttpStatusCode.Conflict, MessageResponse("Username is already taken"))
                    return@post
                }

                val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt(12))

                val newUserId = DatabaseFactory.dbQuery {
                    Users.insert {
                        it[username] = request.username
                        it[passwordHash] = hashedPassword
                    }[Users.id]
                }

                call.respond(HttpStatusCode.Created, MessageResponse("User created successfully", newUserId.toString()))
            }catch (e: kotlinx.serialization.SerializationException){
                call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse("Invalid JSON format or missing required fields")
                )
            }catch (e: Exception){
                call.respond(
                    HttpStatusCode.InternalServerError,
                    MessageResponse("An unexpected server error occurred: ${e.localizedMessage}")
                )
            }
        }

        post("/login"){
            val request = call.receive<LoginRequest>()

            val userRow = DatabaseFactory.dbQuery {
                Users.selectAll().where{
                    Users.username eq request.username
                }.singleOrNull()
            }

            val isValidPassword = userRow != null && BCrypt.checkpw(request.password, userRow[Users.passwordHash])

            if (isValidPassword){
                val userId = userRow[Users.id].toString()

                val token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(userId)
                    .withClaim("username", request.username)
                    .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
                    .sign(rsaAlgorithm)

                call.respond(TokenResponse(token))
            }else{
                call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid Credential"))
            }
        }
    }
}