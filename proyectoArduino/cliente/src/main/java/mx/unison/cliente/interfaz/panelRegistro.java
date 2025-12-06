package mx.unison.cliente.interfaz;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;

import mx.unison.cliente.com.socketCliente;
import mx.unison.cliente.sensorArduino.sensor;
import mx.unison.cliente.main.appFrame;
import org.jfree.chart.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public class panelRegistro extends JPanel {

    private static final Color AZUL_UNISON = new Color(0x00, 0x52, 0x9E);
    private static final Color AZUL_OSCURO = new Color(0x01, 0x52, 0x94);
    private static final Color DORADO_UNISON = new Color(0xF8, 0xBB, 0x00);
    private static final Color DORADO_OSCURO = new Color(0xD9, 0x9E, 0x30);
    private static final Font FONT_SEGOE = new Font("Segoe UI", Font.PLAIN, 13);

    private socketCliente socket = new socketCliente("localhost", 5000);
    private appFrame app;

    private JSpinner fechaSpinner;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;
    private DefaultCategoryDataset dataset;
    private JLabel statusLabel;
    private JButton btnBuscar;
    private JButton btnLimpiar;

    public panelRegistro(appFrame app) {
        this.app = app;
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(crearBarraFiltros(), BorderLayout.NORTH);
        add(crearBarraEstado(), BorderLayout.SOUTH);
        add(crearAreaGrafica(), BorderLayout.CENTER);
    }

    private JPanel crearBarraFiltros() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(250, 250, 250));
        panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Panel de campos
        JPanel panelCampos = new JPanel();
        panelCampos.setLayout(new BoxLayout(panelCampos, BoxLayout.Y_AXIS));
        panelCampos.setOpaque(false);

        // Primera fila: Fecha
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fila1.setOpaque(false);

        JLabel etiquetaFecha = crearEtiqueta("Fecha de consulta:");
        fechaSpinner = new JSpinner(new SpinnerDateModel());
        fechaSpinner.setEditor(new JSpinner.DateEditor(fechaSpinner, "yyyy-MM-dd"));
        fechaSpinner.setFont(FONT_SEGOE);
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) fechaSpinner.getEditor();
        editor.getTextField().setColumns(13);
        editor.getTextField().setPreferredSize(new Dimension(150, 34));
        aplicarEstiloCampo(editor.getTextField());

        fila1.add(etiquetaFecha);
        fila1.add(fechaSpinner);

        // Segunda fila: Horas
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fila2.setOpaque(false);

        JLabel etiquetaHoraIni = crearEtiqueta("Hora inicial:");
        txtHoraInicio = new JTextField("08:00", 9);
        txtHoraInicio.setPreferredSize(new Dimension(110, 34));
        aplicarEstiloCampo(txtHoraInicio);
        txtHoraInicio.setToolTipText("Formato: HH:MM o HH:MM:SS");

        JLabel etiquetaHoraFin = crearEtiqueta("Hora final:");
        txtHoraFin = new JTextField("18:00", 9);
        txtHoraFin.setPreferredSize(new Dimension(110, 34));
        aplicarEstiloCampo(txtHoraFin);
        txtHoraFin.setToolTipText("Formato: HH:MM o HH:MM:SS");

        fila2.add(etiquetaHoraIni);
        fila2.add(txtHoraInicio);
        fila2.add(Box.createHorizontalStrut(15));
        fila2.add(etiquetaHoraFin);
        fila2.add(txtHoraFin);

        panelCampos.add(fila1);
        panelCampos.add(fila2);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotones.setOpaque(false);

        btnBuscar = crearBotonControl("🔍 Buscar", true);
        btnLimpiar = crearBotonControl("✗ Limpiar", false);
        JButton btnVolver = crearBotonControl("← Volver", false);

        btnBuscar.addActionListener(e -> buscarDatos());
        btnLimpiar.addActionListener(e -> limpiarGrafica());
        btnVolver.addActionListener(e -> app.mostrar(appFrame.INICIO));

        panelBotones.add(btnBuscar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnVolver);

        panelPrincipal.add(panelCampos, BorderLayout.WEST);
        panelPrincipal.add(panelBotones, BorderLayout.EAST);

        return panelPrincipal;
    }

    private JPanel crearBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AZUL_UNISON);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        statusLabel = new JLabel("● Preparado - Configure los filtros y ejecute la búsqueda");
        statusLabel.setFont(FONT_SEGOE);
        statusLabel.setForeground(Color.WHITE);
        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private ChartPanel crearAreaGrafica() {
        dataset = new DefaultCategoryDataset();

        JFreeChart chart = ChartFactory.createLineChart(
                "Análisis Histórico - Registros del Sensor por Rango Temporal",
                "Índice del registro",
                "Medición",
                dataset
        );

        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
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

        return chartPanel;
    }

    private void buscarDatos() {
        String horaInicio = txtHoraInicio.getText().trim();
        String horaFin = txtHoraFin.getText().trim();

        if (!validarFormatoHora(horaInicio)) {
            JOptionPane.showMessageDialog(this,
                    "La hora inicial tiene un formato incorrecto.\n\n" +
                            "Formato esperado: HH:MM o HH:MM:SS\n" +
                            "Ejemplo válido: 08:00 o 08:00:00",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
            txtHoraInicio.requestFocus();
            return;
        }

        if (!validarFormatoHora(horaFin)) {
            JOptionPane.showMessageDialog(this,
                    "La hora final tiene un formato incorrecto.\n\n" +
                            "Formato esperado: HH:MM o HH:MM:SS\n" +
                            "Ejemplo válido: 18:00 o 18:00:00",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
            txtHoraFin.requestFocus();
            return;
        }

        Date d = (Date) fechaSpinner.getValue();
        LocalDate fecha = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        btnBuscar.setEnabled(false);
        btnLimpiar.setEnabled(false);
        statusLabel.setText("⏳ Recuperando registros del servidor...");
        statusLabel.setForeground(new Color(255, 160, 0));

        dataset.clear();

        SwingWorker<List<sensor>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<sensor> doInBackground() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}

                return socket.requestHistoricoFiltrado(
                        fecha.toString(),
                        horaInicio,
                        horaFin
                );
            }

            @Override
            protected void done() {
                try {
                    List<sensor> datos = get();
                    poblarGrafica(datos);

                    if (datos.isEmpty()) {
                        statusLabel.setText("⚠ Sin coincidencias para los criterios seleccionados");
                        statusLabel.setForeground(new Color(200, 100, 0));

                        JOptionPane.showMessageDialog(panelRegistro.this,
                                "No existen registros que cumplan:\n\n" +
                                        "Fecha: " + fecha + "\n" +
                                        "Desde: " + horaInicio + "\n" +
                                        "Hasta: " + horaFin + "\n\n" +
                                        "Pruebe modificando los filtros.",
                                "Búsqueda sin resultados",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("✓ Visualizando " + datos.size() + " registro(s) - " +
                                fecha + " (" + horaInicio + " a " + horaFin + ")");
                        statusLabel.setForeground(DORADO_UNISON);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("✖ Fallo en la recuperación de datos");
                    statusLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(panelRegistro.this,
                            "Error al consultar el servidor:\n\n" + ex.getMessage() +
                                    "\n\nVerificaciones:\n" +
                                    "• Servidor en ejecución\n" +
                                    "• Conectividad de red disponible\n" +
                                    "• Puerto 5000 accesible",
                            "Error de comunicación",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnBuscar.setEnabled(true);
                    btnLimpiar.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void poblarGrafica(List<sensor> datos) {
        dataset.clear();

        for (int i = 0; i < datos.size(); i++) {
            sensor s = datos.get(i);
            String etiqueta = String.valueOf(i + 1);
            dataset.addValue(s.getX(), "Eje X", etiqueta);
            dataset.addValue(s.getY(), "Eje Y", etiqueta);
            dataset.addValue(s.getZ(), "Eje Z", etiqueta);
        }
    }

    private void limpiarGrafica() {
        dataset.clear();
        statusLabel.setText("● Gráfica reiniciada - Configure filtros y ejecute la búsqueda");
        statusLabel.setForeground(Color.WHITE);
    }

    private boolean validarFormatoHora(String hora) {
        return hora.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$");
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(AZUL_OSCURO);
        return lbl;
    }

    private void aplicarEstiloCampo(JTextField campo) {
        campo.setFont(FONT_SEGOE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1, false),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AZUL_UNISON, 2, false),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(190, 190, 190), 1, false),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
    }

    private JButton crearBotonControl(String texto, boolean principal) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        if (principal) {
            btn.setBackground(AZUL_UNISON);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(DORADO_UNISON);
            btn.setForeground(AZUL_OSCURO);
        }

        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setPreferredSize(new Dimension(115, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(principal ? AZUL_OSCURO : DORADO_OSCURO, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        Color colorBase = btn.getBackground();
        Color colorActivo = principal ? AZUL_OSCURO : DORADO_OSCURO;

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(colorActivo);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(colorBase);
            }
        });

        return btn;
    }
}