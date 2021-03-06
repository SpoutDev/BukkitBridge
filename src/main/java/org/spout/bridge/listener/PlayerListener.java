/*
 * This file is part of BukkitBridge.
 *
 * Copyright (c) 2012 Spout LLC <http://www.spout.org/>
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
package org.spout.bridge.listener;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.inventory.ItemStack;

import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Order;
import org.spout.api.event.player.Action;
import org.spout.api.event.player.PlayerChatEvent;
import org.spout.api.event.player.PlayerInteractBlockEvent;
import org.spout.api.event.player.PlayerInteractEntityEvent;
import org.spout.api.event.player.PlayerJoinEvent;
import org.spout.api.event.player.PlayerKickEvent;
import org.spout.api.event.player.PlayerLeaveEvent;
import org.spout.api.event.player.PlayerLoginEvent;
import org.spout.api.event.server.PreCommandEvent;
import org.spout.api.material.MaterialRegistry;

import org.spout.bridge.BukkitBridgePlugin;
import org.spout.bridge.BukkitUtil;
import org.spout.bridge.bukkit.entity.BridgePlayer;
import org.spout.bridge.bukkit.entity.EntityFactory;
import org.spout.bridge.player.PlayerMoveComponent;

import org.spout.vanilla.component.entity.inventory.PlayerInventory;
import org.spout.vanilla.component.entity.living.Human;
import org.spout.vanilla.event.player.PlayerRespawnEvent;

public class PlayerListener extends AbstractListener {
	/**
	 * Maintains a list of players from pre-login events to kick on login events, as Spout does not allow kicking in pre-login.
	 */
	private Map<String, String> toKick = new ConcurrentHashMap<String, String>();

	public PlayerListener(BukkitBridgePlugin plugin) {
		super(plugin);
	}

	@EventHandler (order = Order.EARLIEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		// Handle the Bukkit pre-login kicks
		String kickMessage = toKick.remove(event.getPlayer().getName());
		if (kickMessage != null) {
			event.setAllowed(false);
			event.setMessage(kickMessage);
			return;
		}

		BridgePlayer player = EntityFactory.createPlayer(event.getPlayer());
		String hostname = event.getPlayer().getNetwork().getAddress().getHostName();
		org.bukkit.event.player.PlayerLoginEvent login = new org.bukkit.event.player.PlayerLoginEvent(player, hostname, event.getPlayer().getNetwork().getAddress());
		if (!event.isAllowed()) {
			login.disallow(Result.KICK_OTHER, event.getMessage());
		}
		Bukkit.getPluginManager().callEvent(login);
		if (login.getResult() != Result.ALLOWED) {
			event.disallow(login.getKickMessage());
		}
	}

	@EventHandler (order = Order.EARLIEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Re-fire event
		BridgePlayer player = EntityFactory.createPlayer(event.getPlayer());
		final String joinMessage = event.getMessage();
		org.bukkit.event.player.PlayerJoinEvent join = new org.bukkit.event.player.PlayerJoinEvent(player, joinMessage);
		Bukkit.getPluginManager().callEvent(join);
		if (!joinMessage.equals(join.getJoinMessage())) {
			event.setMessage(join.getJoinMessage());
		}

		// Add components
		event.getPlayer().add(PlayerMoveComponent.class);
	}

	@EventHandler (order = Order.EARLIEST)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		BridgePlayer player = EntityFactory.createPlayer(event.getPlayer());
		final String leaveMessage = event.getMessage();
		if (event instanceof PlayerKickEvent) {
			PlayerKickEvent kickEvent = (PlayerKickEvent) event;
			final String kickMessage = kickEvent.getKickReason();
			org.bukkit.event.player.PlayerKickEvent kick = new org.bukkit.event.player.PlayerKickEvent(player, kickMessage, leaveMessage);
			kick.setCancelled(event.isCancelled());
			Bukkit.getPluginManager().callEvent(kick);
			if (!kickMessage.equals(kick.getReason())) {
				kickEvent.setKickReason(kick.getReason());
			}
			if (!leaveMessage.equals(kick.getLeaveMessage())) {
				kickEvent.setMessage(kick.getLeaveMessage());
			}
			kickEvent.setCancelled(kick.isCancelled());
		} else {
			org.bukkit.event.player.PlayerQuitEvent quit = new org.bukkit.event.player.PlayerQuitEvent(player, leaveMessage);
			Bukkit.getPluginManager().callEvent(quit);
		}
	}

	@EventHandler
	public void onPlayerInteractBlock(PlayerInteractEntityEvent event) {

	}

	@EventHandler (order = Order.EARLIEST)
	public void onPlayerInteractBlock(PlayerInteractBlockEvent event) {
		BridgePlayer player = EntityFactory.createPlayer(event.getEntity());
		Human human = event.getEntity().get(Human.class);
		if (human == null) {
			return;
		}
		org.bukkit.event.block.Action bukkitAction;
		if (event.getAction() == Action.LEFT_CLICK) {
			if (event.getInteracted().isMaterial(MaterialRegistry.get(0))) {
				bukkitAction = org.bukkit.event.block.Action.LEFT_CLICK_AIR;
			} else {
				bukkitAction = org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
			}
		} else if (event.getAction() == Action.RIGHT_CLICK) {
			if (event.getInteracted().isMaterial(MaterialRegistry.get(0))) {
				bukkitAction = org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
			} else {
				bukkitAction = org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
			}
		} else {
			bukkitAction = org.bukkit.event.block.Action.PHYSICAL;
		}
		ItemStack item = BukkitUtil.fromItemStack(event.getEntity().get(PlayerInventory.class).getQuickbar().getSelectedItem());
		Block clickedBlock = null;
		if (event.getPoint() != null) {
			clickedBlock = player.getWorld().getBlockAt(event.getPoint().getBlockX(), event.getPoint().getBlockY(), event.getPoint().getBlockZ());
		}
		BlockFace face = BukkitUtil.toBukkitBlockFace(event.getFace());
		org.bukkit.event.player.PlayerInteractEvent interactEvent = new org.bukkit.event.player.PlayerInteractEvent(player, bukkitAction, item, clickedBlock, face);
		Bukkit.getPluginManager().callEvent(interactEvent);
		if (interactEvent.isCancelled()) {
			event.setCancelled(true);
		}
		/*
		 * TODO: Fix useItemInHand interactions.
		if (interactEvent.useItemInHand() != org.bukkit.event.Event.Result.DEFAULT) {
			if (interactEvent.useItemInHand() == org.bukkit.event.Event.Result.ALLOW) {
				event.setUseItemInHand(org.spout.api.event.Result.ALLOW);
			} else {
				event.setUseItemInHand(org.spout.api.event.Result.DENY);
			}
		}
		if (interactEvent.useInteractedBlock() != org.bukkit.event.Event.Result.DEFAULT) {
			if (interactEvent.useInteractedBlock() == org.bukkit.event.Event.Result.ALLOW) {
				event.setInteractWithBlock(org.spout.api.event.Result.ALLOW);
			} else {
				event.setInteractWithBlock(org.spout.api.event.Result.DENY);
			}
		}
		*/
	}

	@EventHandler
	public void onPlayerAnimation() {
		// TODO: Implement onPlayerAnimation
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerAnimationType() {
		// TODO: Implement onPlayerAnimationType
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerBedEnter() {
		// TODO: Implement onPlayerBedEnter
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerBedLeave() {
		// TODO: Implement onPlayerBedLeave
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerBucketEmpty() {
		// TODO: Implement onPlayerBucketEmpty
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerBucket() {
		// TODO: Implement onPlayerBucket
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerBucketFill() {
		// TODO: Implement onPlayerBucketFill
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerChangedWorld() {
		// TODO: Implement onPlayerChangedWorld
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerChannel() {
		// TODO: Implement onPlayerChannel
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
		// TODO: Unable to handle list of players that will receive the message
		//if (event.isCancelled()) {
		//	return;
		//}
		//BridgePlayer player = EntityFactory.createPlayer(event.getPlayer());
		//AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(false, player, event.getMessage().asString(), null);
		//Bukkit.getPluginManager().callEvent(chatEvent);
		//if (chatEvent.isCancelled()) {
		//	event.setCancelled(true);
		//	return;
		//}
		//event.setMessage(ChatArguments.fromString(chatEvent.getMessage()));
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerChatTabComplete() {
		// TODO: Implement onPlayerChatTabComplete
		throw new UnsupportedOperationException();
	}

	@EventHandler (order = Order.EARLIEST)
	public void onPlayerCommandPreProcess(PreCommandEvent event) {
		if (!(event.getCommandSource() instanceof Player) || event.isCancelled()) {
			return;
		}
		BridgePlayer player = EntityFactory.createPlayer((Player) event.getCommandSource());
		PlayerCommandPreprocessEvent preprocessEvent = new PlayerCommandPreprocessEvent(player, event.getCommand() + " " + event.getArguments());
		Bukkit.getPluginManager().callEvent(preprocessEvent);
		event.setCancelled(preprocessEvent.isCancelled());
		List<String> arguments = Arrays.asList(preprocessEvent.getMessage().split(" "));
		String command = arguments.get(0);
		arguments.remove(0);

		event.setCommand(command);
		event.setArguments(arguments.toArray(new String[arguments.size()]));
	}

	@EventHandler
	public void onPlayerDropItem() {
		// TODO: Implement onPlayerDropItem
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerEggThrow() {
		// TODO: Implement onPlayerEggThrow
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayer() {
		// TODO: Implement onPlayer
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerExpChange() {
		// TODO: Implement onPlayerExpChange
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerFish() {
		// TODO: Implement onPlayerFish
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerGameModeChange() {
		// TODO: Implement onPlayerGameModeChange
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerInteractEntity() {
		// TODO: Implement onPlayerInteractEntity
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerInventory() {
		// TODO: Implement onPlayerInventory
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerItemBreak() {
		// TODO: Implement onPlayerItemBreak
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerItemHeld() {
		// TODO: Implement onPlayerItemHeld
		throw new UnsupportedOperationException();
	}

	@EventHandler (order = Order.EARLIEST)
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled()) {
			return;
		}
		BridgePlayer player = EntityFactory.createPlayer(event.getPlayer());
		org.bukkit.event.player.PlayerKickEvent kickEvent = new org.bukkit.event.player.PlayerKickEvent(player, event.getKickReason(), event.getMessage());
		Bukkit.getPluginManager().callEvent(kickEvent);
		event.setCancelled(kickEvent.isCancelled());
		event.setKickReason(kickEvent.getReason());
		event.setMessage(kickEvent.getLeaveMessage());
	}

	@EventHandler
	public void onPlayerLevelChange() {
		// TODO: Implement onPlayerLevelChange
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerMove() {
		// TODO: Implement onPlayerMove
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerPickUpItem() {
		// TODO: Implement onPlayerPickUpItem
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerPortal() {
		// TODO: Implement onPlayerPortal
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerRegisterChannel() {
		// TODO: Implement onPlayerRegisterChannel
		throw new UnsupportedOperationException();
	}

	@EventHandler (order = Order.EARLIEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (event.isCancelled()) {
			return;
		}
		BridgePlayer player = EntityFactory.createPlayer(event.getPlayer());
		org.bukkit.event.player.PlayerRespawnEvent respawn = new org.bukkit.event.player.PlayerRespawnEvent(player, BukkitUtil.fromPoint(event.getPoint()), false);
		Bukkit.getPluginManager().callEvent(respawn);
		event.setPoint(BukkitUtil.toPoint(respawn.getRespawnLocation()));
	}

	@EventHandler
	public void onPlayerShearEntity() {
		// TODO: Implement onPlayerShearEntity
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerToggleFlight() {
		// TODO: Implement onPlayerToggleFlight
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerToggleSneak() {
		// TODO: Implement onPlayerToggleSneak
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerToggleSprint() {
		// TODO: Implement onPlayerToggleSprint
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerUnregisterChannel() {
		// TODO: Implement onPlayerUnregisterChannel
		throw new UnsupportedOperationException();
	}

	@EventHandler
	public void onPlayerVelocity() {
		// TODO: Implement onPlayerVelocity
		throw new UnsupportedOperationException();
	}
}
