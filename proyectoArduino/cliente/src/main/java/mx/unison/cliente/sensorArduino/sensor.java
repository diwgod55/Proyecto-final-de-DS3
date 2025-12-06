package mx.unison.cliente.sensorArduino;
public class sensor {
    private int x,y,z;
    public sensor(int x, int y, int z){this.x=x;this.y=y;this.z=z;}
    public int getX(){return x;} public int getY(){return y;} public int getZ(){return z;}
    public String serialize(){ return String.format("x:%d,y:%d,z:%d", x,y,z); }
    public static sensor deserialize(String s){
        String[] parts = s.split(","); int x=0,y=0,z=0;
        for(String p:parts){ String[] kv=p.split(":"); if(kv[0].trim().equals("x")) x=Integer.parseInt(kv[1]); if(kv[0].trim().equals("y")) y=Integer.parseInt(kv[1]); if(kv[0].trim().equals("z")) z=Integer.parseInt(kv[1]); }
        return new sensor(x,y,z);
    }
}
