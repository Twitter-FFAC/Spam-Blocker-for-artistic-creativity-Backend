package c6h2cl2.spam_blocker_for_artistic_creativity.server

import c6h2cl2.spam_blocker_for_artistic_creativity.app.runApp
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.string
import twitter4j.auth.AccessToken
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.concurrent.schedule

//4時間に1回実行する
var scheduleTime = 14_400_000.toLong()

fun runServer(args: Array<String>) {
    val timer = Timer()
    timer.schedule(scheduleTime) {
        process()
    }
}

private fun process() {
    val userSet = getUserSet()
    userSet.forEach {
        runApp(arrayOf("-access-token", it.token, "-access-secret", it.tokenSecret))
    }
}

private fun getUserSet(): Set<AccessToken> {
    val path = File("users.json").toPath()
    if (Files.exists(path)) {
        return emptySet()
    }
    val json = String(Files.readAllBytes(path))
    val parser = Parser()
    @Suppress("UNCHECKED_CAST")
    val array = parser.parse(json) as JsonArray<JsonObject>
    return array.filter {
        it.string("accessToken") != null && it.string("accessSecret") != null
    }.map {
        AccessToken(it.string("accessToken"), it.string("accessSecret"))
    }.toSet()
}