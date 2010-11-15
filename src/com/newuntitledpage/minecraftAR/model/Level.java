package com.newuntitledpage.minecraftAR.model;

public class Level {
	public byte[][][] blocks;

	public Level(int x, int y, int z) {
		blocks = new byte[y][x][z];
	}


	public void setBlock(Block b, int x, int y, int z) {
		//blocks[x][y][z] = b;
	}
}
