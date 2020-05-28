package com.gabe.woolwars;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class ChestManager {

    private WoolWars plugin;
    File customYml;
    FileConfiguration customConfig;


    public ChestManager(WoolWars plugin) {
        this.plugin = plugin;
        customYml = new File(plugin.getDataFolder() + File.separator + "chests.yml");
        customConfig = YamlConfiguration.loadConfiguration(customYml);

        if(!customYml.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("chests.yml")));
            for (Map.Entry<String, Object> entry : config.getValues(true).entrySet()) {
                customConfig.addDefault(entry.getKey(), entry.getValue());
            }
        }


        customConfig.options().copyDefaults(true);

        saveCustomYml(customConfig, customYml);
    }
    public void saveCustomYml(FileConfiguration ymlConfig, File ymlFile) {
        try {
            ymlConfig.save(ymlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void fillChest(Chest chest){
        Inventory inv = chest.getBlockInventory();
        inv.clear();

        int min = customConfig.getInt("minitems");
        int max = customConfig.getInt("maxitems");

        int amount = new Random().nextInt(max-min);
        amount += min;

        List<ItemStack> items = new ArrayList<>();

        for(int i = 0; i< max; i++){
            int choice = new Random().nextInt(customConfig.getStringList("items").size());
            String itemtxt = customConfig.getStringList("items").get(choice);
            if(itemtxt != null){
                String itemname = itemtxt.split(":")[0];
                String itemamnt = itemtxt.split(":")[1];
                ItemStack item = new ItemStack(Material.getMaterial(itemname), Integer.valueOf(itemamnt));
                items.add(item);

            }
        }

        int[] slots = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26};
        List<Integer> used = new ArrayList<>();
        for(ItemStack item: items){
            int slot= new Random().nextInt(slots.length);
            while (used.contains(slot)){
                slot= new Random().nextInt(slots.length);
            }
            inv.setItem(slot, item);
        }



    }
}
