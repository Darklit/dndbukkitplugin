package com.dankrat.corePlugin;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ButtonClass {
	
	private Location loc;
	private Inventory classItems;
	private String className;
	private boolean finished;
	private CorePlugin plug;
	
	public ButtonClass(Location l, String name) {
		loc = l;
		className = name;
	}
	
	public ButtonClass(Location l, Inventory i, String name, CorePlugin plugin) {
		loc = l;
		classItems = i;
		className = name;
		plug = plugin;
		finished = true;
	}
	
	public ButtonClass(String name, CorePlugin plugin) {
		className = name;
		finished = false;
		plug = plugin;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public void setLocation(Location l) {
		this.loc = l;
	}
	
	public Inventory getItems() {
		return classItems;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public String getName() {
		return className;
	}
	
	public void setFinished(boolean b) {
		finished = b;
	}
	
	public void setItems(Inventory i) {
		classItems = i;
		finished = true;
		plug.getLogger().info("Saving data...");
		saveItems();
	}
	
	public void saveItems() {
		ItemStack[] savableItems = classItems.getContents();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		for(int i = 0; i < savableItems.length; i++) {
			plug.getClassData().set("classes."+className+".items."+i,savableItems[i]);
		}
		plug.getClassData().set("classes."+className+".location.x", x);
		plug.getClassData().set("classes."+className+".location.y", y);
		plug.getClassData().set("classes."+className+".location.z", z);
		plug.getLogger().info("Saving data...");
		plug.saveClasses();
	}
	
}
