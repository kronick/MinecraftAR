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

public class lightTest extends PApplet {

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
	double smoothFactor = 0.5;

	int[][] blockFaceDisplayLists;
	float[][] blockColors;
	int[][] textureMap;

	Texture tex;

	public void setup() {
		// - - - - - - - - - - -
		size(800,600, GLGraphics.GLGRAPHICS);
		colorMode(HSB);
		frameRate(60);

		modelCenter = new PVector(32,46,64);

		cam = new Camera(this, 0,-100,-100, 0,-90,-90,
							radians(90), width/(float)height, .1f, 1000f);
		wheel = new ScrollWheelEvent();

		buildBlockColors();
		buildTextureMap();

		renderer = (GLGraphics)g;
		gl = renderer.beginGL();
		buildDisplayLists(gl);
		try {
        	//InputStream stream = getClass().getResourceAsStream("data/textures/terrain.png");
			FileInputStream stream = new FileInputStream("data/textures/terrain.png");
        	TextureData data = TextureIO.newTextureData(stream, false, "png");
        	tex = TextureIO.newTexture(data);
        	tex.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		}
		catch (IOException exc) {
			exc.printStackTrace();
			System.exit(1);
		}
		renderer.endGL();

	}


	public void draw() {
		println(frameRate);

		background(255);

		renderer = (GLGraphics)g;
		gl = renderer.beginGL();

		translate(width/2, height/2, 0);
		scale(50);

		float[] ambient = {.5f,.5f,.5f,1f};
		float[] diffuse = {1f,1f,1f,.5f};
		float[] specular = {1f,1f,1f,1f};
		float[] position = {-100f,1f,10f,1};

		gl.glLightfv( GL.GL_LIGHT2, GL.GL_AMBIENT, ambient,0);
		gl.glLightfv( GL.GL_LIGHT2, GL.GL_DIFFUSE, diffuse,0);
		gl.glLightfv( GL.GL_LIGHT2, GL.GL_SPECULAR, specular,0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, position,0);
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT2);


		gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
		gl.glEnable(GL.GL_COLOR_MATERIAL);

		rotateX(PI/4 * frameCount * .01f);
		rotateY(PI/4 * frameCount * .005f);

		//gl.glEnable(GL.GL_CULL_FACE);	// For some reason this is the way things are...
		//gl.glCullFace(GL.GL_FRONT);



		// 	DRAW A BOX AT THE CENTER OF THE MARKER
		pushMatrix();
			translate(0,0,0);
			gl.glColor3f(1,1,1);
			gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_FILL );

			tex.disable();
			pushMatrix();
			//translate(0,0,abs(frameCount%10-5)/5f);
				gl.glBegin(GL.GL_QUADS);
				gl.glVertex3f(-4, -4, 0);
				gl.glVertex3f(-2, -4, 0);
				gl.glVertex3f(-2,  4, 0);
				gl.glVertex3f(-4,  4, 0);

				gl.glVertex3f(4,  4, 0);
				gl.glVertex3f(2,  4, 0);
				gl.glVertex3f(2, -4, 0);
				gl.glVertex3f(4, -4, 0);

				gl.glVertex3f(-2,  2, 0);
				gl.glVertex3f( 2,  2, 0);
				gl.glVertex3f( 2,  4, 0);
				gl.glVertex3f(-2,  4, 0);

				gl.glVertex3f(-2, -4, 0);
				gl.glVertex3f( 2, -4, 0);
				gl.glVertex3f( 2, -2, 0);
				gl.glVertex3f(-2, -2, 0);

				gl.glEnd();
			popMatrix();

			tex.enable();
			tex.bind();

			translate(.5f,-.5f,1);
			gl.glBegin(GL.GL_QUADS);
			gl.glCallList(blockFaceDisplayLists[1][0]);	// FRONT
			gl.glCallList(blockFaceDisplayLists[1][1]);	// BACK
			gl.glCallList(blockFaceDisplayLists[1][2]);	// RIGHT
			gl.glCallList(blockFaceDisplayLists[1][3]);	// LEFT
			gl.glCallList(blockFaceDisplayLists[1][4]); // BOTTOM
			gl.glCallList(blockFaceDisplayLists[1][5]); // TOP
			gl.glEnd();

			translate(0,-1,-1);
			gl.glBegin(GL.GL_QUADS);
			gl.glCallList(blockFaceDisplayLists[6][0]);	// FRONT
			gl.glCallList(blockFaceDisplayLists[6][1]);	// BACK
			gl.glCallList(blockFaceDisplayLists[6][2]);	// RIGHT
			gl.glCallList(blockFaceDisplayLists[6][3]);	// LEFT
			gl.glCallList(blockFaceDisplayLists[6][4]); // BOTTOM
			gl.glCallList(blockFaceDisplayLists[6][5]); // TOP
			gl.glEnd();

			translate(-1,0,-1);
			gl.glBegin(GL.GL_QUADS);
			gl.glCallList(blockFaceDisplayLists[20][0]);	// FRONT
			gl.glCallList(blockFaceDisplayLists[20][1]);	// BACK
			gl.glCallList(blockFaceDisplayLists[20][2]);	// RIGHT
			gl.glCallList(blockFaceDisplayLists[20][3]);	// LEFT
			gl.glCallList(blockFaceDisplayLists[20][4]); // BOTTOM
			gl.glCallList(blockFaceDisplayLists[20][5]); // TOP
			gl.glEnd();
		popMatrix();

		//gl.glDisable(gl.GL_CULL_FACE);
		renderer.endGL();

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
				     {.6f, .355f, .191f},	// LOG
				     {.2f,.9f,.2f,.7f}	//{.2f,.9f,.2f,.7f}	// LEAVES
				  };
		blockColors = c;
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
	    PApplet.main(new String[] { "com.newuntitledpage.minecraftAR.lightTest" });

	}



}