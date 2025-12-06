package mx.unison.cliente.interfaz;

import javax.swing.*;
import java.awt.*;
import mx.unison.cliente.serial.simuladorDatos;
import mx.unison.cliente.serial.ArduinoReader;
import mx.unison.cliente.com.socketCliente;
import mx.unison.cliente.sensorArduino.sensor;
import mx.unison.cliente.main.appFrame;
import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.*;

public class panelMonitoreo extends JPanel {

    private static final Color AZUL_UNISON = new Color(0x00, 0x52, 0x9E);
    private static final Color AZUL_OSCURO = new Color(0x01, 0x52, 0x94);
    private static final Color DORADO_UNISON = new Color(0xF8, 0xBB, 0x00);
    private static final Color DORADO_OSCURO = new Color(0xD9, 0x9E, 0x30);
    private static final Font FONT_SEGOE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SEGOE_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private TimeSeries seriesX = new TimeSeries("Eje X");
    private TimeSeries seriesY = new TimeSeries("Eje Y");
    private TimeSeries seriesZ = new TimeSeries("Eje Z");

    private simuladorDatos simulador;
    private ArduinoReader arduino;
    private socketCliente socket = new socketCliente("localhost", 5000);
    private appFrame app;

    private JComboBox<String> puertoCombo;
    private JRadioButton rbSimulador;
    private JRadioButton rbArduino;
    private JButton btnIniciar;
    private JButton btnDetener;
    private JButton btnRefrescar;
    private JLabel statusLabel;

    private boolean isRunning = false;

