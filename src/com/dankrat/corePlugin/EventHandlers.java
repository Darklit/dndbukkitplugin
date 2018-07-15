package com.dankrat.corePlugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EventHandlers implements Listener {
	
	private CorePlugin pl;
	
	public EventHandlers(CorePlugin plugin) {
		pl = plugin;
		pl.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void manageInteractions(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(e.hasBlock() && e.getClickedBlock().getType().toString().contains("BUTTON")) {
			Block b = e.getClickedBlock();
			String gmName = pl.getGMName();
			if(GameMaster.classButton && p.getName().equals(gmName)) {
				ArrayList<ButtonClass> classes = GameMaster.allButtonClasses;
				for(ButtonClass bc : classes) {
					if(!bc.isFinished()) {
						bc.setLocation(b.getLocation());
						break;
					}
				}
				Inventory blankInventory = Bukkit.createInventory(null, 36);
				p.openInventory(blankInventory);
				GameMaster.inInventory = true;
			}else {
				ArrayList<ButtonClass> classes = GameMaster.allButtonClasses;
				for(ButtonClass bc : classes) {
					if(bc.getLocation().equals(b.getLocation()) && p.getName().equals(gmName)) {
						GameMaster.inInventory = true;
						GameMaster.classButton = true;
						bc.setFinished(false);
						p.openInventory(bc.getItems());
						break;
					}else if(bc.getLocation().equals(b.getLocation())) {
						ItemStack[] items = bc.getItems().getContents();
						ArrayList<PartyMember> members = pl.getPlayers();
						for(PartyMember m : members) {
							if(m.getName().equals(p.getName())) {
								m.setClass(bc.getName());
								pl.getServer().broadcastMessage(ChatColor.GREEN + p.getName() + " has selected the " + bc.getName() + " class!");
								break;
							}
						}
						for(ItemStack item : items) {
							if(item != null) p.getInventory().addItem(item);
						}
						break;
					}
				}
			}
		}else if(e.hasItem() && e.getItem().getItemMeta().hasLore()) {
			List<String> lores = e.getItem().getItemMeta().getLore();
			for(String lore : lores) {
				if(lore.toLowerCase().contains("mana")) {
					int manaCost = Character.getNumericValue(lore.charAt(0));
					ArrayList<PartyMember> members = pl.getPlayers();
					for(PartyMember m : members) {
						if(m.getName().equals(p.getName())) {
							if(m.getMana() >= manaCost) {
								m.spendMana(manaCost);
								pl.getServer().broadcastMessage(ChatColor.DARK_PURPLE + p.getName() + " used " + e.getItem().getItemMeta().getDisplayName() +"!");
							}else p.sendMessage(ChatColor.RED + "You don't have enough mana!");
							break;
						}
					}
					break;
				}
			}
		}else if(e.hasItem() && e.getItem().getItemMeta().hasDisplayName()) {
			String name = e.getItem().getItemMeta().getDisplayName();
			if(name.toLowerCase().contains("hearthstone")) {
				Location toTP = new Location(p.getWorld(),GameMaster.CHAPTER1.getX(),GameMaster.CHAPTER1.getY(),GameMaster.CHAPTER1.getZ());
				GameMaster.searchMember(pl.getPlayers(),p.getName()).setFreeMovement(1);
				p.teleport(toTP);
			}
		}
	}
	
	@EventHandler
	public void playerMoved(PlayerMoveEvent e) {
		ArrayList<PartyMember> members = pl.getPlayers();
		for(PartyMember m : members) {
			if(m.getName().equals(e.getPlayer().getName())) {
				m.update(e.getPlayer());
				break;
			}
		}
	}
	
	@EventHandler
	public void handleInventory(InventoryCloseEvent e) {
		String gmName = pl.getGMName();
		HumanEntity p = e.getPlayer();
		Inventory inv = e.getInventory();
		
		if(p.getName().equals(gmName) && GameMaster.classButton && GameMaster.inInventory) {
			ArrayList<ButtonClass> allClasses = GameMaster.allButtonClasses;
			for(ButtonClass b : allClasses) {
				if(!b.isFinished()) {
					b.setItems(inv);
					GameMaster.classButton = false;
					GameMaster.inInventory = false;
				}
			}
		}
	}
	
	@EventHandler
	public void joiningServer(PlayerJoinEvent e) {
		Player joined = e.getPlayer();
		if(pl.getClassData().get("party."+joined.getName()+".turn") != null) {
			try {
				ConfigurationSection playerObjs = pl.getClassData().getConfigurationSection("party");
					ConfigurationSection play = playerObjs.getConfigurationSection(joined.getName());
					int x = play.getInt("x");
					int y = play.getInt("y");
					int z = play.getInt("z");
					int roll = play.getInt("initiative");
					boolean turn = play.getBoolean("turn");
					int mana = play.getInt("mana");
					int movement = play.getInt("movement");
					int xp = play.getInt("xp");
					int health = play.getInt("health");
					String className = play.getString("class");
					Player p = joined;
					pl.getPlayers().add(new PartyMember(p,movement,x,y,z,xp,mana,roll,turn,health,className,pl));
					pl.getServer().getPlayer(pl.getGMName()).sendMessage(ChatColor.YELLOW + joined.getName() + " has rejoined the party!");
			}catch(NullPointerException error) {
				error.printStackTrace();
			}
		}
	}

}
