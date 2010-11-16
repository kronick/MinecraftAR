package com.newuntitledpage.minecraftAR;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.*;
import java.net.*;
import com.newuntitledpage.minecraftAR.model.*;

import processing.core.*;
import processing.opengl.*;
import codeanticode.glgraphics.*;
import javax.media.opengl.*;

import damkjer.ocd.*;

public class Test extends PApplet {

	World world;
	Parser p;

	Camera cam;
	
	ScrollWheelEvent wheel;
	
	int[] cubeFaceDisplayLists;
	float[][] blockColors;

	float[][] clipPlanes;
	
	public void setup() {

		size(800,600, GLConstants.GLGRAPHICS);
		//size(800,600, P3D);
		colorMode(HSB);
		
		cam = new Camera(this, 0,-100,-100, 0,-90,-90,
							radians(90), width/(float)height, .1f, 1000f);
		wheel = new ScrollWheelEvent();
		

		Socket mcSocket = null;
		DataOutputStream out = null;
		DataInputStream  in  = null;
		System.out.println("Starting connection...");

		try {
			mcSocket = new Socket("localhost", 255); //128.54.23.252
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
		
		GLGraphics renderer = (GLGraphics)g;
		renderer.beginGL();
		GL gl = renderer.gl;
		buildDisplayLists(gl);
		renderer.endGL();
	}


	public void draw() {
		println(frameRate);
		// Update the camera's position
		cam.feed();
	
		background(255);
		//translate(-256/2f, -256/2f, 0);
		translate(-128,32,-128);

		float[] camPosition = cam.position();
		camPosition[0] += 128;
		camPosition[1] -= 32; camPosition[1] *= -1;
		camPosition[2] += 128;

		//pointLight(0,0,255, camPosition[0], -camPosition[1]+ 10, camPosition[2]);
		lights();
		
		GLGraphics renderer = (GLGraphics)g;
		renderer.beginGL();
		//ambient(0,255,50);

		GL gl = renderer.gl;
		//gl.glFrustum(width, -width, height, -height, .1f, 1000);

		//gl.glDisable(gl.GL_CULL_FACE);
		//gl.glEnable(gl.GL_CULL_FACE);
		//gl.glCullFace(gl.GL_BACK);
		gl.glEnable(gl.GL_LIGHTING);
		gl.glColorMaterial(gl.GL_FRONT, gl.GL_AMBIENT_AND_DIFFUSE);
		gl.glEnable(gl.GL_COLOR_MATERIAL);

		if( world.level != null) {
			int n = 0;
			
			for(int y=30; y<64; y++) {
				for(int x=64; x<128; x++) {
					for(int z=64; z<128; z++) {
						byte block = world.level.blocks[y][x][z];
						byte exposures = world.level.blockExposures[y][x][z];
						if(block != Block.AIR) {
							pushMatrix();
							translate(x,-y,z);
							//fill(255,255,255);
							noStroke();
							
							if(true || (exposures & Block.ALLFACES) > 0) {	// Are any faces drawn?
								/*
								gl.glColor3f(0,0,0);
								gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_LINE );
								//gl.glLineWidth(4);
								gl.glBegin(GL.GL_LINES);
								if((exposures & Block.FRONTFACE) > 0) gl.glCallList(cubeFaceDisplayLists[0]);
								if((exposures & Block.BACKFACE) > 0) gl.glCallList(cubeFaceDisplayLists[1]);
								if((exposures & Block.LEFTFACE) > 0) gl.glCallList(cubeFaceDisplayLists[2]);
								if((exposures & Block.RIGHTFACE) > 0) gl.glCallList(cubeFaceDisplayLists[3]);
								if((exposures & Block.TOPFACE) > 0) gl.glCallList(cubeFaceDisplayLists[4]);
								if((exposures & Block.BOTTOMFACE) > 0) gl.glCallList(cubeFaceDisplayLists[5]);	
								gl.glEnd();
								*/
								
								if(block < blockColors.length) {
									if(blockColors[block].length == 3)
										gl.glColor3f(blockColors[block][0], blockColors[block][1], blockColors[block][2]);
									else if(blockColors[block].length == 4)
										gl.glColor4f(blockColors[block][0], blockColors[block][1], blockColors[block][2], blockColors[block][3]);
								}
								else
									gl.glColor3f(.25f,.25f,.25f);	
								
								gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_FILL );
								gl.glBegin(GL.GL_QUADS);
								
								if(camPosition[2] > z && (exposures & Block.FRONTFACE) > 0) gl.glCallList(cubeFaceDisplayLists[0]);
								if(camPosition[2] < z && (exposures & Block.BACKFACE) > 0) gl.glCallList(cubeFaceDisplayLists[1]);
								if(camPosition[0] < x && (exposures & Block.LEFTFACE) > 0) gl.glCallList(cubeFaceDisplayLists[2]);
								if(camPosition[0] > x && (exposures & Block.RIGHTFACE) > 0) gl.glCallList(cubeFaceDisplayLists[3]);
								if(camPosition[1] > y && (exposures & Block.TOPFACE) > 0) gl.glCallList(cubeFaceDisplayLists[4]);
								if(camPosition[1] < y && (exposures & Block.BOTTOMFACE) > 0) gl.glCallList(cubeFaceDisplayLists[5]);
								
								/*
								if(camPosition[2] > z) gl.glCallList(cubeFaceDisplayLists[0]);
								if(camPosition[2] < z) gl.glCallList(cubeFaceDisplayLists[1]);
								if(camPosition[0] < x) gl.glCallList(cubeFaceDisplayLists[2]);
								if(camPosition[0] > x) gl.glCallList(cubeFaceDisplayLists[3]);
								if(camPosition[1] > y) gl.glCallList(cubeFaceDisplayLists[4]);
								if(camPosition[1] < y) gl.glCallList(cubeFaceDisplayLists[5]);
								*/
								gl.glEnd();								
								n++;
							}
							popMatrix();
						}
					}
				}
			}
			System.out.println("Blocks drawn: " + n);

		}
		else {
			p.process();
		}

		renderer.endGL();
	}



