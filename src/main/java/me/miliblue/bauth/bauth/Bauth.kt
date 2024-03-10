package me.miliblue.bauth.bauth

import me.miliblue.bauth.bauth.file.FileManager
import me.miliblue.bauth.bauth.github.GithubHandler
import me.miliblue.bauth.bauth.github.GithubInstance
import me.miliblue.bauth.bauth.listeners.PlayerActionListener
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Bauth : JavaPlugin(), Listener {
    override fun onEnable(){
        Bukkit.getPluginManager().registerEvents(PlayerActionListener(), this)
        Bukkit.getPluginManager().registerEvents(this, this)
        if(!FileManager.path.exists()) {
            FileManager.path.mkdir()
            database.createNewFile()
            FileManager.saveFile("database.config", "AntiBug|114514")
            println("[BAuth] Created a new config dir.")
        }
    }

    override fun onDisable() {

    }

    @EventHandler
    fun onChat(e : PlayerChatEvent){
        val player : CPlayer? = getPlayerByBukkitPlayer(e.player)
        if (player != null) {
            if(player.needToListenChat){
                val message = e.message

                if(message.equals("1")){
                    FileManager.saveFile("database.config", player.p.name+"|"+player.oAuthID)
                    player.loggined = true;
                    player.p.sendMessage("Registered.")
                    e.isCancelled = true
                }
            }


            if(!player.loggined){
                e.isCancelled = true
            }
        }
    }


    companion object{
        val players : ArrayList<CPlayer> = ArrayList<CPlayer>()
        val database : File = File(FileManager.path.path+File.separator+"database.config")

        fun getPlayerByName(name : String) : CPlayer? {
            players.forEach {
                if(it.p.name.equals(name)){
                    return it
                }
            }
            return null
        }


        fun getPlayerByBukkitPlayer(player: Player) : CPlayer? {
            players.forEach {
                if(it.p == player){
                    return it
                }
            }
            return null
        }
    }
}