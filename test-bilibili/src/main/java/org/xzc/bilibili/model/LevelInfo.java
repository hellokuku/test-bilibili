package org.xzc.bilibili.model;

import com.alibaba.fastjson.annotation.JSONField;

public class LevelInfo {
	@JSONField(name = "current_level")
	private int currentLevel;
	@JSONField(name = "current_min")
	private int currentMin;
	@JSONField(name = "current_exp")
	private int currentExp;
	@JSONField(name = "next_exp")
	private int nextExp;

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public int getCurrentMin() {
		return currentMin;
	}

	public void setCurrentMin(int currentMin) {
		this.currentMin = currentMin;
	}

	public int getCurrentExp() {
		return currentExp;
	}

	public void setCurrentExp(int currentExp) {
		this.currentExp = currentExp;
	}

	public int getNextExp() {
		return nextExp;
	}

	public void setNextExp(int nextExp) {
		this.nextExp = nextExp;
	}
}
