package com.gabe.woolwars;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collections;
import java.util.List;

public class Kit {

    private final String name;
    private final List<ItemStack> items;
    private final List<PotionEffect> effects;
    private final List<String> lore;
    private final List<KitPower> powers;
    private final ItemStack icon;

    public Kit(String name, List<ItemStack> items, List<PotionEffect> effects, ItemStack icon, List<String> lore, List<KitPower> powers){
        this.name = name;
        this.items = items;
        this.effects = effects;
        this.icon = icon;
        this.lore = lore;
        this.powers = powers;
    }

    public List<KitPower> getPowers() {
        return powers;
    }

    public String getName(){
        return name;
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public List<ItemStack> getItems(){
        return Collections.unmodifiableList(items);
    }

    public List<PotionEffect> getEffects(){
        return Collections.unmodifiableList(effects);
    }

    public ItemStack getIcon() {
        return icon;
    }
}
