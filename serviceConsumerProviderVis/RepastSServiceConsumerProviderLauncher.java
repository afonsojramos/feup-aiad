package serviceConsumerProviderVis;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import sajas.core.Agent;
import sajas.core.Runtime;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class RepastSServiceConsumerProviderLauncher extends RepastSLauncher {

	private static int N = 10;
	private static int N_CONSUMERS = N;
	private static int N_CONSUMERS_FILTERING_PROVIDERS = N;
	protected static int N_PROVIDERS = 2*N;
	
	protected static int FILTER_SIZE = 5;
	
	protected static double FAILURE_PROBABILITY_GOOD_PROVIDER = 0.2;
	protected static double FAILURE_PROBABILITY_BAD_PROVIDER = 0.8;
	
	private int N_CONTRACTS = 100;
	
	public static final boolean USE_RESULTS_COLLECTOR = true;
	
	public static final boolean SEPARATE_CONTAINERS = false;
	private ContainerController mainContainer;
	private ContainerController agentContainer;

	public static Agent getAgent(Context<?> context, AID aid) {
		for(Object obj : context.getObjects(Agent.class)) {
			if(((Agent) obj).getAID().equals(aid)) {
				return (Agent) obj;
			}
		}
		return null;
	}

	public int getN() {
		return N;
	}

	public void setN(int N) {
		this.N = N;
	}

	public int getFILTER_SIZE() {
		return FILTER_SIZE;
	}

	public void setFILTER_SIZE(int FILTER_SIZE) {
		this.FILTER_SIZE = FILTER_SIZE;
	}

	public double getFAILURE_PROBABILITY_GOOD_PROVIDER() {
		return FAILURE_PROBABILITY_GOOD_PROVIDER;
	}

	public void setFAILURE_PROBABILITY_GOOD_PROVIDER(double FAILURE_PROBABILITY_GOOD_PROVIDER) {
		this.FAILURE_PROBABILITY_GOOD_PROVIDER = FAILURE_PROBABILITY_GOOD_PROVIDER;
	}

	public double getFAILURE_PROBABILITY_BAD_PROVIDER() {
		return FAILURE_PROBABILITY_BAD_PROVIDER;
	}

	public void setFAILURE_PROBABILITY_BAD_PROVIDER(double FAILURE_PROBABILITY_BAD_PROVIDER) {
		this.FAILURE_PROBABILITY_BAD_PROVIDER = FAILURE_PROBABILITY_BAD_PROVIDER;
	}

	public int getN_CONTRACTS() {
		return N_CONTRACTS;
	}

	public void setN_CONTRACTS(int N_CONTRACTS) {
		this.N_CONTRACTS = N_CONTRACTS;
	}

	@Override
	public String getName() {
		return "Service Consumer/Provider -- SAJaS RepastS Test";
	}

	@Override
	protected void launchJADE() {
		
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);
		
		if(SEPARATE_CONTAINERS) {
			Profile p2 = new ProfileImpl();
			agentContainer = rt.createAgentContainer(p2);
		} else {
			agentContainer = mainContainer;
		}
		
		launchAgents();
	}
	
	private void launchAgents() {
		
		try {
			
			AID resultsCollectorAID = null;
			if(USE_RESULTS_COLLECTOR) {
				// create results collector
				ResultsCollector resultsCollector = new ResultsCollector(N_CONSUMERS + N_CONSUMERS_FILTERING_PROVIDERS);
				mainContainer.acceptNewAgent("ResultsCollector", resultsCollector).start();
				resultsCollectorAID = resultsCollector.getAID();
			}
			
			// create providers
			// good providers
			for (int i = 0; i < N_PROVIDERS/2; i++) {
				ProviderAgent pa = new ProviderGoodAgent();
				agentContainer.acceptNewAgent("GoodProvider" + i, pa).start();
			}
			// bad providers
			for (int i = 0; i < N_PROVIDERS/2; i++) {
				ProviderAgent pa = new ProviderBadAgent();
				agentContainer.acceptNewAgent("BadProvider" + i, pa).start();
			}

			// create consumers
			// consumers that use all providers
			for (int i = 0; i < N_CONSUMERS; i++) {
				ConsumerAgent ca = new ConsumerAllAgent(N_CONTRACTS, resultsCollectorAID);
				mainContainer.acceptNewAgent("Consumer" + i, ca).start();
			}
			// consumers that filter providers
			for (int i = 0; i < N_CONSUMERS_FILTERING_PROVIDERS; i++) {
				ConsumerAgent ca = new ConsumerFilterAgent(N_CONTRACTS, resultsCollectorAID);
				mainContainer.acceptNewAgent("ConsumerF" + i, ca).start();
			}

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public Context build(Context<Object> context) {
		// http://repast.sourceforge.net/docs/RepastJavaGettingStarted.pdf
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("Service Consumer/Provider network", context, true);
		netBuilder.buildNetwork();
		
		return super.build(context);
	}

}
