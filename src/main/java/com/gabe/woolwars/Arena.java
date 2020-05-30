package com.gabe.woolwars;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Wool;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;


import java.io.IOException;
import java.util.*;

public class Arena {

    private final String name;
    private final Location location;
    private final Set<Player> players;
    private final Set<Player> spectators;
    private Set<Location> spawns;
    public int countdown = -1;
    public int playertimer = -1;
    public int roundtimer = 0;
    private int sandlayer;
    private int towertimer;
    private WoolWars plugin;
    GameScoreboard board;

    Map<UUID, ItemStack[]> items = new HashMap<UUID, ItemStack[]>();
    Map<UUID, ItemStack[]> armor = new HashMap<UUID, ItemStack[]>();
    Map<UUID, Kit> kits = new HashMap<UUID, Kit>();

    public GameState state = GameState.WAITING;

    public Arena(String name, Location location, WoolWars plugin) {
        this.name = name;
        this.location = location;
        this.players = new HashSet<>();
        this.spectators = new HashSet<>();
        this.spawns = new HashSet<>();
        this.sandlayer = location.getBlockY()-1;
        this.board = new GameScoreboard(plugin);
        this.plugin = plugin;
        for(int x = 0; x< 61; x++){
            for(int z = 0; z< 61; z++){
                Location check = new Location(location.getWorld(), x+location.getBlockX()-31, location.getBlockY(), z+location.getBlockZ()-31);
                if(check.getBlock() != null){
                    Block block = check.getBlock();
                    if(block.getType() == Material.BIRCH_PRESSURE_PLATE){
                        spawns.add(check);
                        Bukkit.getLogger().info("[WoolWars] Registered spawn x: "+check.getBlockX()+" y: "+check.getBlockY()+" z: "+check.getBlockZ()+" for arena \""+getName()+"\".");
                    }
                }
            }
        }
    }

    public void setKit(Player player, Kit kit){
        kits.put(player.getUniqueId(), kit);
        Bukkit.getLogger().info(player.getDisplayName() + " has selected "+kit);
    }


    public void storeAndClearInventory(Player player){
        UUID uuid = player.getUniqueId();

        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        items.put(uuid, contents);
        armor.put(uuid, armorContents);

        player.getInventory().clear();

        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }

