package se.fredde.RAMChunk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Main
extends JavaPlugin
implements Listener {
    final JavaPlugin plugin;
    Logger logger;
    File directory;
    File file;
    FileConfiguration fileConfiguration;
    List<Chunk> chunks;
    String CHAT_PREFIX;
    String CHAT_SPACE;
    String CHAT_LINE;
    String CHAT_CHUNK_ADDED;
    String CHAT_CHUNK_REMOVED;
    String CHAT_CHUNK_FOUND;
    String CHAT_CHUNK_NOT_FOUND;
    String CHAT_CHUNK_NOT_NUMERIC;

    public Main() {
        this.plugin = this;
        this.CHAT_PREFIX = "&8[&b&lRAMChunk&8]&r";
        this.CHAT_SPACE = " ";
        this.CHAT_LINE = "&8&l------";
        this.CHAT_CHUNK_ADDED = "&aRAMChunk added.";
        this.CHAT_CHUNK_REMOVED = "&aRAMChunk removed.";
        this.CHAT_CHUNK_FOUND = "&cRAMChunk found.";
        this.CHAT_CHUNK_NOT_FOUND = "&cRAMChunk not found.";
        this.CHAT_CHUNK_NOT_NUMERIC = "&cNot numeric.";
    }

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.logger = this.getLogger();
        this.chunks = new ArrayList<Chunk>();
        this.createFile();
        this.saveFile();
        this.loadFile();
        this.loadAllRAMChunk();
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if (this.isRAMChunk(e.getChunk())) {
            e.setCancelled(true);
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Chunk chunk;
            final Player player = (Player)sender;
            if (label.equalsIgnoreCase("rc") && args.length == 1 && args[0].equalsIgnoreCase("list") && player.hasPermission("rc.list")) {
                this.showAllRAMChunk(player);
                return true;
            }
            if (label.equalsIgnoreCase("rc") && args.length == 1 && args[0].equalsIgnoreCase("add") && player.hasPermission("rc.add")) {
                chunk = player.getLocation().getChunk();
                if (this.isRAMChunk(chunk)) {
                    player.sendMessage(this.cc(this.CHAT_PREFIX + this.CHAT_SPACE + this.CHAT_CHUNK_FOUND));
                } else {
                    this.chunks.add(chunk);
                    player.sendMessage(this.cc(this.CHAT_PREFIX + this.CHAT_SPACE + this.CHAT_CHUNK_ADDED));
                    String name = chunk.getWorld().getName();
                    int x = chunk.getX();
                    int z = chunk.getZ();
                    String path = "chunk." + name + "_" + x + "_" + z + ".";
                    this.fileConfiguration.set(path + "name", (Object)name);
                    this.fileConfiguration.set(path + "x", (Object)x);
                    this.fileConfiguration.set(path + "z", (Object)z);
                    this.saveFile();
                    return true;
                }
            }
            if (label.equalsIgnoreCase("rc") && args.length == 2 && args[0].equalsIgnoreCase("remove") && player.hasPermission("rc.remove")) {
                if (Main.isNumeric(args[1])) {
                    chunk = player.getLocation().getChunk();
                    int index = Integer.valueOf(args[1]);
                    if (index >= 0 && index < this.chunks.size()) {
                        this.chunks.remove(index);
                        player.sendMessage(this.cc(this.CHAT_PREFIX + this.CHAT_SPACE + this.CHAT_CHUNK_REMOVED));
                        String name = chunk.getWorld().getName();
                        int x = chunk.getX();
                        int z = chunk.getZ();
                        String path = "chunk." + name + "_" + x + "_" + z + ".";
                        this.fileConfiguration.set(path, (Object)null);
                        this.saveFile();
                        return true;
                    }
                    player.sendMessage(this.cc(this.CHAT_PREFIX + this.CHAT_SPACE + this.CHAT_CHUNK_NOT_FOUND));
                } else {
                    player.sendMessage(this.cc(this.CHAT_PREFIX + this.CHAT_SPACE + this.CHAT_CHUNK_NOT_NUMERIC));
                }
            }
            if (label.equalsIgnoreCase("rc") && args.length == 2 && args[0].equalsIgnoreCase("tp") && player.hasPermission("rc.tp")) {
                if (Main.isNumeric(args[1])) {
                    int index = Integer.valueOf(args[1]);
                    if (index >= 0 && index < this.chunks.size()) {
                        Chunk chunk2 = this.chunks.get(index);
                        World world = player.getWorld();
                        int x = chunk2.getX() * 16;
                        int z = chunk2.getZ() * 16;
                        Location center = new Location(world, (double)(x + 7), (double)(world.getHighestBlockYAt(x + 7, z + 7) + 2), (double)(z + 7));
                        final Location corner1 = new Location(world, (double)x, (double)(world.getHighestBlockYAt(x, z) + 2), (double)z);
                        final Location corner2 = new Location(world, (double)(x + 15), (double)(world.getHighestBlockYAt(x + 15, z) + 2), (double)z);
                        final Location corner3 = new Location(world, (double)x, (double)(world.getHighestBlockYAt(x, z + 15) + 2), (double)(z + 15));
                        final Location corner4 = new Location(world, (double)(x + 15), (double)(world.getHighestBlockYAt(x + 15, z + 15) + 2), (double)(z + 15));
                        player.teleport(center);
                        final Effect effect = Effect.MOBSPAWNER_FLAMES;
                        player.getWorld().playEffect(corner1, effect, 0);
                        player.getWorld().playEffect(corner2, effect, 0);
                        player.getWorld().playEffect(corner3, effect, 0);
                        player.getWorld().playEffect(corner4, effect, 0);
                        new BukkitRunnable(){

                            public void run() {
                                player.getWorld().playEffect(corner1, effect, 0);
                                player.getWorld().playEffect(corner2, effect, 0);
                                player.getWorld().playEffect(corner3, effect, 0);
                                player.getWorld().playEffect(corner4, effect, 0);
                                new BukkitRunnable(){

                                    public void run() {
                                        player.getWorld().playEffect(corner1, effect, 0);
                                        player.getWorld().playEffect(corner2, effect, 0);
                                        player.getWorld().playEffect(corner3, effect, 0);
                                        player.getWorld().playEffect(corner4, effect, 0);
                                        new BukkitRunnable(){

                                            public void run() {
                                                player.getWorld().playEffect(corner1, effect, 0);
                                                player.getWorld().playEffect(corner2, effect, 0);
                                                player.getWorld().playEffect(corner3, effect, 0);
                                                player.getWorld().playEffect(corner4, effect, 0);
                                                new BukkitRunnable(){

                                                    public void run() {
                                                        player.getWorld().playEffect(corner1, effect, 0);
                                                        player.getWorld().playEffect(corner2, effect, 0);
                                                        player.getWorld().playEffect(corner3, effect, 0);
                                                        player.getWorld().playEffect(corner4, effect, 0);
                                                    }
                                                }.runTaskLater((Plugin)Main.this.plugin, 20);
                                            }

                                        }.runTaskLater((Plugin)Main.this.plugin, 20);
                                    }

                                }.runTaskLater((Plugin)Main.this.plugin, 20);
                            }

                        }.runTaskLater((Plugin)this.plugin, 20);
                        return true;
                    }
                    player.sendMessage(this.cc(this.CHAT_PREFIX + this.CHAT_SPACE + this.CHAT_CHUNK_NOT_FOUND));
                } else {
                    player.sendMessage(this.cc(this.CHAT_PREFIX + this.CHAT_SPACE + this.CHAT_CHUNK_NOT_NUMERIC));
                }
            }
        } else {
            this.logger.info("Commands only work in game.");
            return true;
        }
        return false;
    }

    public boolean isRAMChunk(Chunk chunk) {
        for (Chunk RAMChunk : this.chunks) {
            if (!RAMChunk.equals((Object)chunk)) continue;
            return true;
        }
        return false;
    }

    public void loadAllRAMChunk() {
        int amount = 0;
        for (Chunk RAMChunk : this.chunks) {
            RAMChunk.load(true);
            ++amount;
        }
        this.logger.info("Loaded " + amount + " chunk(s).");
    }

    public void showAllRAMChunk(Player player) {
        int index = 0;
        player.sendMessage(this.cc(this.CHAT_LINE + this.CHAT_PREFIX + this.CHAT_LINE));
        player.sendMessage("");
        if (this.chunks.size() > 0) {
            for (Chunk RAMChunk : this.chunks) {
                player.sendMessage(this.cc(this.CHAT_SPACE + "&7&lIndex: &f&l" + index));
                player.sendMessage(this.cc(this.CHAT_SPACE + "&7World: &f" + RAMChunk.getWorld().getName()));
                player.sendMessage(this.cc(this.CHAT_SPACE + "&7X: &f" + RAMChunk.getX() + "&7, Z: &f" + RAMChunk.getZ()));
                player.sendMessage(this.cc(this.CHAT_SPACE + "&7Loaded: &a" + Boolean.toString(RAMChunk.isLoaded())));
                player.sendMessage(this.cc(this.CHAT_SPACE + "&7Entities: &a" + RAMChunk.getEntities().length));
                player.sendMessage(this.cc(this.CHAT_SPACE + "&7TileEntities: &a" + RAMChunk.getTileEntities().length));
                player.sendMessage("");
                ++index;
            }
        } else {
            player.sendMessage(this.cc(this.CHAT_SPACE + "&7No RAMChunks added."));
            player.sendMessage(this.cc(this.CHAT_SPACE + "&7Use \"/rc add\"."));
            player.sendMessage("");
        }
        player.sendMessage(this.cc(this.CHAT_LINE + this.CHAT_PREFIX + this.CHAT_LINE));
    }

    public boolean createFile() {
        this.directory = new File(this.getDataFolder().toString());
        if (!this.directory.exists() && this.directory.mkdir()) {
            this.logger.info("Directory created.");
        }
        this.file = new File(this.getDataFolder(), "chunks.yml");
        if (!this.file.exists()) {
            try {
                if (this.file.createNewFile()) {
                    this.logger.info("File created.");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        this.fileConfiguration = YamlConfiguration.loadConfiguration((File)this.file);
        return true;
    }

    public boolean saveFile() {
        try {
            this.fileConfiguration.save(this.file);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadFile() {
        String path = "chunk";
        if (this.fileConfiguration.contains("chunk")) {
            ConfigurationSection RAMChunks = this.fileConfiguration.getConfigurationSection("chunk");
            for (String RAMChunk : RAMChunks.getKeys(false)) {
                String name = this.fileConfiguration.getString("chunk." + RAMChunk + ".name");
                int x = this.fileConfiguration.getInt("chunk." + RAMChunk + ".x");
                int z = this.fileConfiguration.getInt("chunk." + RAMChunk + ".z");
                if (this.getServer().getWorld(name) == null) continue;
                this.chunks.add(this.getServer().getWorld(name).getChunkAt(x, z));
            }
        }
        return true;
    }

    public String cc(String message) {
        return ChatColor.translateAlternateColorCodes((char)'&', (String)message);
    }

    public static boolean isNumeric(String message) {
        try {
            double d = Double.parseDouble(message);
        }
        catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
