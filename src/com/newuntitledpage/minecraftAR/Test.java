package com.newuntitledpage.minecraftAR;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.*;
import java.net.*;
import java.util.Arrays;

import com.newuntitledpage.minecraftAR.model.*;
import com.sun.opengl.util.texture.*;

import processing.core.*;
import processing.opengl.*;
import codeanticode.glgraphics.*;
import javax.media.opengl.*;

import processing.video.*;

import jp.nyatla.nyar4psg.*;

import damkjer.ocd.*;

public class Test extends PApplet {

	World world;
	Parser p;

	Camera cam;
	GLGraphics renderer;
	GL gl;

	ScrollWheelEvent wheel;

	Capture cameraIn;
	NyARBoard nya;

	PVector modelCenter;
	float modelScale = 100;

	double[] transMat;
	double smoothFactor = 1;

	int[][] blockFaceDisplayLists;
	int[][] textureMap;
	Texture terrainTex;
	Texture playerTex;
	int playerDisplayListStart;

	PImage testImage;

	float[][] clipPlanes;

	public void setup() {
		// UGLY HACK FOR TEH MAC
		try {
			quicktime.QTSession.open();
			} catch (quicktime.QTException qte) {
			qte.printStackTrace();
		}

		// - - - - - - - - - - -
		size(1280,800, GLGraphics.GLGRAPHICS);
		colorMode(RGB);

		println(Capture.list());
		cameraIn = new Capture(this,640, 512);


		nya = new NyARBoard(this, 640,512, "camera_para.dat", "4x4_35.patt", (int)(80 * modelScale/10f), NyARBoard.CS_LEFT);
		nya.gsThreshold = 100;
		transMat = new double[16];
		Arrays.fill(transMat, 0);

		modelCenter = new PVector(11.5f,43.5f,33.5f);

		cam = new Camera(this, 0,-100,-100, 0,-90,-90,
							radians(90), width/(float)height, .1f, 1000f);
		wheel = new ScrollWheelEvent();


		Socket mcSocket = null;
		DataOutputStream out = null;
		DataInputStream  in  = null;
		System.out.println("Starting connection...");

		try {
			mcSocket = new Socket("128.54.23.252", 255); //128.54.23.252
			//mcSocket = new Socket("localhost", 25565); //128.54.23.252
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
		p = new Parser(in, out, world);
		p.start();

		buildTextureMap();

		// LOAD TEXTURE FILES
		try {
			FileInputStream stream = new FileInputStream("data/textures/terrain.png");
        	TextureData data = TextureIO.newTextureData(stream, false, "png");
        	terrainTex = TextureIO.newTexture(data);
        	terrainTex.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);

        	stream = new FileInputStream("data/textures/char.png");
        	TextureData data2 = TextureIO.newTextureData(stream, false, "png");
        	playerTex = TextureIO.newTexture(data2);
        	playerTex.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		}
		catch (IOException exc) {
			exc.printStackTrace(); System.exit(1);
		}

		renderer = (GLGraphics)g;
		gl = renderer.beginGL();
		buildDisplayLists(gl);
		playerDisplayListStart = Player.makeDisplayLists(gl);

		/*
		float[] fogCol={1f,1f,1f}; // Define a nice light grey
		gl.glFogfv(GL.GL_FOG_COLOR,fogCol,0);     // Set the fog color
		gl.glFogi(GL.GL_FOG_MODE, GL.GL_LINEAR);
		gl.glFogf(GL.GL_FOG_START, 100f * modelScale);
		gl.glFogf(GL.GL_FOG_END, 200f * modelScale);
		gl.glEnable(GL.GL_FOG);
		 */
		renderer.endGL();

		testImage = loadImage("testcapture.png");
	}


