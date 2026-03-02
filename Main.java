import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main extends JPanel {
    private static ArrayList<Triangle> triangles = new ArrayList<>();
    private ArrayList<Shape> shapes = new ArrayList<>();

    static JFrame frame;

    static JTextField xField;
    static JTextField yField;
    static JTextField zField;

    static double xRotation = 5;
    static double yRotation = 5;
    static double zRotation = 5;

    static double xLastRotation = 5;
    static double yLastRotation = 5;
    static double zLastRotation = 5;

    static boolean showBoundingBox = false;

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
        if (width <= 0 || height <= 0) return;

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double[] zBuffer = new double[width * height];
        
        java.util.Arrays.fill(zBuffer, Double.MAX_VALUE);
        if (showBoundingBox) {
            for (Triangle t : triangles) {
                t.drawBoundingBox(g);
            }
        }
        render(triangles.toArray(new Triangle[0]), canvas, zBuffer);

        g.drawImage(canvas, 0, 0, null);
        if (showBoundingBox) {
            for (Shape s : shapes) {
                s.drawBoundingBox(g);
            }
        }
    }

    private static void createAndShowGUI() {
        frame = new JFrame("3D Renderer");
        Main panel = new Main();

        JButton startButton = new JButton("Stop");

        startButton.addActionListener(e -> {
            if (startButton.getText().equals("Start")) {
                startButton.setText("Stop");
                xRotation = xLastRotation;
                yRotation = yLastRotation;
                zRotation = zLastRotation;
            } else {
                startButton.setText("Start");
                xLastRotation = xRotation;
                yLastRotation = yRotation;
                zLastRotation = zRotation;
                xRotation = 0;
                yRotation = 0;
                zRotation = 0;
            }
        });

        JPanel eastWrapper = new JPanel(new BorderLayout());
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));

        eastWrapper.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        JPanel rotationPanel = new JPanel();
        rotationPanel.setLayout(new GridLayout(3, 2, 5, 5));
        JLabel xLabel = new JLabel("Rotate X:");
        JLabel yLabel = new JLabel("Rotate Y:");
        JLabel zLabel = new JLabel("Rotate Z:");
        xField = new JTextField("5", 2);
        yField = new JTextField("5", 2);
        zField = new JTextField("5", 2);

        JButton setRotationButton = new JButton("Confirm");

        setRotationButton.addActionListener(e -> updateRotation());
        JCheckBox showBoundingBoxCheckBox = new JCheckBox("Show Bounding Box", false);
        showBoundingBoxCheckBox.setSelected(Main.showBoundingBox);
        showBoundingBoxCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        showBoundingBoxCheckBox.addActionListener(e -> {
            Main.showBoundingBox = showBoundingBoxCheckBox.isSelected();
            panel.repaint();
        });

        rotationPanel.add(xLabel);
        rotationPanel.add(xField);
        rotationPanel.add(yLabel);
        rotationPanel.add(yField);
        rotationPanel.add(zLabel);
        rotationPanel.add(zField);

        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rotationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        setRotationButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        eastPanel.add(startButton);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(rotationPanel);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(setRotationButton);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(showBoundingBoxCheckBox);

        eastWrapper.add(eastPanel, BorderLayout.NORTH);

        frame.add(eastWrapper, BorderLayout.EAST);

        Vertex vTLF = new Vertex(100, 100, 0); // Top Left Front
        Vertex vBLF = new Vertex(100, 200, 0); // Bottom Left Front
        Vertex vTRF = new Vertex(200, 100, 0); // Top Right Front
        Vertex vBRF = new Vertex(200, 200, 0); // Bottom Right Front

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

        Timer timer = new Timer(1, e -> {
            cube.RotateX(xRotation / 10);
            cube.RotateY(yRotation / 10);
            cube.RotateZ(zRotation / 10);
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

    static private void updateRotation() {
        try {
            xRotation = Double.parseDouble(xField.getText());
            yRotation = Double.parseDouble(yField.getText());
            zRotation = Double.parseDouble(zField.getText()); 
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Rotation values must be valid numbers");
        }
    }
}