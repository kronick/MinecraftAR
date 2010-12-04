package com.newuntitledpage.minecraftAR.model;

import java.util.ArrayList;

public class World {
	public Level level;
	public String[] messages = {"", "", "", "", "", "", "", "", ""};
	public int messageTimeout = 100;

	public ArrayList<Player> players;

	public World() {
		players = new ArrayList<Player>();
	}

	public void addPlayer(byte ID, String name, short x, short y, short z, byte heading, byte pitch) {
		players.add(new Player(ID, name, x, y, z, heading, pitch));
	}

	public void updatePlayer(byte ID, short x, short y, short z, byte heading, byte pitch) {
		Player p = null;
		for(int i=0; i<players.size(); i++) {
			if(players.get(i).ID == ID) {
				p = players.get(i);
				break;
			}
		}
		if(p == null) {	// Create player if it doesn't exist
			players.add(new Player(ID, "unknown_" + ID, x, y, z, heading, pitch));
		}
		else {
			p.position[0] = x;
			p.position[1] = y;
			p.position[2] = z;

			p.orientation[0] = heading;
			p.orientation[1] = pitch;
		}
	}

	public void movePlayer(byte ID, byte dx, byte dy, byte dz) {
		Player p = null;
		for(int i=0; i<players.size(); i++) {
			if(players.get(i).ID == ID) {
				p = players.get(i);
				break;
			}
		}
		if(p != null) {	// Create player if it doesn't exist
			p.position[0] += dx;
			p.position[1] += dy;
			p.position[2] += dz;
		}
	}

	public void rotatePlayer(byte ID, byte heading, byte pitch) {
		Player p = null;
		for(int i=0; i<players.size(); i++) {
			if(players.get(i).ID == ID) {
				p = players.get(i);
				break;
			}
		}
		if(p != null) {	// Create player if it doesn't exist
			p.orientation[0] = heading;
			p.orientation[1] = pitch;
		}
	}

	public boolean removePlayer(byte ID) {
		Player p = null;
		for(int i=0; i<players.size(); i++) {
			if(players.get(i).ID == ID) {
				p = players.get(i);
				break;
			}
		}

		if(p != null) { players.remove(p); return true; }
		else return false;
	}

	public Player getPlayer(byte ID) {
		Player p = null;
		for(int i=0; i<players.size(); i++) {
			if(players.get(i).ID == ID) {
				p = players.get(i);
				break;
			}
		}

		if(p != null) { return p; }
		else return null;
	}
}
