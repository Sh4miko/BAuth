package me.miliblue.bauth.bauth.listeners

import me.miliblue.bauth.bauth.Bauth
import me.miliblue.bauth.bauth.CPlayer
import me.miliblue.bauth.bauth.file.FileManager
import me.miliblue.bauth.bauth.github.GithubInstance
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent


class PlayerActionListener : Listener{
    constructor(){
        println("PAL inited.")
    }
    @EventHandler
    fun onJoinServer(p : PlayerJoinEvent){
        //shits
        GithubInstance.init()
        val cp = CPlayer(p.player)
        var founded = false;
        val dbLines = FileManager.readFile("database.config")
        if (dbLines != null) {
            dbLines.forEach{
                var data = it.split("|")
                if(data[0] == p.player.name){
                    Bauth.players.add(cp)
                    p.player.sendMessage("[BAuth] Click the link to login the server...")
                    p.player.sendMessage("§a"+GithubInstance.getLoginURL())
                    founded = true;
                }
            }
            if(!founded){
                Bauth.players.add(cp)
                p.player.sendMessage("[BAuth] Click the link to register to the server...")
                p.player.sendMessage("§a"+GithubInstance.getLoginURL())
                cp.register = true
                founded = true
            }
        }else if(dbLines == null){
            //create new db file
            Bauth.database.createNewFile()
            FileManager.saveFile("database.config", "AntiBug|114514")
            Bauth.players.add(cp)
            cp.register = true
            p.player.sendMessage("[BAuth] Click the link to register to the server...")
            p.player.sendMessage("§a"+GithubInstance.getLoginURL())
        }
    }

    @EventHandler
    fun onUpdate(e : PlayerMoveEvent){
        Bauth.players.forEach{
            //ily kt
            if(!it.loggined)
                it.savePlayer()
        }
    }

    @EventHandler
    fun onQuitServer(e : PlayerQuitEvent){
        val player = Bauth.getPlayerByBukkitPlayer(e.player)
        Bauth.players.remove(player)
        println("Remove player("+ player!!.p.name+") from player list. Reason:Quit from server.")
    }
}