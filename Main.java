import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main extends JPanel {
    private static ArrayList<Triangle> triangles = new ArrayList<>();
    private static ArrayList<Shape> shapes = new ArrayList<>();

    static JFrame frame;

    static JTextField xField;
    static JTextField yField;
    static JTextField zField;

    static double xRotation = 0;
    static double yRotation = 0;
    static double zRotation = 0;

    static double xLastRotation = 0;
    static double yLastRotation = 0;
    static double zLastRotation = 0;

    static boolean showBoundingBox = false;
    static boolean isPaused = true;

    static double scale = 1;

    static Vector lightDir = new Vector(0, 0, 1).normalize();

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
        
        render(triangles.toArray(new Triangle[0]), canvas, zBuffer);

        g.drawImage(canvas, 0, 0, null);

        if (showBoundingBox) {
            g.translate(width / 2, height / 2);
            for (Shape s : shapes) {
                s.drawBoundingBox(g);
            }
            g.translate(-width / 2, -height / 2);
        }
    }

    private static void createAndShowGUI() {
        frame = new JFrame("3D Renderer");
        Main panel = new Main();

        JButton startButton = new JButton("Start");

        startButton.addActionListener(e -> {
            if (startButton.getText().equals("Start")) {
                startButton.setText("Stop");
                isPaused = false;
                xRotation = xLastRotation;
                yRotation = yLastRotation;
                zRotation = zLastRotation;
            } else {
                startButton.setText("Start");
                isPaused = true;
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
        xField = new JTextField("0", 2);
        yField = new JTextField("0", 2);
        zField = new JTextField("0", 2);

        JButton setRotationButton = new JButton("Confirm");

        setRotationButton.addActionListener(e -> updateRotation());
        JCheckBox showBoundingBoxCheckBox = new JCheckBox("Show Bounding Box", false);
        showBoundingBoxCheckBox.setSelected(Main.showBoundingBox);
        showBoundingBoxCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        showBoundingBoxCheckBox.addActionListener(e -> {
            Main.showBoundingBox = showBoundingBoxCheckBox.isSelected();
            panel.repaint();
        });

        JPanel scalePanel = new JPanel();
        scalePanel.setLayout(new GridLayout(1, 2, 5, 5));
        JLabel scaleLabel = new JLabel("Scale:");
        JTextField scaleField = new JTextField(Double.toString(scale), 2);
        scalePanel.add(scaleLabel);
        scalePanel.add(scaleField);

        scaleField.addActionListener(e -> {
            try {
                scale = Double.parseDouble(scaleField.getText());
                for (Shape s : shapes) {
                    s.scaleBy(scale / s.getScale());
                }
                panel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Scale must be a valid number");
            }
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
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(scalePanel);

        eastWrapper.add(eastPanel, BorderLayout.NORTH);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("New");
        JMenuItem importObjItem = new JMenuItem("Open");

        fileMenu.setMnemonic('F');
        importObjItem.setMnemonic('I');

        newItem.addActionListener(e -> {
            scale = 1;
            scaleField.setText("1.0");
            panel.removeAll();
            triangles.clear();
            shapes.clear();
            panel.repaint();
        });

        importObjItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();

            FileNameExtensionFilter filter = new FileNameExtensionFilter("3D Model Files (.obj)", "obj");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                Shape objShape = panel.parseObjFile(selectedFile.getAbsolutePath());
                if (objShape != null) {
                    panel.removeAll();
                    triangles.clear();
                    shapes.clear();

                    for (Triangle t : objShape.getTriangles()) {
                        triangles.add(t);
                    }

                    scale = 1;
                    scaleField.setText("1.0");

                    shapes.add(objShape);
                    panel.repaint();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to load OBJ file.");
                }
            }
        });

        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(importObjItem);
       

        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        frame.add(eastWrapper, BorderLayout.EAST);

        frame.setSize(600, 600);

        Vertex vTLF = new Vertex(-50, -50, 0); // Top Left Front
        Vertex vBLF = new Vertex(-50, 50, 0); // Bottom Left Front
        Vertex vTRF = new Vertex(50, -50, 0); // Top Right Front
        Vertex vBRF = new Vertex(50, 50, 0); // Bottom Right Front

        Vertex vTLB = new Vertex(-50, -50, 100); // Top Left Back
        Vertex vBLB = new Vertex(-50, 50, 100); // Bottom Left Back
        Vertex vTRB = new Vertex(50, -50, 100); // Top Right Back
        Vertex vBRB = new Vertex(50, 50, 100); // Bottom Right Back

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
            if (!isPaused) {
                for (Shape s : shapes) {
                    s.RotateX(xRotation / 10);
                    s.RotateY(yRotation / 10);
                    s.RotateZ(zRotation / 10);
                    panel.repaint();
                }  
            }
        });
        timer.start();

        panel.addShape(cube);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public void render(Triangle[] triangles, BufferedImage canvas, double[] zBuffer) {
        int offsetX = canvas.getWidth() / 2;
        int offsetY = canvas.getHeight() / 2;

        for (Triangle t : triangles) {
            double[] bb = t.getBoundingBox();
            
            int minX = (int) bb[0] + offsetX;
            int maxX = (int) bb[1] + offsetX;
            int minY = (int) bb[2] + offsetY;
            int maxY = (int) bb[3] + offsetY;

            minX = Math.max(0, minX);
            maxX = Math.min(canvas.getWidth() - 1, maxX);
            minY = Math.max(0, minY);
            maxY = Math.min(canvas.getHeight() - 1, maxY);

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    int modelX = x - offsetX;
                    int modelY = y - offsetY;

                    if (t.isInside(modelX, modelY)) {
                        double currentZ = t.getZAt(modelX, modelY);
                        int pixelIndex = y * canvas.getWidth() + x;
                        
                        if (currentZ < zBuffer[pixelIndex]) {
                            zBuffer[pixelIndex] = currentZ;
                            Vector normal = t.getNormalVector(); 
                            canvas.setRGB(x, y, calculateColor(t.getColor(), currentZ, normal));
                        }
                    }
                }
            }
        }
    }

    public int calculateColor(Color baseColor, double z, Vector normal) {
        Vector n = normal.normalize();
        double dot = n.dotProduct(lightDir);
        
        double lightIntensity = Math.max(0.2, Math.abs(dot));

        double minZ = -200.0; 
        double maxZ = 200.0;  
        double depthFactor = 1.0 - ((z - minZ) / (maxZ - minZ));
        depthFactor = Math.max(0.3, Math.min(1.0, depthFactor));

        double finalFactor = lightIntensity * depthFactor;

        int r = (int)(baseColor.getRed() * finalFactor);
        int g = (int)(baseColor.getGreen() * finalFactor);
        int b = (int)(baseColor.getBlue() * finalFactor);

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

    private Shape parseObjFile(String filePath) {
        File object = new File(filePath);

        ArrayList<Triangle> localTriangles = new ArrayList<>(); 
        ArrayList<Vertex> localVertices = new ArrayList<>();
        ArrayList<Vertex> localNormalVertices = new ArrayList<>();
        ArrayList<double[]> localTextureVertices = new ArrayList<>();

        try(Scanner scanner = new Scanner(object)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("v ")) {
                    // Parse vertex
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);

                    localVertices.add(new Vertex(x, y, z));
                } else if (line.startsWith("vt ")) {
                    // Parse vertex
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    localTextureVertices.add(new double[]{x, y});
                } else if (line.startsWith("vn ")) {
                    // Parse vertex
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);

                    localNormalVertices.add(new Vertex(x, y, z));
                } else if (line.startsWith("f ")) {
                    // example if 3/3/5 9/9/5 7/7/5 5/5/5
                    // break the line into parts ("f", "3/3/5", "9/9/5", "7/7/5", "5/5/5")
                    String[] parts = line.split("\\s+");
                    int numVertices = parts.length - 1;

                    if (numVertices >= 3) {
                        // pull just the vertex indices
                        int[] vertexIndices = new int[numVertices];
                        int[] textureIndices = new int[numVertices];
                        int[] normalIndices = new int[numVertices];
                        for (int i = 0; i < numVertices; i++) {
                            // split by / and parse
                            String[] subParts = parts[i + 1].split("/");
                            vertexIndices[i] = Integer.parseInt(subParts[0]) - 1;
                            if (subParts.length == 2)
                                textureIndices[i] = Integer.parseInt(subParts[1]) - 1;
                            if (subParts.length == 3)
                                normalIndices[i] = Integer.parseInt(subParts[2]) - 1;
                        }

                        for (int i = 1; i < numVertices - 1; i++) {
                            Vertex v1 = localVertices.get(vertexIndices[0]);
                            Vertex v2 = localVertices.get(vertexIndices[i]);
                            Vertex v3 = localVertices.get(vertexIndices[i + 1]);
                            
                            localTriangles.add(new Triangle(v1, v2, v3));
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Triangle[] triangleArray = localTriangles.toArray(new Triangle[0]);
        triangles = localTriangles;
        return new Shape(triangleArray, scale);
    }
}