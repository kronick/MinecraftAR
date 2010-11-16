package com.newuntitledpage.minecraftAR;

import java.util.Arrays;
import java.util.zip.*;
import java.io.*;
import com.newuntitledpage.minecraftAR.model.*;

public class Parser {
	public int state;
	static final int WAITING_FOR_PACKET = 1;
	static final int RECEIVING_LEVEL = 2;
	static final int PARSING_PACKET = 3;

	static final byte SERVER_ID  			= 0x00;
	static final byte PING       			= 0x01;
	static final byte LEVEL_INIT  			= 0x02;
	static final byte LEVEL_DATA  			= 0x03;
	static final byte LEVEL_FINAL  			= 0x04;
	static final byte SET_BLOCK  			= 0x06;
	static final byte PLAYER_SPAWN 		 	= 0x07;
	static final byte PLAYER_TP  			= 0x08;
	static final byte POSITION_ORIENT_UP  	= 0x09;
	static final byte POSITION_UP  			= 0x0a;
	static final byte ORIENT_UP 			= 0x0b;
	static final byte PLAYER_DESPAWN		= 0x0c;
	static final byte MESSAGE	  			= 0x0d;
	static final byte DISCONNECT  			= 0x0e;
	static final byte PLAYER_OP_CHANGE		= 0x0f;

	static final int EXPECTED_LEVEL_SIZE = 200000;

	static final int[] packetLengths = {130,
										 0,
										 0,
										 1027,
										 6,
										 -1,
										 6,
										 71,
										 9,
										 6,
										 4,
										 4,
										 1,
										 65,
										 64,
										 1};

	DataInputStream stream;
	World world;

	byte[] levelData;
	int receivedDataCounter = 0;


	public Parser(DataInputStream stream, World world) {
		this.stream = stream;
		this.world = world;
		this.state = WAITING_FOR_PACKET;
		this.levelData = new byte[EXPECTED_LEVEL_SIZE];

		Arrays.fill(levelData, (byte)0);
	}

	public void process() {
		try {
			if(stream.available() > 0) {
				byte packetID = stream.readByte();
				byte[] inBytes = new byte[packetLengths[packetID]];
				stream.readFully(inBytes);
				switch(packetID) {
					case SERVER_ID:
						parseServerID(inBytes);
						break;
					case PING:
						break;
					case LEVEL_INIT:
						handleLevelInit(); break;
					case LEVEL_DATA:
						parseLevelData(inBytes); break;
					case LEVEL_FINAL:
						parseLevelFinal(inBytes); break;
					default:
						//System.out.println(new Character((char) stream.readByte()).toString());
				}

			}
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void handleLevelInit() {

	}

	private void parseLevelData(byte[] in) {
		short length = byteToShort(in[0], in[1]);
		byte[] data = subByte(in, 2, 1024);
		byte percent = in[1026];
		handleChunk(length, data, percent);
	}
	private void handleChunk(short length, byte[] data, byte percent) {
		if(receivedDataCounter + data.length > levelData.length) {
			System.out.println("Expanding array. New size: " + (receivedDataCounter + data.length)  + " bytes" );
			byte[] newLevelData = new byte[receivedDataCounter + data.length];
			System.arraycopy(levelData, 0, newLevelData, 0, levelData.length);
			levelData = newLevelData;
		}
		System.arraycopy(data, 0, levelData, receivedDataCounter, data.length);
		receivedDataCounter += data.length;

		System.out.println("Chunk of length: " + length + " received. (" + percent + "%)");
	}
	private void parseLevelFinal(byte[] in) throws IOException {
		byte[] unzippedLevel = unzip(levelData);
		int numberOfBlocks = byteToInt(unzippedLevel[0], unzippedLevel[1], unzippedLevel[2], unzippedLevel[3]);

		System.out.println(numberOfBlocks);
		levelData = subByte(unzippedLevel, 4, 256*256*64);

		handleLevelFinal(byteToShort(in[0], in[1]), byteToShort(in[2], in[3]), byteToShort(in[4], in[5]));
	}
	private void handleLevelFinal(short x, short y, short z) {
		Level l = new Level(x, y, z);
		Block _b;
		for(int i=0; i<levelData.length; i++) {
			// Create a new block for each byte
			short b_x = (short)(i%256);
			short b_y = (short)(Math.floor(i/(256*256)));
			short b_z = (short)(Math.floor(i/256)%256);
			l.blocks[b_y][b_x][b_z] = levelData[i];
		}

		world.level = l;
		l.recalcNeighborOcclusions();

		System.out.println("Level loaded. Map size:");
		System.out.println("X: " + x + " Y: " + y + " Z: " + z);
	}

	private void parseServerID(byte[] in) {
		byte version = in[0];
		String name = new String(subByte(in, 1, 64));
		String MOTD = new String(subByte(in, 65,64));
		byte userType = in[129];

		handleServerID(version, name, MOTD, userType);

	}
	private void handleServerID(byte version, String name, String MOTD, byte userType) {
		System.out.println("Server name: " + name);
		System.out.println("MOTD: " + MOTD);
	}

	private void waitForBytes(int n) throws IOException {
		while(stream.available() < n) {
			// Do nothing
		}
	}

	private byte[] subByte(byte[] in, int start, int length) {
		if(in.length >= start+length) {
			byte[] out = new byte[length];
			for(int i=0; i<length; i++) {
				out[i] = in[start+i];
			}
			return out;
		}
		else {
			return null;
		}
	}

	private short byteToShort(byte one, byte two) {
		return (short)(two + ((short)one << 8));
	}

	private int byteToInt(byte one, byte two, byte three, byte four) {
		return (int)(four + ((int)three << 8) + ((int)two << 16) + ((int)one << 32));
	}

	public static final byte[] unzip(byte[] in) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(10 * in.length);

		GZIPInputStream inStream = new GZIPInputStream ( new ByteArrayInputStream(in) );

		byte[] buf = new byte[4096];
		while (true) {
			int size = inStream.read(buf);
			if (size <= 0)
				break;
			outStream.write(buf, 0, size);
		}
		outStream.close();

		return outStream.toByteArray();
	}
}
