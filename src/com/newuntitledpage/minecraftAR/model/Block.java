package com.newuntitledpage.minecraftAR.model;

public class Block {
	short x, y, z;
	byte type;
	public static final int AIR = 			0x00;
	public static final int STONE = 		0x01;
	public static final int GRASS = 		0x02;
	public static final int DIRT = 			0x03;
	public static final int COBBLESTONE = 	0x04;
	public static final int WOOD = 			0x05;
	public static final int SAPLING = 		0x06;
	public static final int BEDROCK = 		0x07;
	public static final int WATER = 		0x08;
	public static final int STILL_WATER = 	0x09;
	public static final int LAVA = 			0x0a;
	public static final int STILL_LAVA = 	0x0b;
	public static final int SAND = 			0x0c;
	public static final int GRAVEL = 		0x0d;
	public static final int GOLD_ORE = 		0x0e;
	public static final int IRON_ORE = 		0x0f;
	public static final int COAL_ORE = 		0x10;
	public static final int LOG = 			0x11;
	public static final int LEAVES = 		0x12;
	public static final int SPONGE = 		0x13;
	public static final int GLASS = 		0x14;
	public static final int RED_CLOTH = 	0x15;
	public static final int ORANGE_CLOTH =	0x16;
	public static final int YELLOW_CLOTH =  0x17;

	public Block(short x, short y, short z, byte type) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
	}
}