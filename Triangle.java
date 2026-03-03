import java.awt.Color;
import java.awt.Graphics;

public class Triangle {
    private Vertex v1;
    private Vertex v2;
    private Vertex v3;

    double[] boundingBox = new double[4];

    private Color color = Color.GREEN;

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.boundingBox = getBoundingBox();
    }

    public Vertex[] getVertices() {
        return new Vertex[]{v1, v2, v3};
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

    public boolean isInside(double x, double y) {
        double d1 = (x - this.v2.getViewX()) * (this.v1.getViewY() - this.v2.getViewY()) - (this.v1.getViewX() - this.v2.getViewX()) * (y - this.v2.getViewY());
        double d2 = (x - this.v3.getViewX()) * (this.v2.getViewY() - this.v3.getViewY()) - (this.v2.getViewX() - this.v3.getViewX()) * (y - this.v3.getViewY());
        double d3 = (x - this.v1.getViewX()) * (this.v3.getViewY() - this.v1.getViewY()) - (this.v3.getViewX() - this.v1.getViewX()) * (y - this.v1.getViewY());

        boolean has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    public Vector getNormalVector() {
        Vertex v1View = new Vertex(v1.getViewX(), v1.getViewY(), v1.getViewZ());
        Vertex v2View = new Vertex(v2.getViewX(), v2.getViewY(), v2.getViewZ());
        Vertex v3View = new Vertex(v3.getViewX(), v3.getViewY(), v3.getViewZ());

        Vector A = new Vector(v2View, v1View);
        Vector B = new Vector(v3View, v1View);
        return A.crossProduct(B);
    }

    public double[] getBoundingBox() {
        double minX = v1.getViewX();
        double maxX = v1.getViewX();
        double minY = v1.getViewY();
        double maxY = v1.getViewY();
        
        for (Vertex v : new Vertex[]{v2, v3}) {
            if (v.getViewX() < minX) minX = v.getViewX();
            if (v.getViewX() > maxX) maxX = v.getViewX();
            if (v.getViewY() < minY) minY = v.getViewY();
            if (v.getViewY() > maxY) maxY = v.getViewY();
        }

        return new double[]{minX, maxX, minY, maxY};
    }

    public double getZAt(double x, double y) {
        Vector n = getNormalVector();
        double nz = n.getZ();
        
        if (Math.abs(nz) < 0.000001) {
            return (v1.getViewZ() + v2.getViewZ() + v3.getViewZ()) / 3.0;
        }

        double d = -(n.getX() * v1.getViewX() + n.getY() * v1.getViewY() + n.getZ() * v1.getViewZ());
        return -(n.getX() * x + n.getY() * y + d) / nz;
    }

    @Override
    public String toString() {
        return "Triangle: " + v1.toString() + ", " + v2.toString() + ", " + v3.toString();
    }

}
