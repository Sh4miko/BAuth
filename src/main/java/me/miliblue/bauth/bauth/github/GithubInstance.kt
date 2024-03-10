package me.miliblue.bauth.bauth.github

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import me.miliblue.bauth.bauth.Bauth
import me.miliblue.bauth.bauth.CPlayer
import me.miliblue.bauth.bauth.file.FileManager
import java.io.*
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URL

class GithubInstance {
    companion object{//ik this is not a good choose to create the companion obj here but im lazy to create a new field
        val clientID : String = "16859e79848bab2309f0"//TODO:read from config file
        val secret : String = "ada6bf550dcb33c80017f7651230d007e16ed135"//TODO : same
        val redirectUrl : String = "http://localhost:1337/github"//TODO: again
        var httpServer: HttpServer? = null
        private var handler: GithubHandler? = null
        var token: String? = null

        fun getLoginURL() : String {
            return "https://github.com/login/oauth/authorize?client_id=<client_id>&redirect_uri=<redirect_uri>"
                    .replace("<client_id>", clientID)
                    .replace("<redirect_uri>", redirectUrl)
        }

        fun init(){
            println("[BAuth] Init oauth server...(Github)")
            handler = GithubHandler()
            httpServer = HttpServer.create(InetSocketAddress("localhost",1337),0)
            httpServer!!.createContext("/github", handler)
            httpServer!!.start()
            println("[BAuth] finished. (Github)")
        }
    }
}

class GithubHandler : HttpHandler, Closeable {
    private var got = false
    private var token : String = ""
    @Throws(IOException::class)
    override fun handle(exchange: HttpExchange) {
        if (!got) {
            val queryUri = exchange.requestURI.query
            if (queryUri.contains("code=")) {
                val code = queryUri.split("code=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                got = true

                println("requestToken = $code")

                val url = URL("https://github.com/login/oauth/access_token?client_id=" + GithubInstance.clientID + "&client_secret=" + GithubInstance.secret + "&code=" + code)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.requestMethod = "POST"

                val read: String = read(connection.inputStream)
                token = read.split("access_token=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                println("Token = $token")

                val profileURL = URL("https://api.github.com/user")
                val profileConnection = profileURL.openConnection() as HttpURLConnection
                profileConnection.doInput = true
                profileConnection.requestMethod = "GET"
                profileConnection.setRequestProperty("Authorization", "Bearer $token")
                val profile: String = read(profileConnection.inputStream)

                //FUCK
                val username = profile.replace("{\"login\":\"", "").split("\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val uid = profile.split("id\":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                println("get the uid:"+uid+" | github username:"+username)
                val dbLines = FileManager.readFile("database.config")
                dbLines?.forEach{
                    println("[fuckbug] "+it)
                    if(it == "\n" || it == ""){
                        println("[debug] break")
                        return@forEach
                    }
                    val data = it.split("|")
                    //if found the target player
                    if(data[1].equals(uid)){
                        println("[fuckbug] found uid")
                        val p : CPlayer? = Bauth.getPlayerByName(data[0])
                        if(p != null){
                            //set loggined
                            p.loggined = true
                            println("[BAuth] Player("+p.p.name+") has login to the server.")
                            p.p.sendMessage("§aLogin Successfully.")
                            close()
                            return
                        }
                    }
                }
                println("[fuckbug] !found player")
                Bauth.players.forEach {
                    println("[fuckbug] foreach")
                    if(it.register){
                        it.p.sendMessage("§bIs this your github account? Name : "+username+" UID:"+uid)
                        it.p.sendMessage("§bSend ‘1’ to confirm, send others messages to confirm this is not your account.")
                        it.needToListenChat = true
                        it.oAuthID = uid
                        close()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun read(stream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        val stringBuilder = StringBuilder()
        var s: String?
        while ((reader.readLine().also { s = it }) != null) {
            stringBuilder.append(s)
        }

        stream.close()
        reader.close()

        return stringBuilder.toString()
    }

    override fun close() {
        println("[BAuth] stop handle server.")
        GithubInstance.httpServer!!.stop(0)
        GithubInstance.httpServer = null
    }
}