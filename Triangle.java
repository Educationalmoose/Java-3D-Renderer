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

    double[] boundingBox = new double[4];

    private Color color = Color.GREEN;
    private Color trailColor = Color.WHITE;
    

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.boundingBox = getBoundingBox();
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

    public void drawBoundingBox(Graphics g) {
        g.setColor(Color.RED);
        g.drawLine((int)boundingBox[0], (int)boundingBox[2], (int)boundingBox[0], (int)boundingBox[3]);
        g.drawLine((int)boundingBox[0], (int)boundingBox[2], (int)boundingBox[1], (int)boundingBox[2]);
        g.drawLine((int)boundingBox[1], (int)boundingBox[2], (int)boundingBox[1], (int)boundingBox[3]);
        g.drawLine((int)boundingBox[1], (int)boundingBox[3], (int)boundingBox[0], (int)boundingBox[3]);
    }

    public void setColor(Color newColor) {
        this.color = newColor;
    }

    public Color getColor() {
        return this.color;
    }

    public Vertex getCenter() {
        double x = (v1.getX() + v2.getX() + v3.getX())/3;
        double y = (v1.getY() + v2.getY() + v3.getY())/3;
        double z = (v1.getZ() + v2.getZ() + v3.getZ())/3;
        return new Vertex(x, y, z);
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

    public boolean isInside(double x, double y) {
        double d1 = (x - this.v2.getX()) * (this.v1.getY() - this.v2.getY()) - (this.v1.getX() - this.v2.getX()) * (y - this.v2.getY());
        double d2 = (x - this.v3.getX()) * (this.v2.getY() - this.v3.getY()) - (this.v2.getX() - this.v3.getX()) * (y - this.v3.getY());
        double d3 = (x - this.v1.getX()) * (this.v3.getY() - this.v1.getY()) - (this.v3.getX() - this.v1.getX()) * (y - this.v1.getY());

        boolean has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    public Vector getNormalVector() {
        Vector A = new Vector(v2, v1);
        Vector B = new Vector(v3, v1);
        return A.crossProduct(B);
    }

    public double[] getBoundingBox() {
        double minX = v1.getX();
        double maxX = v1.getX();
        double minY = v1.getY();
        double maxY = v1.getY();
        
        for (Vertex v : new Vertex[]{v2, v3}) {
            if (v.getX() < minX) minX = v.getX();
            if (v.getX() > maxX) maxX = v.getX();
            if (v.getY() < minY) minY = v.getY();
            if (v.getY() > maxY) maxY = v.getY();
        }

        return new double[]{minX, maxX, minY, maxY};
    }

    public double getZAt(double x, double y) {
        Vector n = getNormalVector();
        double nz = n.getZ();
        
        if (Math.abs(nz) < 0.000001) {
            return (v1.getZ() + v2.getZ() + v3.getZ()) / 3.0;
        }

        double d = -(n.getX() * v1.getX() + n.getY() * v1.getY() + n.getZ() * v1.getZ());
        return -(n.getX() * x + n.getY() * y + d) / nz;
    }



}
