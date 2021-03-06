package com.gabe.woolwars;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.bukkit.event.inventory.InventoryAction.*;

public final class WoolWars extends JavaPlugin implements Listener {
    private GhostManager manager;
    private static ArenaManager arenaManager;
    private static KitManager kitManager;
    private static ChestManager chestManager;

    public static String format(String text){
        String prefix = "&5&lWoolWars &8⋙ &7";
        return ChatColor.translateAlternateColorCodes('&',prefix+text);
    }

    public static String color(String text){
        return ChatColor.translateAlternateColorCodes('&',text);
    }

    public static ArenaManager getArenaManager() {
        return arenaManager;
    }

    public static KitManager getKitManager() {
        return kitManager;
    }

    public static ChestManager getChestManager() {
        return chestManager;
    }

    @Override
    public void onEnable() {

        if(!(new File(this.getDataFolder() + File.separator + "config.yml").exists())){
            getConfig().set("servername", "myserver.com");
            saveConfig();
        }

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("aww").setTabCompleter(new AdminTabCompletions());
        manager = new GhostManager(this);
        arenaManager = new ArenaManager(this);
        kitManager = new KitManager(this);
        chestManager = new ChestManager(this);
        arenaManager.deserialise();

        for(Arena arena : getArenaManager().getArenaList()){
            try {
                ArenaResseter resseter = new ArenaResseter(this);
                resseter.reset(arena.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(label.equalsIgnoreCase("ww")){
                if(args.length == 0){
                    //create arena gui
                    Inventory inv = Bukkit.createInventory(null,27, color("&5&lWoolWars &8&l- &d&lArenas"));
                    for(Arena a : getArenaManager().getArenaList()) {

                        Material state = Material.GLASS;
                        String stateText = "&3Resetting";

                        if(a.state == GameState.INGAME){
                            stateText = "&dIn Progress";
                            state = Material.RED_CONCRETE;
                        } else if(a.state == GameState.STARTING){
                            stateText = "&dStarting";
                            state = Material.YELLOW_CONCRETE;

                        } else if(a.state == GameState.WAITING){
                            stateText = "&dWaiting for players";
                            state = Material.GREEN_CONCRETE;
                        }
                        ItemStack icon = new ItemStack(state,1);

                        ItemMeta im = icon.getItemMeta();
                        im.setDisplayName(color("&5"+a.getName()));

                        //set lore
                        List<String> lore = new ArrayList<String>();
                        lore.add(color("&8Players: ("+a.getPlayers().size()+"/"+a.getSpawns().size()+")"));
                        lore.add(color(stateText));
                        lore.add("");
                        lore.add(color("&aClick to join arena."));
                        im.setLore(lore);

                        icon.setItemMeta(im);
                        inv.addItem(icon);
                    }

                    //show the player the gui
                    player.openInventory(inv);

                }else if(args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("quit")){
                    if(getArenaManager().getArena(player) != null){
                        player.teleport(getArenaManager().getLobby());
                        player.sendMessage(format("Leaving arena."));
                        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                        getArenaManager().getArena(player).removePlayer(player);
                    }else{
                        player.sendMessage(format("You are not in an arena!"));
                    }
                }
                else if(args[0].equalsIgnoreCase("help")){
                    if(args[0].equalsIgnoreCase("help")){
                        player.sendMessage(color("                    &5&lWoolWars &8- &7v"+getDescription().getVersion()));
                        player.sendMessage(color("                              &7Help"));
                        player.sendMessage("");
                        player.sendMessage(color("&8/&dww &8- &7Opens the arenas gui."));
                        player.sendMessage(color("&8/&dww help &8- &7Shows this."));
                        player.sendMessage(color("&8/&dww join <arena> &8- &7Join an arena."));
                        player.sendMessage(color("&8/&dww leave/quit &8- &7Leave the arena you're in."));
                    }
                }else{
                    player.sendMessage(color("                    &5&lWoolWars &8- &7v"+getDescription().getVersion()));
                    player.sendMessage(color("                              &7Help"));
                    player.sendMessage("");
                    player.sendMessage(color("&8/&dww &8- &7Opens the arenas gui."));
                    player.sendMessage(color("&8/&dww help &8- &7Shows this."));
                    player.sendMessage(color("&8/&dww join <arena> &8- &7Join an arena."));
                    player.sendMessage(color("&8/&dww leave/quit &8- &7Leave the arena you're in."));
                }
            }else if(label.equalsIgnoreCase("aww")){
                if(args.length == 0){
                    //send admin help menu
                    player.sendMessage(color("              &5&lAdmin WoolWars &8- &7v"+getDescription().getVersion()));
                    player.sendMessage(color("                              &7Help"));
                    player.sendMessage("");

                    player.sendMessage(color("&8/&daww help &8- &7Shows this."));
                    player.sendMessage(color("&8/&daww create <arena> &8- &7Creates an arena."));
                    player.sendMessage(color("&8/&daww remove <arena> &8- &7Removes an arena."));
                    player.sendMessage(color("&8/&daww list &8- &7Lists arenas."));
                    player.sendMessage(color("&8/&daww setlobby &8- &7Sets Lobby."));
                }else {
                    if(args[0].equalsIgnoreCase("help")){
                        //send admin help menu
                        player.sendMessage(color("&8/&daww help &8- &7Shows this."));
                        player.sendMessage(color("&8/&daww create <arena> &8- &7Creates an arena."));
                        player.sendMessage(color("&8/&daww remove <arena> &8- &7Removes an arena."));
                        player.sendMessage(color("&8/&daww list &8- &7Lists arenas."));
                        player.sendMessage(color("&8/&daww setlobby &8- &7Sets Lobby."));
                    }else if(args[0].equalsIgnoreCase("create")){
                        //create arena
                        if(args.length > 1){
                            if(getArenaManager().getArena(args[1]) == null){
                                Title title = new Title();
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                                title.send(player, color("&dYou Created An arena!"), color("&7Name: &l"+args[1]), 0,2,0);
                                player.sendMessage(format("&aCreated arena \""+args[1]+"\"."));
                                getArenaManager().addArena(new Arena(args[1],player.getLocation(), this));
                                getArenaManager().serialise();
                            }else{
                                player.sendMessage(format("&cThere is already an arena with that name"));
                            }
                        }else{
                            player.sendMessage(format("&cIncorrect usage. &8/&dww create <arena>"));
                        }
                    }else if(args[0].equalsIgnoreCase("list")){
                        //send player names of all arenas
                        Set<Arena> arenas = getArenaManager().getArenaList();
                        if(getArenaManager().getArenaList().size() == 0){
                            player.sendMessage(format("There are no arenas."));
                        }else{
                            player.sendMessage(format("There are "+getArenaManager().getArenaList().size()+" arenas:"));
                            arenas.forEach(a -> player.sendMessage(color("&d&l"+a.getName())));
                        }

                    }else if(args[0].equalsIgnoreCase("remove")){
                        if(args.length > 1){
                            if(getArenaManager().getArena(args[1]) != null){

                                getArenaManager().removeArena(getArenaManager().getArena(args[1]));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                                player.sendMessage(format("&aRemoved arena \""+args[1]+"\"."));
                                Title title = new Title();
                                title.send(player, color("&dYou Removed An arena!"), color("&7Name: &l"+args[1]), 0,2,0);

                                getConfig().set("Arenas." + args[1], null);
                                saveConfig();
                                getArenaManager().serialise();
                            }else{
                                player.sendMessage(format("&cThere not an arena with that name"));
                            }
                        }else{
                            player.sendMessage(format("&cIncorrect usage. &8/&dww remove <arena>"));
                        }

                    }else if(args[0].equalsIgnoreCase("setlobby")){
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                        player.sendMessage(format("&aSet Lobby."));
                        Title title = new Title();
                        Location c = player.getLocation();
                        title.send(player, color("&dYou Set the lobby!"), ChatColor.GRAY+"X: "+c.getBlockX()+" Y: "+c.getBlockY()+" Z: "+c.getBlockZ(), 0,2,0);
                        getConfig().set("lobbyLocation", player.getLocation());
                        saveConfig();
                        getArenaManager().setLobby(c);
                    }else if(args[0].equalsIgnoreCase("reset")){
                        if(args.length > 1){
                            if(getArenaManager().getArena(args[1]) != null){
                                try {
                                    ArenaResseter resseter = new ArenaResseter(this);
                                    resseter.reset(args[1]);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                player.sendMessage(format("&cThere not an arena with that name"));
                            }
                        }else{
                            player.sendMessage(format("&cIncorrect usage. &8/&dww reset <arena>"));
                        }

                    }else{
                        player.sendMessage(format("&7That is not a command!"));
                    }
                }
            }
        }

        return true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = arenaManager.getArena(player);
        if(arena == null) return;
        arena.removePlayer(player);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        arenaManager.serialise();

    }

    @EventHandler
    public void onInv(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        if(event.getView() != null) {
            if (event.getView().getTitle().equals(color("&5&lWoolWars &8&l- &d&lArenas"))) {
                event.setCancelled(true);
                Inventory inv = event.getClickedInventory();

                ItemStack item = event.getCurrentItem();
                if (item != null) {
                    if (item.getType() != null) {

                        String arenaName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

                        Arena arena = getArenaManager().getArena(arenaName);

                        if (arena != null) {
                            arena.addPlayer(player, this);

                        } else {
                            player.sendMessage(format("This arena does not exist anymore!"));
                        }
                    }
                }
            }if (event.getView().getTitle().equals(color("&5&lWoolWars &8&l- &d&lKits"))) {
                ItemStack item = event.getCurrentItem();
                if (item != null) {
                    if (item.getType() != null) {

                        String kitName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

                        Kit kit = getKitManager().getKit(kitName);

                        if (getArenaManager().getArena(player) != null) {
                            if (kit != null) {
                                getArenaManager().getArena(player).setKit(player, kit);
                                player.sendMessage(format("&aSelected kit &d&l" + kit.getName()));
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1 ,1);
                                player.closeInventory();
                            }
                        }

                    }
                }
            }if (event.getView().getTitle().equals(color("&5&lWoolWars &8&l- &d&lTeleport Menu"))) {
                ItemStack item = event.getCurrentItem();
                event.setCancelled(true);
                if (item != null) {
                    if (item.getType() != null) {
                        if (getArenaManager().getArena(player) != null) {
                            Arena arena = getArenaManager().getArena(player);
                            if (Bukkit.getPlayer(ChatColor.stripColor(item.getItemMeta().getDisplayName())) != null) {
                                Player spec = Bukkit.getPlayer(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
                                if (arena.getPlayers().contains(spec)) {
                                    player.sendMessage(format("&aTeleported to player &d&l" + spec.getDisplayName()));
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                    player.teleport(spec);
                                    player.closeInventory();
                                }else {
                                    player.sendMessage(format("&7This player has died."));
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onClickSlot(InventoryClickEvent e) {
        Arena arena = getArenaManager().getArena((Player) e.getWhoClicked());
        if(arena != null) {
            if(arena.state != GameState.INGAME) {
                e.setResult(Event.Result.DENY);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if(getArenaManager().getArena(player) !=null){
            Arena arena = getArenaManager().getArena(player);
            if(arena.state != GameState.INGAME){
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        if(getArenaManager().getArena(player) !=null){
            Arena arena = getArenaManager().getArena(player);
            if(arena.state != GameState.INGAME){
                event.setCancelled(true);
            }else{
                if(event.getBlock().getType() == Material.TNT){
                    event.getBlock().setType(Material.AIR);
                    player.getWorld().spawnEntity(event.getBlock().getLocation(), EntityType.PRIMED_TNT);
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        if(getArenaManager().getArena(player) !=null){
            Arena arena = getArenaManager().getArena(player);
            if(arena.state == GameState.STARTING){
                player.setWalkSpeed(0);
                if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                    event.setCancelled(true);
                }

            }


            List<ItemStack> drops = new ArrayList<>();
            for(ItemStack item : player.getInventory().getContents()){
                drops.add(item);
            }
            if(player.getLocation().subtract(0,0.2,0).getBlock().getType() == Material.SANDSTONE){
                PlayerDeathEvent e = new PlayerDeathEvent(player, drops, 0,0,player.getDisplayName()+" has been vaporized by the sandstone.");
                arena.killPlayer(e);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getEntity();

        if(getArenaManager().getArena(player) != null){
            Arena arena = getArenaManager().getArena(player);

            if(arena.state != GameState.INGAME){
                e.setCancelled(true);
            }
        }


    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event){
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player)event.getEntity();
        if(getArenaManager().getArena(player) != null){
            Arena arena = getArenaManager().getArena(player);
            if(arena.state != GameState.INGAME){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void PlayerDamageReceive(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player) {
            Player damaged = (Player) e.getEntity();

            if(e.getDamager() instanceof Player) {
                Player damager = (Player) e.getDamager();
                    if((damaged.getHealth()-e.getDamage()) <= 0) {
                        if(getArenaManager().getArena(damaged) != null) {
                            Arena arena = getArenaManager().getArena(damaged);
                            //Killed
                            e.setCancelled(true);
                            List<ItemStack> drops = new ArrayList<>();
                            arena.killPlayer(new PlayerDeathEvent(damager, drops, 0,0,damaged.getDisplayName() + " was slain by " + damager.getDisplayName()+"."));
                            damaged.setHealth(20);
                        }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            e.setCancelled(true);
            List<Arena> arenas = new ArrayList<>(getArenaManager().getArenaList());
            for(Arena arena : arenas){
                if(!arena.state.canJoin()){
                    arenas.remove(arena);
                }
            }
            arenas.get(0).addPlayer(e.getPlayer(), this);

        }
    }

    /*@EventHandler
    public void onDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        if(getArenaManager().getArena(player) != null){
            Arena arena = getArenaManager().getArena(player);
            arena.killPlayer(event);
        }
    } */


    @EventHandler
    public void onClickItem(PlayerInteractEvent event){
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if(item != null){
            if(item.getItemMeta() != null){
                if(item.getItemMeta().getDisplayName().equalsIgnoreCase(color("&5&lBack to hub"))){
                    if(getArenaManager().getArena(player) != null) {
                        player.performCommand("ww leave");
                    }
                } else if(item.getItemMeta().getDisplayName().equalsIgnoreCase(color("&5&lKits"))){
                    if(getArenaManager().getArena(player) != null) {
                        getArenaManager().getArena(player).openKitMenu(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        if(getArenaManager().getArena(player) != null){
            if(getArenaManager().getArena(player).state != GameState.INGAME){
                event.setCancelled(true);
            }
            if(getArenaManager().getArena(player).getSpectators().contains(player)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;

        List<Block> blockListCopy = new ArrayList<>();
        blockListCopy.addAll(event.blockList());
        for (Block block : blockListCopy) {
            if (block.getType() == Material.SANDSTONE) event.blockList().remove(block);
        }
    }
}
