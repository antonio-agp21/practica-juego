package es.uva.sockets;

import java.io.Serializable;

public class Coordenadas {
    private final int x;
    private final int y;

    public Coordenadas(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordenadas mover(Direccion dir) {
        // TODO: Devolver unas coordenadas movidas según direccion
        int x0 = this.x;
        int y0 = this.y;
        switch (dir) {
            case UP:
                y0 -= 1;
                break;

            case DOWN:
                y0 += 1;
                break;
            case LEFT:
                x0 -= 1;
                break;
            case RIGHT:
                x0 += 1;
                break;

            default:
                break;
        }
        return new Coordenadas(x0, y0);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean equals(Coordenadas otras) {
        return (this.x == otras.x) && (this.y == otras.y);
    }
}
