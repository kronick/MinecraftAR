package com.newuntitledpage.minecraftAR;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.*;
import java.net.*;
import java.nio.FloatBuffer;
import java.util.Arrays;

import com.newuntitledpage.minecraftAR.model.*;
import com.sun.opengl.util.BufferUtil;
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
	float modelScale = 10;

	double[] transMat;
	double smoothFactor = 0.5;

	int[][] blockFaceDisplayLists;
	int[][] textureMap;
	Texture terrainTex;
	Texture playerTex;
	int playerDisplayListStart;

	float[][] clipPlanes;

	public void setup() {
		// UGLY HACK FOR TEH MAC
		try {
			quicktime.QTSession.open();
			} catch (quicktime.QTException qte) {
			qte.printStackTrace();
		}

		// - - - - - - - - - - -
		size(800,600, GLGraphics.GLGRAPHICS);
		//colorMode(HSB);

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
		renderer.endGL();

	}


	public void draw() {
		//println(frameRate);

		background(255);

		renderer = (GLGraphics)g;
		gl = renderer.beginGL();

		for(int i=0; i<nya.transmat.length; i++) {
			if(true || i<=11) {
				transMat[i] += (nya.transmat[i] - transMat[i]) * smoothFactor;
				//transMat[i] = (nya.transmat[i] + transMat[i]) /2;
			}
			else {
				transMat[i] +=  (nya.transmat[i] - transMat[i]) * 2 * smoothFactor;
			}
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
		}

		glGraphicsBeginTransform(gl, nya);
		/*cam.feed();
		camPosition[0] = cam.position()[0]  / modelScale + modelCenter.x;
		camPosition[1] = -cam.position()[1]  / modelScale + modelCenter.y;
		camPosition[2] = cam.position()[2]  / modelScale + modelCenter.z;
		rotateZ(PI);
		 */
		scale(modelScale);
		gl.glTranslatef(-modelCenter.x, -modelCenter.y, -modelCenter.z);

		gl.glEnable(GL.GL_CULL_FACE);	// For some reason this is the way things are...
		gl.glCullFace(GL.GL_FRONT);

		pushMatrix();
			translate(modelCenter.x, modelCenter.y, modelCenter.z);
			float[] ambient = {.1f,.1f,.1f,1f};
			float[] diffuse = {10f,10f,10f,1f};
			float[] position = {100,100,0,1};

			gl.glLightfv( GL.GL_LIGHT2, GL.GL_AMBIENT, ambient,0);
			gl.glLightfv( GL.GL_LIGHT2, GL.GL_DIFFUSE, diffuse,0);
			gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, position,0);
			gl.glEnable(GL.GL_LIGHTING);
			gl.glEnable(GL.GL_LIGHT2);


			gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
			gl.glEnable(GL.GL_COLOR_MATERIAL);
			gl.glColor3f(1f,1f,1f);
			terrainTex.enable();
			terrainTex.bind();
		popMatrix();
		if(world.level != null) {
			int n = 0;

			for(int y=0; y<64; y++) {
				for(int x=0; x<64; x++) {
					for(int z=0; z<64; z++) {
						byte block = world.level.blocks[y][x][z];
						byte exposures = world.level.blockExposures[y][x][z];
						if(block != Block.AIR) {
							pushMatrix();
							translate(x,y,z);
							//fill(255,255,255);
							//noStroke();

							if((exposures & Block.ALLFACES) > 0) {	// Are any faces drawn?
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
			//System.out.println("Blocks drawn: " + n);

		}
		else {
			//p.process();
		}
		//p.process();
		glGraphicsEndTransform(gl, nya);

		gl.glDisable(gl.GL_CULL_FACE);
		gl.glDisable(GL.GL_LIGHTING);
		renderer.endGL();

		if(cameraIn.available() == true) {
			cameraIn.read();

			hint(DISABLE_DEPTH_TEST);
			pushMatrix();
			scale(width/(float)cameraIn.width, height/(float)cameraIn.height);
			if(!(keyPressed && key == 'r'))tint(255,255,255,80);
			else noTint();
			if(!(keyPressed && key == 'v')) image(cameraIn, 0,0);
			//set(0,0, cameraIn);

			if(nya.detect(cameraIn)) {
				drawMarkerPos(nya.pos2d);
			}
			popMatrix();
			hint(ENABLE_DEPTH_TEST);
		}

	}


	void drawMarkerPos(int[][] points) {
		stroke(100,0,0);
		fill(100,0,0);
		for(int i=0;i<4;i++){
			ellipse(nya.pos2d[i][0], nya.pos2d[i][1],5,5);
		}
		/*fill(0,0,0);
		for(int i=0;i<4;i++){
			text("("+nya.pos2d[i][0]+","+nya.pos2d[i][1]+")",nya.pos2d[i][0],nya.pos2d[i][1]);
		}
		*/
	}

	private FloatBuffer makeBuffer(float[] values) {
        FloatBuffer floatBuffer = BufferUtil.newFloatBuffer(values.length);
        for (int i = 0; i < values.length; i++) {
            floatBuffer.put(values[i]);
        }
        floatBuffer.rewind();
        return floatBuffer;
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
					 {56,56,56,56,56,56},// RED CLOTH
					 {57,57,57,57,57,57},// ORANGE CLOTH
					 {58,58,58,58,58,58},// YELLOW CLOTH
					 {59,59,59,59,59,59},// LIME CLOTH
					 {60,60,60,60,60,60},// GREEN CLOTH
					 {61,61,61,61,61,61},// AQUA GREEN CLOTH
					 {62,62,62,62,62,62},// CYAN CLOTH
					 {63,63,63,63,63,63},// BLUE CLOTH
					 {64,64,64,64,64,64},// PURPLE CLOTH
					 {65,65,65,65,65,65},// INDIGO CLOTH
					 {66,66,66,66,66,66},// VIOLET CLOTH
					 {67,67,67,67,67,67},// MAGENTA CLOTH
					 {68,68,68,68,68,68},// PINK CLOTH
					 {69,69,69,69,69,69},// BLACK CLOTH
					 {70,70,70,70,70,70},// GRAY CLOTH
					 {71,71,71,71,71,71},// WHITE CLOTH
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
	    PApplet.main(new String[] {"com.newuntitledpage.minecraftAR.Test" });

	}

	public static String pad(String s, int len) {
		return String.format("%1$-" + len + "s", s);
	}

}