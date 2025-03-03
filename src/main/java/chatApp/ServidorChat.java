package chatApp;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ServidorChat {
    public static void main(String[] args) {
        MarcoServidorChat mimarco = new MarcoServidorChat();
        mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

class MarcoServidorChat extends JFrame implements Runnable {
    
    public MarcoServidorChat() {
        // Configuración de la ventana del servidor
        setBounds(1200, 300, 280, 350);
        JPanel milamina = new JPanel();
        milamina.setLayout(new BorderLayout());
        
        // Área de texto donde se mostrarán los mensajes recibidos
        areatexto = new JTextArea();
        milamina.add(areatexto, BorderLayout.CENTER);
        add(milamina);
        
        setVisible(true);

        // Iniciar el servidor en un hilo separado para no bloquear la interfaz gráfica
        Thread miHilo = new Thread(this);
        miHilo.start();
    }

    @Override
    public void run() {
        try {
            // Se inicia el servidor en el puerto 50500
            ServerSocket miServer = new ServerSocket(50500);
            System.out.println("Servidor escuchando en el puerto 50500...");
            
            while (true) { // Bucle infinito para recibir múltiples clientes
                // Espera conexiones entrantes
                Socket miSocket = miServer.accept();
                System.out.println("Cliente conectado desde: " + miSocket.getInetAddress());

                // Flujo de entrada para recibir el objeto con los datos del mensaje
                ObjectInputStream entrada = new ObjectInputStream(miSocket.getInputStream());
                EnvioDatos datosRecibidos = (EnvioDatos) entrada.readObject();

                // Extraemos la información del mensaje recibido
                String nick = datosRecibidos.getNickUser();
                String ipFriend = datosRecibidos.getIpFriend();
                String texto = datosRecibidos.getTexto();
                
                // Mostramos el mensaje en el área de texto del servidor
                areatexto.append("\nNICK: " + nick + "\nMENSAJE: " + texto + "\nIP AMIGO: " + ipFriend);
                
                // Intentamos reenviar el mensaje al otro cliente
                try (Socket reenvioSocket = new Socket(ipFriend, 9090);
                     ObjectOutputStream paqueteReenvio = new ObjectOutputStream(reenvioSocket.getOutputStream())) {
                    
                    paqueteReenvio.writeObject(datosRecibidos);
                    paqueteReenvio.flush();  // Asegura que el mensaje se envía
                    System.out.println("Mensaje reenviado a " + ipFriend);
                } catch (IOException e) {
                    System.out.println("No se pudo reenviar el mensaje al cliente en " + ipFriend);
                    e.printStackTrace();
                }
                
                // Cerramos los flujos y sockets
                entrada.close();
                miSocket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private JTextArea areatexto; // Área de texto para mostrar mensajes en el servidor
}

