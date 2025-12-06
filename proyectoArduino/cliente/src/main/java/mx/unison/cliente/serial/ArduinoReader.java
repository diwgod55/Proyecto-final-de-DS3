package mx.unison.cliente.serial;

import com.fazecast.jSerialComm.SerialPort;
import mx.unison.cliente.sensorArduino.sensor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ArduinoReader {
    private SerialPort port;
    private Thread readThread;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Consumer<sensor> dataConsumer;
    private Consumer<String> errorConsumer;

    public ArduinoReader(Consumer<sensor> dataConsumer, Consumer<String> errorConsumer) {
        this.dataConsumer = dataConsumer;
        this.errorConsumer = errorConsumer;
    }

    public static String[] getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }
        return portNames;
    }

    public boolean connect(String portName) {
        try {
            SerialPort[] ports = SerialPort.getCommPorts();
            for (SerialPort p : ports) {
                if (p.getSystemPortName().equals(portName)) {
                    port = p;
                    break;
                }
            }

            if (port == null) {
                errorConsumer.accept("Puerto no encontrado: " + portName);
                return false;
            }

            port.setComPortParameters(9600, 8, 1, 0);
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

            if (port.openPort()) {
                // Esperar a que Arduino se inicialice
                Thread.sleep(2000);
                return true;
            } else {
                errorConsumer.accept("No se pudo abrir el puerto: " + portName);
                return false;
            }
        } catch (Exception ex) {
            errorConsumer.accept("Error conectando: " + ex.getMessage());
            return false;
        }
    }


    public void start() {
        if (port == null || !port.isOpen()) {
            errorConsumer.accept("Puerto no conectado");
            return;
        }

        running.set(true);
        readThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(port.getInputStream()))) {

                while (running.get()) {
                    if (reader.ready()) {
                        String line = reader.readLine();
                        if (line != null && !line.trim().isEmpty()) {
                            try {
                                sensor data = parseLine(line);
                                dataConsumer.accept(data);
                            } catch (Exception ex) {
                                errorConsumer.accept("Error parseando línea: " + line);
                            }
                        }
                    }
                    Thread.sleep(50); // Pequeña pausa para no saturar CPU
                }
            } catch (Exception ex) {
                if (running.get()) {
                    errorConsumer.accept("Error leyendo datos: " + ex.getMessage());
                }
            }
        });
        readThread.start();
    }

    public void stop() {
        running.set(false);
        if (readThread != null) {
            readThread.interrupt();
        }
    }

    public void disconnect() {
        stop();
        if (port != null && port.isOpen()) {
            port.closePort();
        }
    }

    private sensor parseLine(String line) {
        int x = 0, y = 0, z = 0;

        String[] parts = line.replace(" ", "").split(",");

        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim().toLowerCase();
                int value = Integer.parseInt(kv[1].trim());

                switch (key) {
                    case "x": x = value; break;
                    case "y": y = value; break;
                    case "z": z = value; break;
                }
            }
        }

        return new sensor(x, y, z);
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isConnected() {
        return port != null && port.isOpen();
    }
}