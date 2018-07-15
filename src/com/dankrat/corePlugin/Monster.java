package com.dankrat.corePlugin;

import org.bukkit.boss.BossBar;

public class Monster {
	
	private int health;
	private int maxHealth;
	private BossBar healthBar;
	private String name;
	
	public Monster(int health, BossBar healthBar, String name) {
		this.health = health;
		this.healthBar = healthBar;
		this.maxHealth = health;
		this.name = name;
		healthBar.setProgress(1);
	}
	
	public String getName() {
		return name;
	}
	
	public int getHealth() {
		return health;
	}
	
	public BossBar getHealthBar() {
		return healthBar;
	}
	
	public void damage(int val) {
		setHealth(health-val);
	}
	
	private void setHealth(int val) {
		healthBar.setProgress((double)health/(double)maxHealth);
		health = val;
	}
}
