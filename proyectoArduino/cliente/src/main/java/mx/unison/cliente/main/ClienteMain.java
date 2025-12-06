package mx.unison.cliente.main;

import javax.swing.*;

public class ClienteMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new appFrame().setVisible(true);
        });
    }
}
