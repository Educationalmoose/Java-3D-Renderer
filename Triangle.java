import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Triangle {
    private Vertex v1;
    private Vertex v2;
    private Vertex v3;

    double[] boundingBox = new double[4];

    private Color color = Color.LIGHT_GRAY;

    public BufferedImage texture = null;

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

    public void drawWireframe(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.GREEN);
        java.awt.geom.Line2D.Double line1 = new java.awt.geom.Line2D.Double(v1.getProjX(), v1.getProjY(), v2.getProjX(), v2.getProjY());
        java.awt.geom.Line2D.Double line2 = new java.awt.geom.Line2D.Double(v2.getProjX(), v2.getProjY(), v3.getProjX(), v3.getProjY());
        java.awt.geom.Line2D.Double line3 = new java.awt.geom.Line2D.Double(v3.getProjX(), v3.getProjY(), v1.getProjX(), v1.getProjY());
        
        g2.draw(line1);
        g2.draw(line2);
        g2.draw(line3);
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
        // calculate barycentric coordinates to determine if (x,y) is inside the triangle formed by v1,v2,v3 in 2D projected space
        double d1 = (x - this.v2.getProjX()) * (this.v1.getProjY() - this.v2.getProjY()) - (this.v1.getProjX() - this.v2.getProjX()) * (y - this.v2.getProjY());
        double d2 = (x - this.v3.getProjX()) * (this.v2.getProjY() - this.v3.getProjY()) - (this.v2.getProjX() - this.v3.getProjX()) * (y - this.v3.getProjY());
        double d3 = (x - this.v1.getProjX()) * (this.v3.getProjY() - this.v1.getProjY()) - (this.v3.getProjX() - this.v1.getProjX()) * (y - this.v1.getProjY());

        double epsilon = -1e-5;

        boolean has_neg = (d1 < epsilon) || (d2 < epsilon) || (d3 < epsilon);
        boolean has_pos = (d1 > -epsilon) || (d2 > -epsilon) || (d3 > -epsilon);

        return !(has_neg && has_pos);
    }

    public boolean isOnEdge(double x, double y) {
        // calculate barycentric coordinates to determine if (x,y) is on the edge of the triangle formed by v1,v2,v3 in 2D projected space
        double d1 = (x - this.v2.getProjX()) * (this.v1.getProjY() - this.v2.getProjY()) - (this.v1.getProjX() - this.v2.getProjX()) * (y - this.v2.getProjY());
        double d2 = (x - this.v3.getProjX()) * (this.v2.getProjY() - this.v3.getProjY()) - (this.v2.getProjX() - this.v3.getProjX()) * (y - this.v3.getProjY());
        double d3 = (x - this.v1.getProjX()) * (this.v3.getProjY() - this.v1.getProjY()) - (this.v3.getProjX() - this.v1.getProjX()) * (y - this.v1.getProjY());

        return ((d1 == 0) || (d2 == 0) || (d3 == 0));
    }

    public boolean isOnEdgeThreshold(double x, double y, double threshold) {
        // Check if (x,y) is within a small distance of any of the triangle's edges in projected space
        //double threshold = 3.0; // pixels
        java.awt.geom.Line2D.Double edge1 = new java.awt.geom.Line2D.Double(v1.getProjX(), v1.getProjY(), v2.getProjX(), v2.getProjY());
        java.awt.geom.Line2D.Double edge2 = new java.awt.geom.Line2D.Double(v2.getProjX(), v2.getProjY(), v3.getProjX(), v3.getProjY());
        java.awt.geom.Line2D.Double edge3 = new java.awt.geom.Line2D.Double(v3.getProjX(), v3.getProjY(), v1.getProjX(), v1.getProjY());

        return edge1.ptSegDist(x, y) <= threshold || edge2.ptSegDist(x, y) <= threshold || edge3.ptSegDist(x, y) <= threshold;
    }

    public Vector getNormalVector() {
        Vector A = new Vector(v2.getX() - v1.getX(), v2.getY() - v1.getY(), v2.getZ() - v1.getZ());
        Vector B = new Vector(v3.getX() - v1.getX(), v3.getY() - v1.getY(), v3.getZ() - v1.getZ());
        return A.crossProduct(B);
    }

    public Vector getViewNormalVector() {
        Vector A = new Vector(v2.getViewX() - v1.getViewX(), v2.getViewY() - v1.getViewY(), v2.getViewZ() - v1.getViewZ());
        Vector B = new Vector(v3.getViewX() - v1.getViewX(), v3.getViewY() - v1.getViewY(), v3.getViewZ() - v1.getViewZ());
        return A.crossProduct(B);
    }

    public double[] getBoundingBox() {
        double minX = v1.getProjX();
        double maxX = v1.getProjX();
        double minY = v1.getProjY();
        double maxY = v1.getProjY();
        
        for (Vertex v : new Vertex[]{v2, v3}) {
            if (v.getProjX() < minX) minX = v.getProjX();
            if (v.getProjX() > maxX) maxX = v.getProjX();
            if (v.getProjY() < minY) minY = v.getProjY();
            if (v.getProjY() > maxY) maxY = v.getProjY();
        }

        return new double[]{minX, maxX, minY, maxY};
    }

    public double getZAt(double sx, double sy, boolean perspMode, double focalLength) {
        if (!perspMode) {
            Vector n = getViewNormalVector(); 
            
            double nz = n.getZ();
            if (Math.abs(nz) < 0.000001) {
                return (v1.getViewZ() + v2.getViewZ() + v3.getViewZ()) / 3.0;
            }
            double d = -(n.getX() * v1.getViewX() + n.getY() * v1.getViewY() + n.getZ() * v1.getViewZ());
            return -(n.getX() * sx + n.getY() * sy + d) / nz;
        } else {
            double px1 = v1.getProjX(), py1 = v1.getProjY(), vz1 = v1.getViewZ();
            double px2 = v2.getProjX(), py2 = v2.getProjY(), vz2 = v2.getViewZ();
            double px3 = v3.getProjX(), py3 = v3.getProjY(), vz3 = v3.getViewZ();

            double denom = (py2 - py3) * (px1 - px3) + (px3 - px2) * (py1 - py3);
            if (Math.abs(denom) < 0.000001) return (vz1 + vz2 + vz3) / 3.0;

            double l1 = ((py2 - py3) * (sx - px3) + (px3 - px2) * (sy - py3)) / denom;
            double l2 = ((py3 - py1) * (sx - px3) + (px1 - px3) * (sy - py3)) / denom;
            double l3 = 1.0 - l1 - l2;
            double w1 = focalLength + vz1;
            double w2 = focalLength + vz2;
            double w3 = focalLength + vz3;

            double invW = l1 / w1 + l2 / w2 + l3 / w3;
            if (Math.abs(invW) < 0.000001) return (vz1 + vz2 + vz3) / 3.0;

            return (l1 * vz1 / w1 + l2 * vz2 / w2 + l3 * vz3 / w3) / invW;
        }
    }

    @Override
    public String toString() {
        return "Triangle: " + v1.toString() + ", " + v2.toString() + ", " + v3.toString();
    }

}