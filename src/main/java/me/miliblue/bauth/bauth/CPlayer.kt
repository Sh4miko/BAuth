package me.miliblue.bauth.bauth

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class CPlayer(val p:Player) : Listener{
    var loggined : Boolean = false
    val location : Location = p.location;
    val hp : Double = p.health
    var register = false
    var needToListenChat = false
    var oAuthID = ""



    fun savePlayer(){
        p.teleport(location)
        p.health = hp
    }
}