package org.example.agents;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import org.example.MainUI.UILogger;

public class DealerAgent extends Agent {
    private String car;
    private int minPrice; // Reserve Price
    private int retailPrice;
    private UILogger logger;
    private int negotiationCount = 0;

    protected void setup() {
        Object[] args = getArguments();
        car = (String) args[0];
        retailPrice = Integer.parseInt((String) args[1]);
        minPrice = (int)(retailPrice * 0.85); // Won't go below 85%
        logger = (UILogger) args[2];

        log("STATUS: Registered with retail price RM" + retailPrice + ", reserve: RM" + minPrice);

        // Register with Broker
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.addReceiver(new AID("broker", AID.ISLOCALNAME));
                inform.setContent(car + ";" + retailPrice);
                send(inform);
                log("STATUS: Listed " + car + " on marketplace");
            }
        });

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.PROPOSE) {
                    negotiationCount++;
                    int buyerOffer = Integer.parseInt(msg.getContent());
                    log("OFFER " + negotiationCount + ": Buyer offered RM" + buyerOffer);

                    if (buyerOffer >= minPrice) {
                        ACLMessage accept = msg.createReply();
                        accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        accept.setContent(String.valueOf(buyerOffer));
                        send(accept);
                        log("DEAL CLOSED: Accepted offer of RM" + buyerOffer);
                    } else {
                        int counter = (retailPrice + buyerOffer) / 2;
                        ACLMessage reject = msg.createReply();
                        reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reject.setContent(String.valueOf(counter));
                        send(reject);
                        log("COUNTER: Offered RM" + counter);
                    }
                } else block();
            }
        });
    }

    private void log(String m) {
        if (logger != null) logger.log(getLocalName() + ": " + m);
    }
}