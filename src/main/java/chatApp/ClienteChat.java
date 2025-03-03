package chatApp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClienteChat {
    public static void main(String[] args) {
        Marco miMarco = new Marco();
        miMarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

class Marco extends JFrame {
    public Marco() {
        setBounds(600, 300, 350, 400);
        add(new Lamina()); // Agregamos la lámina con los componentes
        setVisible(true);
    }
}

class Lamina extends JPanel implements Runnable {
    
    private JTextField texto, nickUser, ipFriend;
    private JButton miboton;
    private JTextArea area;
    private ObjectOutputStream salida;
    private Socket miSocket;

    public Lamina() {
        try {
            miSocket = new Socket("192.168.1.115", 50500);
            salida = new ObjectOutputStream(miSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread hiloThread = new Thread(this);
        hiloThread.start();

        // Interfaz gráfica
        nickUser = new JTextField(5);
        add(nickUser);

        JLabel etiquetaChat = new JLabel("CHAT");
        add(etiquetaChat);

        ipFriend = new JTextField(10);
        add(ipFriend);

        area = new JTextArea(16, 30);
        area.setEditable(false);
        add(area);

        texto = new JTextField(20);
        add(texto);

        miboton = new JButton("Enviar");
        miboton.addActionListener(e -> enviarMensaje());

        add(miboton);
    }

    private void enviarMensaje() {
        try {
            EnvioDatos datos = new EnvioDatos();
            datos.setIpFriend(ipFriend.getText());
            datos.setNickUser(nickUser.getText());
            datos.setTexto(texto.getText());

            salida.writeObject(datos);
            salida.flush();  // Asegura que se envía el mensaje inmediatamente

            texto.setText("");

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try (ServerSocket escuchaCliente = new ServerSocket(9090)) {
            while (true) {
                Socket cliente = escuchaCliente.accept();
                ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
                EnvioDatos datosRecibidos = (EnvioDatos) entrada.readObject();
                area.append("\n" + datosRecibidos.getNickUser() + ": " + datosRecibidos.getTexto());
                entrada.close();
                cliente.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}


// Clase que representa el objeto que se enviará serializado
class EnvioDatos implements Serializable {
    private static final long serialVersionUID = 1L; // Versión de serialización
    
    private String nickUser;
    private String ipFriend;
    private String texto;
    
    public String getNickUser() {
        return nickUser;
    }
    
    public void setNickUser(String nickUser) {
        this.nickUser = nickUser;
    }
    
    public String getIpFriend() {
        return ipFriend;
    }
    
    public void setIpFriend(String ipFriend) {
        this.ipFriend = ipFriend;
    }
    
    public String getTexto() {
        return texto;
    }
    
    public void setTexto(String texto) {
        this.texto = texto;
    }
}