    public void restoreInventory(Player player){
        UUID uuid = player.getUniqueId();

        ItemStack[] contents = items.get(uuid);
        ItemStack[] armorContents = armor.get(uuid);

        if(contents != null){
            player.getInventory().setContents(contents);
        }
        else{//if the player has no inventory contents, clear their inventory
            player.getInventory().clear();
        }

        if(armorContents != null){
            player.getInventory().setArmorContents(armorContents);
        }
        else{//if the player has no armor, set the armor to null
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);
        }
    }

    public int getSandlayer() {
        return sandlayer - getLocation().getBlockY();
    }

    public void killPlayer(PlayerDeathEvent event){
        if(state == GameState.INGAME) {
            Player player = event.getEntity();
            this.players.remove(player);
            this.spectators.add(player);

            player.setAllowFlight(true);
            player.setFlying(true);

            for(Player p : Bukkit.getOnlinePlayers()){
                p.hidePlayer(player);
            }

            for (Player p : getPlayers()) {
                p.sendMessage(WoolWars.format("&d" + event.getDeathMessage()));
            }

            for (Player p : getSpectators()) {
                p.sendMessage(WoolWars.format("&d" + event.getDeathMessage()));
            }

            player.teleport(getLocation().add(0, getSandlayer() + 5, 0));
            player.getInventory().clear();
            //openTeleportMenu(player);
            checkPlayers();
        }

    }

    public void checkPlayers(){
        if(state==GameState.INGAME){
            if(getPlayers().size() <= 1) {
                Player winner = new ArrayList<Player>(getPlayers()).get(0);
                for (Player player : getSpectators()) {
                    player.sendMessage(WoolWars.color("&7-----------------------------"));
                    player.sendMessage(WoolWars.color(""));
                    player.sendMessage(WoolWars.color("&5&l" + winner.getDisplayName() + " &dhas won the game!"));
                    player.sendMessage(WoolWars.color(""));
                    player.sendMessage(WoolWars.color("&7-----------------------------"));
                }

                for (Player player : getPlayers()) {
                    player.sendMessage(WoolWars.color("&7-----------------------------"));
                    player.sendMessage(WoolWars.color(""));
                    player.sendMessage(WoolWars.color("&5&lYou &dhave won the game!"));
                    player.sendMessage(WoolWars.color(""));
                    player.sendMessage(WoolWars.color("&7-----------------------------"));
                }

                Bukkit.getScheduler().cancelTask(j);
                Bukkit.getScheduler().cancelTask(g);
                state = GameState.ENDING;

                //teleport players and restore inventorys
                startCelebration(plugin, winner);



                state = GameState.WAITING;
            }
        }
    }

    public void reset(){
        ArenaResseter arenaResseter = new ArenaResseter(plugin);
        try {
            arenaResseter.reset(getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<Location> getSpawns(){
        return Collections.unmodifiableSet(spawns);
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public Set<Player> getSpectators(){
        return Collections.unmodifiableSet(spectators);
    }

    public void sendAllToLobby(){
        for (Player player : getSpectators()) {
            spectators.remove(player);
            player.getInventory().clear();
            restoreInventory(player);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setAllowFlight(false);
            player.setFlying(false);
            for (PotionEffect effect : player.getActivePotionEffects())
                player.removePotionEffect(effect.getType());
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            player.teleport(WoolWars.getArenaManager().getLobby());
        }
        for (Player player : getPlayers()) {
            players.remove(player);
            player.getInventory().clear();
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setAllowFlight(false);
            player.setFlying(false);
            restoreInventory(player);
            for (PotionEffect effect : player.getActivePotionEffects())
                player.removePotionEffect(effect.getType());
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            player.teleport(WoolWars.getArenaManager().getLobby());
        }
    }

    public void openTeleportMenu(Player player){
        Inventory inv = Bukkit.createInventory(null,27, WoolWars.color("&5&lWoolWars &8&l- &d&lTeleport Menu"));

        for(Player p : getPlayers()) {

            ItemStack playerskull = new ItemStack(Material.PLAYER_HEAD,1, (short) SkullType.PLAYER.ordinal());

            SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

            meta.setOwner(player.getName());
            meta.setDisplayName(ChatColor.DARK_PURPLE + player.getDisplayName());

            playerskull.setItemMeta(meta);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(WoolWars.color("&aClick to teleport to player."));
            meta.setLore(lore);


            playerskull.setItemMeta(meta);
            inv.addItem(playerskull);
        }

        //show the player the gui
        player.openInventory(inv);

    }

    public void openKitMenu(Player player){
        Inventory inv = Bukkit.createInventory(null,27, WoolWars.color("&5&lWoolWars &8&l- &d&lKits"));

        for(Kit kit : WoolWars.getKitManager().getKitsList()) {

            ItemStack icon = kit.getIcon();

            ItemMeta im = icon.getItemMeta();
            im.setDisplayName(WoolWars.color("&5"+kit.getName()));

            //set lore
            if(kit.getLore() != null) {
                List<String> lore = new ArrayList<>();
                lore.addAll(kit.getLore());
                lore.add("");
                lore.add(WoolWars.color("&aClick to select kit."));
                im.setLore(lore);
            }

            icon.setItemMeta(im);
            inv.addItem(icon);
        }

        //show the player the gui
        player.openInventory(inv);

    }

    public void addPlayer(Player player, WoolWars plugin) {
        if(players.size() < 24) {
            if(state.canJoin()) {
                this.players.add(player);
                player.setHealth(20);
                player.setFoodLevel(20);
                storeAndClearInventory(player);

                /////items

                ItemStack hub = new ItemStack(Material.SLIME_BALL);
                ItemMeta hubm = hub.getItemMeta();
                hubm.setDisplayName(WoolWars.color("&5&lBack to hub"));
                List<String> hublore = new ArrayList<>();
                hublore.add(WoolWars.color("&7Click to go back to hub."));
                hubm.setLore(hublore);
                hub.setItemMeta(hubm);


                ItemStack kit = new ItemStack(Material.CHEST);
                ItemMeta kitm = hub.getItemMeta();
                kitm.setDisplayName(WoolWars.color("&5&lKits"));
                List<String> kitlore = new ArrayList<>();
                kitlore.add(WoolWars.color("&7Click to view kits."));
                kitm.setLore(kitlore);
                kit.setItemMeta(kitm);


                player.getInventory().setItem(0, kit);
                player.getInventory().setItem(8, hub);


                /////////////////


                player.teleport(getLocation());
                updateScoreboard();
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                Title title = new Title();
                title.send(player, WoolWars.color("&d&lWoolWars"), WoolWars.color("&7Beware of the rising sandstone!"), 0, 1, 0);

                for(Player p: getPlayers()){
                    p.sendMessage(WoolWars.format("&d&l"+player.getDisplayName()+" &r&7has joined the game! &8("+getPlayers().size()+"/"+getSpawns().size()+")"));
                }

                if(getPlayers().size() > 1){
                    if(playertimer == -1) {
                        startPlayerTimer(plugin);
                    }
                }else{
                    playertimer = -1;
                }
            }else{
                player.sendMessage(WoolWars.format("You can not join this game at this time!"));
            }
        }else{
            player.sendMessage(WoolWars.format("This game is full!"));
        }
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
        restoreInventory(player);
        if(players.size() < 2) {
            countdown = -1;
        }
        for(Player p: getPlayers()){
            p.sendMessage(WoolWars.format("&d&l"+player.getDisplayName()+" &r&7has left the game. &8("+getPlayers().size()+"/"+getSpawns().size()+")"));
        }

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        updateScoreboard();
    }


    public void updateScoreboard(){
        for(Player player: getPlayers()){

            player.setScoreboard(board.getScoreboard(player));
        }
    }
    int c = 0;
    public void startCountDown(Plugin plugin){
        countdown = 10;
        for (Player player : getPlayers()) {
            player.getInventory().clear();
            PotionEffect effect = new PotionEffect(PotionEffectType.JUMP, 200, -100);
            player.addPotionEffect(effect);
            PotionEffect invincibility = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15*20, 4);
            player.addPotionEffect(invincibility);
        }

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        c = scheduler.scheduleSyncRepeatingTask(plugin, () -> {

                if(countdown > 0) {
                    countdown--;
                    for (Player player : getPlayers()) {
                        updateScoreboard();
                        player.playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1,1);
                        Title title = new Title();
                        title.send(player, WoolWars.color("&d&l"+countdown),WoolWars.color("&7Prepare to tower up fast!"),0,1,0);
                    }
                }else{
                    List<Material> iconmats = new ArrayList<>();
                    iconmats.add(Material.RED_WOOL);
                    iconmats.add(Material.ORANGE_WOOL);
                    iconmats.add(Material.YELLOW_WOOL);
                    iconmats.add(Material.LIME_WOOL);
                    iconmats.add(Material.LIGHT_BLUE_WOOL);
                    iconmats.add(Material.WHITE_WOOL);
                    iconmats.add(Material.CYAN_WOOL);
                    iconmats.add(Material.MAGENTA_WOOL);

                    for (Player player : getPlayers()) {
                        player.getInventory().clear();
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT,1,5);
                        player.setWalkSpeed(0.2f);

                        Random random1 = new Random();
                        int material1 = random1.nextInt(iconmats.size());
                        ItemStack item1 = new ItemStack(iconmats.get(material1),64);
                        Random random2 = new Random();
                        int material2 = random2.nextInt(iconmats.size());
                        ItemStack item2 = new ItemStack(iconmats.get(material2),64);

                        player.getInventory().addItem(item1);
                        player.getInventory().addItem(item2);

                        if(kits.get(player.getUniqueId()) != null){
                            Bukkit.getLogger().info(player.getDisplayName() +" has selected kit");
                            Kit kit = kits.get(player.getUniqueId());

                            for(ItemStack item : kit.getItems()){
                                player.getInventory().addItem(item);
                            }

                            for(PotionEffect effect: kit.getEffects()){
                                player.addPotionEffect(effect);
                            }
                        }else{
                            Bukkit.getLogger().info(player.getDisplayName() +" has not selected a kit.");

                        }
                    }
                    state = GameState.INGAME;
                    riseFloor(plugin);
                    startTowerTimer(plugin);
                    fillChests();
                    Bukkit.getScheduler().cancelTask(c);
            }
        }, 0L, 1 * 20L);
    }

    int m = 0;
    public void startPlayerTimer(Plugin plugin){
        playertimer = 30;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        m = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
                if(getPlayers().size() <2){
                    Bukkit.getScheduler().cancelTask(m);
                    playertimer = -1;
                    updateScoreboard();
                }else {

                    if (playertimer > 0) {
                        playertimer--;
                        updateScoreboard();
                    } else {
                        List<Location> sp = new ArrayList<>();
                        sp.addAll(getSpawns());


                        for (Player player : getPlayers()) {
                            player.teleport(sp.get(0).add(0.5,1,0.5));
                            sp.remove(sp.get(0));
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1,1);
                        }

                        for(int x = 0; x< 61; x++){
                            for(int z = 0; z< 61; z++){
                                for(int y = 0; y< 100; y++) {
                                    Location check = new Location(location.getWorld(), x + location.getBlockX() - 31, location.getBlockY()+ y, z + location.getBlockZ() - 31);
                                    if (check.getBlock() != null) {
                                        Block block = check.getBlock();
                                        if (block.getType() == Material.STRIPPED_OAK_WOOD) {
                                            block.setType(Material.AIR);
                                        }
                                    }
                                }
                            }
                        }

                        List<Entity> entList = WoolWars.getArenaManager().getArena(name).getLocation().getWorld().getEntities();//get all entities in the world

                        for(Entity current : entList) {//loop through the list
                            if (current instanceof Item) {//make sure we aren't deleting mobs/players
                                current.remove();//remove it
                            }
                        }

                        state = GameState.STARTING;
                        Bukkit.getScheduler().cancelTask(m);
                        startRoundTimer(plugin);
                        startCountDown(plugin);
                        playertimer = -1;
                    }
                }

        }, 0L, 1 * 20L);
    }

    int j = 0;
    public void startRoundTimer(Plugin plugin){
        roundtimer = 0;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        j = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            roundtimer ++;
            updateScoreboard();

        }, 0L, 1 * 20L);
    }

    int t = 0;
    public void startTowerTimer(Plugin plugin){
        towertimer = 5;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        Title title = new Title();
        t = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            if(towertimer > 0) {
                towertimer --;
                updateScoreboard();
                for(Player player : getPlayers()){
                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1 ,1);
                    title.send(player,WoolWars.color("&d&l"+towertimer),WoolWars.color("&7Make sure you have towered!"),0,1,0);
                }
            }else{
                for(Player player : getPlayers()){
                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1 ,1);
                    title.send(player,WoolWars.color("&d&lGO!"),WoolWars.color("&7The floor will begin to rise!"),0,1,0);
                }
                Bukkit.getScheduler().cancelTask(t);
                updateScoreboard();
            }


        }, 0L, 1 * 20L);
    }

    int g = 0;
    public void riseFloor(Plugin plugin){
        sandlayer = getLocation().getBlockY();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        g = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            for(int x = 0; x< 61; x++){
                for(int z = 0; z< 61; z++){

                    Location check = new Location(location.getWorld(),x+location.getBlockX()-31 , sandlayer, z+location.getBlockZ()-31);
                    Block block = check.getBlock();
                    BlockState bs = block.getState();
                    if(bs instanceof Chest) {
                        ((Chest) bs).getInventory().clear();
                    }
                    block.setType(Material.AIR);
                    block.setType(Material.SANDSTONE);
                }
            }
            sandlayer ++;
            updateScoreboard();

        }, 5 * 20L, 5 * 20L);
    }

    public void fillChests(){
        for(int x = 0; x< 61; x++){
            for(int z = 0; z< 61; z++){
                for(int y = 0; y< 100; y++) {
                    Location check = new Location(location.getWorld(), x + location.getBlockX() - 31, location.getBlockY()+ y, z + location.getBlockZ() - 31);
                    if (check.getBlock() != null) {
                        Block block = check.getBlock();
                        if (block.getType() == Material.END_PORTAL_FRAME) {
                            block.setType(Material.CHEST);

                        }
                    }
                }
            }
        }

        for (Chunk c : getLocation().getWorld().getLoadedChunks()) {

            for (BlockState b : c.getTileEntities()) {

                if (b instanceof Chest) {
                    Chest chest = (Chest) b;

                    WoolWars.getChestManager().fillChest(chest);
                }
            }
        }
    }

    public void spawnFireworks(Location location, int amount){
        Location loc = location;
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(2);
        Random rand = new Random();
        int randomNum = rand.nextInt((5 - 1) + 1) + 1;

        if(randomNum == 1) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());
        }else if(randomNum == 2) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).build());
        }else if(randomNum == 3) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.BLUE).flicker(true).build());
        }else if(randomNum == 4) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.ORANGE).flicker(true).build());
        }else if(randomNum == 5) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.YELLOW).flicker(true).build());
        }

        fw.setFireworkMeta(fwm);
        fw.detonate();

        for(int i = 0;i<amount; i++){
            Firework fw2 = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            fw2.setFireworkMeta(fwm);
        }
    }

    int h = 0;
    int timeUntilTeleport = 0;
    public void startCelebration(Plugin plugin, Player winner){
        timeUntilTeleport = 5;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        h = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(timeUntilTeleport > 0){
                    timeUntilTeleport --;
                    spawnFireworks(winner.getLocation(), 2);
                }else{
                    Bukkit.getScheduler().cancelTask(h);
                    sendAllToLobby();
                    reset();
                }
            }
        }, 0L, 1 * 20L);
    }

}