package c6h2cl2.spam_blocker_for_artistic_creativity

import c6h2cl2.spam_blocker_for_artistic_creativity.app.*
import c6h2cl2.spam_blocker_for_artistic_creativity.server.*
import java.io.BufferedReader
import java.io.InputStreamReader

fun main(args: Array<String>) {
    if (args.contains("--app") {
        if (args.contains("--server") {
            selectAppOrServer(args)
        } else {
            runApp(args)
        }
    } else if (args.contains("--server")) {
        runServer(args)
    } else {
        selectAppOrServer(args)
    }
}

fun selectAppOrServer(args: Array<String>) {
    val reader = BufferedReader(InputStreamReader(System.`in`))
    while(true) {
        println("""Enter "App" or "Server" """)
        val s = reader.readLine()
        if (s == "App") {
            runApp(args)
            return
        } else if (s == "Server") {
            runServer(args)
            return
        }
    }
}