	private void buildBlockColors() {

		float[][] c = {{0,0,0,0},		// AIR
				     {.4f,.4f,.4f},		// STONE
				  	 {.2f,.9f,.2f},	// GRASS
				  	 {.457f, .355f, .191f},	// DIRT
				     {.6f,.6f,.6f},		// COBBLESTONE
				     {.6f, .355f, .191f},	// WOOD
				     {120,150,255,180},// SAPLING
				     {220,0,255},	// BEDROCK
				     {0,.1f,1f, .7f},	// WATER
				     {0,.1f,1f, .7f},	// STILL WATER
				     {220,0,255},	// BEDROCK
				     {220,0,255},	// BEDROCK
				     {220,0,255},	// BEDROCK
				     {220,0,255},	// BEDROCK
				     {220,0,255},	// BEDROCK
				     {220,0,255},	// BEDROCK
				     {220,0,255},	// BEDROCK
				     {.6f, .355f, .191f}	//{.2f,.9f,.2f,.7f}	// LEAVES
				  };
		blockColors = c;
	}

	private void buildDisplayLists(GL gl) {
		cubeFaceDisplayLists = new int[6];
		
		// Reserve list numbers and assign to all 6 members of the array
		cubeFaceDisplayLists[0] = gl.glGenLists(6);
		for(int i=1; i<6; i++) {
			cubeFaceDisplayLists[i] = cubeFaceDisplayLists[0] + i; 
		}
		
		// FRONT
		gl.glNewList(cubeFaceDisplayLists[0], GL.GL_COMPILE);
		gl.glNormal3f(0.0f, 0.0f, 1.0f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, 0.5f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(0.5f, -0.5f, 0.5f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(0.5f, 0.5f, 0.5f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(-0.5f, 0.5f, 0.5f);
		gl.glEndList();
		
		// BACK
		gl.glNewList(cubeFaceDisplayLists[1], GL.GL_COMPILE);
		gl.glNormal3f(0.0f, 0.0f, -1.0f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, -0.5f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(-0.5f, 0.5f, -0.5f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(0.5f, 0.5f, -0.5f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(0.5f, -0.5f, -0.5f);
		gl.glEndList();
		
		// LEFT
		gl.glNewList(cubeFaceDisplayLists[2], GL.GL_COMPILE);
		gl.glNormal3f(-1.0f, 0.0f, 0.0f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, -0.5f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, 0.5f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(-0.5f, 0.5f, 0.5f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(-0.5f, 0.5f, -0.5f);	
		gl.glEndList();
		
		// RIGHT
		gl.glNewList(cubeFaceDisplayLists[3], GL.GL_COMPILE);
		gl.glNormal3f(1.0f, 0.0f, 0.0f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(0.5f, -0.5f, -0.5f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(0.5f, 0.5f, -0.5f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(0.5f, 0.5f, 0.5f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(0.5f, -0.5f, 0.5f);
		gl.glEndList();

		// TOP
		gl.glNewList(cubeFaceDisplayLists[4], GL.GL_COMPILE);
		gl.glNormal3f(0.0f, -1.0f, 0.0f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(-0.5f, -0.5f, -0.5f);  // Bottom Face
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(0.5f, -0.5f, -0.5f);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(0.5f, -0.50f, 0.5f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, 0.5f);
		gl.glEndList();
		
		//BOTTOM
		gl.glNewList(cubeFaceDisplayLists[5], GL.GL_COMPILE);		
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex3f(-0.5f, 0.5f, -0.5f);// Top Face
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex3f(-0.5f, 0.5f, 0.5f);
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex3f(0.5f, 0.5f, 0.5f);
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex3f(0.5f, 0.5f, -0.5f);
		gl.glEndList();
				
		
	}
	
	/*
	public void regenClipPlanes(PVector pos, PVector dir, float fov, float aspect, float nearDist, float farDist) {
		// To be called every time the FOV changes
		// PLANE: A*x + B*y + C*z + D = 0
		// DISTANCE TO PLANE: d = A*px + B*py + C*pz + D
		cam.
		
		float nearHeight = 2 * tan(fov/2) * nearDist;
		float nearWidth  = nearHeight * aspect;
		float farHeight  = 2 * tan(fov/2) * farDist;
		float farWidth   = farHeight * aspect;
	}
	 */
	
	public void keyPressed() {

	}
	
	public void mouseDragged() {
		if(mouseButton == LEFT) {
			cam.tilt(radians(mouseY - pmouseY) / 2.0f);
			cam.pan(radians(mouseX - pmouseX) / 2.0f);
		}
		else if(mouseButton == RIGHT) {
			cam.arc(-radians(mouseY - pmouseY) / 2.0f);
			cam.circle(-radians(mouseX - pmouseX) / 2.0f);
		}
		else if(mouseButton == CENTER) {
			cam.boom(-(mouseY-pmouseY) / 10f);
			cam.truck(-(mouseX-pmouseX) / 10f);
		}
	}
	
	
	public class ScrollWheelEvent implements MouseWheelListener {
		public ScrollWheelEvent() {
			addMouseWheelListener(this);
		}
		public void mouseWheelMoved(MouseWheelEvent e) {
			String message;
			int notches = e.getWheelRotation();
			float speed = 1;
			if(keyPressed && key == CODED && keyCode == CONTROL) speed = 10f;
			cam.dolly(notches * speed);
		}
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