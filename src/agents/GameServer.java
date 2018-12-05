package agents;

import jade.lang.acl.ACLMessage;
import maps.Map;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GameServer extends Agent {
	
	private static GameServer instance = null;
	public Grid<Object> grid;
	protected Map map;
	
	private ArrayList<String> aliveAgents;
	
	private int MIN_DMG = 20, MAX_DMG = 33, CRIT_DMG = 80, CRIT_CHANCE = 25;
	
	private GameServer() {
		this.aliveAgents = new ArrayList<String>();
		
		for (int i = 1; i <= 5; i++) {
			this.aliveAgents.add("T" + i); this.aliveAgents.add("CT" + i);			
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
	
	public static GameServer getInstance() {
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
			if(agent.contains("T"))
				existsT = true;
			else if( agent.contains("CT"))
				existsCT = true;
		}
		
		
		if(!existsT)
			return 1;
		else if(!existsCT)
			return 2;
		else
			return 0;
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
				
				if (info[0].equals("DEAD")) {
					aliveAgents.remove(msg.getSender().getLocalName());
					broadcastMessage(String.format("DEAD %s", msg.getSender().getName()));
					
					// TODO check if bomb is planted in case all Ts are dead but that doesnt make CTs win cuz it can explode
					int teamDead = checkAllDead();
					switch (teamDead) {
						case 1:
							//Ts are dead
							broadcastMessage("WINNER CT OPPOSITETEAMDEAD");
							break;
						case 2:
							//CTs are dead
							broadcastMessage("WINNER T OPPOSITETEAMDEAD");
							break;
							
						case -1:
							// All dead ???
							break;
						default:
							//Both teams with alive agents
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
