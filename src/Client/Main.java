package Client;

import java.util.ArrayList;
import java.util.HashMap;

public class Main implements BotInterface {
    private HashMap<Integer, Integer> mAllPlanets = null;

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
        if (mAllPlanets == null) {
            mAllPlanets = new HashMap<>(client.allPlanets.size());
        }
        newTurn(client);
    }

    public void newTurn(Client client) {
        int planetCount;
        if (client.allPlanets.size() <= 12) {
            planetCount = 4;
        } else if (client.allPlanets.size() <= 22) {
            planetCount = 5;
        } else {
            planetCount = 7;
        }
        if (client.myPlanets.size() > 0 && client.notMyPlanets.size() > 0) {
            ArrayList<MapObject> targetPlanets;
            ArrayList<MapObject> myPlanets = client.myPlanets;

            if (client.myPlanets.size() < planetCount && client.neutralPlanets.size() > 0) {
                targetPlanets = client.neutralPlanets;
            } else {
                targetPlanets = client.enemyPlanets;
            }

            HashMap<Integer, Integer> targetPlanetsHash = new HashMap<>(targetPlanets.size());
            for (MapObject notMyPlanet : targetPlanets) {
                for (MapObject myPlanet : myPlanets) {
                    int key = notMyPlanet.id;
                    int defaultValue = targetPlanetsHash.getOrDefault(key, 0);
                    int difference = client.turnsFromTo(myPlanet, notMyPlanet) + notMyPlanet.value;
                    targetPlanetsHash.put(key, defaultValue + difference);
                }
            }

            targetPlanets.sort((t0, t1) -> {
                if (targetPlanetsHash.get(t0.id) >= targetPlanetsHash.get(t1.id)) {
                    return 1;
                } else {
                    return -1;
                }
            });

            for (MapObject notMyPlanet : targetPlanets) {
                myPlanets.sort((t0, t1) -> {
                    if (client.turnsFromTo(notMyPlanet, t0) + t0.value >= client.turnsFromTo(notMyPlanet, t1) + t1.value) {
                        return 1;
                    } else if (client.turnsFromTo(notMyPlanet, t0) + t0.value < client.turnsFromTo(notMyPlanet, t1) + t1.value) {
                        return -1;
                    } else {
                        if (client.turnsFromTo(notMyPlanet, t0) > client.turnsFromTo(notMyPlanet, t1)) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
            }

            for (MapObject notMyPlanet : targetPlanets) {
                ArrayList<MapObject> myShips = client.myShips;
                ArrayList<MapObject> shipsToEnemyPlanet = new ArrayList<MapObject>();
                for (MapObject ship : myShips) {
                    if (ship.toId == notMyPlanet.id) {
                        shipsToEnemyPlanet.add(ship);
                    }
                }

                int key = notMyPlanet.id;

                int sentShips = mAllPlanets.getOrDefault(key, 0);

                if (shipsToEnemyPlanet.size() < sentShips) {
                    sentShips = shipsToEnemyPlanet.size();
                }

                if (sentShips > notMyPlanet.value) {
                    continue;
                }

                for (MapObject myPlanet : myPlanets) {
                    int shipIncrementIndex = 10;
                    int shipsToSend = myPlanet.value - 3;
                    int timeOfTurnsTo = client.turnsFromTo(myPlanet, notMyPlanet);
                    if (myPlanets.size() < planetCount - 2 && client.neutralPlanets.size() > 0
                            && myPlanet.value > 2) {
                        client.send(myPlanet, notMyPlanet, myPlanet.value - 2);
                    } else if (myPlanet.value > shipIncrementIndex + 1) {
                        int notMyPlanetIncrementType = 0;
                        if (notMyPlanet.own == 2) {
                            notMyPlanetIncrementType = notMyPlanet.size;
                        }

                        mAllPlanets.put(key, sentShips + shipsToSend - (timeOfTurnsTo * notMyPlanetIncrementType));

                        client.send(myPlanet, notMyPlanet, shipsToSend);
                    }
                }
            }
        }
        client.endTurn();
    }
}