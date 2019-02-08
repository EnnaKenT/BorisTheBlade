package Client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.lang.Math;

public class Client implements Runnable  {
    private int _port;
    private DataInputStream _in;
    private DataOutputStream _out;
    private BotInterface _bot;
    public ArrayList <MapObject> allPlanets = null;
    public ArrayList <MapObject> myPlanets = null;
    public ArrayList <MapObject> enemyPlanets = null;
    public ArrayList <MapObject> neutralPlanets = null;
    public ArrayList <MapObject> notMyPlanets = null;
    public ArrayList <MapObject> allShips = null;
    public ArrayList <MapObject> myShips = null;
    public ArrayList <MapObject> enemyShips = null;
    public int turnNumber;
    public boolean shouldStop;
    private double _shipsSpeed;
    
    public Client(BotInterface bot, String port) {
        this._bot = bot;
        this._port = Integer.parseInt(port);
        this.turnNumber = 0;
        this._shipsSpeed = 0.04; // default value
        this.shouldStop = false;
        (new Thread(this)).start();
    }
    
    public void run() {
        String address = "localhost";
        boolean connected = false;
        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getByName(address);
        } catch(Exception e) {}
        
        while (!connected) {
            connected = true;
            try {
                System.out.println("connecting: "+ ipAddress + " p: " + this._port);
                Socket socket = new Socket(ipAddress, this._port);

                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();
                this._in = new DataInputStream(sin);
                this._out = new DataOutputStream(sout);
            } catch (Exception x) {
                connected = false;
                x.printStackTrace();
                try {
                    Thread.sleep(500);
                } catch(InterruptedException e) {}
            }
        }
        
        StreamReader lsr = new StreamReader(this._in, this);
        Thread thread = new Thread(lsr, "LogStreamReader");
        thread.start();
        while(this.shouldStop == false) {
            try {
                Thread.sleep(10);
            } catch(InterruptedException e) {}
        }
//        System.exit(0);
    }
    
    public void parseMeta(String string) {
        String[] arr = string.split("#");
        if (arr.length > 1) {
            String[] ps = arr[1].split(";");
            this._shipsSpeed = Double.parseDouble(ps[0]);
            this.turnNumber = Integer.parseInt(ps[1]);
        }
    }
    
    public void parseObjects(String string) {
        String[] arr = string.split("#");
        ArrayList <MapObject> list = new ArrayList<MapObject>();

        if (arr.length > 1) {
            String[] ps = arr[1].split(";");
            for (int i = 0; i < ps.length; i++) {
                String objectString = ps[i];
                MapObject object = new MapObject(objectString);
                object.id = i;
                if (object != null) {
                    list.add(object);
                }
            }
        }

        if (arr[0].equals("planets")) {
            this.allPlanets = new ArrayList<MapObject>();
            this.myPlanets = new ArrayList<MapObject>();
            this.notMyPlanets = new ArrayList<MapObject>();
            this.enemyPlanets = new ArrayList<MapObject>();
            this.neutralPlanets = new ArrayList<MapObject>();
            for (MapObject object : list) {
                this.allPlanets.add(object);
                switch (object.own) {
                    case 0: {
                        this.neutralPlanets.add(object);
                        this.notMyPlanets.add(object);
                    } break;
                        
                    case 1: {
                        this.myPlanets.add(object);
                    } break;
                        
                    case 2: {
                        this.enemyPlanets.add(object);
                        this.notMyPlanets.add(object);
                    } break;
                }
            }
        }
        if (arr[0].equals("ships")) {
            this.allShips = new ArrayList<MapObject>();
            this.myShips = new ArrayList<MapObject>();
            this.enemyShips = new ArrayList<MapObject>();
            for (MapObject object : list) {
                this.allShips.add(object);
                switch (object.own) {
                    case 1: {
                        this.myShips.add(object);
                    } break;
                        
                    case 2: {
                        this.enemyShips.add(object);
                    } break;
                }
            }
        }
    }

    // Game methods
    
    public void turn() {
        this._bot.turn(this, this.turnNumber);
    }
    
    public void endTurn() {
        try {
            this._out.writeUTF("#endTurn");
        } catch(Exception x) { x.printStackTrace(); }
    }
    
    public void send(MapObject from, MapObject to, int count) {
        try {
            this._out.writeUTF("#send:"+from.id+","+to.id+","+count);
        } catch(Exception x) { x.printStackTrace(); }
    }
    
    public double distance(MapObject from, MapObject to) {
        double x = to.x - from.x;
        double y = to.y - from.y;
        double distance = Math.sqrt(x*x + y*y);
        return distance;
    }
    
    public int turnsFromTo(MapObject from, MapObject to) {
        double distance = this.distance(from, to);
        double turns = distance / this.shipsSpeed();
        int intTurns = (int)turns;
        if (turns > intTurns) {
            intTurns++;
        }
        return intTurns;
    }

    public double shipsSpeed() {
        return this._shipsSpeed;
    }
    
    public void stop() {
        this.shouldStop = true;
    }
}

class StreamReader implements Runnable {
    
    private DataInputStream reader;
    private Client _client;
    
    public StreamReader(DataInputStream is, Client client) {
        this._client = client;
        this.reader = is;
    }
    
    public void run() {
        try {
            String line = reader.readUTF();
            while (line != null) {
                if (line.equals("stop")) {
                    System.out.println("stop");
                    this._client.stop();
                    line = null;
                } else {
                    String[] arr = line.split(":");
                    this._client.parseObjects(arr[0]);
                    this._client.parseObjects(arr[1]);
                    this._client.parseMeta(arr[2]);
                    
                    this._client.turn();
                    line = reader.readUTF();
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
