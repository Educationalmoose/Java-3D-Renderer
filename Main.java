import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    static ScrubbableField xFieldLight;
    static ScrubbableField yFieldLight;
    static ScrubbableField zFieldLight;

    static double xRotation = 0;
    static double yRotation = 0;
    static double zRotation = 0;

    static boolean showBoundingBox = false;
    static boolean showGrid = true;
    static boolean isPaused = true;

    static double scale = 1;
    static double zoomFactor = 1.0;
    static double sensitivity = 0.25;

    static Vector lightDir = new Vector(0, 0, 1).normalize();

    static Point lastMousePos;
    static double totalAngleX = 0;
    static double totalAngleY = 0;
    static double totalAngleZ = 0;

    static double panX = 0;
    static double panY = 0;

    static Shape selectedShape;

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
            for (Shape s : shapes) {
                s.drawBoundingBox(g, zoomFactor, panX, panY, getWidth(), getHeight());
            }
        }
    }

    private static void createAndShowGUI() {
        frame = new JFrame("3D Renderer");
        Main panel = new Main();

        JButton startButton = new JButton("Start");

        startButton.addActionListener(e -> {
            if (isPaused) {
                startButton.setText("Resume");
                isPaused = false;
            } else {
                startButton.setText("Paused");
                isPaused = true;
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

        rotationPanel.add(xLabel);
        rotationPanel.add(xField);
        rotationPanel.add(yLabel);
        rotationPanel.add(yField);
        rotationPanel.add(zLabel);
        rotationPanel.add(zField);

        JButton setRotationButton = new JButton("Confirm");

        setRotationButton.addActionListener(e -> updateRotation());
        JCheckBox showBoundingBoxCheckBox = new JCheckBox("Show Bounding Box", false);
        showBoundingBoxCheckBox.setSelected(Main.showBoundingBox);
        showBoundingBoxCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox showGridCheckBox = new JCheckBox("Show Grid", false);
        showGridCheckBox.setSelected(Main.showGrid);
        showGridCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        showBoundingBoxCheckBox.addActionListener(e -> {
            Main.showBoundingBox = showBoundingBoxCheckBox.isSelected();
            panel.repaint();
        });

        showGridCheckBox.addActionListener(e -> {
            Main.showGrid = showGridCheckBox.isSelected();
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

        JPanel lightPanel = new JPanel();
        lightPanel.setLayout(new BoxLayout(lightPanel, BoxLayout.Y_AXIS));
        JPanel lightVectorGrid = new JPanel();
        lightVectorGrid.setLayout(new GridLayout(3, 2, 5, 5));

        JLabel lightPanelLabel = new JLabel("Light");
        JLabel xLabelLight = new JLabel("X:");
        xLabelLight.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel yLabelLight = new JLabel("Y:");
        JLabel zLabelLight = new JLabel("Z:");
        xFieldLight = new ScrubbableField(0.0, -1.0, 1.0, 0.01);
        xFieldLight.setOnValueChange(val -> {
            double x = val;
            double y = Double.parseDouble(yFieldLight.getText());
            double z = Double.parseDouble(zFieldLight.getText());
            Main.lightDir = new Vector(x, y, z).normalize();
            
            panel.repaint();
        });
        yFieldLight = new ScrubbableField(0.0, -1.0, 1.0, 0.01);
        yFieldLight.setOnValueChange(val -> {
            double x = Double.parseDouble(xFieldLight.getText());
            double y = val;
            double z = Double.parseDouble(zFieldLight.getText());
            Main.lightDir = new Vector(x, y, z).normalize();
            
            panel.repaint();
        });
        zFieldLight = new ScrubbableField(1.0, -1.0, 1.0, 0.01);
        zFieldLight.setOnValueChange(val -> {
            double x = Double.parseDouble(xFieldLight.getText());
            double y = Double.parseDouble(yFieldLight.getText());
            double z = val;
            Main.lightDir = new Vector(x, y, z).normalize();
            
            panel.repaint();
        });

        lightVectorGrid.add(xLabelLight);
        lightVectorGrid.add(xFieldLight);
        lightVectorGrid.add(yLabelLight);
        lightVectorGrid.add(yFieldLight);
        lightVectorGrid.add(zLabelLight);
        lightVectorGrid.add(zFieldLight);

        lightPanelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lightVectorGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        lightPanel.add(lightPanelLabel);
        lightPanel.add(lightVectorGrid);
        

        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rotationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        setRotationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        lightPanel.setAlignmentX(Component.CENTER_ALIGNMENT);


        eastPanel.add(startButton);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(rotationPanel);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(setRotationButton);
        eastPanel.add(Box.createVerticalStrut(20));
        eastPanel.add(showBoundingBoxCheckBox);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(showGridCheckBox);
        eastPanel.add(Box.createVerticalStrut(20));
        eastPanel.add(scalePanel);
        eastPanel.add(Box.createVerticalStrut(20));
        eastPanel.add(lightPanel);

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
            totalAngleX = 0;
            totalAngleY = 0;
            totalAngleZ = 0;

            panX = 0;
            panY = 0;
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

                    totalAngleX = 0;
                    totalAngleY = 0;
                    totalAngleZ = 0;

                    panX = 0;
                    panY = 0;

                    shapes.add(objShape);
                    panel.repaint();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to load OBJ file.");
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // Left click for rotating
                    lastMousePos = e.getPoint();
                    selectedShape = getObjectAt(lastMousePos.x, lastMousePos.y);
                    if (selectedShape == null) 
                        clearSelectedShape();
                    else {
                        //shape.drawLocalSelectionBox();
                        //drawMovementArrows()
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) { // Right click for panning
                    lastMousePos = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;

                    if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                        Main.panX += dx;
                        Main.panY += dy;
                    } else {
                        double sensitivity = 0.5;
                        totalAngleX += dy * sensitivity;
                        totalAngleY -= dx * sensitivity;

                        for (Shape s : shapes) {
                            s.updateView(totalAngleX, totalAngleY, 0);
                        }
                    }

                    lastMousePos = e.getPoint();
                    panel.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPaused = false;
                panel.repaint();
            }
        };

        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);

        panel.addMouseWheelListener(e -> {
            double rotation = e.getPreciseWheelRotation();
            
            if (rotation == 0) return;

            double zoomStep = (rotation < 0) ? 1.05 : 0.95;
            
            zoomFactor *= zoomStep;
            
            scaleField.setText(String.format("%.2f", zoomFactor));
            
            panel.repaint();
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

        
        Timer timer = new Timer(10, e -> {
            if (!isPaused) {
                totalAngleX += xRotation / 10.0;
                totalAngleY += yRotation / 10.0;
                totalAngleZ += zRotation / 10.0;

                for (Shape s : shapes) {
                    s.updateView(totalAngleX, totalAngleY, totalAngleZ);
                }
                panel.repaint();
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
        int offsetX = (canvas.getWidth() / 2) + (int)panX;
        int offsetY = (canvas.getHeight() / 2) + (int)panY;

        if (showGrid)
            drawGridIntoCanvas(canvas, zBuffer, offsetX, offsetY);

        for (Triangle t : triangles) {
            double[] bb = t.getBoundingBox();
            
            int minX = (int) (bb[0] * zoomFactor) + offsetX;
            int maxX = (int) (bb[1] * zoomFactor) + offsetX;
            int minY = (int) (bb[2] * zoomFactor) + offsetY;
            int maxY = (int) (bb[3] * zoomFactor) + offsetY;

            minX = Math.max(0, minX);
            maxX = Math.min(canvas.getWidth() - 1, maxX);
            minY = Math.max(0, minY);
            maxY = Math.min(canvas.getHeight() - 1, maxY);

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    double worldX = (x - offsetX) / zoomFactor;
                    double worldY = (y - offsetY) / zoomFactor;

                    if (t.isInside(worldX, worldY)) {
                        double currentZ = t.getZAt(worldX, worldY);
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
            if (!isPaused) {
                xRotation = Double.parseDouble(xField.getText());
                yRotation = Double.parseDouble(yField.getText());
                zRotation = Double.parseDouble(zField.getText()); 
            }
            
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

    private void drawGridIntoCanvas(BufferedImage canvas, double[] zBuffer, int offX, int offY) {
        int gridSize = 10; 
        int spacing = 50;  
        int limit = gridSize * spacing;
        int gridColor = new Color(100, 100, 100).getRGB();

        for (int i = -gridSize; i <= gridSize; i++) {
            int pos = i * spacing;
            draw3DLine(canvas, zBuffer, pos, 0, -limit, pos, 0, limit, offX, offY, gridColor);
            draw3DLine(canvas, zBuffer, -limit, 0, pos, limit, 0, pos, offX, offY, gridColor);
        }
    }

    private void draw3DLine(BufferedImage canvas, double[] zBuffer, double x1, double y1, double z1, double x2, double y2, double z2, int offX, int offY, int color) {
        Vertex v1 = new Vertex(x1, y1, z1);
        Vertex v2 = new Vertex(x2, y2, z2);
        
        v1.rotateViewX(totalAngleX);
        v1.rotateViewY(totalAngleY);
        v2.rotateViewX(totalAngleX);
        v2.rotateViewY(totalAngleY);

        double sx1 = v1.getViewX() + offX;
        double sy1 = v1.getViewY() + offY;
        double sz1 = v1.getViewZ();

        double sx2 = v2.getViewX() + offX;
        double sy2 = v2.getViewY() + offY;
        double sz2 = v2.getViewZ();

        double dx = sx2 - sx1;
        double dy = sy2 - sy1;
        double dz = sz2 - sz1;
        double steps = Math.max(Math.abs(dx), Math.abs(dy));

        if (steps == 0) return;

        double xInc = dx / steps;
        double yInc = dy / steps;
        double zInc = dz / steps;

        double cx = sx1;
        double cy = sy1;
        double cz = sz1;

        for (int i = 0; i <= steps; i++) {
            int ix = (int) Math.round(cx);
            int iy = (int) Math.round(cy);

            if (ix >= 0 && ix < canvas.getWidth() && iy >= 0 && iy < canvas.getHeight()) {
                int index = iy * canvas.getWidth() + ix;
                if (cz < zBuffer[index]) {
                    zBuffer[index] = cz;
                    canvas.setRGB(ix, iy, color);
                }
            }
            cx += xInc;
            cy += yInc;
            cz += zInc;
        }
    }

    private static Shape getObjectAt(double x, double y) {
        Shape[] localShapes = new Shape[shapes.size()];
        int index = 0;
        for (Shape shape : shapes) {
            for (Triangle t : shape.getTriangles()) {
                if (t.isInside(x, y)) {
                    localShapes[index] = shape;
                    index++;
                }
            }
        }
        if (localShapes.length == 1) {
            return localShapes[0];
        }
        else if (localShapes.length > 1) {
            double closestZ = localShapes[0].getZAt(x, y);
            Shape closestShape = localShapes[0];
            for (Shape s : localShapes) {
                if (s.getZAt(x, y) > closestZ) {
                    closestZ = s.getZAt(x, y);
                    closestShape = s;
                }
            }
            return closestShape;
        }
        else {
            return null;
        }
    }

    private static void clearSelectedShape() {
        selectedShape = null;
    }

}