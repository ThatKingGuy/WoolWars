package com.gabe.woolwars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class GameScoreboard {

    private WoolWars plugin;

    public GameScoreboard(WoolWars plugin){
        this.plugin = plugin;
    }

    public Scoreboard getScoreboard(Player player){
        String servername = plugin.getConfig().getString("servername");
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Arena arena = WoolWars.getArenaManager().getArena(player);


        Objective objective = board.registerNewObjective("test", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);


        if(arena.state == GameState.WAITING) {
            Score spacer = objective.getScore("             "); //Get a fake offline player
            spacer.setScore(6);

            if (arena.playertimer == -1) {
                Score status = objective.getScore(WoolWars.color("&6Status ⋙ &7Waiting")); //Get a fake offline player
                status.setScore(5);
            } else {
                Score status = objective.getScore(WoolWars.color("&6Status ⋙ &7" + arena.playertimer)); //Get a fake offline player
                status.setScore(5);
            }

            Score map = objective.getScore(WoolWars.color("&6Map ⋙ &7" + arena.getName())); //Get a fake offline player
            map.setScore(4);

            Score players = objective.getScore(WoolWars.color("&6Players ⋙ &7" + arena.getPlayers().size() + "/"+arena.getSpawns().size())); //Get a fake offline player
            players.setScore(3);

            Score spacer1 = objective.getScore("             "); //Get a fake offline player
            spacer1.setScore(2);

            Score ip = objective.getScore(WoolWars.color("&d"+servername)); //Get a fake offline player
            ip.setScore(1);
        }else{
            Score spacer = objective.getScore("             "); //Get a fake offline player
            spacer.setScore(8);

            Score players = objective.getScore(WoolWars.color("&6Players Left ⋙ &7"+arena.getPlayers().size())); //Get a fake offline player
            players.setScore(7);

            Score timer = objective.getScore(WoolWars.color("&6Time ⋙ &7" + arena.roundtimer)); //Get a fake offline player
            timer.setScore(6);

            Score sand = objective.getScore(WoolWars.color("&6Sand Level ⋙ &7" + arena.getSandlayer())); //Get a fake offline player
            sand.setScore(5);


            Score spacer1 = objective.getScore("             "); //Get a fake offline player
            spacer1.setScore(2);

            Score ip = objective.getScore(WoolWars.color("&d"+servername)); //Get a fake offline player
            ip.setScore(1);
        }

//Setting the display name of the scoreboard/objective
        objective.setDisplayName(WoolWars.color("&5&lWoolWars"));
        return board;
    }
}
