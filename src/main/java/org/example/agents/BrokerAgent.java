package org.example.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;
import org.example.MainUI.UILogger;

public class BrokerAgent extends Agent {
    private UILogger logger;
    private List<CarListing> inventory = new ArrayList<>();
    private double totalRevenue = 0;

    public static class CarListing {
        public String dealer, model; public int price;
        public CarListing(String d, String m, int p) { this.dealer = d; this.model = m; this.price = p; }
    }

    protected void setup() {
        if (getArguments().length > 0) logger = (UILogger) getArguments()[0];
        log("KA: Platform Online. Fees: RM50/Neg, 5%/Sale.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        // Dealer Registration
                        String[] data = msg.getContent().split(";");
                        inventory.add(new CarListing(msg.getSender().getLocalName(), data[0], Integer.parseInt(data[1])));
                        log("KA: Listing Registered -> " + data[0] + " from " + msg.getSender().getLocalName());
                    } else if (msg.getPerformative() == ACLMessage.REQUEST) {
                        // Buyer Search
                        handleSearch(msg);
                    } else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                        // Financial Tracking
                        double commission = Double.parseDouble(msg.getContent()) * 0.05;
                        totalRevenue += (commission + 50);
                        log("KA: DEAL CLOSED! Revenue Earned: RM" + (commission + 50) + " | Total: RM" + totalRevenue);
                    }
                } else block();
            }
        });
    }

    private void handleSearch(ACLMessage msg) {
        String target = msg.getContent();
        StringBuilder results = new StringBuilder();
        for (CarListing cl : inventory) {
            if (cl.model.equalsIgnoreCase(target))
                results.append(cl.dealer).append(":").append(cl.price).append(",");
        }
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        reply.setContent(results.length() > 0 ? results.toString() : "NONE");
        send(reply);
    }

    private void log(String m) { if (logger != null) logger.log(m); }
}