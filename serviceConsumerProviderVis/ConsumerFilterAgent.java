package serviceConsumerProviderVis;

import jade.core.AID;

public class ConsumerFilterAgent extends ConsumerAgent {

	public ConsumerFilterAgent(int nContracts, AID resultsCollector) {
		super(RepastSServiceConsumerProviderLauncher.FILTER_SIZE, nContracts, resultsCollector);
	}

}
