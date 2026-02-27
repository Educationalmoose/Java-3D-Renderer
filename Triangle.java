import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class Triangle {
    private Vertex v1;
    private Vertex v2;
    private Vertex v3;

    private ArrayList<Vertex> trailv1 = new ArrayList<>();
    private ArrayList<Vertex> trailv2 = new ArrayList<>();
    private ArrayList<Vertex> trailv3 = new ArrayList<>();

    private Color color = Color.WHITE;
    private Color trailColor = Color.GREEN;

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public Vertex[] getVertices() {
        return new Vertex[]{v1, v2, v3};
    }

    public void draw(Graphics g) {
        g.setColor(color);
        int[] xPoints = {(int)v1.getX(), (int)v2.getX(), (int)v3.getX()};
        int[] yPoints = {(int)v1.getY(), (int)v2.getY(), (int)v3.getY()};
        g.fillPolygon(xPoints, yPoints, 3);
    }

    public void drawTrail(Graphics g) {
        g.setColor(trailColor);
        for (int i = 0; i < trailv1.size(); i++) {
            g.drawLine((int)trailv1.get(i).getX(), (int)trailv1.get(i).getY(), (int)trailv1.get(i).getX(), (int)trailv1.get(i).getY());
            g.drawLine((int)trailv2.get(i).getX(), (int)trailv2.get(i).getY(), (int)trailv2.get(i).getX(), (int)trailv2.get(i).getY());
            g.drawLine((int)trailv3.get(i).getX(), (int)trailv3.get(i).getY(), (int)trailv3.get(i).getX(), (int)trailv3.get(i).getY());
        }
    }

    public void changeColor(Color newColor) {
        this.color = newColor;
    }

    public Vertex getCenter() {
        double x = (v1.getX() + v2.getX() + v3.getX())/3;
        double y = (v1.getY() + v2.getY() + v3.getY())/3;
        double z = (v1.getZ() + v2.getZ() + v3.getZ())/3;
        return new Vertex(x, y, z);
    }

    public void rotateX(double angle) {
        Vertex center = getCenter();

        v1.translate(-center.getX(), -center.getY(), -center.getZ());
        v2.translate(-center.getX(), -center.getY(), -center.getZ());
        v3.translate(-center.getX(), -center.getY(), -center.getZ());

        v1.rotateX(angle);
        v2.rotateX(angle);
        v3.rotateX(angle);

        v1.translate(center.getX(), center.getY(), center.getZ());
        v2.translate(center.getX(), center.getY(), center.getZ());
        v3.translate(center.getX(), center.getY(), center.getZ());

        addTrail();
    }

    public void rotateX(double angle, Vertex center) {
        v1.translate(-center.getX(), -center.getY(), -center.getZ());
        v2.translate(-center.getX(), -center.getY(), -center.getZ());
        v3.translate(-center.getX(), -center.getY(), -center.getZ());

        v1.rotateX(angle);
        v2.rotateX(angle);
        v3.rotateX(angle);

        v1.translate(center.getX(), center.getY(), center.getZ());
        v2.translate(center.getX(), center.getY(), center.getZ());
        v3.translate(center.getX(), center.getY(), center.getZ());

        addTrail();
    }

    public void rotateY(double angle) {
        Vertex center = getCenter();

        v1.translate(-center.getX(), -center.getY(), -center.getZ());
        v2.translate(-center.getX(), -center.getY(), -center.getZ());
        v3.translate(-center.getX(), -center.getY(), -center.getZ());

        v1.rotateY(angle);
        v2.rotateY(angle);
        v3.rotateY(angle);

        v1.translate(center.getX(), center.getY(), center.getZ());
        v2.translate(center.getX(), center.getY(), center.getZ());
        v3.translate(center.getX(), center.getY(), center.getZ());

        addTrail();
    }

    public void rotateY(double angle, Vertex center) {
        v1.translate(-center.getX(), -center.getY(), -center.getZ());
        v2.translate(-center.getX(), -center.getY(), -center.getZ());
        v3.translate(-center.getX(), -center.getY(), -center.getZ());

        v1.rotateY(angle);
        v2.rotateY(angle);
        v3.rotateY(angle);

        v1.translate(center.getX(), center.getY(), center.getZ());
        v2.translate(center.getX(), center.getY(), center.getZ());
        v3.translate(center.getX(), center.getY(), center.getZ());

        addTrail();
    }

    public void rotateZ(double angle) {
        Vertex center = getCenter();

        v1.translate(-center.getX(), -center.getY(), -center.getZ());
        v2.translate(-center.getX(), -center.getY(), -center.getZ());
        v3.translate(-center.getX(), -center.getY(), -center.getZ());

        v1.rotateZ(angle);
        v2.rotateZ(angle);
        v3.rotateZ(angle);

        v1.translate(center.getX(), center.getY(), center.getZ());
        v2.translate(center.getX(), center.getY(), center.getZ());
        v3.translate(center.getX(), center.getY(), center.getZ());
        addTrail();
    }

    public void rotateZ(double angle, Vertex center) {
        v1.translate(-center.getX(), -center.getY(), -center.getZ());
        v2.translate(-center.getX(), -center.getY(), -center.getZ());
        v3.translate(-center.getX(), -center.getY(), -center.getZ());

        v1.rotateZ(angle);
        v2.rotateZ(angle);
        v3.rotateZ(angle);

        v1.translate(center.getX(), center.getY(), center.getZ());
        v2.translate(center.getX(), center.getY(), center.getZ());
        v3.translate(center.getX(), center.getY(), center.getZ());
        addTrail();
    }

    private void addTrail() {
        if (trailv1.size() > 1000) {
            trailv1.remove(0);
            trailv2.remove(0);
            trailv3.remove(0);
        }
        trailv1.add(new Vertex(v1.getX(), v1.getY(), v1.getZ()));
        trailv2.add(new Vertex(v2.getX(), v2.getY(), v2.getZ()));
        trailv3.add(new Vertex(v3.getX(), v3.getY(), v3.getZ()));
    }
}
