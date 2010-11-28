package com.newuntitledpage.minecraftAR.model;

import javax.media.opengl.*;

public class Player {
	public byte ID;
	public short[] position;
	public byte[] orientation;
	public String name;

	// Display list indices for body parts
	public static final int TORSO = 0;
	public static final int HEAD = 1;
	public static final int ARM_LEFT = 2;
	public static final int ARM_RIGHT = 3;
	public static final int LEG_LEFT = 4;
	public static final int LEG_RIGHT = 5;

	public Player(byte ID, String name, short x, short y, short z, byte heading, byte pitch) {
		this.ID = ID;
		this.position = new short[3];
		this.position[0] = x;
		this.position[1] = y;
		this.position[2] = z;
		this.orientation = new byte[2];
		this.orientation[0] = heading;
		this.orientation[1] = pitch;
		this.name = name.trim();
	}

	public static int makeDisplayLists(GL gl) {
		int start = gl.glGenLists(6);

		float[][][] torso = {{{0,0, 1}, {20/64f, 32/32f, -0.25f,-0.375f, 0.125f}, {28/64f, 32/32f,  0.25f,-0.375f, 0.125f},
									    {28/64f, 20/32f,  0.25f, 0.375f, 0.125f}, {20/64f, 20/32f, -0.25f, 0.375f, 0.125f}}, // Front

							 {{0,0,-1}, {32/64f, 32/32f,  0.25f,-0.375f,-0.125f}, {40/64f, 32/32f, -0.25f,-0.375f,-0.125f},
										{40/64f, 20/32f, -0.25f, 0.375f,-0.125f}, {32/64f, 20/32f,  0.25f, 0.375f,-0.125f}}, // Back

							 {{-1,0,0}, {16/64f, 20/32f, -0.25f, 0.375f,-0.125f}, {16/64f, 32/32f, -0.25f,-0.375f,-0.125f},
										{20/64f, 32/32f, -0.25f,-0.375f, 0.125f}, {20/64f, 20/32f, -0.25f, 0.375f, 0.125f}}, // Left

							 {{ 1,0,0}, {28/64f, 20/32f,  0.25f, 0.375f, 0.125f}, {28/64f, 32/32f,  0.25f,-0.375f, 0.125f},
										{32/64f, 32/32f,  0.25f,-0.375f,-0.125f}, {32/64f, 20/32f,  0.25f, 0.375f,-0.125f}}, // Right

							 {{0, 1,0}, {20/64f, 16/32f, -0.25f, 0.375f,-0.125f}, {20/64f, 20/32f, -0.25f, 0.375f, 0.125f},
							 		    {28/64f, 20/32f,  0.25f, 0.375f, 0.125f}, {28/64f, 16/32f,  0.25f, 0.375f,-0.125f}}, // Top

							 {{0, 1,0}, {28/64f, 16/32f,  0.25f,-0.375f,-0.125f}, {28/64f, 20/32f,  0.25f,-0.375f, 0.125f},
								 		{36/64f, 20/32f, -0.25f,-0.375f, 0.125f}, {36/64f, 16/32f, -0.25f,-0.375f,-0.125f}}  // Bottom
							};

		float[][][] head  = {{{0,0, 1}, {20/64f, 32/32f, -0.25f,0.00f, 0.25f}, {28/64f, 32/32f,  0.25f,0.00f, 0.25f},
									    {28/64f, 20/32f,  0.25f,0.50f, 0.25f}, {20/64f, 20/32f, -0.25f,0.50f, 0.25f}}, // Front

							 {{0,0,-1}, {32/64f, 32/32f,  0.25f,0.00f,-0.25f}, {40/64f, 32/32f, -0.25f,0.00f,-0.25f},
										{40/64f, 20/32f, -0.25f,0.50f,-0.25f}, {32/64f, 20/32f,  0.25f,0.50f,-0.25f}}, // Back

							 {{-1,0,0}, {16/64f, 20/32f, -0.25f,0.50f,-0.25f}, {16/64f, 32/32f, -0.25f,0.00f,-0.25f},
										{20/64f, 32/32f, -0.25f,0.00f, 0.25f}, {20/64f, 20/32f, -0.25f,0.50f, 0.25f}}, // Left

							 {{ 1,0,0}, {28/64f, 20/32f,  0.25f,0.50f, 0.25f}, {28/64f, 32/32f,  0.25f,0.00f, 0.25f},
										{32/64f, 32/32f,  0.25f,0.00f,-0.25f}, {32/64f, 20/32f,  0.25f,0.50f,-0.25f}}, // Right

							 {{0, 1,0}, {20/64f, 16/32f, -0.25f,0.50f,-0.25f}, {20/64f, 20/32f, -0.25f,0.50f, 0.25f},
							 		    {28/64f, 20/32f,  0.25f,0.50f, 0.25f}, {28/64f, 16/32f,  0.25f,0.50f,-0.25f}}, // Top

							 {{0, 1,0}, {28/64f, 16/32f,  0.25f,0.00f,-0.25f}, {28/64f, 20/32f,  0.25f,0.00f, 0.25f},
								 		{36/64f, 20/32f, -0.25f,0.00f, 0.25f}, {36/64f, 16/32f, -0.25f,0.00f,-0.25f}}  // Bottom
							};

		float[][][][] bodyParts = {torso, head};

		for(int i=0; i<bodyParts.length; i++) {
			gl.glNewList(start + i, GL.GL_COMPILE);
			for(int f=0; f<bodyParts[i].length; f++) {
				gl.glNormal3f(bodyParts[i][f][0][0], bodyParts[i][f][0][1], bodyParts[i][f][0][2]);
				for(int v=1; v<bodyParts[i][f].length; v++) {
					if(bodyParts[i][f][v].length == 5) {
						gl.glTexCoord2f(bodyParts[i][f][v][0], bodyParts[i][f][v][1]);
						gl.glVertex3f(bodyParts[i][f][v][2], bodyParts[i][f][v][3], bodyParts[i][f][v][4]);
					}
					else {
						gl.glVertex3f(bodyParts[i][f][v][0], bodyParts[i][f][v][1], bodyParts[i][f][v][2]);
					}
				}
			}
			gl.glEndList();
		}
		return start;
	}
}
