package com.gabe.woolwars;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class AdminTabCompletions implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> c = new ArrayList<>();
        if(args.length == 1){
            List<String> commands = new ArrayList<>();
            commands.add("remove");
            commands.add("create");
            commands.add("list");
            commands.add("help");
            commands.add("setlobby");


            for(String cmd: commands){
                if(cmd.contains(args[0])){
                    c.add(cmd);
                }
            }


        }
        return c;
    }
}
