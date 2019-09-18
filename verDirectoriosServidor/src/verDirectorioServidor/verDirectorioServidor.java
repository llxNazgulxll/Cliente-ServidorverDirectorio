package verDirectorioServidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class verDirectorioServidor {

    private Socket socket;
    private ServerSocket serverSocket;
    private DataInputStream bufferDeEntrada = null;
    private DataOutputStream bufferDeSalida = null;
    Scanner escaner = new Scanner(System.in);
    final String COMANDO_TERMINACION = "salir";

    public void AbrirConexion(int puerto) {
        try {
            serverSocket = new ServerSocket(puerto);
            mostrarTexto("Esperando conexión entrante en el puerto " + String.valueOf(puerto) + "...");
            socket = serverSocket.accept();
            mostrarTexto("Conexión establecida con: " + socket.getInetAddress().getHostName() + "\n\n\n");
        } catch (Exception e) {
            mostrarTexto("Error en Abrir Conexion(): " + e.getMessage());
            System.exit(0);
        }
    }

    public void flujos() {
        try {
            bufferDeEntrada = new DataInputStream(socket.getInputStream());
            bufferDeSalida = new DataOutputStream(socket.getOutputStream());
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("Error en la apertura de flujos");
        }
    }

    public String dir(String n) {
        String resultado = "";
        File carpeta = new File(System.getProperty("user.home") + "/Desktop"+"\\"+n);
        String[] listado = carpeta.list();
        if (listado == null || listado.length == 0) {
            System.out.println("No hay elementos dentro de la carpeta actual");
        } else {
            for (int i = 0; i < listado.length; i++) {
                resultado = resultado + "\n" + listado[i];
                //System.out.println(listado[i]);
            }
        }
        return resultado;
    }

    public void recibirDatos() {
        String st = "";
        String n = "";
        Scanner scanner = new Scanner(System.in);
        try {
            do {
                st = (String) bufferDeEntrada.readUTF();
                mostrarTexto("\n Cliente => " + st);
                System.out.print("\n Servidor => ");

                if (st.equalsIgnoreCase("DIR")) {
                    
                    bufferDeSalida.writeUTF("Escribe el nombre de la carpeta:");
                    n = bufferDeEntrada.readUTF();
                    bufferDeSalida.writeUTF(dir(n));

                }

            } while (!st.equals(COMANDO_TERMINACION));
        } catch (IOException e) {
            cerrarConexion();
        }
    }

    public void enviar(String s) {
        try {
            bufferDeSalida.writeUTF(s);
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("Error en enviar(): " + e.getMessage());
        }
    }

    public static void mostrarTexto(String s) {
        System.out.print(s);
    }

    public void escribirDatos() {
        while (true) {
            System.out.print("Servidor => ");
            enviar(escaner.nextLine());
        }
    }

    public void cerrarConexion() {
        try {
            bufferDeEntrada.close();
            bufferDeSalida.close();
            socket.close();
        } catch (IOException e) {
            mostrarTexto("Excepción en cerrarConexion(): " + e.getMessage());
        } finally {
            mostrarTexto("Conversación finalizada....");
            System.exit(0);

        }
    }

    public void ejecutarConexion(int puerto) {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        AbrirConexion(puerto);
                        flujos();
                        recibirDatos();
                    } finally {
                        cerrarConexion();
                    }
                }
            }
        });
        hilo.start();
    }

    public static void main(String[] args) throws IOException {

        verDirectorioServidor s = new verDirectorioServidor();
        Scanner sc = new Scanner(System.in);
        mostrarTexto("ESCRIBE DIR PARA VER EL CONTENIDO DE UNA CARPETA "+"\n");
        mostrarTexto("Ingresa el puerto: Intente con 3000 ");
        String puerto = sc.nextLine();
        if (puerto.length() <= 0) {
            puerto = "3000";
        }
        s.ejecutarConexion(Integer.parseInt(puerto));
        s.escribirDatos();
    }

}
