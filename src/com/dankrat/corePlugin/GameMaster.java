package com.dankrat.corePlugin;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;

public class GameMaster {
	
	public static boolean classButton = false;
	public static boolean inInventory = false;
	public static ArrayList<ButtonClass> allButtonClasses = new ArrayList<ButtonClass>();
	public static boolean freeMode = false;
	public static boolean monsterTurn = false;
	
	public static final int ARCHER_WIZARD_PRIEST_MOVEMENT = 6;
	public static final int ROGUE_MOVEMENT = 7;
	public static final int WARRIOR_MOVEMENT = 5;
	
	public static final Vector CHAPTER1 = new Vector(412.450,26,-1285.676);
	
	public static void endTurn(ArrayList<PartyMember> players, PartyMember player) {
		
		ArrayList<PartyMember> playerPool = new ArrayList<PartyMember>(players.size());
		int done = 0;
		PartyMember[] sortedMembers = new PartyMember[players.size()];
		
		for(PartyMember p : players) {
			playerPool.add(p);
		}
		
		for(int i = 0; i < players.size(); i++) {
			int highestIndex = 0;
			int highestValue = -1;
			for(int g = 0; g < playerPool.size(); g++) {
				if(playerPool.get(g).getStartingRoll() > highestValue) {
					highestValue = playerPool.get(g).getStartingRoll();
					highestIndex = g;
				}
			}
			sortedMembers[done] = playerPool.get(highestIndex);
			playerPool.remove(highestIndex);
			done++;
		}
		
		for(int i = 0; i < sortedMembers.length; i++) {
			if(sortedMembers[i].getName().equals(player.getName())) {
				sortedMembers[i].endTurn();
				if(i+1>=sortedMembers.length) {
					monsterTurn = true;
					Bukkit.getServer().broadcastMessage(ChatColor.RED + "Monster's Turn");
				}
				else sortedMembers[i+1].setTurn(true);
				break;
			}
		}
		
	}
	
	public static PartyMember[] getSortedMembers(ArrayList<PartyMember> players) {
		ArrayList<PartyMember> playerPool = new ArrayList<PartyMember>(players.size());
		int done = 0;
		PartyMember[] sortedMembers = new PartyMember[players.size()];
		
		for(PartyMember p : players) {
			playerPool.add(p);
		}
		
		for(int i = 0; i < players.size(); i++) {
			int highestIndex = 0;
			int highestValue = -1;
			for(int g = 0; g < playerPool.size(); g++) {
				if(playerPool.get(g).getStartingRoll() > highestValue) {
					highestValue = playerPool.get(g).getStartingRoll();
					highestIndex = g;
				}
			}
			sortedMembers[done] = playerPool.get(highestIndex);
			playerPool.remove(highestIndex);
			done++;
		}
		return sortedMembers;
	}
	
	public static PartyMember searchMember(ArrayList<PartyMember> players, String name) {
		for(PartyMember m : players) {
			if(m.getName().equals(name)) {
				return m;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	public static boolean isValidNumber(String str) {
		try {
			int num = Integer.parseInt(str);
			return true;
		}catch(NumberFormatException e) {
			return false;
		}
	}
	
	@SuppressWarnings("unused")
	public static boolean isValidBoolean(String str) {
		try {
			boolean v = Boolean.parseBoolean(str);
			return true;
		}catch(Exception e) {
			return false;
		}
	}

}
