package mx.unison.cliente.com;

import mx.unison.cliente.sensorArduino.sensor;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class socketCliente {
    private String host;
    private int port;

    public socketCliente(String host, int port){
        this.host=host;
        this.port=port;
    }

    public void sendSensorData(sensor d){
        try (Socket s = new Socket(host,port);
             BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
             BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            String plain = "DATA|" + d.serialize();
            String enc = encriptacion.encrypt(plain);

            w.write(enc); w.newLine(); w.flush();
            // esperar ACK (no obligatorio para funcionar)
            r.readLine();

        } catch(Exception ex){ System.err.println("Error enviando datos: "+ex.getMessage()); }
    }

    /**
     * Solicita histórico filtrado al servidor.
     * @param fecha formato YYYY-MM-DD
     * @param h1 hora inicio formato HH:MM[:SS]
     * @param h2 hora fin formato HH:MM[:SS]
     * @return lista de SensorData (puede estar vacía)
     */
    public List<sensor> requestHistoricoFiltrado(String fecha, String h1, String h2){
        List<sensor> out = new ArrayList<>();
        try (Socket s = new Socket(host,port);
             BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
             BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            String plain = "HIST|" + fecha + "|" + h1 + "|" + h2;
            String enc = encriptacion.encrypt(plain);
            w.write(enc); w.newLine(); w.flush();

            String line;
            // Leer hasta que el servidor cierre conexión
            while ((line=r.readLine())!=null){
                if (line.trim().isEmpty()) continue;
                String dec;
                try {
                    dec = encriptacion.decrypt(line);
                } catch (Exception ex) {
                    System.err.println("Error desencriptando línea del servidor: " + ex.getMessage());
                    continue;
                }
                if(dec.startsWith("DATA|")){
                    String payload = dec.substring(5);
                    out.add(sensor.deserialize(payload));
                }
            }

        } catch(Exception ex){ System.err.println("Error historico filtrado: "+ex.getMessage()); }
        return out;
    }
}
