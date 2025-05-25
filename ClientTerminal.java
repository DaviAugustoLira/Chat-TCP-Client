import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientTerminal {
    public static void main (String[] args){
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
                        System.out.println(mensagem.toString());
                    }
                }
            });
            thread.start();

            Scanner sc = new Scanner(System.in);
            while (true){
                Mensagem request = null;
                String texto = sc.nextLine();
                try {
                    String[] partes = texto.split(":", 3);
                    if(partes[0].equalsIgnoreCase("/privado") || partes[0].equalsIgnoreCase("/p")) {
                        request = new Mensagem(null, partes[1], partes[2]);
                    }else{
                        request = new Mensagem(null, null, partes[0]);
                    }

                    output.writeObject(request);
                    output.flush();
                } catch (IOException e) {
                    System.out.println("Erro ao enviar uma mensagem - VERIFIQUE SE O SERVER ESTÁ RODANDO");
                    return;
                }
            }
    }
}
