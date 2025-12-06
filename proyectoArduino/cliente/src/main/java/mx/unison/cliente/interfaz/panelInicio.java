package mx.unison.cliente.interfaz;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import javax.imageio.ImageIO;
import mx.unison.cliente.main.appFrame;

public class panelInicio extends JPanel {

    private static final Color AZUL_UNISON = new Color(0x00, 0x52, 0x9E);
    private static final Color DORADO_UNISON = new Color(0xF8, 0xBB, 0x00);
    private static final Font FONT_SEGOE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);

    public panelInicio(appFrame app) {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.CENTER;

        // Panel contenedor principal con borde
        JPanel contenedorPrincipal = new JPanel();
        contenedorPrincipal.setLayout(new BoxLayout(contenedorPrincipal, BoxLayout.Y_AXIS));
        contenedorPrincipal.setBackground(new Color(250, 250, 250));
        contenedorPrincipal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL_UNISON, 3),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        // Logo
        JLabel logoLabel = cargarLogo();
        if (logoLabel != null) {
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contenedorPrincipal.add(logoLabel);
            contenedorPrincipal.add(Box.createVerticalStrut(35));
        }

        // Título principal
        JLabel titulo = new JLabel("Sistema de Monitoreo");
        titulo.setFont(FONT_TITLE);
        titulo.setForeground(AZUL_UNISON);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedorPrincipal.add(titulo);

        contenedorPrincipal.add(Box.createVerticalStrut(8));

        // Subtítulo
        JLabel subtitulo = new JLabel("Visualización y análisis de sensores");
        subtitulo.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        subtitulo.setForeground(new Color(100, 100, 100));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedorPrincipal.add(subtitulo);

        contenedorPrincipal.add(Box.createVerticalStrut(20));

        // Autor
        JLabel autor = new JLabel("Desarrollado por: Diego Alejandro Torres Salas");
        autor.setFont(FONT_SEGOE);
        autor.setForeground(new Color(80, 80, 80));
        autor.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedorPrincipal.add(autor);

        contenedorPrincipal.add(Box.createVerticalStrut(40));

        // Separador visual
        JSeparator separador = new JSeparator(SwingConstants.HORIZONTAL);
        separador.setMaximumSize(new Dimension(400, 2));
        separador.setForeground(DORADO_UNISON);
        separador.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedorPrincipal.add(separador);

        contenedorPrincipal.add(Box.createVerticalStrut(35));

        // Panel de botones en vertical
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setOpaque(false);

        JButton bMonitor = crearBotonEstilizado("▶ Monitor", "Visualización en tiempo real");
        JButton bHistorico = crearBotonEstilizado("📊 Histórico", "Consulta de registros previos");

        bMonitor.addActionListener(e -> app.mostrar(appFrame.MONITOR));
        bHistorico.addActionListener(e -> app.mostrar(appFrame.HISTORICO));

        bMonitor.setAlignmentX(Component.CENTER_ALIGNMENT);
        bHistorico.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelBotones.add(bMonitor);
        panelBotones.add(Box.createVerticalStrut(18));
        panelBotones.add(bHistorico);

        contenedorPrincipal.add(panelBotones);

        add(contenedorPrincipal, gbc);
    }

    private JLabel cargarLogo() {
        try {
            InputStream is = getClass().getResourceAsStream("/resources/logo_unison.png");
            if (is == null) {
                is = getClass().getResourceAsStream("/logo_unison.png");
            }

            if (is != null) {
                Image img = ImageIO.read(is);
                Image scaled = img.getScaledInstance(190, 190, Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(scaled));
            } else {
                System.out.println("Logo no encontrado. Coloca 'logo_unison.png' en src/main/resources/");
            }
        } catch (Exception ex) {
            System.err.println("Error cargando logo: " + ex.getMessage());
        }
        return null;
    }

    private JButton crearBotonEstilizado(String texto, String tooltip) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 17));
        btn.setBackground(AZUL_UNISON);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(280, 55));
        btn.setMaximumSize(new Dimension(280, 55));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);

        // Borde con esquinas más pronunciadas
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL_UNISON, 1),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));

        // Efecto hover con dorado
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(DORADO_UNISON);
                btn.setForeground(AZUL_UNISON);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(DORADO_UNISON, 2),
                        BorderFactory.createEmptyBorder(12, 25, 12, 25)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(AZUL_UNISON);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AZUL_UNISON, 1),
                        BorderFactory.createEmptyBorder(12, 25, 12, 25)
                ));
            }
        });

        return btn;
    }
}