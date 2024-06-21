package dev.skyphi.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import dev.skyphi.CTFUtils;
import dev.skyphi.SootCTF;
import dev.skyphi.Models.CTFPlayer;
import dev.skyphi.Models.CTFTeam;

public class FlagListener implements Listener {
    
    @EventHandler
    public void on(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if(block == null) return;

        CTFPlayer ctfp = CTFUtils.getCTFPlayer(event.getPlayer());
        CTFTeam team = ctfp.getTeam();
        CTFTeam enemyTeam = ctfp.getEnemyTeam();
        Block enemyFlag = enemyTeam.getFlag();

        if(!ctfp.hasFlag()) {   // no flag - check if player clicked enemy flag
            if(block.getLocation().equals(enemyFlag.getLocation())) {
                enemyFlag.setType(Material.AIR);
                ctfp.setFlag(true);

                // play sounds
                team.playSound(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1, 1);
                enemyTeam.playSound(Sound.ENTITY_WITHER_SPAWN, 1, 1);
                // announce flag picked up
                team.announce(ChatColor.AQUA, ChatColor.BOLD+ctfp.getPlayerName()+ChatColor.GREEN + " picked up the enemy flag!");
                enemyTeam.announce(ChatColor.RED, "Your flag has been picked up by "+ChatColor.DARK_RED+""+ChatColor.BOLD+ctfp.getPlayerName());
            
                //TODO particles?
            }
        }else {                 // has flag - check if player clicked team flag
            Block teamFlag = ctfp.getTeam().getFlag();

            if(block.getLocation().equals(teamFlag.getLocation())) {
                enemyFlag.setType(SootCTF.FLAG_TYPE);
                ctfp.setFlag(false);
                team.addPoint(ctfp);

                // play sounds
                team.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                enemyTeam.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
                // announce flag captured
                team.announce(ChatColor.AQUA, ChatColor.BOLD+ctfp.getPlayerName()+ChatColor.GREEN + " captured the enemy flag!");
                enemyTeam.announce(ChatColor.RED, "Your flag was captured by "+ChatColor.DARK_RED+""+ChatColor.BOLD+ctfp.getPlayerName());
            
                //TODO particles?
                //TODO fireworks in sky above flag?
            }
        }
    }

}
