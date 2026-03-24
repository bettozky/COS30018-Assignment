package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainUI extends Application {
    private TextArea logArea = new TextArea();
    private ContainerController cc;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) throws Exception {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        cc = rt.createMainContainer(p);

        UILogger logger = msg -> Platform.runLater(() ->
                logArea.appendText("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + msg + "\n"));

        // Initialize Broker (KA)
        cc.createNewAgent("broker", "org.example.agents.BrokerAgent", new Object[]{logger}).start();

        TabPane tp = new TabPane();
        tp.getTabs().addAll(
                new Tab("Broker Dashboard", createBrokerView()),
                new Tab("Dealer Portal", createDealerView(logger)),
                new Tab("Buyer Portal", createBuyerView(logger))
        );
        tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        stage.setScene(new Scene(tp, 900, 550));
        stage.setTitle("Automated Car Negotiation System - Group Assignment");
        stage.show();
    }

    private VBox createBrokerView() {
        logArea.setEditable(false);
        VBox box = new VBox(10, new Label("Marketplace Activity & Financial Log"), logArea);
        box.setPadding(new Insets(15));
        VBox.setVgrow(logArea, Priority.ALWAYS);
        return box;
    }

    private VBox createDealerView(UILogger logger) {
        TextField name = new TextField(); name.setPromptText("Dealer Name");
        TextField car = new TextField(); car.setPromptText("Car Model");
        TextField price = new TextField(); price.setPromptText("Retail Price (RM)");
        Button addBtn = new Button("Register Agent & List Car");
        addBtn.setMaxWidth(Double.MAX_VALUE);

        addBtn.setOnAction(e -> {
            try {
                cc.createNewAgent(name.getText(), "org.example.agents.DealerAgent",
                        new Object[]{car.getText(), price.getText(), logger}).start();
            } catch (Exception ex) { logger.log("Error: " + ex.getMessage()); }
        });

        return new VBox(15, new Label("Add New Dealer"), name, car, price, addBtn);
    }

    private VBox createBuyerView(UILogger logger) {
        TextField name = new TextField(); name.setPromptText("Buyer Name");
        TextField car = new TextField(); car.setPromptText("Desired Car");
        TextField budget = new TextField(); budget.setPromptText("Max Budget (RM)");
        Button reqBtn = new Button("Start Automated Search & Negotiation");
        reqBtn.setMaxWidth(Double.MAX_VALUE);

        reqBtn.setOnAction(e -> {
            try {
                cc.createNewAgent(name.getText(), "org.example.agents.BuyerAgent",
                        new Object[]{car.getText(), budget.getText(), logger}).start();
            } catch (Exception ex) { logger.log("Error: " + ex.getMessage()); }
        });

        return new VBox(15, new Label("Add New Buyer"), name, car, budget, reqBtn);
    }

    public interface UILogger { void log(String message); }
}