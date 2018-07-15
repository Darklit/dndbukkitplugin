package com.dankrat.corePlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommonErrors {
	
	public static boolean playerNotFound (CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "Error finding player.");
		return true;
	}
	
	public static boolean invalidValue (CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "Invalid value.");
		return true;
	}
	
	public static boolean invalidParameters(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "Invalid parameters");
		return true;
	}
}
