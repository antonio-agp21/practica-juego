package es.uva.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerCliente extends Thread {
    // Clase para que el encargado de cada cliente
    // Se ejecute en un hilo diferente

    private final Socket socket;
    private final ServidorJuego servidor;
    private final int idJugador;
    private BufferedReader in;
    private PrintWriter out;
    private final static Logger logger = LoggerFactory.getLogger(ManagerCliente.class);
    // Se pueden usar mas atributos ...

    public ManagerCliente(Socket socket, ServidorJuego servidor, int idJugador) {
        this.socket = socket;
        this.servidor = servidor;
        this.idJugador = idJugador;
        System.out.println("Nuevo managerCliente...");
        try {
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

        } catch (IOException e) {
            logger.warn("Error inicializando ManagerCliente");
        }
        // Se pueden usar mas atributos ...
        logger.info("Nuevo managerCliente creado");
    }

    public void enviarMensaje(String message) {
        // TODO: enviar un mensaje. NOTA: a veces hace falta usar flush.
        out.flush();
        out.println(message);
    }

    @Override
    public void run() {
        // Mantener todos los procesos necesarios hasta el final
        // de la partida (alguien encuentra el tesoro)
        while (!servidor.estado.estaTerminado() && !socket.isClosed()) {
            procesarMensajeCliente();
        }
    }

    public void procesarMensajeCliente() {
        // TODO: leer el mensaje del cliente
        // y procesarlo usando interpretarMensaje
        // Si detectamos el final del socket
        // gestionar desconexion ...
        try {
            interpretarMensaje(in.readLine());

        } catch (IOException e) {
            logger.info("Fin de la conexión");
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException ex) {
                logger.warn("Error cerrando conexión: " + e.getMessage());
            }
        }
    }

    public void interpretarMensaje(String mensaje) {
        // TODO: Esta función debe realizar distintas
        // Acciones según el mensaje recibido
        // Manipulando el estado del servidor
        // Si el mensaje recibido no tiene el formato correcto
        // No ocurre nada
        Direccion dir;
        try {
            if (mensaje.contains("DIG")) {
                servidor.estado.buscar(idJugador);
                if (servidor.estado.estaTerminado()) {
                    servidor.broadcast("DIG <" + idJugador + "> <SUCCESS>");
                } else {
                    servidor.broadcast("DIG <" + idJugador + "> <FAIL>");
                }
                return;
            }
            if (mensaje.contains("MOVE")) {
                dir = Direccion.valueOf(mensaje.split(" ")[1]);
                servidor.estado.mover(idJugador, dir);
                servidor.broadcast("MOVE " + dir + " <" + idJugador + ">");
                return;
            }
            if (mensaje.contains("ERASE")) {
                // Si el mensaje es ERASE:
                servidor.estado.borrar(idJugador);
                servidor.broadcast(
                        "ERASE <" + idJugador + "> " + servidor.estado.jugadores.get(idJugador).coordenadas.getX() + " "
                                + servidor.estado.jugadores.get(idJugador).coordenadas.getX() + " "
                                + servidor.estado.buscadas.size());
                return;
            }

        } catch (IndexOutOfBoundsException e) {
            logger.warn("Error interpretando el mensaje del cliente: " + e.getMessage());
        }
    }
}