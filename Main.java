import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main extends JPanel {
    private static ArrayList<Triangle> triangles = new ArrayList<>();
    private ArrayList<Shape> shapes = new ArrayList<>();
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
    }

    public void addTriangle(Triangle t) {
        triangles.add(t);
    }
    public void addShape(Shape s) {
        shapes.add(s);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return; // Safety check for window resizing

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double[] zBuffer = new double[width * height];
        
        java.util.Arrays.fill(zBuffer, Double.MAX_VALUE);
        for (Triangle t : triangles) {
            t.drawBoundingBox(g);
        }

        render(triangles.toArray(new Triangle[0]), canvas, zBuffer);

        g.drawImage(canvas, 0, 0, null);
    }

    

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("3D Renderer");
        Main panel = new Main();

        Vertex vTLF = new Vertex(100, 100, 0);   // Top Left Front
        Vertex vBLF = new Vertex(100, 200, 0);   // Bottom Left Front
        Vertex vTRF = new Vertex(200, 100, 0);   // Top Right Front
        Vertex vBRF = new Vertex(200, 200, 0);   // Bottom Right Front

        Vertex vTLB = new Vertex(100, 100, 100); // Top Left Back
        Vertex vBLB = new Vertex(100, 200, 100); // Bottom Left Back
        Vertex vTRB = new Vertex(200, 100, 100); // Top Right Back
        Vertex vBRB = new Vertex(200, 200, 100); // Bottom Right Back

        // Front
        Triangle f1 = new Triangle(vTLF, vBLF, vTRF);
        Triangle f2 = new Triangle(vTRF, vBLF, vBRF);
        // Back
        Triangle ba1 = new Triangle(vTLB, vBLB, vTRB);
        Triangle ba2 = new Triangle(vTRB, vBLB, vBRB);
        // Left
        Triangle l1 = new Triangle(vTLB, vBLB, vTLF);
        Triangle l2 = new Triangle(vTLF, vBLB, vBLF);
        // Right
        Triangle r1 = new Triangle(vTRF, vBRF, vTRB);
        Triangle r2 = new Triangle(vTRB, vBRF, vBRB);
        // Top
        Triangle t1 = new Triangle(vTLB, vTLF, vTRB);
        Triangle t2 = new Triangle(vTRB, vTLF, vTRF);
        // Bottom
        Triangle bo1 = new Triangle(vBLF, vBLB, vBRF);
        Triangle bo2 = new Triangle(vBRF, vBLB, vBRB);

        Shape cube = new Shape( new Triangle[]{f1, f2, ba1, ba2, l1, l2, r1, r2, t1, t2, bo1, bo2});
        for (Triangle t : cube.getTriangles()) {
            triangles.add(t);
        }

        Timer timer = new Timer(20, e -> {
            cube.RotateX(1);
            cube.RotateY(1);
            cube.RotateZ(1);
            panel.repaint();
        });
        timer.start();

        panel.addShape(cube);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public void render(Triangle[] triangles, BufferedImage canvas, double[] zBuffer) {
        for (Triangle t : triangles) {
            double[] bb = t.getBoundingBox();
            int minX = (int) bb[0];
            int maxX = (int) bb[1];
            int minY = (int) bb[2];;
            int maxY = (int) bb[3];;

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    if (t.isInside(x, y)) {
                        double currentZ = t.getZAt(x, y);
                        //System.out.println(currentZ);

                        int pixelIndex = y * canvas.getWidth() + x;
                        if (currentZ < zBuffer[pixelIndex]) {
                            zBuffer[pixelIndex] = currentZ;
                            canvas.setRGB(x, y, calculateColor(t.getColor(), currentZ));
                        }
                    }
                }
            }
        }
    }

    /*public int calculateColor(Color color, double z) {
        double factor = 1.0 - (z / 200.0); 
        factor = Math.max(0.2, Math.min(1.0, factor));
        int r = (int)(color.getRed() * factor);
        int g = (int)(color.getGreen() * factor);
        int b = (int)(color.getBlue() * factor);

        return new Color(r, g, b).getRGB();
    }*/

    public int calculateColor(Color baseColor, double z) {
        double minZ = -150.0; // closest to camera
        double maxZ = 150.0;  // furthest from camera

        // closer (minZ) = 1 (brighter)
        // further (maxZ) = 0.2 (darker)
        double factor = 1.0 - ((z - minZ) / (maxZ - minZ));
        
        factor = Math.max(0.1, Math.min(1.0, factor));

        int r = (int)(baseColor.getRed() * factor);
        int g = (int)(baseColor.getGreen() * factor);
        int b = (int)(baseColor.getBlue() * factor);

        return (new Color(r, g, b)).getRGB();
    }
}