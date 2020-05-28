package com.gabe.woolwars;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


public class KitManager {


    private WoolWars plugin;
    File customYml;
    FileConfiguration customConfig;

    private final Set<Kit> kits;

    public KitManager(WoolWars plugin){
        this.plugin = plugin;
        this.kits = new HashSet<>();
        customYml = new File(plugin.getDataFolder()+File.separator + "kits.yml");
        customConfig = YamlConfiguration.loadConfiguration(customYml);


        YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("kits.yml")));
        for (Map.Entry<String, Object> entry : config.getValues(true).entrySet()) {
            customConfig.addDefault(entry.getKey(), entry.getValue());
        }
        customConfig.options().copyDefaults(true);

        saveCustomYml(customConfig, customYml);

        ConfigurationSection section = customConfig.getConfigurationSection("kits");
        for(String kitname : section.getKeys(false)){
            List<ItemStack> items = new ArrayList<>();
            List<PotionEffect> effects = new ArrayList<>();

            for(String itemstr : customConfig.getStringList("kits."+kitname+".items")){
                String itemname = itemstr.split(":")[0];
                String itemamnt = itemstr.split(":")[1];

                Bukkit.getLogger().info("[KITS] added "+itemamnt+" of "+ itemname+".");

                items.add(new ItemStack(Material.getMaterial(itemname), Integer.valueOf(itemamnt)));
            }

            for(String potstr : customConfig.getStringList("kits."+kitname+".effects")){
                String potname = potstr.split(":")[0];
                String potamnt = potstr.split(":")[1];
                Bukkit.getLogger().info("[KITS] added "+potname+" "+ potamnt+".");

                effects.add(new PotionEffect(PotionEffectType.getByName(potname), 999999999, Integer.valueOf(potamnt)));
            }

            String itemtxt = customConfig.getString("kits."+kitname+".icon");
            String itemname = itemtxt.split(":")[0];
            int itemamnt = Integer.valueOf(itemtxt.split(":")[1]);
            ItemStack icon = new ItemStack(Material.getMaterial(itemname), itemamnt);
            Bukkit.getLogger().info("[KITS] set icon to "+itemamnt+" of "+itemname);

            List<String> coloredLore = new ArrayList<>();
            if(customConfig.getStringList("kits."+kitname+".lore") != null) {
                List<String> lore = customConfig.getStringList("kits." + kitname + ".lore");

                if (lore != null) {
                    for (String line : lore) {
                        coloredLore.add(WoolWars.color(line));
                    }
                }
            }

            Kit kit = new Kit(kitname, items, effects, icon, coloredLore);
            kits.add(kit);
        }

    }

    public void saveCustomYml(FileConfiguration ymlConfig, File ymlFile) {
        try {
            ymlConfig.save(ymlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<Kit> getKitsList(){
        return Collections.unmodifiableSet(kits);
    }

    public Kit getKit(String name){
        Kit kit = null;
        for (Kit k : getKitsList()){
            if(k.getName().equals(name)){
                kit = k;
            }
        }
        return kit;
    }
}
