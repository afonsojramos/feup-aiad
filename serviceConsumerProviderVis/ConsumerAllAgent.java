package serviceConsumerProviderVis;

import jade.core.AID;

public class ConsumerAllAgent extends ConsumerAgent {

	public ConsumerAllAgent(int nContracts, AID resultsCollector) {
		super(RepastSServiceConsumerProviderLauncher.N_PROVIDERS, nContracts, resultsCollector);
	}

}
