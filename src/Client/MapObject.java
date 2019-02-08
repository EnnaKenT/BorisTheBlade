package Client;

public class MapObject {
    public double x;
    public double y;
    public int type;
    public int size;
    public int value;
    public int own;
    public int numberOfTurns;
    public int id;
    public int fromId;
    public int toId;
    
    public MapObject(String string) {
        String[] arr = string.split(",");
        this.type = Integer.parseInt(arr[0]);
        this.x = Double.parseDouble(arr[1]);
        this.y = Double.parseDouble(arr[2]);
        this.value = Integer.parseInt(arr[3]);
        this.own = Integer.parseInt(arr[4]);
        if (this.type == 1) {
            this.size = Integer.parseInt(arr[5]);
            this.fromId = 0;
            this.toId = 0;
            this.numberOfTurns = 0;
        } else {
            this.size = 0;
            this.id = 0;
            this.fromId = Integer.parseInt(arr[5]);
            this.toId = Integer.parseInt(arr[6]);
            this.numberOfTurns = Integer.parseInt(arr[7]);
        }
    }
}
