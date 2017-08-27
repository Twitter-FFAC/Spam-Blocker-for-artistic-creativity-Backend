package c6h2cl2.spam_blocker_for_artistic_creativity.app

import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import kotlin.streams.toList

/**
 * @author C6H2Cl2
 */

fun main(args: Array<String>) {
    val accessToken = getAccessToken(args)
    val targetList = getTargetList()
    val twitter = TwitterFactory.getSingleton()
    twitter.oAuthAccessToken = accessToken
    val handledSet = loadHandledSet()
    val succeedSet = handleReportAndBlock(twitter, targetList, handledSet)
    saveSucceedSet(succeedSet)
}

/**
 * OAuth認証を行い、AccessTokenを取得します。
 * 既にAccessTokenを取得している場合、コマンド引数に -access-token 及び -access-secret を用いることで、認証をスキップできます。
 */
fun getAccessToken(args: Array<String>): AccessToken {
    val twitter = TwitterFactory.getSingleton()
    val requestToken = twitter.oAuthRequestToken
    var accessToken: AccessToken? = null
    if (args.isEmpty() || !args.contains("-access-token") || !args.contains("-access-secret")) {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        while (accessToken == null) {
            println("Open the following URL and grant access to your account")
            println(requestToken.authorizationURL)
            println("Enter PIN")
            val pin = reader.readLine()
            try {
                accessToken = if (pin.isNotEmpty()) {
                    twitter.getOAuthAccessToken(requestToken, pin)
                } else {
                    twitter.oAuthAccessToken
                }
            } catch (e: TwitterException) {
                if (e.statusCode == 401) {
                    println("Unable to get the access token.")
                } else {
                    e.printStackTrace()
                }
            }
        }
    } else {
        accessToken = AccessToken(args[args.indexOf("-access-token") + 1], args[args.indexOf("-access-secret") + 1])
    }
    return accessToken
}

/**
 * スパム通報&ブロックの対象となるUserのIDの一覧を取得します。
 */
fun getTargetList(): List<Long> {
    val url = URL("https://raw.githubusercontent.com/acid-chicken/fight-for-artistic-creativity/master/lists/blacklist.csv")
    val reader = BufferedReader(InputStreamReader(url.openConnection().getInputStream()))
    return reader.lines()
            .map { it.toLong() }
            .toList()
}

/**
 * UserのScreenNameを取得します。
 */
fun getScreenName(twitter: Twitter, id: Long): String {
    return "@${twitter.showUser(id).screenName}"
}

/**
 * スパム通報&ブロックの処理を行います。
 */
fun handleReportAndBlock(twitter: Twitter, targetList: List<Long>, handledSet: Set<Long>): Set<Long> {
    var spamRateLimit = false
    var blockRateLimit = false
    val failedSet = emptySet<Long>().toMutableSet()
    targetList.filter { !handledSet.contains(it) }
            .forEach {
                val screenName = getScreenName(twitter, it)
                var flag = true
                try {
                    if (!spamRateLimit) {
                        twitter.reportSpam(it)
                    }
                } catch (e: TwitterException) {
                    if (e.errorMessage == "You are over the limit for spam reports.") {
                        spamRateLimit = true
                        println("Rate limit exceeded")
                    } else {
                        println("Failed to Report Spam$screenName")
                    }
                    flag = false
                    failedSet.add(it)
                }
                try {
                    if (!twitter.blocksIDs.iDs.contains(it) && !blockRateLimit) {
                        twitter.createBlock(it)
                    }
                } catch (e: TwitterException) {
                    if (e.errorMessage == "Rate limit exceeded") {
                        blockRateLimit = true
                        println("Rate limit exceeded")
                    } else {
                        println("Failed to Block $screenName")
                    }
                    flag = false
                    failedSet.add(it)
                }
                if (flag) {
                    println("Succeed to Report Spam & Block @${twitter.showUser(it).screenName}")
                }
            }
    return targetList.filter { !failedSet.contains(it) }.toSet()
}

/**
 * スパム通報&ブロックに成功したアカウントのIDの一覧をCSV形式で保存します。
 */
fun saveSucceedSet(succeedSet: Set<Long>) {
    val path = File("succeed.csv").toPath()
    if (!Files.exists(path)) {
        Files.createFile(path)
    }
    val writer = Files.newBufferedWriter(path)
    succeedSet.forEach {
        writer.write(it.toString())
        writer.write("\n")
    }
}

/**
 * 既にスパム通報&ブロックが行われているアカウントのIDの一覧を読み込みます。
 */
fun loadHandledSet(): Set<Long> {
    val path = File("succeed.csv").toPath()
    if (!Files.exists(path)) {
        return emptySet()
    }
    val reader = Files.newBufferedReader(path)
    return reader.lines()
            .map { it.toLong() }
            .toList()
            .toSet()
}