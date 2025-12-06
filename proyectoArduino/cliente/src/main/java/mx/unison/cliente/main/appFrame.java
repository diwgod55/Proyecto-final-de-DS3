package mx.unison.cliente.main;

import javax.swing.*;
import java.awt.*;
import mx.unison.cliente.interfaz.*;

public class appFrame extends JFrame {

    private CardLayout cards;
    private JPanel container;

    public static final String INICIO = "inicio";
    public static final String MONITOR = "monitor";
    public static final String HISTORICO = "historico";

    public appFrame() {
        setTitle("Sistema de Monitoreo - UNISON");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);



        cards = new CardLayout();
        container = new JPanel(cards);

        container.add(new panelInicio(this), INICIO);
        container.add(new panelMonitoreo(this), MONITOR);
        container.add(new panelRegistro(this), HISTORICO);

        add(container);
    }

    public void mostrar(String vista) {
        cards.show(container, vista);
    }
}
