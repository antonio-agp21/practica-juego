package es.uva.sockets;

import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServidorJuego {
    // El juego consiste en encontrar un tesoro
    // en un mapa cuadriculado, cuando un jugador
    // se conecta aparece en un cuadrado aleatorio
    // no ocupado.
    // El _PROTOCOLO_ del cliente, la manera en que
    // se comunica con el servidor es el siguiente
    // MOVE UP|DOWN|LEFT|RIGHT
    // DIG
    // EL Servidor Verifica la validez de los movimientos
    // Los aplica sobre su estado y envía la actualización
    // A todos los jugadores.
    // EL _PROTOCOLO_ con el que el servidor comunica las
    // actualizaciones a los clientes es el siguiente
    // PLAYER JOIN <PLAYER-ID> <X> <Y>
    // MOVE UP|DOWN|LEFT|RIGHT <PLAYER-ID>
    // DIG <PLAYER-ID> <SUCCESS>
    // El delimitador de lo que constituye un mensaje es
    // un caracter de salto de linea

    public final Estado estado;
    public final ServerSocket serverSocket;
    private final List<ManagerCliente> clientes;
    private final static Logger logger = LoggerFactory.getLogger(ServidorJuego.class);

    public ServidorJuego(int size, int puerto) throws IOException {
        estado = new Estado(size);
        clientes = new ArrayList<>();
        // Crear un serverSocket que acepte
        // conexiones de VARIOS clientes
        serverSocket = new ServerSocket(puerto);
    }

    public void iniciar() throws IOException {
        while (!estado.estaTerminado()) {
            ManagerCliente nuevo = aceptarConexion();
            nuevo.start();
            logger.debug("Nuevo jugador añadido correctamente");
        }
    }

    public ManagerCliente aceptarConexion() throws IOException {
        // TODO: Usando el serverSocket
        // Al añadir un nuevo jugador se le deben enviar
        // la posicion de los jugadores existentes, aunque no
        // sabe donde han estado buscando.

        int id = estado.jugadores.size();
        estado.nuevoJugador(new Jugador(id, new Coordenadas(0, 0)));
        ManagerCliente nuevoManager = new ManagerCliente(serverSocket.accept(), this, id);
        for (int i = 0; i < estado.jugadores.size(); i++) {
            // Envía la posición de los jugadores actuales
            nuevoManager.enviarMensaje("PLAYER JOIN <"
                    + (estado.jugadores.get(i).id + "> <" + estado.jugadores.get(i).coordenadas.getX() + "> <"
                            + estado.jugadores.get(i).coordenadas.getY() + ">"));

        }
        broadcast("PLAYER JOIN <" + (id) + "> <0> <0>--");
        clientes.add(nuevoManager);
        return nuevoManager;

    }

    public synchronized void broadcast(String message) {
        // TODO: Enviar un mensaje a todos los clientes
        for (int i = 0; i < clientes.size(); i++) {
            clientes.get(i).enviarMensaje(message);
            logger.info(message);
            estado.mostrar();
        }
    }

}
