package agents;

import jade.lang.acl.ACLMessage;
import maps.Map;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GameServer extends Agent {
	
	private static GameServer instance = null;
	public Grid<Object> grid;
	protected Map map;
	public boolean isPlanted;
	
	private ArrayList<String> aliveAgents;
	
	public int T_HEALTH = 100, CT_HEALTH = 200;
	private int MIN_DMG = 20, MAX_DMG = 33, CRIT_DMG = 80, CRIT_CHANCE = 25;
	
	private GameServer() {
		this.isPlanted = false;
		this.aliveAgents = new ArrayList<String>();
		
		for (int i = 1; i <= 5; i++) {
			this.aliveAgents.add("T" + i); this.aliveAgents.add("CT" + i);			
		}
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("src/data/data.txt", true)))) {
		    out.print(T_HEALTH + "," + CT_HEALTH + "," + MIN_DMG + "," + MAX_DMG + "," + CRIT_DMG + "," + CRIT_CHANCE);
		}catch (IOException e) {
		    System.err.println(e);
		}
	}
	
	@Override
	public void setup() {
		this.map = new Map();
		System.out.println("Generated graph!");
		
		addBehaviour(new ListeningBehaviour());
		this.broadcastMessage("SERVER_OPERATIONAL");
	}
	
	@Override
	public void takeDown() {
		System.out.println("Server shutdown.");
	}
	
	public static GameServer getInstance() throws FileNotFoundException {
		if (instance == null) instance = new GameServer();
		return instance;
	}

	public int rollDamageOutput() {
		if (ThreadLocalRandom.current().nextInt(100) < CRIT_CHANCE)
			return CRIT_DMG;
		
		return ThreadLocalRandom.current().nextInt(MIN_DMG, MAX_DMG + 1);
	}
	
	public GridPoint generateSpawnPoint(boolean isCTSide) {
		if (isCTSide)
			return new GridPoint(ThreadLocalRandom.current().nextInt(25, 31), 38);
		
		return new GridPoint(ThreadLocalRandom.current().nextInt(14, 21), 5);
	}
	
	private void sendMessage(String content, AID agent) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(content); 
		msg.addReceiver(agent);
		send(msg);
	}
	
	private void broadcastMessage(String content) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(content);
		
		for (int i = 1; i <= 5; i++) {
			msg.addReceiver(new AID(String.format("CT%d@aiadsource", i), true));
			msg.addReceiver(new AID(String.format("T%d@aiadsource", i), true));
		}
		
		send(msg);
	}
	
	private int checkAllDead() {
		boolean existsCT = false, existsT = false;
		if(this.aliveAgents.size() == 0)
			return -1;
		
		for (String agent : this.aliveAgents) {
			if(agent.contains("CT"))
				existsCT = true;
			else if( agent.contains("T"))
				existsT = true;
		}
		
		
		if(!existsCT)
			return 1;
		else if(!existsT) {
			if (isPlanted)
				return 2;
			else
				return 3;
		} else
			return 0;
	}
	
	private void finish(String winner) {
		
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("src/data/data.txt", true)))) {
		    out.println("," + winner);
		}catch (IOException e) {
		    System.err.println(e);
		}
	}
	
	private class ListeningBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		
		@Override
		public void action() {
			ACLMessage msg = receive();
			
			if (msg != null) {
				String[] info = msg.getContent().split(" ");
				//System.out.println(msg.getContent());
				
				// TODO: Probably convert these to enum.
				if (info[0].equals("SHOT"))
					sendMessage(String.format("SHOT %s", info[2]), new AID(info[1], true));
				
				if (info[0].equals("PLANTED"))
					isPlanted = true;
				
				if (info[0].equals("DEAD")) {
					aliveAgents.remove(msg.getSender().getLocalName());
					broadcastMessage(String.format("DEAD %s", msg.getSender().getName()));
					
					int teamDead = checkAllDead();
					switch (teamDead) {
						case 1:
							//CTs are dead
							broadcastMessage("WINNER T");
							finish("T");
							System.out.println("CTs dead");
							break;
						case 3:
							// Ts dead and bomb not planted
							broadcastMessage("WINNER CT");
							finish("CT");
							System.out.println("Ts dead");
							break;
						default:
							//Both teams with alive agents or Ts dead with bomb planted
							break;
					}
					//TODO Send message of winner in case of all team dead
				}
			}
		}
	}
	
	public ArrayList<String> getAliveAgents() {
		return this.aliveAgents;
	}
	
}
