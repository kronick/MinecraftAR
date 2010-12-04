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

	PFont sansFont;

	ScrollWheelEvent wheel;

	float modelScale = 100;

	Capture cameraIn;
	NyARMultiBoard nya;
	//PVector modelCenter = new PVector(11.5f,43.5f,33.5f);
	PVector modelCenter = new PVector(0,0,0);
	String[] patternFiles = {"4x4_35.patt", "4x4_14.patt", "4x4_51.patt", "4x4_61.patt", "4x4_24.patt", "4x4_89.patt",
							 "4x4_7.patt", "4x4_17.patt", "4x4_9.patt", "4x4_23.patt", "4x4_33.patt", "4x4_34.patt",
							 "4x4_45.patt", "4x4_47.patt", "4x4_73.patt", "4x4_83.patt", "4x4_91.patt", "4x4_95.patt",
							 "4x4_98.patt", "4x4_66.patt"};
	/*double[] patternWidths = {(80 * modelScale/10f), (80 * modelScale/10f), (80 * modelScale/10f),
							  (80 * modelScale/10f), (80 * modelScale/10f), (80 * modelScale/10f)};

	// Translation: Looking at origin in global coordinates, how do you translate to the marker?
	// Rotation: Looking at the marker face-on, what are the directions of the global axis, in local coordinates?
	PMatrix3D[] patternModelTransforms = {new PMatrix3D(1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1),
										  new PMatrix3D(1,0,0,0, 0,1,0,0, 0,0,1,0, -8,14,0,1),
										  new PMatrix3D(0,0,-1,0, 0,1,0,0, 1,0,0,0, -4,-8,26,1),
										  new PMatrix3D(0,0,-1,0, 0,1,0,0, 1,0,0,0, 4,0,-10,1),
										  new PMatrix3D(0,0,-1,0, 0,1,0,0, 1,0,0,0, 4,14,-10,1),
										  new PMatrix3D(1,0,0,0, 0,0,-1,0, 0,1,0,0, 30,-8,-12,1)};

	 */
	double[] patternWidths = {(2.75 * modelScale), (2.75 * modelScale), (2.75 * modelScale),
			  				  (1.75 * modelScale), (1.75 * modelScale), (1.75 * modelScale),
			  				  .5*modelScale, .5*modelScale, .5*modelScale, .5*modelScale,
			  				  .5*modelScale, .5*modelScale, .5*modelScale, .5*modelScale,
			  				  .5*modelScale, .5*modelScale, .5*modelScale, .5*modelScale,
			  				  .5*modelScale, .5*modelScale};

	// Translation: Looking at origin in global coordinates, how do you translate to the marker?
	// Rotation: Looking at the marker face-on, what are the directions of the global axis, in local coordinates?
	PMatrix3D[] patternModelTransforms = {new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 40.5f,43,31,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 20.5f,43,31,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 60.5f,43,31,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 10.5f,37,34,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 30.5f,37,34,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 50.5f,37,34,1),

										  // First floor doors
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 3,35,34,1),	// 7
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 18,35,34,1),	// 17
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 22,35,34,1),	// 9
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 38,35,34,1),	// 23
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 42,35,34,1),	// 33
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 48,35,34,1),	// 34
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 52,35,34,1),	// 45
										  //Second floor doors
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 3.5f,45,40,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 17.5f,45,40,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 22.5f,45,40,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 37.5f,45,40,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 42.5f,45,40,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 47.5f,45,40,1),
										  new PMatrix3D(-1,0,0,0, 0,1,0,0, 0,0,-1,0, 52.5f,45,40,1)};

	double[][] patternCalculatedTransforms = new double[patternFiles.length][];	// populated later

	double[] transMat;
	double rotateSmoothFactor = .3;
	double translateSmoothFactor = .3;

	float[] camPosition = {0,0,0};

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

		sansFont = createFont("Helvetica Bold", 18, true);
		textFont(sansFont);

		println(Capture.list());
		cameraIn = new Capture(this,640, 512);

		nya = new NyARMultiBoard(this, 640,512, "fisheye.dat", patternFiles, patternWidths, NyARBoard.CS_LEFT);
		nya.gsThreshold = 160;		// Binary threshold (black/white cutoff point) [Default = 110]
		nya.cfThreshold = 0.4;		// Threshold of marker.confidence for marker.detected == true (?????) [Default = 0.4];
		transMat = new double[16];
		Arrays.fill(transMat, 0);

		// Scale pattern transforms to model scale
		for(int i=0; i<patternModelTransforms.length; i++) {
			patternModelTransforms[i].m30 *= modelScale;
			patternModelTransforms[i].m31 *= modelScale;
			patternModelTransforms[i].m32 *= modelScale;

			patternModelTransforms[i].invert();
		}

		cam = new Camera(this, 0,-100,-100, 0,-90,-90,
							radians(90), width/(float)height, .1f, 1000f);
		wheel = new ScrollWheelEvent();


		Socket mcSocket = null;
		DataOutputStream out = null;
		DataInputStream  in  = null;
		System.out.println("Starting connection...");

		try {
			mcSocket = new Socket("128.54.23.252", 257); //128.54.23.252
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


	public void drawCameraImage() {
		if(cameraIn.available() == true) {
			cameraIn.read();
			if(!(keyPressed && key == 'v')) {
				/*
				loadPixels();
				cameraIn.loadPixels();
				int s, t, n;
				for(int x=0; x<width; x++) {
					for(int y=0; y<height; y++) {
						n = (int)(cameraIn.width * x/(float)width) + (int)(cameraIn.height * y/(float)height) * cameraIn.width;
						if(keyPressed && key == 'b') {
							if(brightness(cameraIn.pixels[n]) < nya.gsThreshold)
								pixels[x + y*width] = color(0,0,0);
							else pixels[x + y*width] = color(255,255,255);
						}
						else pixels[x + y*width] = cameraIn.pixels[n];
					}
				}
				updatePixels();
				*/
				if(keyPressed && key == 'b') {
					loadPixels();
					cameraIn.loadPixels();
					int s, t, n;
					for(int x=0; x<width; x++) {
						for(int y=0; y<height; y++) {
							n = (int)(cameraIn.width * x/(float)width) + (int)(cameraIn.height * y/(float)height) * cameraIn.width;
							if(brightness(cameraIn.pixels[n]) < nya.gsThreshold)
								pixels[x + y*width] = color(0,0,0);
							else pixels[x + y*width] = color(255,255,255);
						}
					}
					updatePixels();
				}
				else {
					if(!(keyPressed && key == 'r'))
						tint(255,255,255,120);
					else noTint();
					image(cameraIn, 0,0, width, height);
				}
			}

			pushMatrix();
				scale(width/(float)cameraIn.width, height/(float)cameraIn.height);
				if(nya.detect(cameraIn)) {
					for(int i=0; i<nya.markers.length; i++) {
						if(nya.markers[i].detected)
							drawMarkerPos(nya.markers[i].pos2d);
					}
				}
			popMatrix();
		}
	}

	public void draw() {
		//nya.gsThreshold = (int)(mouseY/(float)height * 256);
		//println(nya.gsThreshold);
		//println(frameRate);

		background(255,255,255);

		//drawCameraImage();

		renderer = (GLGraphics)g;
		gl = renderer.beginGL();
		if(nya.detect(cameraIn)) {
			// Go through each detected marker, multiply calculated transform by patternModelTransform
			PMatrix3D _t, _m;
			double[] weights = new double[nya.markers.length];
			for(int i=0; i<nya.markers.length; i++) {
				char k = i == 0 ? '1' : '2';
				boolean draw = false;
				if(!keyPressed) draw = true;
				else if(key == k) draw = true;

				if(nya.markers[i].detected) {
					println("Detected marker #" + i + " " + (int)(nya.markers[i].confidence * 100) + "%");
					_t = glMatrixToPMatrix(nya.markers[i].transmat);
					//println(_t.m30 + ", " + _t.m31 + ", " + _t.m32);
					//_t.apply(patternModelTransforms[i]);
					//println(_t.m30 + ", " + _t.m31 + ", " + _t.m32);
					//patternCalculatedTransforms[i] = PMatrixToglMatrix(_t);
					_m = patternModelTransforms[i].get();
					_m.apply(_t);
					//println(_t.m30 + ", " + _t.m31 + ", " + _t.m32);
					println(_m.m30 + ", " + _m.m31 + ", " + _m.m32);

					patternCalculatedTransforms[i] = PMatrixToglMatrix(_m);
					weights[i] = 1 / sqrt(sq(_t.m30) +sq(_t.m31) +sq(_t.m32)) * nya.markers[i].confidence;
				}
				else patternCalculatedTransforms[i] = null;
			}

			// Average all marker transforms that aren't null
			// Weight by a combination of distance from camera and confidence
			// TODO: Throw out the outliers!
			double[] averageTransform = new double[16];
			float sum = 0;
			for(int i=0; i<nya.markers.length; i++) {
				if(patternCalculatedTransforms[i] != null) {
					for(int n=0; n<16; n++) {
						averageTransform[n] += patternCalculatedTransforms[i][n] * weights[i];
					}
					sum += weights[i];
				}
			}
			for(int n=0; n<16; n++) {
				averageTransform[n] /= sum;
			}

			if(sum > 0) { // Make sure new data was gathered this cycle-- basically, was a marker in sight?
				for(int i=0; i<averageTransform.length; i++) {
					if(i<12)
						transMat[i] += (averageTransform[i] - transMat[i]) * rotateSmoothFactor;
					else
						transMat[i] += (averageTransform[i] - transMat[i]) * translateSmoothFactor;
				}
			}

			PMatrix3D toCameraMatrix = new PMatrix3D((float)transMat[0], (float)transMat[1], (float)transMat[2], (float)transMat[3],
												(float)transMat[4], (float)transMat[5], (float)transMat[6], (float)transMat[7],
												(float)transMat[8], (float)transMat[9], (float)transMat[10], (float)transMat[11],
												(float)transMat[12], (float)transMat[13], (float)transMat[14], (float)transMat[15]);

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
		}
		if(nya.projection != null)
			glGraphicsBeginTransform(gl, nya.projection);

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
				for(int x=0; x<80; x++) {
					for(int z=0; z<80; z++) {
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
			//System.out.println("Blocks drawn: " + n);
		}

		glGraphicsEndTransform(gl);

		gl.glDisable(GL.GL_CULL_FACE);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glFlush();
		gl.glFinish();
		renderer.endGL();

		hint(DISABLE_DEPTH_TEST);
		drawCameraImage();
		if(world.messageTimeout > 0) {
			fill(0,0,0);
			noStroke();
			for(int i=0; i<world.messages.length; i++) {
				text(world.messages[i], 10, height-20 * (i) - 20);
			}
			world.messageTimeout--;
		}
		hint(ENABLE_DEPTH_TEST);

	}


	void drawMarkerPos(int[][] points) {
		noStroke();
		fill(0,0,0,abs((frameCount%6)-3) * 20 + 50);
		beginShape();
		for(int i=0;i<4;i++){
			vertex(points[i][0], points[i][1]);
		}
		endShape();
	}

	private void glGraphicsBeginTransform(GL gl, double[] projection) {
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glLoadMatrixd(projection,0);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glLoadMatrixd(transMat,0);

		gl.glPushMatrix();
		return;
	}

	private void glGraphicsEndTransform(GL gl) {
		if(gl==null){
			die("The function beginTransform is never called.", null);
		}
		gl.glPopMatrix();
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}

	public PMatrix3D glMatrixToPMatrix(double[] mat) {
		if(mat.length == 16)
			return new PMatrix3D((float)mat[0], (float)mat[1], (float)mat[2], (float)mat[3],
								 (float)mat[4], (float)mat[5], (float)mat[6], (float)mat[7],
								 (float)mat[8], (float)mat[9], (float)mat[10], (float)mat[11],
								 (float)mat[12], (float)mat[13], (float)mat[14], (float)mat[15]);
		else return null;
	}

	public double[] PMatrixToglMatrix(PMatrix3D mat) {
		if(mat != null) {
			double[] out = {mat.m00, mat.m01, mat.m02, mat.m03,
						    mat.m10, mat.m11, mat.m12, mat.m13,
						    mat.m20, mat.m21, mat.m22, mat.m23,
						    mat.m30, mat.m31, mat.m32, mat.m33};
			return out;
		}
		else return null;
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
		if(key == CODED && keyCode == UP)
			nya.gsThreshold += 5;
		if(key == CODED && keyCode == DOWN)
			nya.gsThreshold -= 5;
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