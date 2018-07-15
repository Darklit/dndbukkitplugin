package com.dankrat.corePlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {
	
	/*TODO:
		- Make it so dice don't go to 0 (FINISHED)
		- Perhaps put players and such in the GameMaster class
		- Hearthstone implementation (FINISHED)
		- Add tools for players to keep track of everything (have it notify the gm) [Revise]
		- Allow for notes
		- Allow hearthstone to bypass movement code [Revise]
		- Use doubles instead of ints when locating [Revise]
		- Add mana buff [Revise]
		- Add GM Damage command [Revise]
		- Add monster turn [Revise]
		- Add class specific commands <Priest done>
		- Automate abilities
		- Constitution save thing
		- Add comments
	*/
	
	private ArrayList<PartyMember> players;
	private String gmName;
	private File classes;
	private FileConfiguration classesConfig;
	private Monster currentMonster;
	
	@Override
	public void onEnable() {
		players = new ArrayList<PartyMember>(this.getServer().getMaxPlayers());
		new EventHandlers(this);
		File dir = getDataFolder();
		if(!dir.exists()) {
			if(!dir.mkdir()) {
				getLogger().info("Could not create DataFolder");
			}
		}
		
		createFiles();
	}
	
	@Override
	public void onDisable() {
		saveClasses();
	}
	
	public void createFiles() {
		classes = new File(getDataFolder(),"classes.yml");
		
		if(!classes.exists()) {
			classes.getParentFile().mkdirs();
			saveResource("classes.yml",false);
		}
		
		classesConfig = new YamlConfiguration();
		
		try {
			classesConfig.load(classes);
			loadFiles();
		}catch(IOException e) {
			e.printStackTrace();
		}catch(InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void loadFiles() {
		try {
			ConfigurationSection classObjs = classesConfig.getConfigurationSection("classes");
			Set<String> keys = classObjs.getKeys(false);
			for(String c : keys) {
				ConfigurationSection items = classObjs.getConfigurationSection(c+".items");
				double x = classObjs.getDouble(c+".location.x");
				double y = classObjs.getDouble(c+".location.y");
				double z = classObjs.getDouble(c+".location.z");
				Set<String> allItems = items.getKeys(false);
				ItemStack[] itemObjs = new ItemStack[allItems.size()];
				int g = 0;
				for(String i : allItems) {
					itemObjs[g] = items.getItemStack(""+i);
					g++;
				}
				Inventory toAdd = Bukkit.createInventory(null, 36);
				toAdd.addItem(itemObjs);
				Location toAddLocation = new Location(this.getServer().getWorld("world"),x,y,z);
				GameMaster.allButtonClasses.add(new ButtonClass(toAddLocation,toAdd,c,this));
			}
		}catch(NullPointerException e) {
			e.printStackTrace();
		}
		try {
			ConfigurationSection playerObjs = classesConfig.getConfigurationSection("party");
			Set<String> playerKeys = playerObjs.getKeys(false);
			for(String c : playerKeys) {
				ConfigurationSection play = playerObjs.getConfigurationSection(c);
				double x = play.getDouble("x");
				double y = play.getDouble("y");
				double z = play.getDouble("z");
				int roll = play.getInt("initiative");
				boolean turn = play.getBoolean("turn");
				int mana = play.getInt("mana");
				int movement = play.getInt("movement");
				int xp = play.getInt("xp");
				int health = play.getInt("health");
				String className = play.getString("class");
				Player p = this.getServer().getPlayer(c);
				players.add(new PartyMember(p,movement,x,y,z,xp,mana,roll,turn,health,className,this));
			}
		}catch(NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getClassData() {
		return classesConfig;
	}
	
	public void saveClasses() {
		try {
			for(PartyMember p : players) {
				p.saveCharacter();
			}
		}catch(NullPointerException e) {
			e.printStackTrace();
		}
		try {
			classesConfig.save(classes);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public void saveClassFile() {
		try {
			classesConfig.save(classes);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			if(cmd.getName().equalsIgnoreCase("gm")) {
				if(args.length == 0) {
					if(gmName == null && sender.isOp()) {
						gmName = sender.getName();
						sender.sendMessage("You are now the GM!");
					}else if(!sender.isOp()) {
						sender.sendMessage(ChatColor.RED + "You do not have permission");
					}else if(gmName != null && sender.getName().equals(gmName)){
						gmName = null;
						sender.sendMessage("You are no longer the GM!");
					}else {
						sender.sendMessage(ChatColor.RED + "There is already a GM.");
					}
				}else {
					if(gmName != null && gmName.equals(sender.getName())) {
						if(args[0].equalsIgnoreCase("addall")) {
							if(this.players.size() == 0) {
								Collection<? extends Player> allPlayers = this.getServer().getOnlinePlayers();
								Player[] players = allPlayers.toArray(new Player[allPlayers.size()]);
								for(Player p: players) {
									if(!p.getName().equals(gmName)) {
										this.players.add(new PartyMember(p,this));
										p.sendMessage("You have been added to the game!");
									}else {
										p.sendMessage("You are the GM!!!!!!");
									}
								}
								GameMaster.getSortedMembers(this.players)[0].setTurn(true);
							}
						}else if(args[0].equalsIgnoreCase("set")) {
							try {
								for(PartyMember p : this.players) {
									if(p.getName().equals(args[1])) {
										switch(args[2].toLowerCase()) {
											case "movement":
												p.setMovement(Integer.parseInt(args[3]));
												sender.sendMessage("Movement set.");
												break;
											case "turn":
												p.setTurn(Boolean.parseBoolean(args[3]));
												sender.sendMessage("Turn set.");
												break;
											case "health":
												p.setHealth(Integer.parseInt(args[3]));
												sender.sendMessage("Health set.");
												break;
											case "mana":
												p.setMana(Integer.parseInt(args[3]));
												sender.sendMessage("Mana set.");
												break;
											case "free":
												p.setFreeMovement(Integer.parseInt(args[3]));
												sender.sendMessage("Free movement set.");
												break;
										}
									}
								}
							}catch(IndexOutOfBoundsException e) {
								sender.sendMessage(ChatColor.RED + "Invalid command use");
							}
						}else if(args[0].equalsIgnoreCase("class")) {
							try {
								if(args[1].equalsIgnoreCase("set")) {
									ArrayList<ButtonClass> classes = GameMaster.allButtonClasses;
									boolean nameExists = false;
									for(ButtonClass c : classes) {
										if(c.getName().equals(args[2])) {
											nameExists = true;
											break;
										}
									}
									if(!nameExists) {
										sender.sendMessage(ChatColor.GREEN + "Right click the button you want players to use.");
										GameMaster.allButtonClasses.add(new ButtonClass(args[2],this));
										GameMaster.classButton = true;
									}else {
										sender.sendMessage(ChatColor.RED + "Name already in use!");
									}
								}
							}catch(IndexOutOfBoundsException e) {
								sender.sendMessage(ChatColor.RED + "Invalid command use");
							}
						}else if(args[0].equalsIgnoreCase("freemode")) {
							GameMaster.freeMode = !GameMaster.freeMode;
							if(GameMaster.freeMode) this.getServer().broadcastMessage(ChatColor.GREEN + "Free-Mode has been initiated!");
							else this.getServer().broadcastMessage(ChatColor.DARK_RED + "Free-Mode has been disabled!");
						}else if(args[0].equalsIgnoreCase("add")) {
							for(PartyMember m : players) {
								if(m.getName().equals(args[1])) {
									sender.sendMessage("Player already in party!");
									return true;
								}
							}
							Player p = this.getServer().getPlayer(args[1]);
							players.add(new PartyMember(p,this));
							sender.sendMessage("Added player to party.");
							return true;
						}else if(args[0].equalsIgnoreCase("remove")) {
							for(int i = 0; i < players.size(); i++) {
								if(players.get(i).getName().equals(args[1])) {
									players.get(i).getPlayer().sendMessage(ChatColor.RED + "You have been removed from the party.");
									if(players.get(i).isTurn()) players.get(i).endTurn();
									this.getClassData().set("party."+args[1], null);
									players.remove(i);
									this.saveClasses();
									sender.sendMessage("Player removed from party");
									return true;
								}
							}
							
						}else if(args[0].equalsIgnoreCase("reward")) {
							if(args.length < 3) {
								sender.sendMessage("Invalid arguments.");
								return false;
							}
							if(args[2].equalsIgnoreCase("xp")) {
								try {
									int xp = Integer.parseInt(args[3]);
									if(args[1].equalsIgnoreCase("all")) {
										for(PartyMember m : players) {
											m.addXP(xp);
										}
										sender.sendMessage("All players rewarded with xp.");
										return true;
									}else {
										for(PartyMember m : players) {
											if(m.getName().equalsIgnoreCase(args[1])) {
												m.addXP(xp);
												sender.sendMessage("Player rewarded with xp.");
												return true;
											}
										}
										return CommonErrors.playerNotFound(sender);
									}
								}catch(NumberFormatException e) {
									return CommonErrors.invalidValue(sender);
								}
							}else if(args[2].equalsIgnoreCase("item")) {
								ItemStack itemToGive = ((Player) sender).getInventory().getItemInMainHand();
								if(itemToGive != null) {
									if(args[1].equalsIgnoreCase("all")) {
										for(PartyMember m : players) {
											m.getPlayer().getInventory().addItem(itemToGive);
										}
										sender.sendMessage("All players given item.");
										return true;
									}else {
										Player pl = this.getServer().getPlayer(args[1]);
										if(pl != null) {
											pl.getInventory().addItem(itemToGive);
											sender.sendMessage("Item given.");
											return true;
										}
										else sender.sendMessage(ChatColor.RED + "Invalid player.");
									}
									
								}else sender.sendMessage(ChatColor.RED + "Error with item.");
							}
						}else if(args[0].equalsIgnoreCase("monster")) {
							/*
							 * TODO:
							 * 	- Use Bukkit.createBossBar
							 * 	- Allow for the manipulation of the boss health
							 *  - Perhaps have stuff like player health (O I NEED TO ADD THAT)
							 */
							
							if(args[1].equalsIgnoreCase("create")) {
								//Args[2] should be name and [3] should be health.
								try {
									if(currentMonster != null) {
										sender.sendMessage(ChatColor.RED + "You already have a boss monster in play!");
										return true;
									}else {
										BossBar boss = Bukkit.createBossBar(args[2], BarColor.RED, BarStyle.SEGMENTED_10, BarFlag.PLAY_BOSS_MUSIC);
										boss.addPlayer(this.getServer().getPlayer(gmName));
										for(PartyMember m : players) {
											boss.addPlayer(m.getPlayer());
										}
										currentMonster = new Monster(Integer.parseInt(args[3]),boss,args[2]);
										sender.sendMessage(ChatColor.GREEN + "Boss created.");
										return true;
									}
								}catch(IndexOutOfBoundsException error) {
									sender.sendMessage("Invalid arguments");
									return true;
								}
							}else if(args[1].equalsIgnoreCase("remove")) {
								currentMonster.getHealthBar().setVisible(false);
								currentMonster = null;
								sender.sendMessage("Boss removed.");
							}else if(args[1].equalsIgnoreCase("damage")) {
								try {
									if(GameMaster.isValidNumber(args[2])) {
										currentMonster.damage(Integer.parseInt(args[2]));
										this.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + currentMonster.getName() + " was damaged for " + args[2] + " HP and is now at " + currentMonster.getHealth() + " HP");
										if(currentMonster.getHealth() <= 0) {
											this.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + currentMonster.getName() + " was defeated!");
											currentMonster.getHealthBar().setVisible(false);
											currentMonster = null;
										}
										return true;
									}else return CommonErrors.invalidValue(sender);
								}catch(IndexOutOfBoundsException e) {
									return CommonErrors.invalidParameters(sender);
								}
								
							}
						}else if(args[0].equalsIgnoreCase("start")) {
							this.getServer().broadcastMessage(ChatColor.GREEN + "The game has started!");
							GameMaster.getSortedMembers(players)[0].setTurn(true);
							return true;
						}else if(args[0].equalsIgnoreCase("damage")) {
							for(PartyMember m : players) {
								if(m.getName().equalsIgnoreCase(args[1])) {
									try {
										m.setHealth(m.getHealth()-Integer.parseInt(args[2]));
										sender.sendMessage("Character's health is now at " + m.getHealth());
										return true;
									}catch(NumberFormatException e) {
										return CommonErrors.invalidValue(sender);
									}
									
								}
							}
							return CommonErrors.playerNotFound(sender);
						}else if(args[0].equalsIgnoreCase("heal")) {
							for(PartyMember m : players) {
								if(m.getName().equalsIgnoreCase(args[1])) {
									try {
										m.setHealth(m.getHealth()+Integer.parseInt(args[2]));
										sender.sendMessage("Character's health is now at " + m.getHealth());
										return true;
									}catch(NumberFormatException e) {
										return CommonErrors.invalidValue(sender);
									}
									
								}
							}
							return CommonErrors.playerNotFound(sender);
						}else if(args[0].equalsIgnoreCase("roll")) {
							sender.sendMessage("Rolling...");
							if(args.length == 2) {
								//This means roll one of the desired dice
								try {
									String dice = args[1].replaceAll("d", "");
									int diceRoll = 1 + (int)(Math.random() * Integer.parseInt(dice));
									sender.sendMessage(sender.getName() + " rolled a " + diceRoll + " on a d"+dice);
									return true;
								}catch(NumberFormatException error) {
									return CommonErrors.invalidValue(sender);
								}
							}else if(args.length == 3) {
								try {
									int[] diceRolls = new int[Integer.parseInt(args[0])];
									for(int i = 0; i < diceRolls.length; i++) {
										int roll = 1 + (int)(Math.random()*Integer.parseInt(args[2].replace("d","")));
										diceRolls[i] = roll;
									}
									String message = sender.getName() + " rolled " + args[1] + "d" + args[2].replace("d","") + "'s and got: " + diceRolls[0];
									for(int i = 1; i < diceRolls.length; i++) {
										message+=","+diceRolls[i];
									}
									sender.sendMessage(message);
									return true;
								}catch(NumberFormatException error) {
									sender.sendMessage(ChatColor.RED+"Invalid command input");
								}
							}
							return true;
						}else if(args[0].equalsIgnoreCase("turn")) {
							if(GameMaster.monsterTurn) {
								GameMaster.monsterTurn = false;
								GameMaster.getSortedMembers(players)[0].setTurn(true);
								this.getServer().broadcastMessage(ChatColor.RED + "The monsters have ended their assult!");
							}else sender.sendMessage(ChatColor.RED + "It is not the monster's turn!");
						}
					}else {
						sender.sendMessage("You are not the GM!");
					}
				}
				return true;
			}else if(cmd.getName().equalsIgnoreCase("turn")) {
				for(PartyMember m : players) {
					if(m.getName().equals(sender.getName())) {
						if(m.isTurn()) {
							this.getServer().broadcastMessage(ChatColor.AQUA + sender.getName() + " has ended their turn!");
							GameMaster.endTurn(players, m);
							return true;
						}
						else sender.sendMessage(ChatColor.RED + "It is not your turn!");
						return true;
					}
				}
				sender.sendMessage("You are not in the party!");
				return true;
			}else if(cmd.getName().equalsIgnoreCase("roll")) {
				sender.sendMessage("Rolling...");
				if(args.length == 1) {
					//This means roll one of the desired dice
					try {
						String dice = args[0].replaceAll("d", "");
						int diceRoll = 1 + (int)(Math.random() * Integer.parseInt(dice));
						this.getServer().broadcastMessage(sender.getName() + " rolled a " + diceRoll + " on a d"+dice);
						return true;
					}catch(NumberFormatException error) {
						return CommonErrors.invalidValue(sender);
					}
				}else if(args.length == 2) {
					try {
						int[] diceRolls = new int[Integer.parseInt(args[0])];
						for(int i = 0; i < diceRolls.length; i++) {
							int roll = 1 + (int)(Math.random()*Integer.parseInt(args[1].replace("d","")));
							diceRolls[i] = roll;
						}
						String message = sender.getName() + " rolled " + args[0] + "d" + args[1].replace("d","") + "'s and got: " + diceRolls[0];
						for(int i = 1; i < diceRolls.length; i++) {
							message+=","+diceRolls[i];
						}
						this.getServer().broadcastMessage(message);
						return true;
					}catch(NumberFormatException error) {
						sender.sendMessage(ChatColor.RED+"Invalid command input");
					}
				}
				return true;
			}else if(cmd.getName().equalsIgnoreCase("party")) {
				if(players.size() == 0) {
					sender.sendMessage("There is no active party.");
					return true;
				}else {
					PartyMember[] sortedMembers = GameMaster.getSortedMembers(players);
					sender.sendMessage(ChatColor.DARK_PURPLE + "Current Party:");
					for(int i = 0; i < sortedMembers.length; i++) {
						if(!sortedMembers[i].isTurn()) sender.sendMessage(i+1 + ": " + sortedMembers[i].getName());
						else sender.sendMessage("Current Turn: " + sortedMembers[i].getName());
					}
					return true;
				}
			}else if(cmd.getName().equalsIgnoreCase("character")) {
				if(args.length == 0) {
					for(PartyMember m : players) {
						if(m.getName().equals(sender.getName())) {
							sender.sendMessage(ChatColor.DARK_PURPLE + m.getName() + ": " + m.getClassName());
							sender.sendMessage(ChatColor.RED + "Health: " + m.getHealth());
							sender.sendMessage(ChatColor.DARK_BLUE + "Mana: " + m.getMana());
							sender.sendMessage(ChatColor.BLUE + "XP: " + m.getXP());
							sender.sendMessage(ChatColor.YELLOW + "Initiative: " + m.getStartingRoll());
							return true;
						}
					}
				}else if(args.length == 1) {
					for(PartyMember m : players) {
						if(m.getName().equalsIgnoreCase(args[0])) {
							sender.sendMessage(ChatColor.DARK_PURPLE + m.getName() + ": " + m.getClassName());
							sender.sendMessage(ChatColor.RED + "Health: " + m.getHealth());
							sender.sendMessage(ChatColor.DARK_BLUE + "Mana: " + m.getMana());
							sender.sendMessage(ChatColor.BLUE + "XP: " + m.getXP());
							sender.sendMessage(ChatColor.YELLOW + "Initiative: " + m.getStartingRoll());
							return true;
						}
					}
					return CommonErrors.playerNotFound(sender);
				}else {
					if(args[0].equalsIgnoreCase("heal")) {
						try {
							int amount = Integer.parseInt(args[1]);
							for(PartyMember m : players) {
								if(m.getName().equals(sender.getName())) {
									m.setHealth(m.getHealth()+amount);
									sender.sendMessage("Healed for " + amount + " damage");
									this.getServer().getPlayer(gmName).sendMessage(ChatColor.DARK_PURPLE + m.getName() + " healed to " + m.getHealth() + " HP.");
									return true;
								}
							}
						}catch(NumberFormatException e) {
							return CommonErrors.invalidValue(sender);
						}
					}else if(args[0].equalsIgnoreCase("damage")) {
						try {
							int amount = Integer.parseInt(args[1]);
							for(PartyMember m : players) {
								if(m.getName().equals(sender.getName())) {
									m.setHealth(m.getHealth()+amount);
									sender.sendMessage("Damaged for " + amount + " damage");
									this.getServer().getPlayer(gmName).sendMessage(ChatColor.DARK_PURPLE + m.getName() + " got damaged for " + amount + " HP.");
									return true;
								}
							}
						}catch(NumberFormatException e) {
							sender.sendMessage(ChatColor.RED + "Invalid value.");
							return true;
						}
					}else if(args[0].equalsIgnoreCase("xp")) {
						try {
							int amount = Integer.parseInt(args[1]);
							for(PartyMember m : players) {
								if(m.getName().equals(sender.getName())) {
									m.setHealth(m.getHealth()+amount);
									sender.sendMessage("Gained " + amount + " xp");
									this.getServer().getPlayer(gmName).sendMessage(ChatColor.DARK_PURPLE + m.getName() + " gained " + amount + " XP");
									return true;
								}
							}
						}catch(NumberFormatException e) {
							return CommonErrors.invalidValue(sender);
						}
						
						sender.sendMessage(ChatColor.RED + "Error finding player.");
						return true;
					}
				}
			}else if(cmd.getName().equalsIgnoreCase("priest")) {
				for(PartyMember m : players) {
					if(m.getName().equals(sender.getName())) {
						if(m.getClassName().toLowerCase().contains("priest")) {
							if(args[0].equalsIgnoreCase("sethealth")) {
								for(PartyMember pm : players) {
									if(pm.getName().equalsIgnoreCase(args[1])) {
										try {
											pm.setHealth(Integer.parseInt(args[2]));
											pm.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Your priest, " + sender.getName() +", healed you to " + pm.getHealth() + " HP");
											sender.sendMessage("Health set.");
											return true;
										}catch(NumberFormatException e) {
											sender.sendMessage(ChatColor.RED + "Invalid value.");
											return true;
										}catch(IndexOutOfBoundsException e) {
											return CommonErrors.invalidParameters(sender);
										}
									}
								}
							}else if(args[0].equalsIgnoreCase("buff")) {
								PartyMember pm = GameMaster.searchMember(players, args[1]);
								if(pm == null) return CommonErrors.playerNotFound(sender);
								if(GameMaster.isValidBoolean(args[2])) {
									pm.setHealthBuff(Boolean.parseBoolean(args[2]));
									sender.sendMessage("Player buffed");
									getGMPlayer().sendMessage(ChatColor.LIGHT_PURPLE + sender.getName() + " just gave a health buff to " + args[2]);
									return true;
								}else return CommonErrors.invalidValue(sender);
							}else if(args[0].equalsIgnoreCase("list")) {
								for(PartyMember mp : players) {
									if(!mp.getName().equals(sender.getName())) {
										sender.sendMessage(mp.getName() + ": " + ChatColor.DARK_RED + mp.getHealth() + " HP");
									}
								}
								return true;
							}
						}else {
							sender.sendMessage(ChatColor.RED + "You are not a priest!");
							return true;
						}
					}
				}
				sender.sendMessage(ChatColor.RED + "Error finding you!");
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<PartyMember> getPlayers() {
		return players;
	}
	
	public Player getGMPlayer() {
		return this.getServer().getPlayer(gmName);
	}
	
	public void addPlayerTest(Player p) {
		players.add(new PartyMember(p,this));
	}
	
	
	public String getGMName() {
		return gmName;
	}

}
