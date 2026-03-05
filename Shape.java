import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Shape {
    ArrayList<Triangle> triangles = new ArrayList<>();
    ArrayList<double[]> textureVertices = new ArrayList<>();
    ArrayList<Vertex> normalVertices = new ArrayList<>();
    Vertex center;
    double[] boundingBox = new double[4];
    double[] localSelectionBox = new double[6];
    Vertex[] selectionBoxCorners = new Vertex[8];
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
        double x1 = triangles.get(0).getVertices()[0].getProjX();
        double x2 = triangles.get(0).getVertices()[0].getProjX();
        double y1 = triangles.get(0).getVertices()[0].getProjY();
        double y2 = triangles.get(0).getVertices()[0].getProjY();
        
        for (Triangle t : triangles) {
            for (Vertex v: t.getVertices()) {
                if (v.getProjX() < x1) x1 = v.getProjX();
                if (v.getProjX() > x2) x2 = v.getProjX();
                if (v.getProjY() < y1) y1 = v.getProjY();
                if (v.getProjY() > y2) y2 = v.getProjY();
            }
        }
        this.boundingBox = new double[]{x1, x2, y1, y2};
        return boundingBox;
    }

    public double[] getLocalSelectionBox() {
        double x1 = triangles.get(0).getVertices()[0].getX();
        double x2 = x1;
        double y1 = triangles.get(0).getVertices()[0].getY();
        double y2 = y1;
        double z1 = triangles.get(0).getVertices()[0].getZ();
        double z2 = z1;

        for (Triangle t : triangles) {
            for (Vertex v : t.getVertices()) {
                if (v.getX() < x1) x1 = v.getX();
                if (v.getX() > x2) x2 = v.getX();
                if (v.getY() < y1) y1 = v.getY();
                if (v.getY() > y2) y2 = v.getY();
                if (v.getZ() < z1) z1 = v.getZ();
                if (v.getZ() > z2) z2 = v.getZ();
            }
        }

        return new double[]{x1, x2, y1, y2, z1, z2};
    }

    public Triangle[] getTriangles() {
        Triangle[] ts = new Triangle[triangles.size()];
        for(int i = 0; i < triangles.size(); i++) {
            ts[i] = triangles.get(i);
        }
        return ts;
    }

    public void drawBoundingBox(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.RED);
        
        double x1 = boundingBox[0];
        double x2 = boundingBox[1];
        double y1 = boundingBox[2];
        double y2 = boundingBox[3];

        java.awt.geom.Rectangle2D.Double rect = new java.awt.geom.Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
        g2.draw(rect);
    }

    public void drawLocalSelectionBox(Graphics g) {
        if (selectionBoxCorners[0] == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);

        int[][] edges = {
            {0,1}, {1,3}, {3,2}, {2,0},
            {4,5}, {5,7}, {7,6}, {6,4},
            {0,4}, {1,5}, {2,6}, {3,7}
        };

        for (int[] edge : edges) {
            Vertex v1 = selectionBoxCorners[edge[0]];
            Vertex v2 = selectionBoxCorners[edge[1]];
            g2.draw(new java.awt.geom.Line2D.Double(
                v1.getProjX(), v1.getProjY(),
                v2.getProjX(), v2.getProjY()
            ));
        }
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

    public void translateShape(double dx, double dy, double dz) {
        java.util.Set<Vertex> uniqueVertices = new java.util.HashSet<>();
        for (Triangle t : triangles) {
            for (Vertex v : t.getVertices()) {
                uniqueVertices.add(v);
            }
        }

        for (Vertex v : uniqueVertices) {
            v.translate(dx, dy, dz);
        }

        this.center = getCenter();
    }
    
    public double getScale() {
        return this.scale;
    }

    public void updateView(double totalAngleX, double totalAngleY, double totalAngleZ, boolean perspectiveMode, double focalLength, double camX, double camY, double camZ) {
        center.resetView();
        center.setViewX(center.getX() - camX);
        center.setViewY(center.getY() - camY);
        center.setViewZ(center.getZ() - camZ);
        center.rotateViewY(totalAngleY);
        center.rotateViewX(totalAngleX);
        center.rotateViewZ(totalAngleZ);

        for (Triangle t : triangles) {
            for (Vertex v : t.getVertices()) {
                v.resetView();
                v.setViewX(v.getX() - camX);
                v.setViewY(v.getY() - camY);
                v.setViewZ(v.getZ() - camZ);

                v.rotateViewY(totalAngleY);
                v.rotateViewX(totalAngleX);
                v.rotateViewZ(totalAngleZ);

                if (perspectiveMode) {
                    double denom = Math.max(1.0, focalLength + v.getViewZ());
                    v.setProjX(v.getViewX() * focalLength / denom);
                    v.setProjY(v.getViewY() * focalLength / denom);
                } else {
                    v.setProjX(v.getViewX());
                    v.setProjY(v.getViewY());
                }
            }
        }
        double[] b = getLocalSelectionBox(); 
        selectionBoxCorners = new Vertex[] {
            new Vertex(b[0], b[2], b[4]), new Vertex(b[1], b[2], b[4]),
            new Vertex(b[0], b[3], b[4]), new Vertex(b[1], b[3], b[4]),
            new Vertex(b[0], b[2], b[5]), new Vertex(b[1], b[2], b[5]),
            new Vertex(b[0], b[3], b[5]), new Vertex(b[1], b[3], b[5])
        };

        for (Vertex v : selectionBoxCorners) {
            v.resetView();
            v.setViewX(v.getX() - camX);
            v.setViewY(v.getY() - camY);
            v.setViewZ(v.getZ() - camZ);
            v.rotateViewY(totalAngleY);
            v.rotateViewX(totalAngleX);
            v.rotateViewZ(totalAngleZ);
            if (perspectiveMode) {
                double denom = Math.max(1.0, focalLength + v.getViewZ());
                v.setProjX(v.getViewX() * focalLength / denom);
                v.setProjY(v.getViewY() * focalLength / denom);
            } else {
                v.setProjX(v.getViewX());
                v.setProjY(v.getViewY());
            }
        }
        this.boundingBox = getBoundingBox();
    }

    public double getZAt(double x, double y, boolean perspMode, double focalLength) {
        double closestZ = triangles.get(0).getZAt(x, y, perspMode, focalLength);
        for (Triangle t : triangles) {
            double z = t.getZAt(x, y, perspMode, focalLength);
            if (z < closestZ) closestZ = z;
        }
        return closestZ;
    }

    public void drawTranslationGizmo(Graphics g, double rotX, double rotY, double rotZ, double zoom, boolean perspMode, double focalLength) {
        Graphics2D g2 = (Graphics2D) g;

        double screenLength = 60.0; 
        double arrowLength = screenLength / zoom; 
        double headSize = 10.0 / zoom;

        double[] b = getLocalSelectionBox(); 
        double midX = (b[0] + b[1]) / 2.0;
        double midY = (b[2] + b[3]) / 2.0;
        double midZ = (b[4] + b[5]) / 2.0;

        Vertex boxCenter = new Vertex(midX, midY, midZ);
        boxCenter.resetView();

        boxCenter.setViewX(boxCenter.getViewX() - center.getX());
        boxCenter.setViewY(boxCenter.getViewY() - center.getY());
        boxCenter.setViewZ(boxCenter.getViewZ() - center.getZ());

        boxCenter.rotateViewY(rotY);
        boxCenter.rotateViewX(rotX);
        boxCenter.rotateViewZ(rotZ);

        boxCenter.setViewX(boxCenter.getViewX() + center.getViewX());
        boxCenter.setViewY(boxCenter.getViewY() + center.getViewY());
        boxCenter.setViewZ(boxCenter.getViewZ() + center.getViewZ());

        double cx, cy;
        if (perspMode) {
            double w = Math.max(1.0, focalLength + boxCenter.getViewZ());
            cx = boxCenter.getViewX() * focalLength / w;
            cy = boxCenter.getViewY() * focalLength / w;
        } else {
            cx = boxCenter.getViewX();
            cy = boxCenter.getViewY();
        }

        Vertex tipX = projectVertex(new Vertex(arrowLength, 0, 0), rotX, rotY, rotZ);
        Vertex tipY = projectVertex(new Vertex(0, -arrowLength, 0), rotX, rotY, rotZ);
        Vertex tipZ = projectVertex(new Vertex(0, 0, arrowLength), rotX, rotY, rotZ);

        g2.setColor(Color.RED);
        drawArrow(g2, cx, cy, cx + tipX.getViewX(), cy + tipX.getViewY(), headSize, zoom);
        
        g2.setColor(Color.GREEN);
        drawArrow(g2, cx, cy, cx + tipY.getViewX(), cy + tipY.getViewY(), headSize, zoom);
        
        g2.setColor(Color.BLUE);
        drawArrow(g2, cx, cy, cx + tipZ.getViewX(), cy + tipZ.getViewY(), headSize, zoom);
    }

    private void drawArrow(Graphics2D g2, double x1, double y1, double x2, double y2, double headSize, double zoom) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        g2.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));

        if (length * zoom >= 10) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
        
            java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();

            double tipX = x2 + headSize * Math.cos(angle);
            double tipY = y2 + headSize * Math.sin(angle);

            path.moveTo(tipX, tipY);
            path.lineTo(x2 + (headSize) * Math.cos(angle + Math.PI*0.8), y2 + (headSize) * Math.sin(angle + Math.PI*0.8));
            path.lineTo(x2 + (headSize) * Math.cos(angle - Math.PI*0.8), y2 + (headSize) * Math.sin(angle - Math.PI*0.8));
            path.closePath();
            g2.fill(path);
        }
    }

    private Vertex projectVertex(Vertex v, double rx, double ry, double rz) {
        Vertex projected = new Vertex(v.getX(), v.getY(), v.getZ());
        projected.resetView();

        projected.rotateViewY(ry);
        projected.rotateViewX(rx);
        projected.rotateViewZ(rz);

        return projected;
    }

    public String getGizmoHit(int mouseX, int mouseY, double offsetX, double offsetY, double zoom, double rotX, double rotY, double rotZ, boolean perspMode, double focalLength) {
        double arrowLength = 60.0 / zoom; 
        
        double[] b = getLocalSelectionBox(); 
        double midX = (b[0] + b[1]) / 2.0;
        double midY = (b[2] + b[3]) / 2.0;
        double midZ = (b[4] + b[5]) / 2.0;
        
        Vertex bc = new Vertex(midX, midY, midZ);
        bc.resetView();
        bc.setViewX(bc.getViewX() - center.getX());
        bc.setViewY(bc.getViewY() - center.getY());
        bc.setViewZ(bc.getViewZ() - center.getZ());
        bc.rotateViewY(rotY);
        bc.rotateViewX(rotX);
        bc.rotateViewZ(rotZ);
        bc.setViewX(bc.getViewX() + center.getViewX());
        bc.setViewY(bc.getViewY() + center.getViewY());
        bc.setViewZ(bc.getViewZ() + center.getViewZ());

        double bcProjX, bcProjY;
        if (perspMode) {
            double w = Math.max(1.0, focalLength + bc.getViewZ());
            bcProjX = bc.getViewX() * focalLength / w;
            bcProjY = bc.getViewY() * focalLength / w;
        } else {
            bcProjX = bc.getViewX();
            bcProjY = bc.getViewY();
        }

        double cx = (bcProjX * zoom) + offsetX;
        double cy = (bcProjY * zoom) + offsetY;

        String[] axes = {"X", "Y", "Z"};
        Vertex[] tips = {
            projectVertex(new Vertex(arrowLength, 0, 0), rotX, rotY, rotZ),
            projectVertex(new Vertex(0, -arrowLength, 0), rotX, rotY, rotZ),
            projectVertex(new Vertex(0, 0, arrowLength), rotX, rotY, rotZ)
        };

        for (int i = 0; i < 3; i++) {
            double tx = cx + (tips[i].getViewX() * zoom);
            double ty = cy + (tips[i].getViewY() * zoom);
            if (Math.hypot(mouseX - tx, mouseY - ty) < 20) return axes[i];
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Shape with ").append(triangles.size()).append(" triangles\n");
        return sb.toString();
    }
}