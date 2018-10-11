import jade.core.Agent;
import jade.core.AID;

public class CTAgent extends Agent {
    protected void setup() {
        System.out.println(getAID().getName() + ": So that we may be free!");
    }
}