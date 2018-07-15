package com.dankrat.corePlugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PartyMember {
	
	private boolean turn;
	private int movement;
	private double xpos;
	private double ypos;
	private double zpos;
	private int xp;
	private String name;
	private int mana;
	private Player player;
	private int startingRoll;
	private CorePlugin plugin;
	private int health;
	private String className;
	private boolean buffed = false;
	private boolean manaBuffed = false;
	private final int MAX_HEALTH = 9;
	private int freeMovement = 0;
	
	public PartyMember(Player p, CorePlugin plug) {
		this.turn = false;
		this.movement = 0;
		xpos = p.getLocation().getX();
		ypos = p.getLocation().getY();
		zpos = p.getLocation().getZ();
		name = p.getName();
		mana = 5;
		health = 9;
		xp = 0;
		player = p;
		startingRoll = 1 + (int) (Math.random()*20);
		p.sendMessage("You rolled a " + startingRoll + " for your initiative.");
		p.setLevel(mana);
		plugin = plug;
	}
	
	public PartyMember(Player p, int move, int x, int y, int z, int xp, int mana, int roll, boolean turn, int health, String className, CorePlugin plug) {
		player = p;
		movement = move;
		this.xp = xp;
		this.health = health;
		this.mana = mana;
		startingRoll = roll;
		plugin = plug;
		name = p.getName();
		this.turn = turn;
		this.className = className;
	}
	
	public PartyMember(Player p, int move, double x, double y, double z, int xp, int mana, int roll, boolean turn, int health, String className, CorePlugin plug) {
		player = p;
		movement = move;
		this.xpos = x;
		this.ypos = y;
		this.zpos = z;
		this.xp = xp;
		this.health = health;
		this.mana = mana;
		startingRoll = roll;
		plugin = plug;
		name = p.getName();
		this.turn = turn;
		this.className = className;
	}
	
	public PartyMember(Player p, int move) {
		this.turn = false;
		this.movement = move;
		xpos = p.getLocation().getX();
		ypos = p.getLocation().getY();
		zpos = p.getLocation().getZ();
		name = p.getName();
		mana = 5;
		player = p;
		health = 9;
		startingRoll = (int) (Math.random()*20);
		p.sendMessage("You rolled a " + startingRoll + " for your initiative.");
		p.setLevel(mana);
	}
	
	public int getStartingRoll() {
		return startingRoll;
	}
	
	public void setClass(String name) {
		className = name;
	}
	
	public void setMana(int val) {
		mana = val;
		player.setLevel(mana);
	}
	
	public void setFreeMovement(int val) {
		freeMovement = val;
	}
	
	public void setHealthBuff(boolean val) {
		buffed = val;
	}
	
	public boolean getHealthBuffed() {
		return buffed;
	}
	
	public void endTurn() {
		mana++;
		if(mana>5 && !manaBuffed) mana = 5;
		turn = false;
		try {
			if(className.toLowerCase().contains("archer") || className.toLowerCase().contains("wizard") || className.toLowerCase().contains("priest")) {
				movement = 1 + (int)(Math.random()*GameMaster.ARCHER_WIZARD_PRIEST_MOVEMENT);
			}else if(className.toLowerCase().contains("rogue")) {
				movement = 1 + (int)(Math.random()*GameMaster.ROGUE_MOVEMENT);
			}else if(className.toLowerCase().contains("warrior")) {
				movement = 1 + (int)(Math.random()*GameMaster.WARRIOR_MOVEMENT);
			}else movement = 0;
		}catch(NullPointerException e) {
			movement = 0;
		}
		player.setLevel(mana);
		saveCharacter();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void update(Player p) {
		double newXD = p.getLocation().getX();
		double newZD = p.getLocation().getZ();
		boolean teleported = false;
		if(!GameMaster.freeMode && checkIfMoved(newXD,newZD)){
			if(this.movement != 0 && turn) {
				this.movement--;
				p.sendMessage("Your remaining movement: " + this.movement);
			}else if(freeMovement>0) {
				this.freeMovement--;
				p.sendMessage("Your free movement: " + this.freeMovement);
			}else if(!turn) {
				p.teleport(new Location(p.getWorld(),this.xpos,this.ypos,this.zpos));
				teleported = true;
				p.sendMessage("It is not your turn!");
			}else{
				p.teleport(new Location(p.getWorld(),this.xpos,this.ypos,this.zpos));
				teleported = true;
				p.sendMessage("You have no more remaining movement in this turn!");
			}
			
			//Value is around 0.2 the common PLEASE FIX
			if(!teleported) {
				this.xpos = newXD;
				this.zpos = newZD;
				this.ypos = p.getLocation().getY();
			}
		}
	}
	
	public void setTurn(boolean t) {
		turn = t;
		if(t) {
			player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1, (float) 0.5);
			player.sendMessage(ChatColor.GREEN + "It is your turn!");
		}
	}
	
	public void setMovement(int x) {
		this.movement = x;
	}
	
	
	private boolean checkIfMoved(double x, double z) {
		if(Math.abs(this.xpos-x)>=1 || Math.abs(this.zpos-z)>=1) return true;
		else {
			return false;
		}
	}
	
	public int getMana() {
		return mana;
	}
	
	public boolean isTurn() {
		return turn;
	}
	
	public void addXP(int val) {
		xp+=val;
		player.sendMessage("You have been rewarded with " + val + " xp! You now have " + xp + " xp.");
	}
	
	public void spendMana(int val) {
		mana-=val;
		player.sendMessage(ChatColor.BLUE + "You've spent " + val + " mana! You have " + mana + " remaining.");
		player.setLevel(mana);
	}
	
	public String getName() {
		return name;
	}
	
	public int getHealth() {
		return health;
	}
	
	public int getXP() {
		return xp;
	}
	
	@Override
	public String toString() {
		return "Player: " + player.toString() + "\nName: " + name;
	}
	
	public void saveCharacter() {
		try {
			plugin.getClassData().set("party."+this.getName()+".turn", this.turn);
			plugin.getClassData().set("party."+this.getName()+".mana",mana);
			plugin.getClassData().set("party."+this.getName()+".x", xpos);
			plugin.getClassData().set("party."+this.getName()+".y", ypos);
			plugin.getClassData().set("party."+this.getName()+".z", zpos);
			plugin.getClassData().set("party."+this.getName()+".initiative", startingRoll);
			plugin.getClassData().set("party."+this.getName()+".movement", movement);
			plugin.getClassData().set("party."+this.getName()+".xp", xp);
			plugin.getClassData().set("party."+this.getName()+".health", health);
			plugin.getClassData().set("party."+this.getName()+".class", className);
		}catch(NullPointerException e) {
			e.printStackTrace();
		}
		plugin.getLogger().info("Saving players...");
		plugin.saveClassFile();
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setHealth(int val) {
		if(!buffed && val > MAX_HEALTH) health = MAX_HEALTH;
		else health = val;
	}

}
