package com.auth.utils

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.io.File

object Users: Table() {
    val id = uuid("id").autoGenerate()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init(dbDir: String){
        File(dbDir).mkdirs()

        val driverClassName = "org.sqlite.JDBC"
        val jdbcURL = "jdbc:sqlite:$dbDir/auth.db"

        val database = Database.connect(jdbcURL, driverClassName)

        transaction(database){
            SchemaUtils.create(Users)

            if (Users.selectAll().empty()){
                val initialAdmin = System.getenv("DEFAULT_ADMIN_USER")
                val initialPass = System.getenv("DEFAULT_ADMIN_PASS")

                if (initialAdmin != null && initialPass != null){
                    Users.insert {
                        it[username] = initialAdmin
                        it[passwordHash] = BCrypt.hashpw(initialPass, BCrypt.gensalt(12))
                    }
                }
            }
        }
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}