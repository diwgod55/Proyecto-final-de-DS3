package mx.unison.cliente.serial;

import mx.unison.cliente.sensorArduino.sensor;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class simuladorDatos {
    private Thread t;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Consumer<sensor> consumer;
    private Random rnd = new Random();

    public simuladorDatos(Consumer<sensor> consumer) {
        this.consumer = consumer;
    }

    public void start() {
        running.set(true);
        t = new Thread(() -> {
            while (running.get()) {
                long startTime = System.currentTimeMillis();

                // Generar y enviar datos
                int x = rnd.nextInt(200) - 100;
                int y = rnd.nextInt(200) - 100;
                int z = rnd.nextInt(200) - 100;
                sensor d = new sensor(x, y, z);
                consumer.accept(d);

                // Calcular tiempo transcurrido y esperar el resto para completar 1 segundo
                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = 1000 - elapsedTime;

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        });
        t.start();
    }

    public void stop() {
        running.set(false);
        if (t != null) t.interrupt();
    }

    public boolean isRunning() {
        return running.get();
    }
}