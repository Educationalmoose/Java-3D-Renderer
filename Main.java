import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends JPanel {
    private List<Triangle> triangles = new ArrayList<>();
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (Triangle t : triangles) {
            t.draw(g);
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("3D Renderer");
        Main panel = new Main();

        Vertex v1 = new Vertex(100, 100, 0);
        Vertex v2 = new Vertex(200, 100, 0);
        Vertex v3 = new Vertex(150, 200, 0);

        Triangle t = new Triangle(v1, v2, v3);

        Timer timer = new Timer(100, e -> {
            t.rotateY(1); 
            t.rotateX(1); 
            panel.repaint();
        });
        timer.start();

        panel.addTriangle(t);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}