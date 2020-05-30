package com.gabe.woolwars;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ArenaResseter {

    private WoolWars plugin;

    public ArenaResseter(WoolWars plugin){
        this.plugin = plugin;
    }






    public void reset(String name) throws IOException {

        Location location = WoolWars.getArenaManager().getArena(name).getLocation();
        for(int x = 0; x< 61; x++){
            for(int z = 0; z< 61; z++){
                for(int y = 0; y< 120; y++) {
                    Location check = new Location(location.getWorld(), x + location.getBlockX() - 31, location.getBlockY()+ y-20, z + location.getBlockZ() - 31);
                    if (check.getBlock() != null) {
                        Block block = check.getBlock();
                        block.setType(Material.AIR);
                    }
                }
            }
        }


        File myfile = new File(plugin.getDataFolder().getAbsolutePath() + "/"+name+"_reset.schem");

        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(WoolWars.getArenaManager().getArena(name).getLocation().getWorld());

        ClipboardFormat format = ClipboardFormats.findByFile(myfile);

        try (ClipboardReader reader = format.getReader(new FileInputStream(myfile))) {

            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld,
                    -1)) {

                Location loc = WoolWars.getArenaManager().getArena(name).getLocation();
                int x = loc.getBlockX();
                int y = loc.getBlockY();
                int z = loc.getBlockZ();
                Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                        .to(BlockVector3.at(x, y, z)).ignoreAirBlocks(false).build();

                try {
                    Operations.complete(operation);
                    editSession.flushSession();

                    List<Entity> entList = WoolWars.getArenaManager().getArena(name).getLocation().getWorld().getEntities();//get all entities in the world

                    for(Entity current : entList) {//loop through the list
                        if (current instanceof Item) {//make sure we aren't deleting mobs/players
                            current.remove();//remove it
                        }
                    }

                } catch (WorldEditException e) {
                    //player.sendMessage(ChatColor.RED + "OOPS! Something went wrong, please contact an administrator");
                    e.printStackTrace();
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
