package com.newuntitledpage.minecraftAR.model;

import java.util.ArrayList;

public class World {
	public Level level;

	public ArrayList<Player> players;

	public World() {
		players = new ArrayList<Player>();
	}

	public void addPlayer(byte ID, String name, short x, short y, short z, byte heading, byte pitch) {
		players.add(new Player(ID, name, x, y, z, heading, pitch));
	}

	public boolean updatePlayer(byte ID, short x, short y, short z, byte heading, byte pitch) {
		Player p = null;
		for(int i=0; i<players.size(); i++) {
			if(players.get(i).ID == ID) {
				p = players.get(i);
				break;
			}
		}
		if(p != null) {
			p.position[0] = x;
			p.position[1] = y;
			p.position[2] = z;

			p.orientation[0] = heading;
			p.orientation[1] = pitch;

			return true;
		}
		else return false;	// Player does not exist!
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
