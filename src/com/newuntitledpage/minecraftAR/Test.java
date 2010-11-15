package com.newuntitledpage.minecraftAR;

import java.io.*;
import java.net.*;
import com.newuntitledpage.minecraftAR.model.*;

import processing.core.*;
import processing.opengl.*;
import codeanticode.glgraphics.*;
import javax.media.opengl.*;

import peasy.*;


public class Test extends PApplet {

	World world;
	Parser p;

	PeasyCam cam;
	GLModel cube;

	int[] blockColors;

	public void setup() {

		//size(800,600, GLConstants.GLGRAPHICS);
		size(800,600, P3D);
		colorMode(HSB);
		cam = new PeasyCam(this, 100);
		cam.setMinimumDistance(1);
		cam.setMaximumDistance(500);

		Socket mcSocket = null;
		DataOutputStream out = null;
		DataInputStream  in  = null;
		System.out.println("Starting connection...");

		try {
			mcSocket = new Socket("localhost", 25565); //128.54.23.252
			out = new DataOutputStream(mcSocket.getOutputStream());
			in  = new DataInputStream(mcSocket.getInputStream());
		}
		catch(UnknownHostException e) {
			System.err.println("Unknown hostname!");
			System.exit(1);
		}
		catch(IOException e) {
			System.err.println("IO Error connecting to server: " + e);
			System.exit(1);
		}
		// Send identification
		try {
			out.writeByte(0x00);
			out.writeByte(0x07);
			out.write(pad("skronick",64).getBytes());
			out.write(pad("d896eb60d2e98cfacafdabf0a7c313ad", 64).getBytes());	// This must be gathered from the minecraft.net page
																				// if server authentication is on
			out.writeByte(0x00);
		}
		catch(IOException e) {
			System.err.println(e);
		}

		System.out.println("Login info sent.");

		world = new World();
		p = new Parser(in, world);

		buildBlockColors();
	}


	public void draw() {
		//frustum(width, -width, height, -height, -1000, 0);
		background(0);
		translate(-256/2f, -256/2f, -32);

		lights();

		/*
		GLGraphics renderer = (GLGraphics)g;
		renderer.beginGL();
		ambient(0,255,50);

		GL gl = renderer.gl;
*/
		//gl.glCullFace(gl.GL_BACK);
		//gl.glEnable(gl.GL_LIGHTING);
		//gl.glColorMaterial(gl.GL_FRONT, gl.GL_AMBIENT_AND_DIFFUSE);
		//gl.glEnable(gl.GL_COLOR_MATERIAL);

		if( world.level != null) {
			int n = 0;

			for(int y=10; y<51; y++) {
				for(int x=0; x<256; x++) {
					for(int z=0; z<256; z++) {
						byte b = world.level.blocks[y][x][z];
						if(b != Block.AIR) {
							pushMatrix();
							translate(z,x,y);
							fill(255,255,255);
							noStroke();

							if(b < blockColors.length)
								fill(blockColors[b]);
							else
								fill(0,0,255,0);
							//cube(1, gl);
							box(1);


							popMatrix();
							n++;
						}
					}
				}
			}
			System.out.println("Blocks drawn: " + n);

		}
		else {
			p.process();
		}

		//renderer.endGL();
	}


	public void cube(float size, GL gl) {
		cube(size, gl, GL.GL_QUADS);
	}
	public void cube(float size, GL gl, int mode) {
		gl.glBegin(mode);

		// FRONT
		//gl.glNormal3f(0,0,1);
		gl.glVertex3f(-size/2, size/2, size/2);
		gl.glVertex3f(size/2, size/2, size/2);
		gl.glVertex3f(size/2, -size/2, size/2);
		gl.glVertex3f(-size/2, -size/2, size/2);

		// BOTTOM
		//gl.glNormal3f(0,-1,0);
		gl.glVertex3f(-size/2, -size/2, size/2);
		gl.glVertex3f(-size/2, -size/2, -size/2);
		gl.glVertex3f(size/2, -size/2, -size/2);
		gl.glVertex3f(size/2, -size/2, size/2);

		// TOP
		//gl.glNormal3f(0,1,0);
		gl.glVertex3f(-size/2, size/2, size/2);
		gl.glVertex3f(-size/2, size/2, -size/2);
		gl.glVertex3f(size/2, size/2, -size/2);
		gl.glVertex3f(size/2, size/2, size/2);

		// RIGHT
		//gl.glNormal3f(1,0,0);
		gl.glVertex3f(size/2, size/2, size/2);
		gl.glVertex3f(size/2, size/2, -size/2);
		gl.glVertex3f(size/2, -size/2, -size/2);
		gl.glVertex3f(size/2, -size/2, size/2);

		// LEFT
		//gl.glNormal3f(-1,0,0);
		gl.glVertex3f(-size/2, -size/2, size/2);
		gl.glVertex3f(-size/2, -size/2, -size/2);
		gl.glVertex3f(-size/2, size/2, -size/2);
		gl.glVertex3f(-size/2, size/2, size/2);

		// BACK
		//gl.glNormal3f(0,0,-1);
		gl.glVertex3f(-size/2, -size/2, -size/2);
		gl.glVertex3f(size/2, -size/2, -size/2);
		gl.glVertex3f(size/2, size/2, -size/2);
		gl.glVertex3f(-size/2, size/2, -size/2);

		gl.glEnd();
	}

	private void buildBlockColors() {
		/*
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
		*/
		int[] c = {color(0,0,0,0),		// AIR
				   color(0,0,150),		// STONE
				   color(120,200,255),	// GRASS
				   color(20,10,200),	// DIRT
				   color(0,0,175),		// COBBLESTONE
				   color(20,10,170),	// WOOD
				   color(120,150,255,180),// SAPLING
				   color(220,0,255)	// BEDROCK
				  };
		blockColors = c;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    PApplet.main(new String[] { "com.newuntitledpage.minecraftAR.Test" });

	}



	public static String pad(String s, int len) {
		return String.format("%1$-" + len + "s", s);
	}

}