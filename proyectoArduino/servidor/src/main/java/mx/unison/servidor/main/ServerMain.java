package mx.unison.servidor.main;
import mx.unison.servidor.conexion.servidor;
public class ServerMain {
    public static void main(String[] args) {
        servidor s = new servidor(5000);
        s.start();
    }
}
