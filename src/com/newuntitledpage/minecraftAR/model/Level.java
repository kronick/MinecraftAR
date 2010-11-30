package com.newuntitledpage.minecraftAR.model;

import java.util.Arrays;

public class Level {
	public byte[][][] blocks;
	public byte[][][] blockExposures;

	int xsize, ysize, zsize;

	public Level(int x, int y, int z) {
		this.xsize = x;
		this.ysize = y;
		this.zsize = z;

		blocks = new byte[y][x][z];
		blockExposures = new byte[y][x][z];

		// Initialize with all faces on, drawing everything
		// Byte order is dtfblrTB (d = drawn?, t = transparent?, b = back, B = bottom)
		// Checking if a block is drawn is as easy as doing blockFaces[y][x][z] > 0
		for(int _y=0; _y<ysize; _y++) {
			for(int _x=0; _x<xsize; _x++) {
				for(int _z=0; _z<zsize; _z++) {
					blockExposures[_y][_x][_z] = (byte)(Block.ALLFACES | Block.BLOCKDRAWN);
				}
			}
		}
	}

	public void recalcNeighborOcclusions() {
		for(int y=0; y<ysize; y++) {
			for(int x=0; x<xsize; x++) {
				for(int z=0; z<zsize; z++) {
					recalcNeighborOcclusions(x,y,z);
				}
			}
		}
	}

	public void recalcNeighborOcclusions(int x, int y, int z) {
		recalcNeighborOcclusions(x,y,z, false);
	}

	public void recalcNeighborOcclusions(int x, int y, int z, boolean neighborsNeighbors) {
		// Turn on faces of this block that do not touch an opaque, drawn neighbor
		blockExposures[y][x][z] = (byte)((blockExposures[y][x][z] & Block.BLOCKDRAWN) |
										 (!neighborOccludes(x, y, z,  0,  0,  1) ? Block.FRONTFACE : 0) |
								  		 (!neighborOccludes(x, y, z,  0,  0, -1) ? Block.BACKFACE : 0) |
								  		 (!neighborOccludes(x, y, z, -1,  0,  0) ? Block.LEFTFACE : 0) |
								  		 (!neighborOccludes(x, y, z,  1,  0,  0) ? Block.RIGHTFACE : 0) |
								  		 (!neighborOccludes(x, y, z,  0,  1,  0) ? Block.TOPFACE : 0) |
								  		 (!neighborOccludes(x, y, z,  0, -1,  0) ? Block.BOTTOMFACE : 0));

		if(neighborsNeighbors) {
			if(z+1 < zsize) recalcNeighborOcclusions(x,y,z+1, false);
			if(z-1 > 0)     recalcNeighborOcclusions(x,y,z-1, false);
			if(y-1 > 0)     recalcNeighborOcclusions(x,y-1,z, false);
			if(y+1 < ysize) recalcNeighborOcclusions(x,y+1,z, false);
			if(x+1 < xsize) recalcNeighborOcclusions(x+1,y,z, false);
			if(x-1 > 0)     recalcNeighborOcclusions(x-1,y,z, false);
		}
	}

	private boolean neighborOccludes(int x, int y, int z, int dx, int dy, int dz) {
		if(blockIsDrawn(x+dx, y+dy, z+dz) && !isTransparent(x+dx, y+dy, z+dz))
			return true;
		else {
			if(getBlock(x+dx, y+dy, z+dz) == getBlock(x,y,z)  && isTransparent(x,y,z))
				if(getBlock(x,y,z) != -1) return true;
				else return false;
			else
				return false;
		}
	}

	private boolean blockIsDrawn(int x, int y, int z) {
		if(x > 0 && x < xsize && y > 0 && y<ysize && z > 0 && z < zsize)
			return(blockExposures[y][x][z] < 0);
		else return false;
	}

	private boolean isTransparent(int x, int y, int z) {
		if(x > 0 && x < xsize && y > 0 && y<ysize && z > 0 && z < zsize)
			return(Block.isTransparent(blocks[y][x][z]));
		else return true;
	}

	private byte getBlock(int x, int y, int z) {
		if(x > 0 && x < xsize && y > 0 && y<ysize && z > 0 && z < zsize)
			return(blocks[y][x][z]);
		else return -1;
	}
	public void setBlock(int x, int y, int z, byte type) {
		blocks[y][x][z] = type;
		System.out.println("Setting block (" + x + ", " + y + ", " + z + ") to: " + type);
		recalcNeighborOcclusions(x,y,z, true);
	}
}
