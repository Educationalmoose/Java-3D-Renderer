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
    double scale = 1.0;

    public Shape(Triangle[] triangles) {
        for (Triangle triangle : triangles) {
            this.triangles.add(triangle);
        }
        center = getCenter();
        boundingBox = getBoundingBox();
    }

    public Shape(Triangle[] triangles, double scale) {
        for (Triangle triangle : triangles) {
            this.triangles.add(triangle);
        }
        this.scale = scale;
        center = getCenter();
        boundingBox = getBoundingBox();
    }

    public double[] getBoundingBox() {
        double x1 = triangles.get(0).getVertices()[0].getViewX();
        double x2 = triangles.get(0).getVertices()[0].getViewX();
        double y1 = triangles.get(0).getVertices()[0].getViewY();
        double y2 = triangles.get(0).getVertices()[0].getViewY();
        
        for (Triangle t : triangles) {
            for (Vertex v: t.getVertices()) {
                if (v.getViewX() < x1)
                    x1 = v.getViewX();
                if (v.getViewX() > x2)
                    x2 = v.getViewX();

                if (v.getViewY() < y1)
                    y1 = v.getViewY();
                if (v.getViewY() > y2)
                    y2 = v.getViewY();
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

    public void scaleBy(double factor) {
        Set<Vertex> uniqueVertices = new HashSet<>();
        
        for (Triangle t : triangles) {
            for (Vertex v : t.getVertices()) {
                uniqueVertices.add(v);
            }
        }

        for (Vertex v : uniqueVertices) {
            v.translate(-center.getX(), -center.getY(), -center.getZ());
            v.scale(factor);
            v.translate(center.getX(), center.getY(), center.getZ());
        }
        this.scale *= factor;
        this.boundingBox = getBoundingBox();
    }

    public double getScale() {
        return this.scale;
    }

    public void updateView(double totalAngleX, double totalAngleY, double totalAngleZ) {
        for (Triangle t : triangles) {
            for (Vertex v : t.getVertices()) {
                v.resetView();

                v.setViewX(v.getViewX() - center.getX());
                v.setViewY(v.getViewY() - center.getY());
                v.setViewZ(v.getViewZ() - center.getZ());

                v.rotateViewX(totalAngleX);
                v.rotateViewY(totalAngleY);
                v.rotateViewZ(totalAngleZ);

                v.setViewX(v.getViewX() + center.getX());
                v.setViewY(v.getViewY() + center.getY());
                v.setViewZ(v.getViewZ() + center.getZ());
            }
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
