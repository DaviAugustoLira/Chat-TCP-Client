
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ClienteSwing extends JFrame{
    private JTextArea areaTexto;
    private JTextField campoEntrada;


    public ClienteSwing() {
        setTitle("Chat TCP - Cliente");
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        campoEntrada = new JTextField();
        add(campoEntrada, BorderLayout.SOUTH);

        int port = 9090;
        String host = "localhost";

        Socket socket = null;
        ObjectOutputStream output;
        ObjectInputStream input;

        try {
            socket = new Socket(host, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Servidor não está rodando");
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Mensagem mensagem = null;
                while(true) {
                    try {
                        mensagem = (Mensagem) input.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Erro ao receber uma mensagem - VERIFIQUE SE O SERVER ESTÁ RODANDO");
                        return;
                    }
                    areaTexto.append(mensagem.toString() +"\n");
                }
            }
        });
        thread.start();

        campoEntrada.addActionListener(e -> {
            //Lê o texto do campo de entrada
            String texto = campoEntrada.getText();
            if (!texto.isBlank()) {

                //adiciona texto no campo de Entrada
                campoEntrada.setText("");
            }
            try {
                Mensagem mensagem = null;
                String[] partes = texto.split(":", 3);
                if(partes[0].equalsIgnoreCase("/privado") || partes[0].equalsIgnoreCase("/p")) {
                    mensagem = new Mensagem(null, partes[1], partes[2]);
                }else{
                    mensagem = new Mensagem(null, null, partes[0]);
                }

                output.writeObject(mensagem);
                output.flush();
            } catch (IOException error) {
                System.out.println("Erro ao enviar uma mensagem - VERIFIQUE SE O SERVER ESTÁ RODANDO");
                return;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteSwing cliente = new ClienteSwing();
            cliente.setVisible(true);
        });
    }
}