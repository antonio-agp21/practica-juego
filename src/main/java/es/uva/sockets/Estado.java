package es.uva.sockets;

import java.util.ArrayList;

public class Estado {
    public final ArrayList<Jugador> jugadores;
    public Coordenadas tesoro;
    public final ArrayList<Coordenadas> buscadas;
    private final int size;
    private boolean terminado;

    public Estado(int size) {
        // Utilizamos el valor absoluto para que size sea siempre positivo.
        this.size = Math.abs(size);
        terminado = false;
        this.jugadores = new ArrayList<>();
        this.buscadas = new ArrayList<>();
        // TODO: Coordenadas aleatorias para el tesoro
        if (size < 0)
            this.tesoro = new Coordenadas(-1, -1);
        else
            this.tesoro = new Coordenadas((int) (Math.random() * size), (int) (Math.random() * size));
    }

    // Los métodos que modifican el estado son synchronized,
    // Esto quiere decir que un hilo debe esperar a que otro
    // hilo acabe de utilizarlo
    // Lo necesitamos ya que vamos a gestionar cada conexión
    // con un cliente en un hilo distinto
    public synchronized void terminar() {
        terminado = true;
    }

    public synchronized boolean estaTerminado() {
        return terminado;
    }

    public synchronized void nuevoJugador(Jugador jugador) {
        jugadores.add(jugador);
    }

    public synchronized void buscar(int id) {
        // TODO busca el tesoro el jugador con este id
        // Si se encuentra finaliza el juego
        int i;
        for (i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).id == id) {
                break;
            }
        }

        if (!buscadas.contains(jugadores.get(i).coordenadas)) {
            buscadas.add(jugadores.get(i).coordenadas);
            if (jugadores.get(i).coordenadas.equals(tesoro)) {
                // Finalizó el Juego
                terminar();
                return;
            }
        }

    }

    public synchronized void borrar(int id) {
        // Elimina el rastro de la casilla donde esté el jugador que la invoque, de modo
        // que si alguien había buscado en esa casilla, se borre de las casillas
        // buscadas

        // Corresponde a cuando el jugador pulsa la tecla B
        int i;
        for (i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).id == id) {
                break;
            }
        }
        buscadas.remove(jugadores.get(i).coordenadas);
    }

    public synchronized void mover(int id, Direccion dir) {
        // TODO mueve a el jugador id en la direccion dir
        int i;
        for (i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).id == id) {
                break;
            }
        }
        Coordenadas c = jugadores.get(i).coordenadas;
        if ((c.getX() == 0 && dir.equals(Direccion.valueOf("LEFT")))
                || (c.getY() == 0 && dir.equals(Direccion.valueOf("UP")))
                || (c.getX() == size - 1 && dir.equals(Direccion.valueOf("RIGHT")))
                || (c.getY() == size - 1 && dir.equals(Direccion.valueOf("DOWN"))))
            return;
        jugadores.get(i).coordenadas = jugadores.get(i).coordenadas.mover(dir);
    }

    public void mostrar() {
        // Limpiar pantalla
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println(" ");
        // Linea horizontal
        for (int col = 0; col < size; col++) {
            System.out.print("---");
        }
        System.out.println("-");
        for (int row = 0; row < size; row++) {
            // Columnas
            for (int col = 0; col < size; col++) {
                System.out.print("| "); // Cada celda representada por "| |"
                // Si no hay nada imprimimos vacio
                char toPrint = ' ';
                // Si ya esta cavado imprimos una x
                for (Coordenadas coordenadas : buscadas) {
                    if (coordenadas.equals(new Coordenadas(col, row))) {
                        toPrint = 'x';
                    }
                }
                // Si hay un jugador imprimimos su representacion
                for (Jugador jugador : jugadores) {
                    if (jugador.coordenadas.equals(new Coordenadas(col, row))) {
                        toPrint = jugador.getChar();
                    }
                }
                System.out.print(toPrint);
            }
            System.out.println("|"); // End of row with a final column border

            // Print a row separator
            for (int col = 0; col < size; col++) {
                System.out.print("---");
            }
            System.out.println("-");
        }
    }
}
