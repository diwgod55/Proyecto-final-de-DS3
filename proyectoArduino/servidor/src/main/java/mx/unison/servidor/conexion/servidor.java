package mx.unison.servidor.conexion;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import mx.unison.servidor.basededatos.conexionBD;
import mx.unison.servidor.com.encriptacion;

public class servidor {
    private int port;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private conexionBD db = new conexionBD();

    public servidor(int port){
        this.port = port;
        db.init();
    }

    public void start(){
        try (ServerSocket ss = new ServerSocket(port)){
            System.out.println("Servidor iniciado en puerto " + port);
            while (true){
                Socket s = ss.accept();
                pool.submit(() -> handle(s));
            }
        } catch(Exception ex){ ex.printStackTrace(); }
    }

    private void handle(Socket s){
        try (BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            String line = r.readLine();
            if (line == null) return;

            String dec;
            try {
                dec = encriptacion.decrypt(line);
            } catch (Exception ex) {
                System.err.println("No pude desencriptar el mensaje entrante: " + ex.getMessage());
                return;
            }

            if (dec.startsWith("DATA|")){
                String payload = dec.substring(5);
                String[] parts = payload.split(",");
                int x=0,y=0,z=0;
                for(String p:parts){
                    String[] kv = p.split(":");
                    if (kv.length < 2) continue;
                    String key = kv[0].trim();
                    String val = kv[1].trim();
                    if(key.equals("x")) x = Integer.parseInt(val);
                    if(key.equals("y")) y = Integer.parseInt(val);
                    if(key.equals("z")) z = Integer.parseInt(val);
                }
                db.insertSensor(x,y,z);
                w.write("OK\n"); w.flush();
            }
            else if (dec.startsWith("HIST|")){
                String[] p = dec.split("\\|");
                if (p.length < 4) {
                    System.err.println("Solicitud HIST con formato inválido");
                    return;
                }
                String fecha = p[1];
                String h1 = p[2];
                String h2 = p[3];

                for(String row : db.getFilteredSerialized(fecha, h1, h2)){
                    String enc = encriptacion.encrypt("DATA|" + row);
                    w.write(enc);
                    w.newLine();
                    w.flush();
                }
            } else {
                System.err.println("Comando no reconocido: " + dec);
            }

        } catch(Exception ex){
            System.err.println("Error en manejo de cliente: "+ex.getMessage());
        } finally {
            try { s.close(); } catch(Exception ignored) {}
        }
    }
}