    public panelMonitoreo(appFrame app) {
        this.app = app;
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelInferior(), BorderLayout.SOUTH);
        add(crearPanelGrafica(), BorderLayout.CENTER);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 18, 15, 18)
        ));

        // Sección izquierda: Configuración
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        izquierda.setOpaque(false);

        // Radio buttons
        JPanel grupoRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        grupoRadio.setOpaque(false);
        grupoRadio.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AZUL_UNISON, 1),
                "Modo de operación",
                0,
                0,
                FONT_SEGOE_BOLD,
                AZUL_UNISON
        ));

        rbSimulador = new JRadioButton("Simulador", true);
        rbArduino = new JRadioButton("Arduino");

        configurarRadioButton(rbSimulador);
        configurarRadioButton(rbArduino);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbSimulador);
        grupo.add(rbArduino);

        grupoRadio.add(rbSimulador);
        grupoRadio.add(rbArduino);

        // Selector de puerto
        JPanel panelPuerto = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelPuerto.setOpaque(false);

        JLabel lblPuerto = new JLabel("Puerto:");
        lblPuerto.setFont(FONT_SEGOE_BOLD);
        lblPuerto.setForeground(AZUL_OSCURO);

        puertoCombo = new JComboBox<>(ArduinoReader.getAvailablePorts());
        puertoCombo.setFont(FONT_SEGOE);
        puertoCombo.setPreferredSize(new Dimension(120, 30));
        puertoCombo.setEnabled(false);

        btnRefrescar = crearBotonIcono("⟳");
        btnRefrescar.setToolTipText("Actualizar lista de puertos");
        btnRefrescar.setEnabled(false);
        btnRefrescar.addActionListener(e -> refrescarPuertos());

        panelPuerto.add(lblPuerto);
        panelPuerto.add(puertoCombo);
        panelPuerto.add(btnRefrescar);

        rbArduino.addActionListener(e -> {
            puertoCombo.setEnabled(true);
            btnRefrescar.setEnabled(true);
        });

        rbSimulador.addActionListener(e -> {
            puertoCombo.setEnabled(false);
            btnRefrescar.setEnabled(false);
        });

        izquierda.add(grupoRadio);
        izquierda.add(panelPuerto);

        // Sección derecha: Acciones
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);

        btnIniciar = crearBotonAccion("▶ Iniciar", true);
        btnDetener = crearBotonAccion("■ Detener", false);
        JButton btnVolver = crearBotonAccion("← Volver", true);

        btnDetener.setEnabled(false);

        btnIniciar.addActionListener(e -> iniciarMonitoreo());
        btnDetener.addActionListener(e -> detenerMonitoreo());
        btnVolver.addActionListener(e -> {
            detenerMonitoreo();
            app.mostrar(appFrame.INICIO);
        });

        derecha.add(btnIniciar);
        derecha.add(btnDetener);
        derecha.add(btnVolver);

        panel.add(izquierda, BorderLayout.WEST);
        panel.add(derecha, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AZUL_UNISON);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        statusLabel = new JLabel("● Sistema detenido - Configure el modo y presione Iniciar");
        statusLabel.setFont(FONT_SEGOE);
        statusLabel.setForeground(Color.WHITE);
        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private ChartPanel crearPanelGrafica() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Monitoreo en Tiempo Real - Sensor Triaxial",
                "Tiempo",
                "Valor del sensor",
                dataset,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(252, 252, 252));
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        chartPanel.setPreferredSize(new Dimension(800, 500));

        return chartPanel;
    }

    private void iniciarMonitoreo() {
        seriesX.clear();
        seriesY.clear();
        seriesZ.clear();

        if (rbSimulador.isSelected()) {
            iniciarSimulador();
        } else {
            iniciarArduino();
        }
    }

    private void iniciarSimulador() {
        simulador = new simuladorDatos(this::procesarDato);
        simulador.start();
        actualizarInterfaz(true, "● Simulador en ejecución - Datos generados automáticamente");
    }

    private void iniciarArduino() {
        String puerto = (String) puertoCombo.getSelectedItem();

        if (puerto == null || puerto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un puerto COM.\n\n" +
                            "Si la lista está vacía:\n" +
                            "• Verifique la conexión USB del Arduino\n" +
                            "• Haga clic en el botón Actualizar (⟳)",
                    "Puerto requerido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        statusLabel.setText("⏳ Estableciendo conexión con " + puerto + "...");
        statusLabel.setForeground(new Color(255, 160, 0));
        btnIniciar.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                arduino = new ArduinoReader(
                        panelMonitoreo.this::procesarDato,
                        error -> SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("⚠ Error: " + error);
                            statusLabel.setForeground(Color.RED);
                        })
                );
                return arduino.connect(puerto);
            }

            @Override
            protected void done() {
                try {
                    boolean conectado = get();
                    if (conectado) {
                        arduino.start();
                        actualizarInterfaz(true, "● Conectado a " + puerto + " - Recibiendo datos");
                    } else {
                        actualizarInterfaz(false, "● Sistema detenido");
                        JOptionPane.showMessageDialog(panelMonitoreo.this,
                                "Fallo en la conexión al puerto " + puerto + "\n\n" +
                                        "Verificaciones necesarias:\n" +
                                        "• Arduino conectado correctamente\n" +
                                        "• Puerto no utilizado por otra aplicación\n" +
                                        "• Permisos de acceso disponibles\n" +
                                        "• Puerto correcto seleccionado\n\n" +
                                        "Acciones sugeridas:\n" +
                                        "1. Revisar conexión física\n" +
                                        "2. Cerrar otras aplicaciones (Arduino IDE)\n" +
                                        "3. Actualizar lista de puertos\n" +
                                        "4. Probar con otro puerto",
                                "Error de conexión",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    actualizarInterfaz(false, "● Error en la conexión");
                    JOptionPane.showMessageDialog(panelMonitoreo.this,
                            "Error durante la conexión:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void detenerMonitoreo() {
        if (simulador != null) {
            simulador.stop();
            simulador = null;
        }
        if (arduino != null) {
            arduino.disconnect();
            arduino = null;
        }
        actualizarInterfaz(false, "● Sistema detenido");
    }

    private void procesarDato(sensor d) {
        SwingUtilities.invokeLater(() -> {
            Millisecond ahora = new Millisecond();
            seriesX.addOrUpdate(ahora, d.getX());
            seriesY.addOrUpdate(ahora, d.getY());
            seriesZ.addOrUpdate(ahora, d.getZ());

            if (seriesX.getItemCount() > 60) {
                seriesX.delete(0, 0);
                seriesY.delete(0, 0);
                seriesZ.delete(0, 0);
            }
        });

        new Thread(() -> socket.sendSensorData(d)).start();
    }

    private void actualizarInterfaz(boolean activo, String mensaje) {
        isRunning = activo;
        btnIniciar.setEnabled(!activo);
        btnDetener.setEnabled(activo);
        rbSimulador.setEnabled(!activo);
        rbArduino.setEnabled(!activo);
        puertoCombo.setEnabled(!activo && rbArduino.isSelected());
        btnRefrescar.setEnabled(!activo && rbArduino.isSelected());

        statusLabel.setText(mensaje);
        statusLabel.setForeground(activo ? DORADO_UNISON : Color.WHITE);
    }

    private void refrescarPuertos() {
        puertoCombo.removeAllItems();
        String[] puertos = ArduinoReader.getAvailablePorts();

        for (String puerto : puertos) {
            puertoCombo.addItem(puerto);
        }

        if (puertos.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "No se detectaron puertos COM.\n\n",
                    "Lista vacía",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            String plural = puertos.length == 1 ? "" : "s";
            JOptionPane.showMessageDialog(this,
                    "Puerto" + plural + " detectado" + plural + ": " + puertos.length + "\n\n" +
                            String.join(", ", puertos),
                    "Lista actualizada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JButton crearBotonAccion(String texto, boolean esPrimario) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        if (esPrimario) {
            btn.setBackground(AZUL_UNISON);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(DORADO_UNISON);
            btn.setForeground(AZUL_OSCURO);
        }

        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setPreferredSize(new Dimension(110, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(esPrimario ? AZUL_OSCURO : DORADO_OSCURO, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        Color colorOriginal = btn.getBackground();
        Color colorHover = esPrimario ? AZUL_OSCURO : DORADO_OSCURO;

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(colorHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(colorOriginal);
            }
        });

        return btn;
    }

    private JButton crearBotonIcono(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(Color.WHITE);
        btn.setForeground(AZUL_UNISON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setPreferredSize(new Dimension(35, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(new Color(240, 240, 240));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    private void configurarRadioButton(JRadioButton rb) {
        rb.setFont(FONT_SEGOE);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}