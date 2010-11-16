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
		// Turn on faces of this block that do not touch an opaque, drawn neighbor
		blockExposures[y][x][z] = (byte)((blockExposures[y][x][z] & Block.BLOCKDRAWN) | 
										 (!neighborOccludes(x, y, z,  0,  0,  1) ? Block.FRONTFACE : 0) |
								  		 (!neighborOccludes(x, y, z,  0,  0, -1) ? Block.BACKFACE : 0) |
								  		 (!neighborOccludes(x, y, z, -1,  0,  0) ? Block.LEFTFACE : 0) |
								  		 (!neighborOccludes(x, y, z,  1,  0,  0) ? Block.RIGHTFACE : 0) |
								  		 (!neighborOccludes(x, y, z,  0,  1,  0) ? Block.TOPFACE : 0) |
								  		 (!neighborOccludes(x, y, z,  0, -1,  0) ? Block.BOTTOMFACE : 0));
		/*
		if(blockIsDrawn(x,y,z) && !Block.isTransparent(blocks[y][x][z])) {
			// Set neighbors' shared faces to be off if this block occludes them
			if(z+1 < zsize) blockExposures[y][x][z+1] &= ~Block.BACKFACE;
			if(z-1 > 0)     blockExposures[y][x][z-1] &= ~Block.FRONTFACE;
			if(y-1 > 0)     blockExposures[y-1][x][z] &= ~Block.RIGHTFACE;
			if(y+1 < ysize) blockExposures[y+1][x][z] &= ~Block.LEFTFACE;
			if(x+1 < xsize) blockExposures[y][x+1][z] &= ~Block.BOTTOMFACE;
			if(x-1 > 0)     blockExposures[y][x-1][z] &= ~Block.TOPFACE;
		}
		*/
	}
		
	private boolean neighborOccludes(int x, int y, int z, int dx, int dy, int dz) {
		if(blockIsDrawn(x+dx, y+dy, z+dz) && !isTransparent(x+dx, y+dy, z+dz))
			return true;
		else return false;
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
	
	public void setBlock(Block b, int x, int y, int z) {
		//blocks[x][y][z] = b;
	}
}
