package com.onarandombox.MultiverseNetherPortals;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiverseNetherPortals.commands.LinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.ShowLinkCommand;
import com.onarandombox.MultiverseNetherPortals.commands.UnlinkCommand;
import com.onarandombox.MultiverseNetherPortals.enums.PortalType;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPCoreListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPEntityListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPlayerListener;
import com.onarandombox.MultiverseNetherPortals.listeners.MVNPPluginListener;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiverseNetherPortals.utils.MVLink;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class MultiverseNetherPortals extends JavaPlugin implements MVPlugin {

    private static final String NETHER_PORTALS_CONFIG = "config.yml";
    protected MultiverseCore core;
    protected Plugin multiversePortals;
    protected MVNPPluginListener pluginListener;
    protected MVNPPlayerListener playerListener;
    protected MVNPCoreListener customListener;
    protected FileConfiguration MVNPconfiguration;
    private static final String DEFAULT_NETHER_SUFFIX = "_nether";
    private static final String DEFAULT_END_SUFFIX = "_the_end";
    private String netherPrefix = "";
    private String netherSuffix = DEFAULT_NETHER_SUFFIX;
    private String endPrefix = "";
    private String endSuffix = DEFAULT_END_SUFFIX;
    private Map<String, MVLink> netherLinkMap;
    private Map<String, MVLink> endLinkMap;
    protected CommandHandler commandHandler;
    private final static int requiresProtocol = 9;
    private MVNPEntityListener entityListener;

    @Override
    public void onEnable() {
        Logging.init(this);
        this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
        this.multiversePortals = getServer().getPluginManager().getPlugin("Multiverse-Portals");

        // Test if the Core was found, if not we'll disable this plugin.
        if (this.core == null) {
            Logging.info("Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.core.getProtocolVersion() < requiresProtocol) {
            Logging.severe("Your Multiverse-Core is OUT OF DATE");
            Logging.severe("This version of NetherPortals requires Protocol Level: " + requiresProtocol);
            Logging.severe("Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            Logging.severe("Grab an updated copy at: ");
            Logging.severe("http://dev.bukkit.org/bukkit-plugins/multiverse-core/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.core.incrementPluginCount();
        // As soon as we know MVCore was found, we can use the debug log!

        this.pluginListener = new MVNPPluginListener(this);
        this.playerListener = new MVNPPlayerListener(this);
        this.entityListener = new MVNPEntityListener(this);
        this.customListener = new MVNPCoreListener(this);
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this.pluginListener, this);
        pm.registerEvents(this.playerListener, this);
        pm.registerEvents(this.entityListener, this);
        pm.registerEvents(this.customListener, this);

        Logging.info("- Version Edited by Jewome62" + this.getDescription().getVersion() + " Enabled - By " + getAuthors());

        loadConfig();
        this.registerCommands();

    }

    public void loadConfig() {
        this.MVNPconfiguration = new YamlConfiguration();
        try {
            this.MVNPconfiguration.load(new File(this.getDataFolder(), NETHER_PORTALS_CONFIG));
        } catch (IOException e) {
            this.log(Level.SEVERE, "Could not load " + NETHER_PORTALS_CONFIG);
        } catch (InvalidConfigurationException e) {
            this.log(Level.SEVERE, NETHER_PORTALS_CONFIG + " contained INVALID YAML. Please look at the file.");
        }
        this.netherLinkMap = new HashMap<String, MVLink>();
        this.endLinkMap = new HashMap<String, MVLink>();

        this.setUsingBounceBack(this.isUsingBounceBack());

        this.setNetherPrefix(this.MVNPconfiguration.getString("netherportals.name.prefix", this.getNetherPrefix()));
        this.setNetherSuffix(this.MVNPconfiguration.getString("netherportals.name.suffix", this.getNetherSuffix()));

        if (this.getNetherPrefix().length() == 0 && this.getNetherSuffix().length() == 0) {
            Logging.warning("I didn't find a prefix OR a suffix defined! I made the suffix \"" + DEFAULT_NETHER_SUFFIX + "\" for you.");
            this.setNetherSuffix(this.MVNPconfiguration.getString("netherportals.name.suffix", this.getNetherSuffix()));
        }
        if (this.MVNPconfiguration.getConfigurationSection("worlds") == null) {
            this.MVNPconfiguration.createSection("worlds");
        }
        Set<String> worldKeys = this.MVNPconfiguration.getConfigurationSection("worlds").getKeys(false);
        if (worldKeys != null) {
            for (String worldString : worldKeys) {
                String nether = this.MVNPconfiguration.getString("worlds." + worldString + ".portalgoesto.NETHER.destination", null);
                Double xNether = this.MVNPconfiguration.getDouble("worlds." + worldString + ".portalgoesto.NETHER.x", 0.0);
                Double yNether = this.MVNPconfiguration.getDouble("worlds." + worldString + ".portalgoesto.NETHER.y", 0.0);
                Double zNether = this.MVNPconfiguration.getDouble("worlds." + worldString + ".portalgoesto.NETHER.z", 0.0);
                String end = this.MVNPconfiguration.getString("worlds." + worldString + ".portalgoesto.END.destination", null);
                Double xEnd = this.MVNPconfiguration.getDouble("worlds." + worldString + ".portalgoesto.END.x", 0.0);
                Double yEnd = this.MVNPconfiguration.getDouble("worlds." + worldString + ".portalgoesto.END.y", 0.0);
                Double zEnd = this.MVNPconfiguration.getDouble("worlds." + worldString + ".portalgoesto.END.z", 0.0);
                if (nether != null) {
                    MVLink netherLink = new MVLink(nether,xNether,yNether,zNether);
                    Logging.info("Put Nether link "+worldString+" => "+nether+" ["+xNether+","+yNether+","+zNether+"]");
                    this.netherLinkMap.put(worldString, netherLink);
                }
                if (end != null) {
                    MVLink endLink = new MVLink(end,xEnd,yEnd,zEnd);
                    Logging.info("Put End link "+worldString+" => "+end+" ["+xEnd+","+yEnd+","+zEnd+"]");
                    this.endLinkMap.put(worldString, endLink);
                }

            }
        }
        this.saveMVNPConfig();
    }

    /** Register commands to Multiverse's CommandHandler so we get a super sexy single menu */
    private void registerCommands() {
        this.commandHandler = this.core.getCommandHandler();
        this.commandHandler.registerCommand(new LinkCommand(this));
        this.commandHandler.registerCommand(new UnlinkCommand(this));
        this.commandHandler.registerCommand(new ShowLinkCommand(this));
        for (com.pneumaticraft.commandhandler.multiverse.Command c : this.commandHandler.getAllCommands()) {
            if (c instanceof HelpCommand) {
                c.addKey("mvnp");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!this.isEnabled()) {
            sender.sendMessage("This plugin is Disabled!");
            return true;
        }
        ArrayList<String> allArgs = new ArrayList<String>(Arrays.asList(args));
        allArgs.add(0, command.getName());
        return this.commandHandler.locateAndRunCommand(sender, allArgs);
    }

    @Override
    public void onDisable() {
        Logging.info("- Disabled");
    }

    @Override
    public void onLoad() {
        getDataFolder().mkdirs();
    }

    private String getAuthors() {
        String authors = "";
        for (int i = 0; i < this.getDescription().getAuthors().size(); i++) {
            if (i == this.getDescription().getAuthors().size() - 1) {
                authors += " and " + this.getDescription().getAuthors().get(i);
            } else {
                authors += ", " + this.getDescription().getAuthors().get(i);
            }
        }
        return authors.substring(2);
    }

    public void setNetherPrefix(String netherPrefix) {
        this.netherPrefix = netherPrefix;
    }

    public String getNetherPrefix() {
        return this.netherPrefix;
    }

    public void setNetherSuffix(String netherSuffix) {
        this.netherSuffix = netherSuffix;
    }

    public String getNetherSuffix() {
        return this.netherSuffix;
    }

    public String getEndPrefix() {
        return this.endPrefix;
    }

    public String getEndSuffix() {
        return this.endSuffix;
    }

    public MVLink getWorldLink(String fromWorld, PortalType type) {
      
        if (type == PortalType.NETHER) {
            return this.netherLinkMap.get(fromWorld);
        } else if (type == PortalType.END) {
            return this.endLinkMap.get(fromWorld);
        }

        return null;
    }

    public Map<String, MVLink> getWorldLinks() {
        return this.netherLinkMap;
    }

    public Map<String, MVLink> getEndWorldLinks() {
        return this.endLinkMap;
    }

    public boolean addWorldLink(String from, String to, PortalType type) {
        if (type == PortalType.NETHER) {
            MVLink netherLink = new MVLink(to);
            this.netherLinkMap.put(from, netherLink);
        } else if (type == PortalType.END) {
            MVLink endLink = new MVLink(to);
            this.endLinkMap.put(from, endLink);
        } else {
            return false;
        }

        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".destination", to);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".x", null);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".y", null);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".z", null);
        this.saveMVNPConfig();
        return true;
    }
    
     public boolean addWorldLink(String from, String to, PortalType type, double x, double y, double z) {
        if (type == PortalType.NETHER) {
            MVLink netherLink = new MVLink(to, x, y, z);
            this.netherLinkMap.put(from, netherLink);
        } else if (type == PortalType.END) {
            MVLink endLink = new MVLink(to, x, y, z);
            this.endLinkMap.put(from, endLink);
        } else {
            return false;
        }

        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".destination", to);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".x", x);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".y", y);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".z", z);
        this.saveMVNPConfig();
        return true;
    }

    public void removeWorldLink(String from, String to, PortalType type) {
        if (type == PortalType.NETHER) {
            this.netherLinkMap.remove(from);
        } else if (type == PortalType.END) {
            this.endLinkMap.remove(from);
        } else {
            return;
        }

        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".destination", null);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".x", null);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".y", null);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type +".z", null);
        this.MVNPconfiguration.set("worlds." + from + ".portalgoesto." + type, null);
        this.saveMVNPConfig();
    }

    public boolean saveMVNPConfig() {
        try {
            this.MVNPconfiguration.save(new File(this.getDataFolder(), NETHER_PORTALS_CONFIG));
            return true;
        } catch (IOException e) {
            this.log(Level.SEVERE, "Could not save " + NETHER_PORTALS_CONFIG);
        }
        return false;
    }

    public boolean isUsingBounceBack() {
        return this.MVNPconfiguration.getBoolean("bounceback", true);
    }

    public void setUsingBounceBack(boolean useBounceBack) {
        this.MVNPconfiguration.set("bounceback", useBounceBack);
    }

    public boolean isHandledByNetherPortals(Location l) {
        if (multiversePortals != null) {
            // Catch errors which could occur if classes aren't present or are missing methods.
            try {
                MultiversePortals portals = (MultiversePortals) multiversePortals;
                if (portals.getPortalManager().isPortal(l)) {
                    return false;
                }
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Error checking if portal is handled by Multiverse-Portals", t);
            }
        }
        return true;
    }

    public void setPortals(Plugin multiversePortals) {
        this.multiversePortals = multiversePortals;
    }

    public Plugin getPortals() {
        return multiversePortals;
    }

    @Override
    public MultiverseCore getCore() {
        return this.core;
    }

    @Override
    public void log(Level level, String msg) {
        Logging.log(level, msg);
    }

    @Override
    public void setCore(MultiverseCore core) {
        this.core = core;
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    @Override
    public String dumpVersionInfo(String buffer) {
        buffer += logAndAddToPasteBinBuffer("Multiverse-NetherPortals Version: " + this.getDescription().getVersion() + " Edit by jewome62\n");
        buffer += logAndAddToPasteBinBuffer("Bukkit Version: " + this.getServer().getVersion());
        buffer += logAndAddToPasteBinBuffer("World links: " + this.getWorldLinks());
        buffer += logAndAddToPasteBinBuffer("Nether Prefix: " + netherPrefix);
        buffer += logAndAddToPasteBinBuffer("Nether Suffix: " + netherSuffix);
        buffer += logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }

    private String logAndAddToPasteBinBuffer(String string) {
        this.log(Level.INFO, string);
        return "[Multiverse-NetherPortals] " + string + "\n";
    }

    public String getVersionInfo() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[Multiverse-NetherPortals] Multiverse-NetherPortals Version: ").append(this.getDescription().getVersion()).append("Edit by jewome62\n");
        buffer.append("[Multiverse-NetherPortals] World links: ").append(this.getWorldLinks()).append('\n');
        buffer.append("[Multiverse-NetherPortals] Nether Prefix: ").append(netherPrefix).append('\n');
        buffer.append("[Multiverse-NetherPortals] Nether Suffix: ").append(netherSuffix).append('\n');
        buffer.append("[Multiverse-NetherPortals] Special Code: ").append("FRN001").append('\n');
        return buffer.toString();
    }
}
