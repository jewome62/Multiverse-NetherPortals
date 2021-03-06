package com.onarandombox.MultiverseNetherPortals.commands;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiverseNetherPortals.enums.PortalType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class LinkCommand extends NetherPortalCommand {
    private MVWorldManager worldManager;

    public LinkCommand(MultiverseNetherPortals plugin) {
        super(plugin);
        this.setName("Sets NP Destination");
        this.setCommandUsage("/mvnp link " + ChatColor.GREEN + "{end|nether} " + ChatColor.GOLD + "[FROM_WORLD] " + ChatColor.GREEN + " {TO_WORLD} {X} {Y} {Z}");
        this.setArgRange(2, 6);
        this.addKey("mvnp link");
        this.addKey("mvnpl");
        this.addKey("mvnplink");
        this.addCommandExample("/mvnp link end world world_nether");
        this.addCommandExample("/mvnp link end world world_nether x y z");
        this.addCommandExample("/mvnp link end world_nether");
        this.setPermission("multiverse.netherportals.link", "Sets which world to link to when a player enters a NetherPortal in this world.", PermissionDefault.OP);
        this.worldManager = this.plugin.getCore().getMVWorldManager();
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player) && args.size() == 2) {
            sender.sendMessage("From the command line, FROM_WORLD is required");
            sender.sendMessage("No changes were made...");
            return;
        }
        MultiverseWorld fromWorld;
        MultiverseWorld toWorld;
        String fromWorldString;
        String toWorldString;
        PortalType type;
        Player p;
        type = PortalType.parse(args.get(0).toUpperCase());
        if (args.size() == 2) {
            p = (Player) sender;
            fromWorldString = p.getWorld().getName();
            toWorldString = args.get(1);
        } else {
            fromWorldString = args.get(1);
            toWorldString = args.get(2);
        }

        if (type == null) {
            sender.sendMessage("The type must either be 'end' or 'nether'");
            return;
        }

        fromWorld = this.worldManager.getMVWorld(fromWorldString);
        toWorld = this.worldManager.getMVWorld(toWorldString);

        if (fromWorld == null) {
            this.plugin.getCore().showNotMVWorldMessage(sender, fromWorldString);
            return;
        }
        if (toWorld == null) {
            this.plugin.getCore().showNotMVWorldMessage(sender, toWorldString);
            return;
        }
        
        if ((args.size() > 3)) {
            if((args.size() < 6)){
                sender.sendMessage("You must add complete coordonate");
                return;
            }
            double x = Double.parseDouble(args.get(3));
            double y = Double.parseDouble(args.get(4));
            double z = Double.parseDouble(args.get(5));
            
            this.plugin.addWorldLink(fromWorld.getName(), toWorld.getName(), type, x, y, z);
        } else {
            this.plugin.addWorldLink(fromWorld.getName(), toWorld.getName(), type);
        }

        
        String coloredFrom = fromWorld.getColoredWorldString();
        String coloredTo = toWorld.getColoredWorldString();
        if (fromWorld.getName().equals(toWorld.getName())) {
            sender.sendMessage(ChatColor.RED + "NOTE: " + ChatColor.WHITE + "You have successfully disabled " + type.toString() + " Portals in " + coloredTo);
        } else {
            sender.sendMessage("The " + type + " portals in " + coloredFrom + ChatColor.WHITE + " are now linked to " + coloredTo);
        }

    }

}
