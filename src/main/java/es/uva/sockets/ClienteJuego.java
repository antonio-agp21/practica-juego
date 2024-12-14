package es.uva.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClienteJuego {
    // La clase cliente tiene las siguientes responsabilidades
    // Unirse al juego conectandose al servidor
    // Mantener un estado de juego actualizado interpretando los
    // mensajes del servidor (y mostrar el estado)
    // Convertir input del jugador en un mensaje que enviar al servidor
    // NOTA: para simplificar el manejo de input podemos considerar
    // que el usario manda cada comando en una linea distinta
    // (aunque sea muy incomodo)

    public final Estado estado;
    // TODO: Faltarán atributos ...
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final static Logger logger = LoggerFactory.getLogger(ClienteJuego.class);

    public ClienteJuego(int size) {
        // [OPCIONAL] TODO: Extiende el protocolo de comunicacion para
        // que el servidor envie el tamaño del mapa tras la conexion
        // de manera que el estado no se instancie hasta entonces
        // y conocer este parametro a priori no sea necesario.

        // Pasamos un valor negativo de size para que nunca se encuentre el tesoro del
        // estado de ClienteJuego (tesoro ficticio)
        estado = new Estado(-size);
    }

    public void iniciar(String host, int puerto) {
        // Metodo que reune todo y mantiene lo necesario en un bucle
        conectar(host, puerto);
        Thread procesadorMensajesServidor = new Thread(() -> {
            while (!estado.estaTerminado()) {
                procesarMensajeServidor();
            }
        });
        Thread procesadorInput = new Thread(() -> {
            while (!estado.estaTerminado()) {
                procesarInput();
            }
        });
        procesadorMensajesServidor.start();
        procesadorInput.start();
        try {
            procesadorInput.join();
            procesadorMensajesServidor.join();
        } catch (InterruptedException e) {
            // Si acaban los hilos es que el juego terminó
            logger.debug("El juego terminó");
            cerrarConexion();
        }
    }

    public void cerrarConexion() {
        // TODO: cierra todos los recursos asociados a la conexion con el servidor
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            logger.warn("Error cerrando conexión");
        }
        logger.info("Conexión cerrada");

    }

    public void conectar(String host, int puerto) {
        // TODO: iniciar la conexion con el servidor
        // (Debe guardar la conexion en un atributo)
        try {
            socket = new Socket(host, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            logger.warn("Error conectando: " + e.getMessage());
        } catch (IOException ex) {
            logger.warn("Error conectando: " + ex.getMessage());
        }

    }

    public void procesarInput() {
        // TODO: Comprueba la entrada estandar y
        // se procesa mediante interpretar input,
        // Se genera un mensaje que se envia al servidor
        String str = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (str.isEmpty()) {
                str = br.readLine();
                final String validos = "aAwWsSdDqQbB";
                // B para borrar las búsquedas en la casilla en la que te encuentres
                if (str.isEmpty() || validos.indexOf(str.substring(0, 1)) == -1) {
                    str = "";
                } else {
                    break;
                }
            }
            out.println(interpretarInput(str.toUpperCase().toCharArray()[0]));
        } catch (IOException e) {
            logger.warn("Error procesando el input: " + e.getMessage());
        }
    }

    public void procesarMensajeServidor() {
        // TODO: Comprueba la conexion y obtiene un mensaje
        // que se procesa con interpretarMensaje
        // Al recibir la actualizacion del servidor podeis
        // Usar el metodo mostrar del estado
        // Para enseñarlo

        try {
            String mensaje = in.readLine();
            interpretarMensaje(mensaje);
            estado.mostrar();
            if (mensaje.contains("SUCCESS")) {
                logger.info("Juego finalizado. Cerrando conexión...");
                cerrarConexion();
            }
        } catch (IOException e) {
            logger.warn("Ya no estás conectado, finalizando");
            cerrarConexion();
        }
    }

    public String interpretarInput(char tecla) {
        // TODO: WASD para moverse, Q para buscar
        // Este metodo debe devolver el comando necesario
        // Que enviar al servidor

        // La B significa borrar el rastro de haber buscado el tesoro en la casilla
        // actual

        switch (tecla) {
            case 'W':
                return "MOVE UP";
            case 'A':
                return "MOVE LEFT";
            case 'S':
                return "MOVE DOWN";
            case 'D':
                return "MOVE RIGHT";
            case 'B':
                return "ERASE";
            default:
                return "DIG";
        }
    }

    public void interpretarMensaje(String mensaje) {
        // TODO: interpretar los mensajes del servidor actualizando el estado
        String[] strs = mensaje.split(" ");

        if (strs[0].contains("MOVE")) {
            Direccion dir = Direccion.valueOf(strs[1].replaceAll("[^a-zA-Z0-9]", ""));
            int id = Integer.parseInt(strs[2].replaceAll("[^a-zA-Z0-9]", ""));
            this.estado.mover(id, dir);
            return;
        }
        if (strs[0].contains("DIG")) {

            if (strs[2].contains("SUCCESS")) {
                this.estado.terminar();
                return;
            }

            int id = Integer.parseInt(strs[1].replaceAll("[^a-zA-Z0-9]", ""));
            this.estado.buscar(id);
            return;
        }
        if (strs[1].contains("JOIN")) {
            int id = Integer.parseInt(strs[2].replaceAll("[^a-zA-Z0-9]", ""));
            int x = Integer.parseInt(strs[3].replaceAll("[^a-zA-Z0-9]", ""));
            int y = Integer.parseInt(strs[4].replaceAll("[^a-zA-Z0-9]", ""));
            this.estado.nuevoJugador(new Jugador(id, new Coordenadas(x, y)));
        }

        if (strs[0].contains("ERASE")) {
            int id = Integer.parseInt(strs[1].replaceAll("[^a-zA-Z0-9]", ""));
            this.estado.borrar(id);
            return;
        }

    }
}
