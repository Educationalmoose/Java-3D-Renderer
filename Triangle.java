import java.awt.Color;
import java.awt.Graphics;

public class Triangle {
    private Vertex v1;
    private Vertex v2;
    private Vertex v3;

    private Color color = Color.WHITE;

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public void draw(Graphics g /*, Color color*/) {
        g.setColor(color);
        int[] xPoints = {v1.getX(), v2.getX(), v3.getX()};
        int[] yPoints = {v1.getY(), v2.getY(), v3.getY()};
        g.fillPolygon(xPoints, yPoints, 3);
    }

    public void changeColor(Color newColor) {
        this.color = newColor;
    }

    public void rotateX(double angle) {
        v1 = v1.rotateX(angle);
        v2 = v2.rotateX(angle);
        v3 = v3.rotateX(angle);
    }

    public void rotateY(double angle) {
        v1 = v1.rotateY(angle);
        v2 = v2.rotateY(angle);
        v3 = v3.rotateY(angle);
    }

    public void rotateZ(double angle) {
        v1 = v1.rotateZ(angle);
        v2 = v2.rotateZ(angle);
        v3 = v3.rotateZ(angle);
    }
}
