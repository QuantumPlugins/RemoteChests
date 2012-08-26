package com.quantumdev.remotechest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ChestListener implements Listener {

	private RemoteChest plugin;
	
	public ChestListener(RemoteChest instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		if (event.getMessage().toLowerCase().contains("chests reload")) {
			reloadChests(p.getWorld());
			p.sendMessage("[RemoteChest] All chests have been updated!");
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getBlock().getType().equals(Material.CHEST)) {
			addChest((Chest) event.getBlock().getState());
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Location l = event.getPlayer().getLocation();
		for (int x = l.getBlockX()-5; x < l.getBlockX()+5; x++) {
			for (int y = l.getBlockY()-5; y < l.getBlockY()+5; y++) {
				for (int z = l.getBlockZ()-5; z < l.getBlockZ()+5; z++) {
					if (event.getPlayer().getWorld().getBlockAt(x, y, z).getType().equals(Material.CHEST)) {
						addChest((Chest) event.getPlayer().getWorld().getBlockAt(x, y, z).getState());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onChestInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null)
			return;
		if (event.getClickedBlock().getType().equals(Material.CHEST)) {
			Chest chest = (Chest) event.getClickedBlock().getState();
			addChest(chest);
		}
	}
	
	public void addChest(Chest c) {
		String contents = "";
		ItemStack[] cs = c.getInventory().getContents();
		boolean existing = false;
		
		for (int i = 0; i < cs.length; i++)
			if (cs[i] != null)
				if (cs[i].getAmount() > 0)
					contents += cs[i].getAmount() + "x" + cs[i].getTypeId() + ((cs[i].getData().getData() != 0) ? ":" + cs[i].getData().getData() + ";" : ";");
		
		try {
			ResultSet rs = plugin.st.executeQuery("SELECT * FROM chests WHERE x = '" + c.getX() + "' AND y = '" + c.getY() + "' AND z = '" + c.getZ() + "'");
			while (rs.next()) {
				existing = true;
				rs.updateString("contents", contents);
				rs.updateRow();
				continue;
			}
			if (!existing)
				plugin.st.executeUpdate("REPLACE INTO chests SET x = '" + c.getX() + "', y = '" + c.getY() + "', z = '" + c.getZ() + "', contents = '" + contents + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void reloadChests(World w) {
		try {
			ResultSet rs = plugin.st.executeQuery("SELECT * FROM chests");
			while (rs.next()) {
				int x = rs.getInt("x");
				int y = rs.getInt("y");
				int z = rs.getInt("z");
				if (w.getBlockAt(x, y, z).getType().equals(Material.CHEST)) {
					Chest c = (Chest) w.getBlockAt(x, y, z).getState();
					String contents = rs.getString("contents");
					if (contents.isEmpty()) return;
					String[] items = contents.split(";");
					List<ItemStack> list = new ArrayList<ItemStack>();
					for (int i = 0; i < items.length; i++) {
						if (items[i].split("x")[1].contains(":"))
							list.add(new ItemStack(Integer.parseInt(items[i].split("x")[1].split(":")[0]), Integer.parseInt(items[i].split("x")[0]), (short) 0, Byte.parseByte(items[i].split(":")[1])));
						else 
							list.add(new ItemStack(Integer.parseInt(items[i].split("x")[1]), Integer.parseInt(items[i].split("x")[0])));
					}
					ItemStack[] lst = list.toArray(new ItemStack[list.size()]);
					c.getInventory().setContents(lst);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
