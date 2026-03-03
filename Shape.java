import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.awt.Color;
import java.awt.Graphics;

public class Shape {
    ArrayList<Triangle> triangles = new ArrayList<>();
    ArrayList<double[]> textureVertices = new ArrayList<>();
    ArrayList<Vertex> normalVertices = new ArrayList<>();
    Vertex center;
    double[] boundingBox = new double[4];

    public Shape(Triangle[] triangles) {
        for (Triangle triangle : triangles) {
            this.triangles.add(triangle);
        }
        center = getCenter();
        //System.out.println(center.toString());
        boundingBox = getBoundingBox();
    }

    public double[] getBoundingBox() {
        double x1 = triangles.get(0).getVertices()[0].getX();
        double x2 = triangles.get(0).getVertices()[0].getX();
        double y1 = triangles.get(0).getVertices()[0].getY();
        double y2 = triangles.get(0).getVertices()[0].getY();
        
        for (Triangle t : triangles) {
            for (Vertex v: t.getVertices()) {
                if (v.getX() < x1)
                    x1 = v.getX();
                if (v.getX() > x2)
                    x2 = v.getX();

                if (v.getY() < y1)
                    y1 = v.getY();
                if (v.getY() > y2)
                    y2 = v.getY();
            }
        }

        return new double[]{x1, x2, y1, y2};
    }

    public Triangle[] getTriangles() {
        Triangle[] ts = new Triangle[triangles.size()];
        for(int i = 0; i < triangles.size(); i++) {
            ts[i] = triangles.get(i);
        }
        return ts;
    }

    public void drawBoundingBox(Graphics g) {
        g.setColor(Color.RED);
        g.drawLine((int)boundingBox[0], (int)boundingBox[2], (int)boundingBox[0], (int)boundingBox[3]);
        g.drawLine((int)boundingBox[0], (int)boundingBox[2], (int)boundingBox[1], (int)boundingBox[2]);
        g.drawLine((int)boundingBox[1], (int)boundingBox[2], (int)boundingBox[1], (int)boundingBox[3]);
        g.drawLine((int)boundingBox[1], (int)boundingBox[3], (int)boundingBox[0], (int)boundingBox[3]);
    }

    public Vertex getCenter() {
        Vertex center = new Vertex(0, 0, 0);
        for (int i = 0; i < triangles.size(); i++) {
            center.add(triangles.get(i).getCenter());
        }
        double cx = center.getX() / triangles.size();
        double cy = center.getY() / triangles.size();
        double cz = center.getZ() / triangles.size();
        return new Vertex(cx, cy, cz);
    }

    public void RotateX(double angle) {
        Set<Vertex> uniqueVertices = new HashSet<>();
        
        for (Triangle t : triangles) {
            uniqueVertices.add(t.getVertices()[0]);
            uniqueVertices.add(t.getVertices()[1]);
            uniqueVertices.add(t.getVertices()[2]);
        }

        for (Vertex v : uniqueVertices) {
            v.translate(-center.getX(), -center.getY(), -center.getZ());
            v.rotateX(angle);
            v.translate(center.getX(), center.getY(), center.getZ());
        }
    
        this.boundingBox = getBoundingBox();
    }

    public void RotateY(double angle) {
        Set<Vertex> uniqueVertices = new HashSet<>();
        
        for (Triangle t : triangles) {
            uniqueVertices.add(t.getVertices()[0]);
            uniqueVertices.add(t.getVertices()[1]);
            uniqueVertices.add(t.getVertices()[2]);
        }

        for (Vertex v : uniqueVertices) {
            v.translate(-center.getX(), -center.getY(), -center.getZ());
            v.rotateY(angle);
            v.translate(center.getX(), center.getY(), center.getZ());
        }
    
        this.boundingBox = getBoundingBox();
    }

    public void RotateZ(double angle) {
        Set<Vertex> uniqueVertices = new HashSet<>();
        
        for (Triangle t : triangles) {
            uniqueVertices.add(t.getVertices()[0]);
            uniqueVertices.add(t.getVertices()[1]);
            uniqueVertices.add(t.getVertices()[2]);
        }

        for (Vertex v : uniqueVertices) {
            v.translate(-center.getX(), -center.getY(), -center.getZ());
            v.rotateZ(angle);
            v.translate(center.getX(), center.getY(), center.getZ());
        }
    
        this.boundingBox = getBoundingBox();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Shape with ").append(triangles.size()).append(" triangles\n");
        return sb.toString();
    }
}
