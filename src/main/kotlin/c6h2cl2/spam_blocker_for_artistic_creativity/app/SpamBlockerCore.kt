package c6h2cl2.spam_blocker_for_artistic_creativity.app

import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import kotlin.streams.toList

/**
 * @author C6H2Cl2
 */

fun main(args: Array<String>) {
    val accessToken = getAccessToken(args)
    val targetList = getTargetList()
    val twitter = TwitterFactory.getSingleton()
    twitter.oAuthAccessToken = accessToken
    targetList.forEach {
        val screenName = getScreenName(twitter, it)
        var flag = true
        try {
            twitter.reportSpam(it)
        } catch (e: Exception) {
            println("Failed to Report Spam$screenName")
            flag = false
        }
        try {
            twitter.createBlock(it)
        } catch (e: Exception) {
            println("Failed to Block $screenName")
            flag = false
        }
        if (flag) {
            println("Succeed to Report Spam & Block @${twitter.showUser(it).screenName}")
        }
    }
}

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

fun getTargetList(): List<Long> {
    val url = URL("https://raw.githubusercontent.com/acid-chicken/fight-for-artistic-creativity/master/lists/blacklist.csv")
    val reader = BufferedReader(InputStreamReader(url.openConnection().getInputStream()))
    return reader.lines()
            .map { it.toLong() }
            .toList()
}

fun getScreenName(twitter: Twitter, id: Long): String {
    return "@${twitter.showUser(id).screenName}"
}