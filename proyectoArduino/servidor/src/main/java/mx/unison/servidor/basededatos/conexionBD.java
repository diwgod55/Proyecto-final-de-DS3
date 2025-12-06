package mx.unison.servidor.basededatos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class conexionBD {
    private static final String URL = "jdbc:sqlite:monitorBD.db";

    public void init(){
        try (Connection c = DriverManager.getConnection(URL)){
            String sql = "CREATE TABLE IF NOT EXISTS datos_sensor ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "x INTEGER NOT NULL, y INTEGER NOT NULL, z INTEGER NOT NULL, "
                    + "fecha_de_captura TEXT NOT NULL, hora_de_captura TEXT NOT NULL);";
            c.createStatement().execute(sql);
            System.out.println("Base de datos inicializada.");
        } catch(Exception ex){ ex.printStackTrace(); }
    }

    public void insertSensor(int x,int y,int z){
        try (Connection c = DriverManager.getConnection(URL)){
            String sql = "INSERT INTO datos_sensor(x,y,z,fecha_de_captura,hora_de_captura) VALUES(?,?,?,?,?);";
            PreparedStatement p = c.prepareStatement(sql);
            p.setInt(1,x); p.setInt(2,y); p.setInt(3,z);
            p.setString(4, java.time.LocalDate.now().toString());
            // Guardamos hora en formato HH:MM:SS (sin nanos)
            p.setString(5, java.time.LocalTime.now().withNano(0).toString());
            p.executeUpdate();
        } catch(Exception ex){ ex.printStackTrace(); }
    }

    /**
     * Devuelve registros serializados (x:,y:,z:) filtrados por fecha y rango horario.
     * fecha: "YYYY-MM-DD"
     * h1, h2: "HH:MM" o "HH:MM:SS"
     */
    public List<String> getFilteredSerialized(String fecha, String h1, String h2){
        List<String> out = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(URL)){
            String sql = "SELECT x,y,z FROM datos_sensor WHERE fecha_de_captura=? AND hora_de_captura>=? AND hora_de_captura<=? ORDER BY id;";
            PreparedStatement p = c.prepareStatement(sql);
            p.setString(1, fecha);
            p.setString(2, h1.length() == 5 ? h1 + ":00" : h1);
            p.setString(3, h2.length() == 5 ? h2 + ":00" : h2);

            ResultSet rs = p.executeQuery();
            while (rs.next()){
                out.add(String.format("x:%d,y:%d,z:%d", rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
            }

        } catch(Exception ex){ ex.printStackTrace(); }

        return out;
    }
}
