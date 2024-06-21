package dev.skyphi.Models;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import dev.skyphi.CTFUtils;
import dev.skyphi.SootCTF;

public class CTFTeam {
    
    private HashMap<UUID, CTFPlayer> playerList = new HashMap<>();
    private Block flag;
    private int score;
    private String name;
    private Team mcTeam;

    public CTFTeam(String name, Block flag) {
        this.name = name;
        this.flag = flag;
    }

    // GETTERS/SETTERS

    public void addPlayer(CTFPlayer player) { playerList.put(player.getUniqueId(), player); }
    public void removePlayer(Player player) { playerList.remove(player.getUniqueId()); }
    public CTFPlayer getCtfPlayer(Player player) { return playerList.get(player.getUniqueId()); }
    public Player removeRandomPlayer() {
        int randomNum = (int)(Math.random() * playerList.size());
        CTFPlayer ctfp = playerList.get(playerList.keySet().toArray()[randomNum]);
        Player player = ctfp.getPlayer();
        removePlayer(player);
        return player;
    }

    public int getPlayerCount() { return playerList.size(); }

    public int getScore() { return score; }
    public void addPoint(CTFPlayer flagCarrier) {
        if(++score == SootCTF.FLAGS_TO_WIN) {
            CTFUtils.announceWinner(this);
            CTFUtils.teleportTeamsToWorldSpawn();
            CTFUtils.stop();
        }else {
            CTFUtils.showScore(flagCarrier);
            teleport();
        }
    }

    public Block getFlag() { return flag; }
    public void setFlag(Block flagBlock) { this.flag = flagBlock; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Team getMcTeam() { return mcTeam; }
    public void setMcTeam(Team mcTeam) { this.mcTeam = mcTeam; }

    // UTILITY

    public void announce(ChatColor startColour, String msg) {
        for(CTFPlayer ctfp : playerList.values()) {
            ctfp.getPlayer().sendMessage(startColour + msg);
        }
    }

    public void title(ChatColor titleColour, String title, ChatColor subtitleColour, String subtitle) {
        for(CTFPlayer ctfp : playerList.values()) {
            ctfp.getPlayer().sendTitle(titleColour+title, subtitleColour+subtitle, -1, -1, -1);
        }
    }

    public void playSound(Sound sound, float volume, float pitch) {
        for(CTFPlayer ctfp : playerList.values()) {
            ctfp.getPlayer().playSound(ctfp.getPlayer().getLocation(), sound, volume, pitch);
        }
    }

    public void notifyPlayers() {
        ChatColor teamColour = CTFUtils.getTeamChatColour(this);

        for(CTFPlayer ctfp : playerList.values()) {
            ctfp.getPlayer().sendMessage(ChatColor.GOLD + "You are on " + teamColour + ChatColor.BOLD + name + "!");
        }
    }

    public void teleport() {
        Location flagLoc = flag.getLocation().clone();
        int x = -4;
        int z = -4;
        for(CTFPlayer ctfp : playerList.values()) {
            flagLoc.add(x+0.5, 0, z+0.5);
            ctfp.getPlayer().teleport(flagLoc);
            ctfp.getPlayer().setHealth(20);
            ctfp.getPlayer().setSaturation(20);

            x++;
            if(x > 4) {
                x = -4;
                z++;
            }
            if(z > 4) {
                x = -4;
                z = -4;
            }
        }
    }

    public void teleportWorldSpawn() {
        for(CTFPlayer ctfp : playerList.values()) {
            Player player = ctfp.getPlayer();
            player.teleport(player.getWorld().getSpawnLocation().add(0.5, 0, 0.5));
        }
    }

}
