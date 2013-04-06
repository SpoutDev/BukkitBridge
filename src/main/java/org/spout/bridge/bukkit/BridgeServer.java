/*
 * This file is part of BukkitBridge.
 *
 * Copyright (c) 2012, VanillaDev <http://www.spout.org/>
 * BukkitBridge is licensed under the GNU General Public License.
 *
 * BukkitBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spout.bridge.bukkit;

import com.avaje.ebean.config.ServerConfig;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Warning.WarningState;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.permissions.DefaultPermissions;

import org.spout.api.util.access.BanType;

import org.spout.bridge.BukkitUtil;
import org.spout.bridge.VanillaBridgePlugin;
import org.spout.bridge.bukkit.command.BridgeConsoleCommandSender;
import org.spout.bridge.bukkit.entity.EntityFactory;
import org.spout.bridge.bukkit.scheduler.BridgeScheduler;

import org.spout.vanilla.data.configuration.VanillaConfiguration;
import org.spout.vanilla.data.configuration.WorldConfiguration;
import org.spout.vanilla.inventory.recipe.VanillaRecipes;

/**
 * BridgeServer is Bridge's version of Bukkit's Server.
 */
public class BridgeServer implements Server {
	private final VanillaBridgePlugin plugin;
	private final org.spout.api.Server server;
	private final SimpleServicesManager servicesManager;
	private final BridgeScheduler scheduler;
	private final PluginManager pluginManager;
	private final String bridgeVersion = getPOMVersion();
	private final String serverVersion;
	private final ConsoleCommandSender consoleSender;

	public BridgeServer(org.spout.api.Server server, VanillaBridgePlugin plugin) {
		this.server = server;
		this.plugin = plugin;
		this.consoleSender = new BridgeConsoleCommandSender();
		serverVersion = "Spout Server ( " + server.getVersion() + " )";
		servicesManager = new SimpleServicesManager();
		scheduler = new BridgeScheduler(plugin);
		pluginManager = new ForwardingPluginManager(this);
		org.bukkit.enchantments.Enchantment.stopAcceptingRegistrations();
		PotionEffectType.stopAcceptingRegistrations();
	}

	public void loadPlugins() {
		pluginManager.registerInterface(JavaPluginLoader.class);

		File pluginFolder = new File(plugin.getDataFolder(), "plugins");

		if (pluginFolder.exists()) {
			Plugin[] plugins = pluginManager.loadPlugins(pluginFolder);
			for (Plugin plugin : plugins) {
				try {
					String message = String.format("Loading %s", plugin.getDescription().getFullName());
					plugin.getLogger().info(message);
					plugin.onLoad();
				} catch (Throwable ex) {
					getLogger().log(Level.SEVERE, ex.getMessage() + " initializing " + plugin.getDescription().getFullName(), ex);
				}
			}
		} else {
			pluginFolder.mkdirs();
		}
	}

	public void enablePlugins(PluginLoadOrder type) {
		Plugin[] plugins = pluginManager.getPlugins();

		for (Plugin plugin : plugins) {
			if ((!plugin.isEnabled()) && (plugin.getDescription().getLoad() == type)) {
				loadPlugin(plugin);
			}
		}

		if (type == PluginLoadOrder.POSTWORLD) {
			DefaultPermissions.registerCorePermissions();
		}
	}

	public void disablePlugins() {
		pluginManager.disablePlugins();
	}

