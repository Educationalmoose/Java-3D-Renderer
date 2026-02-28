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
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (Triangle t : triangles) {
            t.draw(g);
            t.drawTrail(g);
            t.drawBoundingBox(g);
        }

        /*for (Shape s : shapes) {
            s.draw(g);
            s.drawTrail(g);
            s.drawBoundingBox(g);
        }*/

        BufferedImage canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        
        //g.drawImage(canvas, 0, 0, null);
    }

    

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("3D Renderer");
        Main panel = new Main();

        Vertex v1 = new Vertex(100, 100, 0);
        Vertex v2 = new Vertex(200, 100, 0);
        Vertex v3 = new Vertex(150, 200, 0);
        Triangle t1 = new Triangle(v1, v2, v3);

        Vertex v4 = new Vertex(250, 200, 0);
        Triangle t2 = new Triangle(v3, v2, v4);

        Vertex v5 = new Vertex(300, 300, 0);
        Triangle t3 = new Triangle(v3, v4, v5);

        Shape s1 = new Shape(new Triangle[]{t1, t2, t3});

        for (Triangle t : s1.getTriangles()) {
            triangles.add(t);
        }

        Timer timer = new Timer(20, e -> {
            //t1.rotateX(1); 
            //t1.rotateY(1);
            //t1.rotateZ(1);
            s1.RotateX(1);
            s1.RotateY(2);
            s1.RotateZ(1);
            panel.repaint();
        });
        timer.start();

        panel.addShape(s1);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}