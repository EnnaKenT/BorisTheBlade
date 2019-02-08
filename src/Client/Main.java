package Client;

import java.util.ArrayList;

public class Main implements BotInterface {

    public static void main(String[] args) {
        String port = "15000";
        if (args.length == 2) {
            port = args[1];
        }
        Client client = new Client(new Main(), port);
        while (client.shouldStop == false) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

    public void turn(Client client, int turnNumber) {
        newTurn(client);
    }

    public void newTurn(Client client) {
        if (client.myPlanets.size() > 0 && client.notMyPlanets.size() > 0) {
            int[] allPlanets = new int[client.allPlanets.size()];
            ArrayList<MapObject> notMyPlanets = client.notMyPlanets;
            for (MapObject myPlanet : client.myPlanets) {
                notMyPlanets.sort((t0, t1) -> {
                    if (client.turnsFromTo(myPlanet, t0) >= client.turnsFromTo(myPlanet, t1)) {
                        return 1;
                    } else {
                        return -1;
                    }
                });
            }

            for (MapObject notMyPlanet : notMyPlanets) {
                for (MapObject myPlanet : client.myPlanets) {
                    int sentShips = allPlanets[notMyPlanet.id];
                    if (sentShips > notMyPlanet.value) {
                        break;
                    }
                    int shipIncrementIndex = 10;
                    if (myPlanet.value > shipIncrementIndex + 1) {
                        int notMyPlanetIncrementType = 0;
                        if (notMyPlanet.own == 2) {
                            notMyPlanetIncrementType = notMyPlanet.size;
                        }

                        allPlanets[notMyPlanet.id] = myPlanet.value - 3 - client.turnsFromTo(myPlanet, notMyPlanet) * notMyPlanetIncrementType;

                        client.send(myPlanet, notMyPlanet, myPlanet.value - 3);
                    }
                }
            }
        }
        client.endTurn();
    }
}