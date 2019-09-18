package verDirectoriosCliente;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class verDirectoriosCliente {

    private Socket socket;
    private DataInputStream bufferDeEntrada = null;
    private DataOutputStream bufferDeSalida = null;
    Scanner teclado = new Scanner(System.in);
    final String COMANDO_TERMINACION = "salir";

    public void AbrirConexion(String ip, int puerto) {
        try {
            socket = new Socket(ip, puerto);
            mostrarTexto("Conectado a :" + socket.getInetAddress().getHostName());
        } catch (Exception e) {
            mostrarTexto("Excepción al Abrir conexión: " + e.getMessage());
            System.exit(0);
        }
    }

    public static void mostrarTexto(String s) {
        System.out.println(s);
    }

    public void abrirFlujos() {
        try {
            bufferDeEntrada = new DataInputStream(socket.getInputStream());
            bufferDeSalida = new DataOutputStream(socket.getOutputStream());
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("Error en la apertura de flujos");
        }
    }

    public void enviar(String s) {
        try {
            bufferDeSalida.writeUTF(s);
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("IOException on enviar");
        }
    }

    public String dir(String n) {
        String resultado = "";
        File carpeta = new File(System.getProperty("user.home") + "/Desktop" + "\\" + n);
        String[] listado = carpeta.list();
        if (listado == null || listado.length == 0) {
            System.out.println("No hay elementos dentro de la carpeta actual");
        } else {
            for (int i = 0; i < listado.length; i++) {
                resultado = resultado + "\n" + listado[i];
                
            }
        }
        return resultado;
    }

    public void cerrarConexion() {
        try {
            bufferDeEntrada.close();
            bufferDeSalida.close();
            socket.close();
            mostrarTexto("Conexión terminada");
        } catch (IOException e) {
            mostrarTexto("IOException on cerrarConexion()");
        } finally {
            System.exit(0);
        }
    }

    public void ejecutarConexion(String ip, int puerto) {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AbrirConexion(ip, puerto);
                    abrirFlujos();
                    recibirDatos();
                } finally {
                    cerrarConexion();
                }
            }
        });
        hilo.start();
    }

    public void recibirDatos() {
        String st = "";
        String n = "";
        try {
            do {
                st = (String) bufferDeEntrada.readUTF();
                mostrarTexto("\n Servidor => " + st);
                System.out.print("\n Cliente => ");

                if (st.equalsIgnoreCase("DIR")) {
                    
                    bufferDeSalida.writeUTF("Escribe el nombre de la carpeta:");
                    n = bufferDeEntrada.readUTF();
                    bufferDeSalida.writeUTF(dir(n));

                }
            } while (!st.equals(COMANDO_TERMINACION));
        } catch (IOException e) {
        }
    }

    public void escribirDatos() {
        String entrada = "";
        while (true) {
            System.out.print("Cliente => ");
            entrada = teclado.nextLine();
            if (entrada.length() > 0) {
                enviar(entrada);
            }
        }
    }

    public static void main(String[] args) {
        verDirectoriosCliente cliente = new verDirectoriosCliente();
        mostrarTexto("ESCRIBE DIR PARA VER EL CONTENIDO DE UNA CARPETA ");
        Scanner escaner = new Scanner(System.in);
        mostrarTexto("Ingresa la IP: 127.0.0.1 ");
        String ip = escaner.nextLine();
        if (ip.length() <= 0) {
            ip = "localhost";
        }

        mostrarTexto("Puerto: intente con 3000  ");
        String puerto = escaner.nextLine();
        if (puerto.length() <= 0) {
            puerto = "3000";
        }
        cliente.ejecutarConexion(ip, Integer.parseInt(puerto));
        cliente.escribirDatos();
    }

}