	public void draw() {
		println(frameRate);

		background(255,255,255);

		if(cameraIn.available() == true) {
			cameraIn.read();

			//hint(DISABLE_DEPTH_TEST);
			if(!(keyPressed && key == 'v')) {
				loadPixels();
				cameraIn.loadPixels();
				int s, t, n;
				for(int x=0; x<width; x++) {
					for(int y=0; y<height; y++) {
						n = (int)(cameraIn.width * x/(float)width) + (int)(cameraIn.height * y/(float)height) * cameraIn.width;
						pixels[x + y*width] = cameraIn.pixels[n];
					}
				}
				updatePixels();
			}

			pushMatrix();
				scale(width/(float)cameraIn.width, height/(float)cameraIn.height);
				if(nya.detect(cameraIn)) {
				//if(nya.detect(testImage)) {
					drawMarkerPos(nya.pos2d);
				}
			popMatrix();
			hint(ENABLE_DEPTH_TEST);
		}

		renderer = (GLGraphics)g;
		gl = renderer.beginGL();

		for(int i=0; i<nya.transmat.length; i++) {
			transMat[i] += (nya.transmat[i] - transMat[i]) * smoothFactor;
		}
		PMatrix3D toCameraMatrix = new PMatrix3D((float)transMat[0], (float)transMat[1], (float)transMat[2], (float)transMat[3],
											(float)transMat[4], (float)transMat[5], (float)transMat[6], (float)transMat[7],
											(float)transMat[8], (float)transMat[9], (float)transMat[10], (float)transMat[11],
											(float)transMat[12], (float)transMat[13], (float)transMat[14], (float)transMat[15]);

		float[] camPosition = {0,0,0};
		if(toCameraMatrix.invert()){
			float[] toCamera = null;
			toCamera = toCameraMatrix.get(toCamera);

			camPosition[0] = ((toCamera[12])) / modelScale + modelCenter.x;
			camPosition[1] = ((toCamera[13])) / modelScale + modelCenter.y;
			camPosition[2] = ((toCamera[14])) / modelScale + modelCenter.z;

			if(frameCount%10==0) {
				try {
					p.out.writeByte(0x08);
					p.out.writeByte(0xFF);
					p.out.writeShort((int)camPosition[0]*32+1);
					p.out.writeShort((int)camPosition[1]*32+1);
					p.out.writeShort((int)camPosition[2]*32+1);
					p.out.writeByte(0x00);
					p.out.writeByte(0x00);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		glGraphicsBeginTransform(gl, nya);
		scale(modelScale);
		gl.glTranslatef(-modelCenter.x, -modelCenter.y, -modelCenter.z);

		gl.glEnable(GL.GL_CULL_FACE);	// For some reason this is the way things are...
		gl.glCullFace(GL.GL_FRONT);

		pushMatrix();
			translate(modelCenter.x, modelCenter.y, modelCenter.z);
			float[] ambient = {.2f,.2f,.2f,1f};
			float[] diffuse = {50f,50f,50f,1f};
			float[] position = {0,1024,512,1};

			gl.glLightfv( GL.GL_LIGHT1, GL.GL_AMBIENT, ambient,0);
			gl.glLightfv( GL.GL_LIGHT1, GL.GL_DIFFUSE, diffuse,0);
			gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, position,0);

			position[2] = -512;
			gl.glLightfv( GL.GL_LIGHT2, GL.GL_AMBIENT, ambient,0);
			gl.glLightfv( GL.GL_LIGHT2, GL.GL_DIFFUSE, diffuse,0);
			gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, position,0);

			gl.glEnable(GL.GL_LIGHTING);
			gl.glEnable(GL.GL_LIGHT1);
			gl.glEnable(GL.GL_LIGHT2);
			gl.glShadeModel(GL.GL_FLAT);


			gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
			gl.glEnable(GL.GL_COLOR_MATERIAL);
			gl.glColor3f(1f,1f,1f);

		popMatrix();
		if(world.level != null) {

			// Draw players
			playerTex.enable();
			playerTex.bind();
			Player _p;
			for(int i=0; i<world.players.size(); i++) {
				_p = world.players.get(i);
				pushMatrix();
					translate(_p.position[0]/32f-0.5f, _p.position[1]/32f-2, _p.position[2]/32f-0.5f);
					rotateY(-_p.orientation[0]/256f * 2 * PI + PI);

					pushMatrix();
						translate(0,Player.TORSO_CENTER_Y,0);
						gl.glCallList(playerDisplayListStart + Player.TORSO);
					popMatrix();

					pushMatrix();
						translate(0,Player.HEAD_CENTER_Y,0);
						pushMatrix();
							translate(-Player.TORSO_HALF_WIDTH,0,0);
							rotateX(-sin(frameCount/3f) * PI / 6);
							gl.glCallList(playerDisplayListStart + Player.ARM_LEFT);
						popMatrix();
						pushMatrix();
							translate( Player.TORSO_HALF_WIDTH,0,0);
							rotateX(sin(frameCount/3f) * PI / 6);
							gl.glCallList(playerDisplayListStart + Player.ARM_RIGHT);
						popMatrix();

						rotateX(_p.orientation[1]/256f * 2 * PI);
						gl.glCallList(playerDisplayListStart + Player.HEAD);
					popMatrix();

					translate(0,Player.WAIST_Y,0);
					pushMatrix();
						rotateX(sin(frameCount/3f) * PI / 6);
						gl.glCallList(playerDisplayListStart + Player.LEG_LEFT);
					popMatrix();
					pushMatrix();
						rotateX(-sin(frameCount/3f) * PI / 6);
						gl.glCallList(playerDisplayListStart + Player.LEG_RIGHT);
					popMatrix();

				popMatrix();
			}

			// Draw blocks
			terrainTex.enable();
			terrainTex.bind();
			int n = 0;
			for(int y=30; y<64; y++) {
				for(int x=0; x<64; x++) {
					for(int z=0; z<64; z++) {
						byte block = world.level.blocks[y][x][z];
						byte exposures = world.level.blockExposures[y][x][z];
						if(true && block != Block.AIR) {
							pushMatrix();
								translate(x,y,z);
								if((exposures & Block.ALLFACES) > 0) {	// Are any faces drawn?
									/*
									gl.glColor3f(0,0,0);
									gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_LINE );
									gl.glBegin(GL.GL_LINES);
									*/

									if(block == Block.STEP) {
										translate(0, -0.25f, 0);
										scale(1,0.5f,1);
									}

									gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_FILL);
									gl.glBegin(GL.GL_QUADS);

									if(camPosition[2] > z && (exposures & Block.FRONTFACE) > 0) gl.glCallList(blockFaceDisplayLists[block][0]);
									if(camPosition[2] < z && (exposures & Block.BACKFACE) > 0) gl.glCallList(blockFaceDisplayLists[block][1]);
									if(camPosition[0] > x && (exposures & Block.RIGHTFACE) > 0) gl.glCallList(blockFaceDisplayLists[block][2]);
									if(camPosition[0] < x && (exposures & Block.LEFTFACE) > 0) gl.glCallList(blockFaceDisplayLists[block][3]);
									if(camPosition[1] < y && (exposures & Block.BOTTOMFACE) > 0) gl.glCallList(blockFaceDisplayLists[block][4]);
									if(camPosition[1] > y && (exposures & Block.TOPFACE) > 0) gl.glCallList(blockFaceDisplayLists[block][5]);

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

		glGraphicsEndTransform(gl, nya);

		gl.glDisable(GL.GL_CULL_FACE);
		gl.glDisable(GL.GL_LIGHTING);
		renderer.endGL();
	}


	void drawMarkerPos(int[][] points) {
		stroke(100,0,0);
		fill(100,0,0);
		for(int i=0;i<4;i++){
			ellipse(nya.pos2d[i][0], nya.pos2d[i][1],5,5);
		}
	}

	private void glGraphicsBeginTransform(GL gl, NyARBoard nya) {
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glLoadMatrixd(nya.projection,0);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glLoadMatrixd(transMat,0);

		gl.glPushMatrix();
		return;
	}

	private void glGraphicsEndTransform(GL gl, NyARBoard nya) {
		if(gl==null){
			die("The function beginTransform is never called.", null);
		}
		gl.glPopMatrix();
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}

	public void buildTextureMap() {
		int[][] t = {{14,14,14,14,14,14},	// AIR
					 {1,1,1,1,1,1},			// STONE
					 {3,3,3,3,2,0},			// GRASS
					 {2,2,2,2,2,2},			// DIRT
					 {16,16,16,16,16,16},	// COBBLESTONE
					 {4,4,4,4,4,4}, // WOOD
					 {15,15,15,15,15,15}, // SAPLING
					 {17,17,17,17,17,17},// BEDROCK
					 {14,14,14,14,14,14},// WATER
					 {14,14,14,14,14,14},// STATIONARY WATER
					 {30,30,30,30,30,30},// LAVA
					 {30,30,30,30,30,30},// STATIONARY LAVA
					 {18,18,18,18,18,18},// SAND
					 {19,19,19,19,19,19},// GRAVEL
					 {32,32,32,32,32,32},// GOLD ORE
					 {33,33,33,33,33,33},// IRON ORE
					 {34,34,34,34,34,34},// COAL ORE
					 {20,20,20,20,21,21},// LOG
					 {22,22,22,22,22,22},// LEAVES
					 {48,48,48,48,48,48},// SPONGE
					 {49,49,49,49,49,49},// GLASS
					 {64,64,64,64,64,64},// RED CLOTH
					 {65,65,65,65,65,65},// ORANGE CLOTH
					 {66,66,66,66,66,66},// YELLOW CLOTH
					 {67,67,67,67,67,67},// LIME CLOTH
					 {68,68,68,68,68,68},// GREEN CLOTH
					 {69,69,69,69,69,69},// AQUA GREEN CLOTH
					 {70,70,70,70,70,70},// CYAN CLOTH
					 {71,71,71,71,71,71},// BLUE CLOTH
					 {72,72,72,72,72,72},// PURPLE CLOTH
					 {73,73,73,73,73,73},// INDIGO CLOTH
					 {74,74,74,74,74,74},// VIOLET CLOTH
					 {75,75,75,75,75,75},// MAGENTA CLOTH
					 {76,76,76,76,76,76},// PINK CLOTH
					 {77,77,77,77,77,77},// BLACK CLOTH
					 {78,78,78,78,78,78},// GRAY CLOTH
					 {79,79,79,79,79,79},// WHITE CLOTH
					 {13,13,13,13,13,13},// YELLOW FLOWER
					 {12,12,12,12,12,12},// RED ROSE
					 {29,29,29,29,29,29},// BROWN MUSHROOM
					 {28,28,28,28,28,28},// RED MUSHROOM
					 {40,40,40,40,56,34},// GOLD BLOCK
					 {39,39,39,39,55,33},// IRON BLOCK
					 {5,5,5,5,5,6,6},// DOUBLE STEP
					 {5,5,5,5,5,6,6},// STEP
					 {7,7,7,7,7,7},// BRICK
					 {8,8,8,8,10,9},// TNT
					 {35,35,35,35,4,4},// BOOKSHELF
					 {36,36,36,36,36,36},// MOSSY COBBLESTONE
					 {37,37,37,37,37,37}// OBSIDIAN
					};

		textureMap = t;
	}

	public float[] getTextureCoords(int block, int face, int corner) {
		if(block >= textureMap.length) block = 1;
		int i = textureMap[block][face];
		float dx = (corner == 0 || corner == 1) ? 1 : 0;
		float dy = (corner == 1 || corner == 2) ? 1 : 0;
		if(block == Block.STEP && face < 4) dy *= .5f;

		float[] out = {1/16f * ((i%16) + dx),
					   1/16f * (floor(i/16) + dy)};
		return out;
	}


	private void buildDisplayLists(GL gl) {
		blockFaceDisplayLists = new int[50][6];

		// Reserve list numbers and assign to all 6 members of the array
		blockFaceDisplayLists[0][0] = gl.glGenLists(6);
		for(int i=0; i<50; i++) {
			for(int j=0; j<6; j++) {
				blockFaceDisplayLists[i][j] = blockFaceDisplayLists[0][0] + 6*i + j;
			}
		}

		for(int i=0; i<50; i++) {	// 50 different types of blocks, all slightly different.
			// FRONT
			gl.glNewList(blockFaceDisplayLists[i][0], GL.GL_COMPILE);
			gl.glNormal3f(0.0f, 0.0f, 1.0f);
			gl.glTexCoord2fv(getTextureCoords(i,0,0),0);
			gl.glVertex3f(-0.5f, 0.5f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,0,1),0);
			gl.glVertex3f(-0.5f, -0.5f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,0,2),0);
			gl.glVertex3f(0.5f, -0.5f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,0,3),0);
			gl.glVertex3f(0.5f, 0.5f, 0.5f);
			gl.glEndList();

			// BACK
			gl.glNewList(blockFaceDisplayLists[i][1], GL.GL_COMPILE);
			gl.glNormal3f(0.0f, 0.0f, -1.0f);
			gl.glTexCoord2fv(getTextureCoords(i,1,0),0);
			gl.glVertex3f(0.5f, 0.5f, -0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,1,1),0);
			gl.glVertex3f(0.5f, -0.5f, -0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,1,2),0);
			gl.glVertex3f(-0.5f, -0.5f, -0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,1,3),0);
			gl.glVertex3f(-0.5f, 0.5f, -0.5f);
			gl.glEndList();

			// RIGHT
			gl.glNewList(blockFaceDisplayLists[i][2], GL.GL_COMPILE);
			gl.glNormal3f(1.0f, 0.0f, 0.0f);
			gl.glTexCoord2fv(getTextureCoords(i,2,0),0);
			gl.glVertex3f(0.5f, 0.5f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,2,1),0);
			gl.glVertex3f(0.5f, -0.5f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,2,2),0);
			gl.glVertex3f(0.5f, -0.5f, -0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,2,3),0);
			gl.glVertex3f(0.5f, 0.5f, -0.5f);
			gl.glEndList();

			// LEFT
			gl.glNewList(blockFaceDisplayLists[i][3], GL.GL_COMPILE);
			gl.glNormal3f(-1.0f, 0.0f, 0.0f);
			gl.glTexCoord2fv(getTextureCoords(i,3,1),0);
			gl.glVertex3f(-0.5f, -0.5f, -0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,3,2),0);
			gl.glVertex3f(-0.5f, -0.5f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,3,3),0);
			gl.glVertex3f(-0.5f, 0.5f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,3,0),0);
			gl.glVertex3f(-0.5f, 0.5f, -0.5f);
			gl.glEndList();


			// BOTTOM
			gl.glNewList(blockFaceDisplayLists[i][4], GL.GL_COMPILE);
			gl.glNormal3f(0.0f, -1.0f, 0.0f);
			gl.glTexCoord2fv(getTextureCoords(i,4,0),0);
			gl.glVertex3f(-0.5f, -0.5f, -0.5f);  // Bottom Face
			gl.glTexCoord2fv(getTextureCoords(i,4,1),0);
			gl.glVertex3f(0.5f, -0.5f, -0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,4,2),0);
			gl.glVertex3f(0.5f, -0.50f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,4,3),0);
			gl.glVertex3f(-0.5f, -0.5f, 0.5f);
			gl.glEndList();

			// TOP
			gl.glNewList(blockFaceDisplayLists[i][5], GL.GL_COMPILE);
			gl.glNormal3f(0.0f, 1.0f, 0.0f);
			gl.glTexCoord2fv(getTextureCoords(i,5,0),0);
			gl.glVertex3f(-0.5f, 0.5f, -0.5f);// Top Face
			gl.glTexCoord2fv(getTextureCoords(i,5,1),0);
			gl.glVertex3f(-0.5f, 0.5f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,5,2),0);
			gl.glVertex3f(0.5f, 0.5f, 0.5f);
			gl.glTexCoord2fv(getTextureCoords(i,5,3),0);
			gl.glVertex3f(0.5f, 0.5f, -0.5f);
			gl.glEndList();
		}
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
	    PApplet.main(new String[] {"--present", "com.newuntitledpage.minecraftAR.Test" });

	}

	public static String pad(String s, int len) {
		return String.format("%1$-" + len + "s", s);
	}

}