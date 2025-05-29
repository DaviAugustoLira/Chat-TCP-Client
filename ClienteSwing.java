
import javax.swing.*;
import java.awt.*;

import java.io.*;
import java.net.Socket;

public class ClienteSwing extends JFrame{
    private JTextArea areaTexto;
    private JTextField campoEntrada;


    public ClienteSwing(String username) {
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
            //Criação do Socket e Stream
            socket = new Socket(host, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            //Envia mensagem com username para "logar" no servidor
            Mensagem mensagemI = new Mensagem(username, null, null);
            output.writeObject(mensagemI);

            try {
                Mensagem mensagemLogin = (Mensagem) input.readObject();
                if(mensagemLogin.getConteudo().contains("Usuário já cadastrado")){
                    boolean isLogin = false;
                    while(mensagemLogin.getConteudo().contains("Usuário já cadastrado") || !isLogin){
                        username = JOptionPane.showInputDialog("Informe o seu Username:");
                        if (username == null || username.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Username Inválido");
                            continue;
                        };
                        isLogin = true;
                        output.writeObject(new Mensagem(username, null, null));
                        mensagemLogin = (Mensagem) input.readObject();
                    }
                }
                areaTexto.append(mensagemLogin.toString() +"\n");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "VERIFIQUE SE O SERVER ESTÁ RODANDO");
            throw new RuntimeException("VERIFIQUE SE O SERVER ESTÁ RODANDO");
//            return;
        }

        //Thread que "escuta" a entrada de dados
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Mensagem mensagem = null;
                while(true) {
                    try {
                        mensagem = (Mensagem) input.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        JOptionPane.showMessageDialog(null, "Erro inesperado ao receber uma mensagem " +
                                "- VERIFIQUE SE O SERVER ESTÁ RODANDO");
                        throw new RuntimeException("VERIFIQUE SE O SERVER ESTÁ RODANDO");
                    }
                    areaTexto.append(mensagem.toString() +"\n");
                }
            }
        });
        thread.start();

        String temp = username;
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
                    mensagem = new Mensagem(temp, partes[1], partes[0]+":"+partes[2]);
                }else{
                    mensagem = new Mensagem(temp, null, partes[0]);
                }

                output.writeObject(mensagem);
                output.flush();
            } catch (IOException error) {
                JOptionPane.showMessageDialog(null, "Erro inesperado ao enviar uma mensagem " +
                        "   - VERIFIQUE SE O SERVER ESTÁ RODANDO");
                throw new RuntimeException("VERIFIQUE SE O SERVER ESTÁ RODANDO");
            }
        });
    }

    public static void main(String[] args) {
        String username = " ";
        boolean isLogin = false;
        //Pede o username do usuário, e enquanto o username for null ou vazio continua pedindo um username até ser válido
        while(!isLogin){
            username = JOptionPane.showInputDialog("Informe o seu Username:");
            if (username == null || username.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username Inválido");
                continue;
            };
            isLogin = true;
        }
        //Inicia Cliente e Swing
        String finalUsername = username;
        SwingUtilities.invokeLater(() -> {
            ClienteSwing cliente = new ClienteSwing(finalUsername);
            cliente.setVisible(true);
        });
    }
}