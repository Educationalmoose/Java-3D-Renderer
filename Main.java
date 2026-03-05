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

    static ScrubbableField xFieldShape;
    static ScrubbableField yFieldShape;
    static ScrubbableField zFieldShape;

    static double xRotation = 0;
    static double yRotation = 0;
    static double zRotation = 0;

    static boolean showBoundingBox = false;
    static boolean showGrid = true;
    static boolean wireframeMode = false;
    static boolean showSelectionBox = false;
    static boolean isPaused = true;
    static boolean perspectiveMode = false;
    static double focalLength = 500.0;

    static double scale = 1;
    static double zoomFactor = 1.0;
    static double sensitivity = 0.25;
    static double flySpeed = 5.0;
    static double accelerationSpeed = 1.0;

    static Vector lightDir = new Vector(0, 0, 1).normalize();

    static Point lastMousePos;
    static double totalAngleX = 0;
    static double totalAngleY = 0;
    static double totalAngleZ = 0;

    static String draggingAxis = null;
    static double startWorldX, startWorldY;

    static double camX = 0;
    static double camY = 0;
    static double camZ = 0;

    static java.util.Set<Integer> pressedKeys = new java.util.HashSet<>();

    static Shape selectedShape;

    static Point pressPoint;

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
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(getWidth() / 2.0, getHeight() / 2.0);
            g2.scale(zoomFactor, zoomFactor);
            g2.setStroke(new BasicStroke((float)(1.0 / zoomFactor)));
            for (Shape s : shapes) {
                s.drawBoundingBox(g2);
            }
            g2.dispose();
        }

        if (selectedShape != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(getWidth() / 2.0, getHeight() / 2.0);
            g2.scale(zoomFactor, zoomFactor);
            g2.setStroke(new BasicStroke((float)(2.0 / zoomFactor)));
            selectedShape.drawLocalSelectionBox(g2);
            selectedShape.drawTranslationGizmo(g2, totalAngleX, totalAngleY, totalAngleZ, zoomFactor, perspectiveMode, focalLength);
            g2.dispose();
        }
    }

    private static void createAndShowGUI() {
        frame = new JFrame("3D Renderer");
        Main panel = new Main();

        JPanel eastWrapper = new JPanel(new BorderLayout());
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));

        eastWrapper.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        JCheckBox showBoundingBoxCheckBox = new JCheckBox("Show Bounding Box", false);
        showBoundingBoxCheckBox.setSelected(Main.showBoundingBox);
        showBoundingBoxCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox showGridCheckBox = new JCheckBox("Show Grid", false);
        showGridCheckBox.setSelected(Main.showGrid);
        showGridCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox showWireframeCheckBox = new JCheckBox("Wireframe Mode", false);
        showWireframeCheckBox.setSelected(Main.wireframeMode);
        showWireframeCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        showBoundingBoxCheckBox.addActionListener(e -> {
            Main.showBoundingBox = showBoundingBoxCheckBox.isSelected();
            panel.repaint();
        });

        showGridCheckBox.addActionListener(e -> {
            Main.showGrid = showGridCheckBox.isSelected();
            panel.repaint();
        });

        showWireframeCheckBox.addActionListener(e -> {
            Main.wireframeMode = showWireframeCheckBox.isSelected();
            panel.repaint();
        });

        JCheckBox perspectiveCheckBox = new JCheckBox("Perspective", false);
        perspectiveCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel fovPanel = new JPanel();
        fovPanel.setLayout(new GridLayout(1, 2, 5, 5));
        JLabel fovLabel = new JLabel("Focal Length:");
        JTextField fovField = new JTextField(Double.toString(Main.focalLength), 4);
        fovPanel.add(fovLabel);
        fovPanel.add(fovField);
        fovPanel.setVisible(false);

        perspectiveCheckBox.addActionListener(e -> {
            Main.perspectiveMode = perspectiveCheckBox.isSelected();
            fovPanel.setVisible(Main.perspectiveMode);
            if (Main.perspectiveMode) {
                // Pull the camera straight back along world -Z so it's not at the origin
                // on top of the scene objects. Do NOT reset angles — that causes a sudden
                // jarring snap in the view which felt like "distortion".
                camX = 0;
                camY = 0;
                camZ = -150; // close to scene — avoids the "orbiting" feel from being far away
            } else {
                camX = 0;
                camY = 0;
                camZ = 0;
            }
            for (Shape s : shapes) {
                s.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
            }
            panel.repaint();
        });

        fovField.addActionListener(e -> {
            try {
                Main.focalLength = Double.parseDouble(fovField.getText());
                for (Shape s : shapes) {
                    s.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
                }
                panel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Focal length must be a valid number");
            }
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
        JLabel yLabelLight = new JLabel("Y:");
        JLabel zLabelLight = new JLabel("Z:");
        xFieldLight = new ScrubbableField(0.0, -1.0, 1.0, 0.005);
        xFieldLight.setOnValueChange(val -> {
            double x = val;
            double y = Double.parseDouble(yFieldLight.getText());
            double z = Double.parseDouble(zFieldLight.getText());
            Main.lightDir = new Vector(x, y, z).normalize();
            
            panel.repaint();
        });
        yFieldLight = new ScrubbableField(0.0, -1.0, 1.0, 0.005);
        yFieldLight.setOnValueChange(val -> {
            double x = Double.parseDouble(xFieldLight.getText());
            double y = val;
            double z = Double.parseDouble(zFieldLight.getText());
            Main.lightDir = new Vector(x, y, z).normalize();
            
            panel.repaint();
        });
        zFieldLight = new ScrubbableField(1.0, -1.0, 1.0, 0.005);
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


        JPanel selectedShapePanel = new JPanel();
        selectedShapePanel.setLayout(new BoxLayout(selectedShapePanel, BoxLayout.Y_AXIS));
        JPanel shapePositionGrid = new JPanel();
        shapePositionGrid.setLayout(new GridLayout(3, 2, 5, 5));

        JLabel selectedShapePanelLabel = new JLabel("Selected Shape");
        JLabel xLabelShape = new JLabel("X:");
        JLabel yLabelShape = new JLabel("Y:");
        JLabel zLabelShape = new JLabel("Z:");
        xFieldShape = new ScrubbableField(selectedShape == null ? 0.0 : selectedShape.getCenter().getX(), -100000.0, 100000.0, 0.1);
        xFieldShape.setOnValueChange(val -> {
            double x = val;
            double y = Double.parseDouble(yFieldShape.getText());
            double z = Double.parseDouble(zFieldShape.getText());
            selectedShape.translateShape(x - selectedShape.getCenter().getX(), 0, 0);
            
             for (Shape s : shapes) {
                s.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
            }
            
            panel.repaint();
        });
        yFieldShape = new ScrubbableField(selectedShape == null ? 0.0 : selectedShape.getCenter().getY(), -100000.0, 100000.0, 0.1);
        yFieldShape.setOnValueChange(val -> {
            double x = Double.parseDouble(xFieldShape.getText());
            double y = val;
            double z = Double.parseDouble(zFieldShape.getText());
            selectedShape.translateShape(0, y - selectedShape.getCenter().getY(), 0);
            
             for (Shape s : shapes) {
                s.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
            }
            
            panel.repaint();
        });
        zFieldShape = new ScrubbableField(selectedShape == null ? 0.0 : selectedShape.getCenter().getZ(), -100000.0, 100000.0, 0.1);
        zFieldShape.setOnValueChange(val -> {
            double x = Double.parseDouble(xFieldShape.getText());
            double y = Double.parseDouble(yFieldShape.getText());
            double z = val;
            selectedShape.translateShape(0, 0, z - selectedShape.getCenter().getZ());
            
             for (Shape s : shapes) {
                s.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
            }
            
            panel.repaint();
        });

        shapePositionGrid.add(xLabelShape);
        shapePositionGrid.add(xFieldShape);
        shapePositionGrid.add(yLabelShape);
        shapePositionGrid.add(yFieldShape);
        shapePositionGrid.add(zLabelShape);
        shapePositionGrid.add(zFieldShape);

        selectedShapePanelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        shapePositionGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        selectedShapePanel.add(selectedShapePanelLabel);
        selectedShapePanel.add(shapePositionGrid);
        
        lightPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectedShapePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        eastPanel.add(selectedShapePanel);
        eastPanel.add(Box.createVerticalStrut(20));
        eastPanel.add(lightPanel);
        eastPanel.add(Box.createVerticalStrut(20));
        eastPanel.add(fovPanel);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(scalePanel);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(showBoundingBoxCheckBox);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(showGridCheckBox);
        eastPanel.add(Box.createVerticalStrut(20));
        eastPanel.add(showWireframeCheckBox);
        eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(perspectiveCheckBox);


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

            camX = 0;
            camY = 0;
            camZ = 0;
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

                    camX = 0;
                    camY = 0;
                    camZ = 0;

                    shapes.add(objShape);
                    objShape.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
                    panel.repaint();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to load OBJ file.");
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pressPoint = e.getPoint();
                lastMousePos = e.getPoint();

                if (selectedShape != null) {
                    double offsetX = panel.getWidth() / 2.0;
                    double offsetY = panel.getHeight() / 2.0;
                    
                    draggingAxis = selectedShape.getGizmoHit(
                        e.getX(), e.getY(), 
                        offsetX, offsetY, 
                        zoomFactor, totalAngleX, totalAngleY, totalAngleZ,
                        perspectiveMode, focalLength
                    );
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;

                    if (draggingAxis != null && selectedShape != null) {
                        int dx2 = e.getX() - lastMousePos.x;
                        int dy2 = e.getY() - lastMousePos.y;

                        Vector worldAxis = new Vector(0, 0, 0);
                        if (draggingAxis.equals("X")) worldAxis = new Vector(1, 0, 0);
                        else if (draggingAxis.equals("Y")) worldAxis = new Vector(0, -1, 0);
                        else if (draggingAxis.equals("Z")) worldAxis = new Vector(0, 0, 1);

                        Vector screenVector = new Vector(worldAxis.getX(), worldAxis.getY(), worldAxis.getZ());
                        screenVector.rotateY(totalAngleY);
                        screenVector.rotateX(totalAngleX);
                        screenVector.rotateZ(totalAngleZ);

                        double screenX = screenVector.getX();
                        double screenY = screenVector.getY();
                        double mag = Math.sqrt(screenX * screenX + screenY * screenY);

                        double moveAmount;
                        if (mag > 0.0001) {
                            screenX /= mag;
                            screenY /= mag;

                            moveAmount = (dx2 * screenX + dy2 * screenY) / zoomFactor;
                        } else {
                            moveAmount = dx2 / zoomFactor;
                        }

                        double moveX = worldAxis.getX() * moveAmount;
                        double moveY = worldAxis.getY() * moveAmount; 
                        double moveZ = worldAxis.getZ() * moveAmount;

                        selectedShape.translateShape(moveX, moveY, moveZ);
                        
                        for (Shape s : shapes) {
                            s.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
                        }
                        
                        lastMousePos = e.getPoint();
                        panel.repaint();
                    } else if (javax.swing.SwingUtilities.isRightMouseButton(e)) {

                        double sensitivity = 0.3;
                        totalAngleX += dy * sensitivity;
                        totalAngleX = Math.max(-89.0, Math.min(89.0, totalAngleX)); // clamp pitch — prevents flip
                        totalAngleY -= dx * sensitivity;

                        for (Shape s : shapes) {
                            s.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
                        }
                    }

                    lastMousePos = e.getPoint();
                    panel.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPaused = false;
                draggingAxis = null;
                if (pressPoint != null) {
                    double distance = pressPoint.distance(e.getPoint());
                    
                    if (distance < 5) {
                        double mouseX = e.getX();
                        double mouseY = e.getY();
                        double offsetX = panel.getWidth() / 2.0;
                        double offsetY = panel.getHeight() / 2.0;

                        double worldX = (mouseX - offsetX) / Main.zoomFactor;
                        double worldY = (mouseY - offsetY) / Main.zoomFactor;

                        selectedShape = getObjectAt(worldX, worldY);
                    }
                }
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

        panel.setFocusable(true);
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { panel.requestFocusInWindow(); }
        });
        panel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e)  { pressedKeys.add(e.getKeyCode()); }
            public void keyReleased(java.awt.event.KeyEvent e) { pressedKeys.remove(e.getKeyCode()); }
        });

        Timer movementTimer = new Timer(1, e -> {
            if (pressedKeys.isEmpty()) return;

            double[] fwd   = localToWorld(0,  0,  1);
            double[] right = localToWorld(1,  0,  0);
            double[] up    = localToWorld(0, -1,  0);

            double spd = flySpeed * accelerationSpeed;
            boolean moved = false;
            boolean accelerating = pressedKeys.contains(java.awt.event.KeyEvent.VK_SHIFT);

            if (pressedKeys.contains(java.awt.event.KeyEvent.VK_W)) { camX += fwd[0]*spd; camY += fwd[1]*spd; camZ += fwd[2]*spd; moved = true; }
            if (pressedKeys.contains(java.awt.event.KeyEvent.VK_S)) { camX -= fwd[0]*spd; camY -= fwd[1]*spd; camZ -= fwd[2]*spd; moved = true; }
            if (pressedKeys.contains(java.awt.event.KeyEvent.VK_A)) { camX -= right[0]*spd; camY -= right[1]*spd; camZ -= right[2]*spd; moved = true; }
            if (pressedKeys.contains(java.awt.event.KeyEvent.VK_D)) { camX += right[0]*spd; camY += right[1]*spd; camZ += right[2]*spd; moved = true; }
            if (pressedKeys.contains(java.awt.event.KeyEvent.VK_SPACE)) { camX += up[0]*spd; camY += up[1]*spd; camZ += up[2]*spd; moved = true; }
            if (pressedKeys.contains(java.awt.event.KeyEvent.VK_CONTROL)) { camX -= up[0]*spd; camY -= up[1]*spd; camZ -= up[2]*spd; moved = true; }
            if (!accelerating) {
                accelerationSpeed = 1.0;
                accelerating = true;
            } else {
                accelerationSpeed = Math.min(accelerationSpeed + 0.05, 5.0);
            }

            if (moved) {
                for (Shape s : shapes) {
                    s.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
                }
                panel.repaint();
            }
        });
        movementTimer.start();
   

        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(importObjItem);
       

        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        frame.add(eastWrapper, BorderLayout.EAST);

        frame.setSize(900, 900);

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
            for (Shape s : shapes) {
                s.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);
            }
            panel.repaint();
        });
        timer.start();

        panel.addShape(cube);
        cube.updateView(totalAngleX, totalAngleY, totalAngleZ, perspectiveMode, focalLength, camX, camY, camZ);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 900);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static double[] localToWorld(double lx, double ly, double lz) {
        Vertex dir = new Vertex(lx, ly, lz);
        dir.rotateViewZ(-totalAngleZ);
        dir.rotateViewX(-totalAngleX);
        dir.rotateViewY(-totalAngleY);
        return new double[]{ dir.getViewX(), dir.getViewY(), dir.getViewZ() };
    }

    public void render(Triangle[] triangles, BufferedImage canvas, double[] zBuffer) {
        int offsetX = canvas.getWidth() / 2;
        int offsetY = canvas.getHeight() / 2;

        if (showGrid)
            drawGridIntoCanvas(canvas, zBuffer, offsetX, offsetY);

        if (wireframeMode) {
            Graphics2D g2 = (Graphics2D) canvas.getGraphics();
            g2.translate(offsetX, offsetY);
            g2.scale(zoomFactor, zoomFactor);
            g2.setStroke(new BasicStroke((float)(1.0 / zoomFactor)));
            for (Triangle t : triangles) {
                t.drawWireframe(g2);
            }
            g2.dispose();
        }
        else {

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
                            double currentZ = t.getZAt(worldX, worldY, perspectiveMode, focalLength);
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
    }

    public int calculateColor(Color baseColor, double z, Vector normal) {
        Vector n = normal.normalize();
        double dot = n.dotProduct(lightDir);

        double lightIntensity = Math.max(0.2, Math.abs(dot));

        int r = (int)(baseColor.getRed()   * lightIntensity);
        int g = (int)(baseColor.getGreen() * lightIntensity);
        int b = (int)(baseColor.getBlue()  * lightIntensity);

        return new Color(
            Math.min(255, r),
            Math.min(255, g),
            Math.min(255, b)
        ).getRGB();
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
                    // parse vertex
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);

                    localVertices.add(new Vertex(x, y, z));
                } else if (line.startsWith("vt ")) {
                    // parse texture vertex
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    localTextureVertices.add(new double[]{x, y});
                } else if (line.startsWith("vn ")) {
                    // parse normal vertex
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

        double maxCoord = 0;
        for (Vertex v : localVertices) {
            maxCoord = Math.max(maxCoord, Math.abs(v.getX()));
            maxCoord = Math.max(maxCoord, Math.abs(v.getY()));
            maxCoord = Math.max(maxCoord, Math.abs(v.getZ()));
        }

        if (maxCoord > 0 && maxCoord < 10) {
            double normalizationFactor = 100.0 / maxCoord;
            for (Vertex v : localVertices) {
                v.setX(v.getX() * normalizationFactor);
                v.setY(v.getY() * normalizationFactor);
                v.setZ(v.getZ() * normalizationFactor);
            }
        }

        Triangle[] triangleArray = localTriangles.toArray(new Triangle[0]);
        triangles = localTriangles;
        return new Shape(triangleArray, scale);
    }

    private void drawGridIntoCanvas(BufferedImage canvas, double[] zBuffer, int offX, int offY) {
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int spacing = 100; 
        int baseColorRGB = 100;
        
        double viewHalfWidth  = (canvas.getWidth()  / 2.0) / zoomFactor;
        double viewHalfHeight = (canvas.getHeight() / 2.0) / zoomFactor;
        
        double buffer = Math.max(viewHalfWidth, viewHalfHeight) * 6;
        double minVisibleWorldX = camX - buffer;
        double maxVisibleWorldX = camX + buffer;
        double minVisibleWorldZ = camZ - buffer;
        double maxVisibleWorldZ = camZ + buffer;

        int startX = (int) (minVisibleWorldX / spacing) - 2; 
        int endX   = (int) (maxVisibleWorldX / spacing) + 2;
        int startZ = (int) (minVisibleWorldZ / spacing) - 2;
        int endZ   = (int) (maxVisibleWorldZ / spacing) + 2;

        Color gridColor = new Color(baseColorRGB, baseColorRGB, baseColorRGB, 150);

        for (int i = startX; i <= endX; i++) {
            double worldX = i * spacing;
            draw3DLine(g2, worldX, 0.0, minVisibleWorldZ, worldX, 0.0, maxVisibleWorldZ, offX, offY, gridColor);
        }

        for (int i = startZ; i <= endZ; i++) {
            double worldZ = i * spacing;
            draw3DLine(g2, minVisibleWorldX, 0, worldZ, maxVisibleWorldX, 0, worldZ, offX, offY, gridColor);
        }
        
        g2.dispose();
    }

    private void draw3DPoint(Graphics2D g2, double x, double y, double z, int offX, int offY, Color color) {
        Vertex v = new Vertex(x, y, z);
        v.rotateViewY(totalAngleY);
        v.rotateViewX(totalAngleX);

        double sx = (v.getViewX() * zoomFactor) + offX;
        double sy = (v.getViewY() * zoomFactor) + offY;

        g2.setColor(color);
        g2.fill(new java.awt.geom.Ellipse2D.Double(sx - 2, sy - 2, 4, 4));
    }
    
    private void draw3DLine(Graphics2D g2, double x1, double y1, double z1, double x2, double y2, double z2, int offX, int offY, Color color) {
        Vertex v1 = new Vertex(x1, y1, z1);
        Vertex v2 = new Vertex(x2, y2, z2);
        v1.setViewX(x1 - camX); v1.setViewY(y1 - camY); v1.setViewZ(z1 - camZ);
        v2.setViewX(x2 - camX); v2.setViewY(y2 - camY); v2.setViewZ(z2 - camZ);

        v1.rotateViewY(totalAngleY); v1.rotateViewX(totalAngleX); v1.rotateViewZ(totalAngleZ);
        v2.rotateViewY(totalAngleY); v2.rotateViewX(totalAngleX); v2.rotateViewZ(totalAngleZ);

        double vx1 = v1.getViewX(), vy1 = v1.getViewY(), vz1 = v1.getViewZ();
        double vx2 = v2.getViewX(), vy2 = v2.getViewY(), vz2 = v2.getViewZ();

        if (perspectiveMode) {
            double near = -focalLength + 1.0;
            if (vz1 < near && vz2 < near) return;
            if (vz1 < near) {
                double t = (near - vz1) / (vz2 - vz1);
                vx1 += t * (vx2 - vx1); vy1 += t * (vy2 - vy1); vz1 = near;
            } else if (vz2 < near) {
                double t = (near - vz2) / (vz1 - vz2);
                vx2 += t * (vx1 - vx2); vy2 += t * (vy1 - vy2); vz2 = near;
            }
            double w1 = focalLength + vz1, w2 = focalLength + vz2;
            double sx1 = (vx1 * focalLength / w1 * zoomFactor) + offX;
            double sy1 = (vy1 * focalLength / w1 * zoomFactor) + offY;
            double sx2 = (vx2 * focalLength / w2 * zoomFactor) + offX;
            double sy2 = (vy2 * focalLength / w2 * zoomFactor) + offY;
            g2.setColor(color);
            g2.draw(new java.awt.geom.Line2D.Double(sx1, sy1, sx2, sy2));
        } else {
            double sx1 = (vx1 * zoomFactor) + offX;
            double sy1 = (vy1 * zoomFactor) + offY;
            double sx2 = (vx2 * zoomFactor) + offX;
            double sy2 = (vy2 * zoomFactor) + offY;
            g2.setColor(color);
            g2.draw(new java.awt.geom.Line2D.Double(sx1, sy1, sx2, sy2));
        }
    }

    private static Shape getObjectAt(double x, double y) {
        ArrayList<Shape> localShapes = new ArrayList<>();
        int index = 0;
        //m.out.println("\n(" + x + ", " + y + ")");
        for (Shape shape : shapes) {
            //System.out.println("Checking shape: " + shape.toString());
            for (Triangle t : shape.getTriangles()) {
                if (t.isInside(x, y)) {
                    localShapes.add(shape);
                    index++;
                }
            }
        }
        if (localShapes.size() > 0) {
            //System.out.println("\tSelected Shape: " + localShapes.get(0).toString());
            return localShapes.get(0);
        }
        else if (index > 0) {
            double closestZ = localShapes.get(0).getZAt(x, y, perspectiveMode, focalLength);
            Shape closestShape = localShapes.get(0);
            for (int i = 0; i < index; i++) {
                Shape s = localShapes.get(i);
                if (s.getZAt(x, y, perspectiveMode, focalLength) > closestZ) {
                    closestZ = s.getZAt(x, y, perspectiveMode, focalLength);
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