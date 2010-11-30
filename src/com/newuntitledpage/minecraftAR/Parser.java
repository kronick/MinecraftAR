package com.newuntitledpage.minecraftAR;

import java.util.Arrays;
import java.util.zip.*;
import java.io.*;
import com.newuntitledpage.minecraftAR.model.*;

public class Parser extends Thread {

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
										 7,
										 73,
										 9,
										 6,
										 4,
										 3,
										 1,
										 65,
										 64,
										 1};

	DataInputStream stream;
	public DataOutputStream out;
	World world;

	byte[] levelData;
	int receivedDataCounter = 0;


	public Parser(DataInputStream stream, DataOutputStream out, World world) {
		this.stream = stream;
		this.out = out;
		this.world = world;
		this.levelData = new byte[EXPECTED_LEVEL_SIZE];
		Arrays.fill(levelData, (byte)0);
	}

	public void run() {
		while(true) {
			this.process();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void process() {
		try {
			if(stream.available() > 0) {
				byte packetID = stream.readByte();
				if(packetID != 1) System.out.println("Packed ID: " + packetID);
				if(packetID >= 0 && packetID <= 0x0f) {
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
						case SET_BLOCK:
							parseBlockChange(inBytes); break;
						case PLAYER_SPAWN:
							parsePlayerSpawn(inBytes); break;
						case PLAYER_TP:
							parsePlayerTP(inBytes); break;
						case POSITION_UP:
							parsePlayerMove(inBytes); break;
						case ORIENT_UP:
							parsePlayerRotate(inBytes); break;
						case PLAYER_DESPAWN:
							parsePlayerDespawn(inBytes); break;
						case MESSAGE:
							parseMessage(inBytes); break;
						default:
							//System.out.println(new Character((char) stream.readByte()).toString());
					}
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
		levelData = subByte(unzippedLevel, 4, numberOfBlocks);

		handleLevelFinal(byteToShort(in[0], in[1]), byteToShort(in[2], in[3]), byteToShort(in[4], in[5]));
	}
	private void handleLevelFinal(short x, short y, short z) {
		Level l = new Level(x, y, z);
		Block _b;
		for(int i=0; i<levelData.length; i++) {
			// Create a new block for each byte
			short b_x = (short)(i%z);
			short b_y = (short)(Math.floor(i/(x*z)));
			short b_z = (short)(Math.floor(i/x)%z);
			//System.out.println(b_x + ", " + b_y + ", " + b_z);
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

	private void parseBlockChange(byte[] in) {
		short x = byteToShort(in[0], in[1]);
		short y = byteToShort(in[2], in[3]);
		short z = byteToShort(in[4], in[5]);
		byte type = in[6];
		handleBlockChange(x,y,z, type);
	}

	private void handleBlockChange(short x, short y, short z, byte type) {
		world.level.setBlock(x,y,z, type);
	}

	private void parsePlayerSpawn(byte[] in) {
		byte ID = in[0];
		String name = new String(subByte(in, 1,64));
		short x = byteToShort(in[65],in[66]);
		short y = byteToShort(in[67],in[68]);
		short z = byteToShort(in[69],in[70]);
		byte heading = in[71];
		byte pitch = in[72];
		handlePlayerSpawn(ID, name, x, y, z, heading, pitch);
	}

	private void handlePlayerSpawn(byte ID, String name, short x, short y, short z, byte heading, byte pitch) {
		System.out.println("ID: " + ID);
		System.out.println("name: " + name);
		System.out.println("position: " + x + ", " + y + ", " + z);
		System.out.println("orientation: " + heading + ", " + pitch);
		world.addPlayer(ID, name, x, y, z, heading, pitch);
	}

	private void parsePlayerTP(byte[] in) {
		byte ID = in[0];
		short x = byteToShort(in[1],in[2]);
		short y = byteToShort(in[3],in[4]);
		short z = byteToShort(in[5],in[6]);
		byte heading = in[7];
		byte pitch = in[8];
		handlePlayerTP(ID, x, y, z, heading, pitch);
	}

	private void handlePlayerTP(byte ID, short x, short y, short z, byte heading, byte pitch) {
		world.updatePlayer(ID, x, y, z, heading, pitch);
	}

	private void parsePlayerMove(byte[] in) {
		byte ID = in[0];
		byte dx = in[1];
		byte dy = in[2];
		byte dz = in[3];
		handlePlayerMove(ID, dx, dy, dz);
	}
	private void handlePlayerMove(byte ID, byte dx, byte dy, byte dz) {
		world.movePlayer(ID, dx, dy, dz);
	}

	private void parsePlayerRotate(byte[] in) {
		byte ID = in[0];
		byte heading = in[1];
		byte pitch = in[2];

		handlePlayerRotate(ID, heading, pitch);
	}
	private void handlePlayerRotate(byte ID, byte heading, byte pitch) {
		world.rotatePlayer(ID, heading, pitch);
	}

	private void parsePlayerDespawn(byte[] in) {
		handlePlayerDespawn(in[0]);
	}

	private void handlePlayerDespawn(byte ID) {
		world.removePlayer(ID);
	}

	private void parseMessage(byte[] in) {
		byte ID = in[0];
		String text = new String(subByte(in, 1,64));
		handleMessage(ID, text);
	}

	private void handleMessage(byte ID, String text) {
		Player p = world.getPlayer(ID);
		if(p != null)
			System.out.println(">" + p.name + ": " + text);
		else
			System.out.println(">>>" + text);
		// TODO: Log the message somewhere, too
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
		short twoShort = (short)((two > 0) ? two : (256 + two));
		return (short)(twoShort + ((short)one << 8));
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
