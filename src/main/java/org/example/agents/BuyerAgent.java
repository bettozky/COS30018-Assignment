package org.example.agents;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import org.example.MainUI.UILogger;

public class BuyerAgent extends Agent {
    private String desiredCar;
    private int maxBudget;
    private UILogger logger;

    protected void setup() {
        Object[] args = getArguments();
        desiredCar = (String) args[0];
        maxBudget = Integer.parseInt((String) args[1]);
        logger = (UILogger) args[2];

        // 1. Search
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver(new AID("broker", AID.ISLOCALNAME));
                req.setContent(desiredCar);
                send(req);
            }
        });

        // 2. Negotiate
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        // Pick first dealer from shortlist
                        String firstDealer = msg.getContent().split(":")[0];
                        startNegotiation(firstDealer);
                    } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                        int counter = Integer.parseInt(msg.getContent());
                        if (counter <= maxBudget) {
                            ACLMessage propose = msg.createReply();
                            propose.setPerformative(ACLMessage.PROPOSE);
                            propose.setContent(String.valueOf(counter));
                            send(propose);
                        } else {
                            log("Walked away. Counter RM" + counter + " exceeds budget.");
                            doDelete();
                        }
                    } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        log("SUCCESS! Bought " + desiredCar + " for RM" + msg.getContent());
                        notifyBroker(msg.getContent());
                        doDelete();
                    }
                } else block();
            }
        });
    }

    private void startNegotiation(String dealer) {
        ACLMessage start = new ACLMessage(ACLMessage.PROPOSE);
        start.addReceiver(new AID(dealer, AID.ISLOCALNAME));
        start.setContent(String.valueOf((int)(maxBudget * 0.7))); // Start at 70% of budget
        send(start);
        log("Starting negotiation with " + dealer);
    }

    private void notifyBroker(String finalPrice) {
        ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);
        confirm.addReceiver(new AID("broker", AID.ISLOCALNAME));
        confirm.setContent(finalPrice);
        send(confirm);
    }

    private void log(String m) { if (logger != null) logger.log(getLocalName() + ": " + m); }
}