	private void loadPlugin(Plugin plugin) {
		try {
			pluginManager.enablePlugin(plugin);

			List<Permission> perms = plugin.getDescription().getPermissions();

			for (Permission perm : perms) {
				try {
					pluginManager.addPermission(perm);
				} catch (IllegalArgumentException ex) {
					getLogger().log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + perm.getName() + "' but it's already registered", ex);
				}
			}
		} catch (Throwable ex) {
			getLogger().log(Level.SEVERE, ex.getMessage() + " loading " + plugin.getDescription().getFullName(), ex);
		}
	}

	@Override
	public Set<String> getListeningPluginChannels() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addRecipe(Recipe recipe) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void banIP(String ip) {
		server.getAccessManager().ban(BanType.IP, ip);
	}

	@Override
	public int broadcast(String perm, String message) {
		server.broadcastMessage(perm, message);
		return 0;
	}

	@Override
	public int broadcastMessage(String message) {
		server.broadcastMessage(message);
		return 0;
	}

	@Override
	public void clearRecipes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void configureDbConfig(ServerConfig arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Inventory createInventory(InventoryHolder arg0, InventoryType arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Inventory createInventory(InventoryHolder arg0, int arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Inventory createInventory(InventoryHolder arg0, int arg1, String arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MapView createMap(World arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public World createWorld(WorldCreator arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean dispatchCommand(CommandSender sender, String commandLine) throws CommandException {
		if (sender instanceof Player) {
			((Player) sender).performCommand(commandLine);
		} else if (sender instanceof ConsoleCommandSender) {
			BukkitUtil.processCommand(server.getCommandSource(), commandLine);
		}
		return true;
	}

	@Override
	public boolean getAllowEnd() {
		return WorldConfiguration.THE_END.LOAD.getBoolean();
	}

	@Override
	public boolean getAllowFlight() {
		return WorldConfiguration.NORMAL.ALLOW_FLIGHT.getBoolean();
	}

	@Override
	public boolean isHardcore() {
		return false;  //TODO:
	}

	@Override
	public boolean getAllowNether() {
		return WorldConfiguration.NETHER.LOAD.getBoolean();
	}

	@Override
	public int getAnimalSpawnLimit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<OfflinePlayer> getBannedPlayers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getBukkitVersion() {
		return bridgeVersion;
	}

	@Override
	public Map<String, String[]> getCommandAliases() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getConnectionThrottle() {
		return 0;
	}

	@Override
	public ConsoleCommandSender getConsoleSender() {
		return consoleSender;
	}

	@Override
	public GameMode getDefaultGameMode() {
		return GameMode.valueOf(WorldConfiguration.NORMAL.GAMEMODE.getString().toUpperCase());
	}

	@Override
	public boolean getGenerateStructures() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HelpMap getHelpMap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getIPBans() {
		return new HashSet<String>(server.getAccessManager().getBanned(BanType.IP));
	}

	@Override
	public String getIp() {
		return "";
	}

	@Override
	public Logger getLogger() {
		return server.getLogger();
	}

	@Override
	public MapView getMap(short arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxPlayers() {
		return server.getMaxPlayers();
	}

	@Override
	public Messenger getMessenger() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMonsterSpawnLimit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMotd() {
		return VanillaConfiguration.MOTD.getString();
	}

	@Override
	public String getShutdownMessage() {
		return null;  //TODO
	}

	@Override
	public String getName() {
		return server.getName();
	}

	@Override
	public OfflinePlayer getOfflinePlayer(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OfflinePlayer[] getOfflinePlayers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getOnlineMode() {
		return VanillaConfiguration.ONLINE_MODE.getBoolean();
	}

	@Override
	public Player[] getOnlinePlayers() {
		org.spout.api.entity.Player[] online = server.getOnlinePlayers();
		Player[] copy = new Player[online.length];
		for (int i = 0; i < online.length; i++) {
			copy[i] = EntityFactory.createPlayer(online[i]);
		}
		return copy;
	}

	@Override
	public Set<OfflinePlayer> getOperators() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Player getPlayer(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Player getPlayerExact(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PluginCommand getPluginCommand(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	@Override
	public int getPort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Recipe> getRecipesFor(ItemStack arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BukkitScheduler getScheduler() {
		return scheduler;
	}

	@Override
	public String getServerId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServerName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	@Override
	public int getSpawnRadius() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTicksPerAnimalSpawns() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTicksPerMonsterSpawns() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUpdateFolder() {
		return "update";
	}

	@Override
	public File getUpdateFolderFile() {
		return new File(plugin.getDataFolder(), "update");
	}

	@Override
	public String getVersion() {
		return serverVersion;
	}

	@Override
	public int getViewDistance() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getWaterAnimalSpawnLimit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getAmbientSpawnLimit() {
		return 0;  //TODO
	}

	@Override
	public Set<OfflinePlayer> getWhitelistedPlayers() {
		HashSet<OfflinePlayer> set = new HashSet<OfflinePlayer>();
		for (String name : server.getAccessManager().getWhitelistedPlayers()) {
			set.add(getOfflinePlayer(name));
		}
		return set;
	}

	@Override
	public BridgeWorld getWorld(String name) {
		for (World w : getWorlds()) {
			if (w.getName().equals(name)) {
				return (BridgeWorld) w;
			}
		}
		return null;
	}

	@Override
	public BridgeWorld getWorld(UUID uid) {
		for (World w : getWorlds()) {
			if (w.getUID().equals(uid)) {
				return (BridgeWorld) w;
			}
		}
		return null;
	}

	@Override
	public File getWorldContainer() {
		return server.getWorldFolder();
	}

	@Override
	public String getWorldType() {
		return "DEFAULT";
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public List<World> getWorlds() {
		return (List<World>) (List) plugin.getWorldListener().getWorlds();
	}

	@Override
	public boolean hasWhitelist() {
		return server.getAccessManager().isWhitelistEnabled();
	}

	@Override
	public boolean isPrimaryThread() {
		return server.getMainThread() == Thread.currentThread();
	}

	@Override
	public List<Player> matchPlayer(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Recipe> recipeIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reload() {
	}

	@Override
	public void reloadWhitelist() {
		server.getAccessManager().load();
	}

	@Override
	public void resetRecipes() {
		this.clearRecipes();
		VanillaRecipes.initialize();
	}

	@Override
	public void savePlayers() {
		for (org.spout.api.entity.Player p : server.getOnlinePlayers()) {
			p.save();
		}
	}

	@Override
	public void setDefaultGameMode(GameMode mode) {
		WorldConfiguration.NORMAL.GAMEMODE.setValue(mode.name().toLowerCase());
		WorldConfiguration.FLAT.GAMEMODE.setValue(mode.name().toLowerCase());
		WorldConfiguration.NETHER.GAMEMODE.setValue(mode.name().toLowerCase());
		WorldConfiguration.THE_END.GAMEMODE.setValue(mode.name().toLowerCase());
	}

	@Override
	public void setSpawnRadius(int r) {
		VanillaConfiguration.SPAWN_PROTECTION_RADIUS.setValue(r);
	}

	@Override
	public void setWhitelist(boolean whitelisted) {
		server.getAccessManager().setWhitelistEnabled(whitelisted);
	}

	@Override
	public void shutdown() {
		server.stop();
	}

	@Override
	public void unbanIP(String ip) {
		server.getAccessManager().unban(BanType.IP, ip);
	}

	@Override
	public boolean unloadWorld(String name, boolean save) {
		return unloadWorld(getWorld(name), save);
	}

	@Override
	public boolean unloadWorld(World world, boolean save) {
		if (world != null) {
			((BridgeWorld) world).getHandle().unload(save);
			return true;
		}
		return false;
	}

	@Override
	public boolean useExactLoginLocation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WarningState getWarningState() {
		return WarningState.DEFAULT;
	}

	@Override
	public ItemFactory getItemFactory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ScoreboardManager getScoreboardManager() {
		throw new UnsupportedOperationException();
	}

	private static String getPOMVersion() {
		String result = "Unknown-Version";
		InputStream stream = Bukkit.class.getClassLoader().getResourceAsStream("META-INF/maven/org.spout/bukkitbridge/pom.properties");
		Properties properties = new Properties();
		if (stream != null) {
			try {
				properties.load(stream);
				result = properties.getProperty("version");
			} catch (IOException ex) {
				VanillaBridgePlugin.getInstance().getEngine().getLogger().severe("Could not get Bridge version!\n" + ex);
			}
		}
		return result;
	}
}
