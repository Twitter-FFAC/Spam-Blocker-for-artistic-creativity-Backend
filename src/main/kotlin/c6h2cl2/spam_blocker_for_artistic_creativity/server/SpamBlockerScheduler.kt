package c6h2cl2.spam_blocker_for_artistic_creativity.server

import c6h2cl2.spam_blocker_for_artistic_creativity.app.*

import com.beust.klaxon.Parser
import com.beust.klaxon.JsonObject
import com.beust.klaxon.JsonArray

import java.util.Timer
import java.util.TimerTask
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import kotlin.streams.toList

//4時間に1回実行する
var scheduleTime = 14_400_000.toDouble()

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
    val array = parser.parse(json) as JsonArray<JsonObject>
    return array.filter {
        it.string("accessToken") != null && it.string("accessSecret") != null
    }.map {
        AccessToken(it.string("accessToken"), it.string("accessSecret"))
    }
}