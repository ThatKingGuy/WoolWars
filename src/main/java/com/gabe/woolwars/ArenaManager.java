package com.gabe.woolwars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class ArenaManager {

    private final WoolWars plugin;
    private final FileConfiguration config;
    private final Set<Arena> arenaSet;

    private Location lobby;

    public ArenaManager(WoolWars plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.arenaSet = new HashSet<>();
    }

    public void setLobby(Location lobby){
        this.lobby = lobby;
    }

    public Location getLobby(){
        return lobby;
    }

    public void deserialise() {
        ConfigurationSection configSection = config.getConfigurationSection("Arenas");

        if(config.get("lobbyLocation") != null) {
            setLobby((Location) config.get("lobbyLocation"));
        }
        if(configSection == null) return;
        List<String> arenas = new ArrayList<>();
        arenas.addAll(configSection.getKeys(false));

        for(String arenaname : arenas){
            Arena arena  = new Arena(arenaname, (Location) config.get("Arenas." + arenaname + ".location"));
            arenaSet.add(arena);
        }
    }

    //save config
    public void serialise() {
        if(arenaSet.isEmpty()) return;

        arenaSet.forEach(arena -> config.set("Arenas." + arena.getName() + ".location", arena.getLocation()));
        config.set("lobbyLocation", getLobby());
        plugin.saveConfig();
    }

    public Arena getArena(Player player) {
        return arenaSet.stream().filter(arena -> arena.getPlayers().contains(player)).findFirst().orElse(null);
    }

    public Arena getArena(String key) {
        return arenaSet.stream().filter(arena -> arena.getName().equals(key)).findFirst().orElse(null);
    }

    public Set<Arena> getArenaList() {
        return Collections.unmodifiableSet(arenaSet);
    }

    public void addArena(Arena arena) {
        this.arenaSet.add(arena);
    }

    public void removeArena(Arena arena) {
        this.arenaSet.remove(arena);
    }
}