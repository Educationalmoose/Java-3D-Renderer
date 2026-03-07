import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main extends Canvas implements Runnable {
    private static ArrayList<Shape> shapes = new ArrayList<>();

    static JFrame frame;

    static JTextField xField;
    static JTextField yField;
    static JTextField zField;

    static ScrubbableField xFieldLight;
    static ScrubbableField yFieldLight;
    static ScrubbableField zFieldLight;
    static ScrubbableField intensityFieldLight;

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
    static double focalLength = 1000.0;

    static double scale = 1;
    static double zoomFactor = 1.0;
    static double sensitivity = 0.1;
    static double flySpeed = 5.0;
    static double accelerationSpeed = 1.0;

    static Light pointLight = new Light(new Vertex(0, -150, -100), 10.0, Color.WHITE);
    //static Vector lightDir = new Vector(0, 0, 1).normalize();

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

    private Thread thread;
    private boolean running = false;
    private int currentFPS = 0;
    private double currentFrameTimeMs = 0;

    static Matrix4x4 viewMatrix = new Matrix4x4();
    static Matrix4x4 projectionMatrix = new Matrix4x4();

    static java.awt.Robot robot;
    static Cursor blankCursor;
    static boolean isRightMouseDragging = false;
    static Point lockPos = null;

    static BufferedImage canvas;
    static double[] zBuffer;
    static int[] pixels;
    static int lastWidth = -1;
    static int lastHeight = -1;

    static {
        try {
            robot = new java.awt.Robot();
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            blankCursor = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "RenderThread");
        thread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            currentFrameTimeMs = (now - lastTime) / 1_000_000.0;
            lastTime = now;

            update();
            render();

            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                currentFPS = frames;
                frames = 0;
            }
        }
        stop();
    }

    private void update() {
        if (!pressedKeys.isEmpty()) {
            double[] fwd = localToWorld(0,  0,  1);
            double[] right = localToWorld(1,  0,  0);
            double[] up = localToWorld(0, -1,  0);

            double spd = flySpeed * accelerationSpeed * (currentFrameTimeMs / 10.0);
            boolean accelerating = pressedKeys.contains(java.awt.event.KeyEvent.VK_SHIFT);

            if (pressedKeys.contains(java.awt.event.KeyEvent.VK_A)) { camX -= right[0]*spd; camY -= right[1]*spd; camZ -= right[2]*spd; }
            if (pressedKeys.contains(java.awt.event.KeyEvent.VK_D)) { camX += right[0]*spd; camY += right[1]*spd; camZ += right[2]*spd; }
            if (perspectiveMode) {
                if (pressedKeys.contains(java.awt.event.KeyEvent.VK_W)) { camX += fwd[0]*spd; camY += fwd[1]*spd; camZ += fwd[2]*spd; }
                if (pressedKeys.contains(java.awt.event.KeyEvent.VK_S)) { camX -= fwd[0]*spd; camY -= fwd[1]*spd; camZ -= fwd[2]*spd; }
                if (pressedKeys.contains(java.awt.event.KeyEvent.VK_SPACE)) { camX += up[0]*spd; camY += up[1]*spd; camZ += up[2]*spd; }
                if (pressedKeys.contains(java.awt.event.KeyEvent.VK_CONTROL)) { camX -= up[0]*spd; camY -= up[1]*spd; camZ -= up[2]*spd; }
            }
            
            if (!accelerating) {
                accelerationSpeed = 1.0;
            } else {
                accelerationSpeed = Math.min(accelerationSpeed + 0.01, 2.0);
            }
        }

        Matrix4x4 camTranslation = Matrix4x4.makeTranslation(-camX, -camY, -camZ);
        Matrix4x4 focalPush = Matrix4x4.makeTranslation(0, 0, focalLength);
        Matrix4x4 focalPull = Matrix4x4.makeTranslation(0, 0, -focalLength);

        Matrix4x4 camRotX = Matrix4x4.makeRotationX(totalAngleX);
        Matrix4x4 camRotY = Matrix4x4.makeRotationY(totalAngleY);
        Matrix4x4 camRotZ = Matrix4x4.makeRotationZ(totalAngleZ);

        viewMatrix = camTranslation;
        
        if (perspectiveMode) {
            viewMatrix = viewMatrix.multiply(focalPush);
        }
        
        viewMatrix = viewMatrix.multiply(camRotY).multiply(camRotX).multiply(camRotZ);
        
        if (perspectiveMode) {
            viewMatrix = viewMatrix.multiply(focalPull);
        }

        projectionMatrix = Matrix4x4.makeProjection(focalLength, perspectiveMode);

        synchronized(shapes) {
            for (Shape s : shapes) {
                s.updateView(viewMatrix, projectionMatrix);
            }
        }
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            g.dispose();
            bs.show();
            return;
        }

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        if (width != lastWidth || height != lastHeight) {
            canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            pixels = ((java.awt.image.DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
            zBuffer = new double[width * height];
            lastWidth = width;
            lastHeight = height;
        }

        java.util.Arrays.fill(pixels, 0);
        java.util.Arrays.fill(zBuffer, Double.MAX_VALUE);
        
        renderScene(shapes.toArray(new Shape[0]), canvas, zBuffer);

        g.drawImage(canvas, 0, 0, null);

        if (showBoundingBox) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(width / 2.0, height / 2.0);
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
            g2.translate(width / 2.0, height / 2.0);
            g2.scale(zoomFactor, zoomFactor);
            g2.setStroke(new BasicStroke((float)(2.0 / zoomFactor)));
            selectedShape.drawLocalSelectionBox(g2);
            selectedShape.drawTranslationGizmo(g2, viewMatrix, projectionMatrix, zoomFactor, perspectiveMode, focalLength);
            g2.dispose();
        }

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString("FPS: " + currentFPS, 10, 20);
        g.drawString(String.format("Frame Time: %.2f ms", currentFrameTimeMs), 10, 40);

        g.dispose();
        bs.show();
    }
    public void addShape(Shape s) {
        shapes.add(s);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("3D Renderer");
        Main panel = new Main();

        panel.setPreferredSize(new Dimension(900, 900));

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


        showBoundingBoxCheckBox.addActionListener(e -> Main.showBoundingBox = showBoundingBoxCheckBox.isSelected());
        showGridCheckBox.addActionListener(e -> Main.showGrid = showGridCheckBox.isSelected());
        showWireframeCheckBox.addActionListener(e -> Main.wireframeMode = showWireframeCheckBox.isSelected());

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
        });

        fovField.addActionListener(e -> {
            try {
                Main.focalLength = Double.parseDouble(fovField.getText());
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
                double tempScale = Double.parseDouble(scaleField.getText());
                if (tempScale <= 0.3)
                    scale = 0.3;
                if (tempScale >= 5)
                    scale = 5;
                for (Shape s : shapes) {
                    s.scaleBy(scale / s.getScale());
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Scale must be a valid number");
            }
        });

        JPanel lightPanel = new JPanel();
        lightPanel.setLayout(new BoxLayout(lightPanel, BoxLayout.Y_AXIS));
        JPanel lightVectorGrid = new JPanel();
        lightVectorGrid.setLayout(new GridLayout(4, 2, 5, 5));

        JLabel lightPanelLabel = new JLabel("Light");
        JLabel xLabelLight = new JLabel("X:");
        JLabel yLabelLight = new JLabel("Y:");
        JLabel zLabelLight = new JLabel("Z:");
        JLabel intensityLabelLight = new JLabel("Intensity:");
        xFieldLight = new ScrubbableField(0.0, -1000.0, 1000.0, 1.0);
        xFieldLight.setOnValueChange(val -> {
            Main.pointLight.origin.setX(val);
        });
        
        yFieldLight = new ScrubbableField(-150.0, -1000.0, 1000.0, 1.0);
        yFieldLight.setOnValueChange(val -> {
            Main.pointLight.origin.setY(val);
        });
        
        zFieldLight = new ScrubbableField(-100.0, -1000.0, 1000.0, 1.0);
        zFieldLight.setOnValueChange(val -> {
            Main.pointLight.origin.setZ(val);
        });

        intensityFieldLight = new ScrubbableField(pointLight.getIntensity(), 0.0, 100.0, 0.05); 
        intensityFieldLight.setOnValueChange(val -> {
            Main.pointLight.setIntensity(val);
        });

        lightVectorGrid.add(xLabelLight); lightVectorGrid.add(xFieldLight);
        lightVectorGrid.add(yLabelLight); lightVectorGrid.add(yFieldLight);
        lightVectorGrid.add(zLabelLight); lightVectorGrid.add(zFieldLight);
        lightVectorGrid.add(intensityLabelLight); lightVectorGrid.add(intensityFieldLight);

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
            if(selectedShape != null) {
                selectedShape.translateShape(val - selectedShape.getCenter().getX(), 0, 0);
            }
        });
        yFieldShape = new ScrubbableField(selectedShape == null ? 0.0 : selectedShape.getCenter().getY(), -100000.0, 100000.0, 0.1);
        yFieldShape.setOnValueChange(val -> {
            if(selectedShape != null) {
                selectedShape.translateShape(0, val - selectedShape.getCenter().getY(), 0);
            }
        });
        zFieldShape = new ScrubbableField(selectedShape == null ? 0.0 : selectedShape.getCenter().getZ(), -100000.0, 100000.0, 0.1);
        zFieldShape.setOnValueChange(val -> {
            if(selectedShape != null) {
                selectedShape.translateShape(0, 0, val - selectedShape.getCenter().getZ());
            }
        });

        shapePositionGrid.add(xLabelShape); shapePositionGrid.add(xFieldShape);
        shapePositionGrid.add(yLabelShape); shapePositionGrid.add(yFieldShape);
        shapePositionGrid.add(zLabelShape); shapePositionGrid.add(zFieldShape);

        selectedShapePanelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        shapePositionGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        selectedShapePanel.add(selectedShapePanelLabel);
        selectedShapePanel.add(shapePositionGrid);
        
        lightPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectedShapePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        eastPanel.add(selectedShapePanel); eastPanel.add(Box.createVerticalStrut(20));
        eastPanel.add(lightPanel); eastPanel.add(Box.createVerticalStrut(20));
        eastPanel.add(fovPanel); eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(scalePanel); eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(showBoundingBoxCheckBox); eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(showGridCheckBox); eastPanel.add(Box.createVerticalStrut(20));
        eastPanel.add(showWireframeCheckBox); eastPanel.add(Box.createVerticalStrut(10));
        eastPanel.add(perspectiveCheckBox);

        eastWrapper.add(eastPanel, BorderLayout.NORTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openObjItem = new JMenuItem("Open");

        JMenu importMenu = new JMenu("Import");
        JMenuItem importItem = new JMenuItem("Add OBJ");

        fileMenu.setMnemonic('F');
        openObjItem.setMnemonic('O');
        importItem.setMnemonic('I');

        newItem.addActionListener(e -> {
            scale = 1; scaleField.setText("1.0");
            totalAngleX = 0; totalAngleY = 0; totalAngleZ = 0;
            camX = 0; camY = 0; camZ = 0;
            shapes.clear();
        });

        openObjItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("3D Model Files (.obj)", "obj");
            fileChooser.setFileFilter(filter);

            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                Shape objShape = panel.parseObjFile(fileChooser.getSelectedFile().getAbsolutePath());
                if (objShape != null) {
                    shapes.clear();
                    scale = 1; scaleField.setText("1.0");
                    totalAngleX = 0; totalAngleY = 0; totalAngleZ = 0;
                    camX = 0; camY = 0; camZ = 0;
                    shapes.add(objShape);
                    objShape.updateView(viewMatrix, projectionMatrix);
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to load OBJ file.");
                }
            }
        });

        importItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("3D Model Files (.obj)", "obj");
            fileChooser.setFileFilter(filter);

            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                Shape objShape = panel.parseObjFile(fileChooser.getSelectedFile().getAbsolutePath());
                if (objShape != null) {
                    shapes.add(objShape);
                    objShape.updateView(viewMatrix, projectionMatrix);
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to load OBJ file.");
                }
            }
        });

        JPopupMenu shapeMenu = new JPopupMenu();
        JMenuItem colorItem = new JMenuItem("Change Color");
        JMenuItem deleteItem = new JMenuItem("Delete");

        colorItem.addActionListener(e -> {
            if (selectedShape != null) {
                Color newColor = JColorChooser.showDialog(frame, "Choose Shape Color", Color.GREEN);
                if (newColor != null) {
                    for (Triangle t : selectedShape.getTriangles()) {
                        t.setColor(newColor);
                    }
                }
            }
        });

        deleteItem.addActionListener(e -> {
            if (selectedShape != null) {
                synchronized(shapes) {
                    shapes.remove(selectedShape);
                    deleteObject(selectedShape);
                    selectedShape = null;
                    
                    xFieldShape.setText("0.0");
                    yFieldShape.setText("0.0");
                    zFieldShape.setText("0.0");
                }
            }
        });

        shapeMenu.add(colorItem);
        shapeMenu.addSeparator();
        shapeMenu.add(deleteItem);
        

        MouseAdapter mouseAdapter = new MouseAdapter() {
            boolean dragging = false;
            @Override
            public void mousePressed(MouseEvent e) {
                pressPoint = e.getPoint(); 
                lastMousePos = e.getPoint();

                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    isRightMouseDragging = true;
                    panel.setCursor(blankCursor);
                    lockPos = e.getLocationOnScreen();
                } else if (selectedShape != null) {
                    draggingAxis = selectedShape.getGizmoHit(
                        e.getX(), e.getY(), panel.getWidth() / 2.0, panel.getHeight() / 2.0, 
                        zoomFactor, viewMatrix, projectionMatrix, perspectiveMode, focalLength
                    );
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isRightMouseDragging && lockPos != null) {
                    Point currentScreenPos = e.getLocationOnScreen();
                    int dx = currentScreenPos.x - lockPos.x;
                    int dy = currentScreenPos.y - lockPos.y;
                    dragging = true;

                    if (dx != 0 || dy != 0) {
                        sensitivity = 0.1;
                        totalAngleX = Math.max(-89.0, Math.min(89.0, totalAngleX + (dy * sensitivity))); 
                        totalAngleY -= dx * sensitivity;
                        
                        if (robot != null) {
                            robot.mouseMove(lockPos.x, lockPos.y);
                        }
                    }
                } else if (lastMousePos != null) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;

                    synchronized(shapes) {
                        if (draggingAxis != null && selectedShape != null) {
                            Vector worldAxis = new Vector(0, 0, 0);
                            if (draggingAxis.equals("X")) worldAxis = new Vector(1, 0, 0);
                            else if (draggingAxis.equals("Y")) worldAxis = new Vector(0, -1, 0);
                            else if (draggingAxis.equals("Z")) worldAxis = new Vector(0, 0, 1);

                            Vector screenVector = new Vector(worldAxis.getX(), worldAxis.getY(), worldAxis.getZ());
                            screenVector.rotateY(totalAngleY); screenVector.rotateX(totalAngleX); screenVector.rotateZ(totalAngleZ);

                            double screenX = screenVector.getX(), screenY = screenVector.getY();
                            double mag = Math.sqrt(screenX * screenX + screenY * screenY);
                            double moveAmount = (mag > 0.0001) ? ((dx * (screenX / mag) + dy * (screenY / mag)) / zoomFactor) : (dx / zoomFactor);

                            selectedShape.translateShape(worldAxis.getX() * moveAmount, worldAxis.getY() * moveAmount, worldAxis.getZ() * moveAmount);

                            xFieldShape.setText(String.format("%.2f", selectedShape.getCenter().getX()));
                            yFieldShape.setText(String.format("%.2f", -selectedShape.getCenter().getY()));
                            zFieldShape.setText(String.format("%.2f", selectedShape.getCenter().getZ()));
                        }
                    }
                    lastMousePos = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPaused = false; 
                draggingAxis = null;

                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    isRightMouseDragging = false;
                    panel.setCursor(Cursor.getDefaultCursor());
                    lockPos = null;
                }

                if (pressPoint != null && pressPoint.distance(e.getPoint()) < 5) {
                    double worldX = (e.getX() - panel.getWidth() / 2.0) / Main.zoomFactor;
                    double worldY = (e.getY() - panel.getHeight() / 2.0) / Main.zoomFactor;
                    selectedShape = getObjectAt(worldX, worldY);

                    if (selectedShape != null) {
                        xFieldShape.setText(String.format("%.2f", selectedShape.getCenter().getX()));
                        yFieldShape.setText(String.format("%.2f", -selectedShape.getCenter().getY()));
                        zFieldShape.setText(String.format("%.2f", selectedShape.getCenter().getZ()));
                    } else {
                        xFieldShape.setText("0.0");
                        yFieldShape.setText("0.0");
                        zFieldShape.setText("0.0");
                    }

                    if (javax.swing.SwingUtilities.isRightMouseButton(e) && selectedShape != null && !dragging) {
                        shapeMenu.show(panel, e.getX(), e.getY());
                    }
                    dragging = false;
                }
            }
        };

        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
        panel.addMouseWheelListener(e -> {
            double rotation = e.getPreciseWheelRotation();
            if (rotation == 0) return;
            zoomFactor *= (rotation < 0) ? 1.05 : 0.95;
            scaleField.setText(String.format("%.2f", zoomFactor));
        });

        panel.setFocusable(true);
        panel.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { panel.requestFocusInWindow(); } });
        panel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e)  { pressedKeys.add(e.getKeyCode()); }
            public void keyReleased(java.awt.event.KeyEvent e) { pressedKeys.remove(e.getKeyCode()); }
        });

        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(openObjItem);
        menuBar.add(fileMenu);

        importMenu.add(importItem);
        menuBar.add(importMenu);

        frame.setJMenuBar(menuBar);
        frame.add(eastWrapper, BorderLayout.EAST);

        // default cube shape
        Vertex vTLF = new Vertex(-50, -50, -50), vBLF = new Vertex(-50, 50, -50), vTRF = new Vertex(50, -50, -50), vBRF = new Vertex(50, 50, -50);
        Vertex vTLB = new Vertex(-50, -50, 50), vBLB = new Vertex(-50, 50, 50), vTRB = new Vertex(50, -50, 50), vBRB = new Vertex(50, 50, 50);
        Shape cube = new Shape(new Triangle[]{
            new Triangle(vTLF, vBLF, vTRF), new Triangle(vTRF, vBLF, vBRF),
            new Triangle(vTLB, vTRB, vBLB), new Triangle(vTRB, vBRB, vBLB),
            new Triangle(vTLB, vBLB, vTLF), new Triangle(vTLF, vBLB, vBLF),
            new Triangle(vTRB, vTRF, vBRB), new Triangle(vTRF, vBRF, vBRB),
            new Triangle(vTLB, vTLF, vTRB), new Triangle(vTRB, vTLF, vTRF),
            new Triangle(vBLB, vBRB, vBLF), new Triangle(vBLF, vBRB, vBRF)
        });

        panel.addShape(cube);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        panel.start();
    }

    private static double[] localToWorld(double lx, double ly, double lz) {
        Vertex dir = new Vertex(lx, ly, lz);
        dir.rotateViewZ(-totalAngleZ); dir.rotateViewX(-totalAngleX); dir.rotateViewY(-totalAngleY);
        return new double[]{ dir.getViewX(), dir.getViewY(), dir.getViewZ() };
    }

    public void renderScene(Shape[] localShapes, BufferedImage canvas, double[] zBuffer) {
        int offsetX = canvas.getWidth() / 2;
        int offsetY = canvas.getHeight() / 2;

        if (showGrid) drawGridIntoCanvas(canvas, zBuffer, offsetX, offsetY);

        if (wireframeMode) {
            Graphics2D g2 = (Graphics2D) canvas.getGraphics();
            g2.translate(offsetX, offsetY);
            g2.scale(zoomFactor, zoomFactor);
            g2.setStroke(new BasicStroke((float)(1.0 / zoomFactor)));
            
            for (Shape shape : localShapes) {
                for (Triangle t : shape.getTriangles()) {
                    if (perspectiveMode) {
                        double nearPlane = -focalLength + 0.1;
                        Vertex[] v = t.getVertices();
                        if (v[0].getViewZ() < nearPlane || v[1].getViewZ() < nearPlane || v[2].getViewZ() < nearPlane) {
                            continue; 
                        }
                    }

                    Vector viewNormal = t.getViewNormalVector();
                    double dot;
                    if (perspectiveMode) {
                        double camRayX = t.getVertices()[0].getViewX();
                        double camRayY = t.getVertices()[0].getViewY();
                        double camRayZ = t.getVertices()[0].getViewZ() + focalLength;
                        
                        dot = (camRayX * viewNormal.getX()) + (camRayY * viewNormal.getY()) + (camRayZ * viewNormal.getZ());
                    } else {
                        dot = viewNormal.getZ();
                    }

                    if (dot >= 0) continue;

                    t.drawWireframe(g2);
                }
            }
            g2.dispose();
        } else {
            Object[] rowLocks = new Object[canvas.getHeight()];
            for (int i = 0; i < rowLocks.length; i++) {
                rowLocks[i] = new Object();
            }

            synchronized(localShapes) {
                java.util.Arrays.stream(localShapes).flatMap(s -> java.util.Arrays.stream(s.getTriangles())).parallel().forEach(t -> {
                    if (perspectiveMode) {
                        double nearPlane = -focalLength + 0.1; 
                        Vertex[] v = t.getVertices();
                        if (v[0].getViewZ() < nearPlane || v[1].getViewZ() < nearPlane || v[2].getViewZ() < nearPlane) {
                            return; 
                        }
                    }

                    Vector viewNormal = t.getViewNormalVector();
                    double dot;
                    if (perspectiveMode) {
                        double camRayX = t.getVertices()[0].getViewX();
                        double camRayY = t.getVertices()[0].getViewY();
                        double camRayZ = t.getVertices()[0].getViewZ() + focalLength;
                        
                        dot = (camRayX * viewNormal.getX()) + (camRayY * viewNormal.getY()) + (camRayZ * viewNormal.getZ());
                    } else {
                        dot = viewNormal.getZ();
                    }

                    if (dot >= 0) return;


                    double[] bb = t.getBoundingBox();
                    int minX = Math.max(0, (int) (bb[0] * zoomFactor) + offsetX);
                    int maxX = Math.min(canvas.getWidth() - 1, (int) (bb[1] * zoomFactor) + offsetX);
                    int minY = Math.max(0, (int) (bb[2] * zoomFactor) + offsetY);
                    int maxY = Math.min(canvas.getHeight() - 1, (int) (bb[3] * zoomFactor) + offsetY);

                    Vector normal = t.getNormalVector();
                    int triangleColor = calculateColor(t.getColor(), t);

                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            double worldX = (x - offsetX) / zoomFactor;
                            double worldY = (y - offsetY) / zoomFactor;

                            if (t.isInside(worldX, worldY)) {
                                double currentZ = t.getZAt(worldX, worldY, perspectiveMode, focalLength);
                                int pixelIndex = y * canvas.getWidth() + x;
                                
                                synchronized(rowLocks[y]) {
                                    if (currentZ < zBuffer[pixelIndex]) {
                                        zBuffer[pixelIndex] = currentZ;
                                        pixels[pixelIndex] = triangleColor;
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    public int calculateColor(Color baseColor, Triangle t) {
        Vertex tCenter = t.getCenter();
        Vector normal = t.getNormalVector().normalize();
        
        Vector dirToLight = new Vector(
            pointLight.origin.getX() - tCenter.getX(),
            pointLight.origin.getY() - tCenter.getY(),
            pointLight.origin.getZ() - tCenter.getZ()
        ).normalize();

        double dot = normal.dotProduct(dirToLight);
        if (dot < 0) dot = 0; 

        double falloff = pointLight.getIntensityAt(tCenter) * pointLight.intensity;

        double finalIntensity = Math.min(1.0, 0.1 + (dot * falloff));

        return new Color(
            Math.min(255, (int)(baseColor.getRed() * finalIntensity)),
            Math.min(255, (int)(baseColor.getGreen() * finalIntensity)),
            Math.min(255, (int)(baseColor.getBlue() * finalIntensity))
        ).getRGB();
    }

    private Shape parseObjFile(String filePath) {
        File object = new File(filePath);
        ArrayList<Triangle> localTriangles = new ArrayList<>(); 
        ArrayList<Vertex> localVertices = new ArrayList<>();

        try(Scanner scanner = new Scanner(object)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("v ")) {
                    String[] parts = line.split("\\s+");
                    localVertices.add(new Vertex(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
                } else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");
                    int numVertices = parts.length - 1;
                    if (numVertices >= 3) {
                        int[] vertexIndices = new int[numVertices];
                        for (int i = 0; i < numVertices; i++) vertexIndices[i] = Integer.parseInt(parts[i + 1].split("/")[0]) - 1;
                        for (int i = 1; i < numVertices - 1; i++) {
                            localTriangles.add(new Triangle(localVertices.get(vertexIndices[0]), localVertices.get(vertexIndices[i]), localVertices.get(vertexIndices[i + 1])));
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) { e.printStackTrace(); }

        double cx = 0, cy = 0, cz = 0;
        for (Vertex v : localVertices) {
            cx += v.getX(); cy += v.getY(); cz += v.getZ();
        }
        cx /= localVertices.size(); 
        cy /= localVertices.size(); 
        cz /= localVertices.size();

        for (Vertex v : localVertices) {
            v.setX(v.getX() - cx);
            v.setY(v.getY() - cy);
            v.setZ(v.getZ() - cz);
        }

        double maxCoord = 0;
        for (Vertex v : localVertices) {
            maxCoord = Math.max(maxCoord, Math.max(Math.abs(v.getX()), Math.max(Math.abs(v.getY()), Math.abs(v.getZ()))));
        }

        if (maxCoord > 0 && maxCoord < 10) {
            double normalizationFactor = 100.0 / maxCoord;
            for (Vertex v : localVertices) {
                v.setX(v.getX() * normalizationFactor); v.setY(v.getY() * normalizationFactor); v.setZ(v.getZ() * normalizationFactor);
            }
        }

        return new Shape(localTriangles.toArray(new Triangle[0]), scale);
    }

    private void drawGridIntoCanvas(BufferedImage canvas, double[] zBuffer, int offX, int offY) {
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        int spacing = 100; 
        double buffer;

        if (perspectiveMode) {
            buffer = focalLength * 4.0;
        } else {
            buffer = Math.max((canvas.getWidth() / 2.0) / zoomFactor, (canvas.getHeight() / 2.0) / zoomFactor) * 2;
        }

        double minVisibleWorldX = camX - buffer, maxVisibleWorldX = camX + buffer;
        double minVisibleWorldZ = camZ - buffer, maxVisibleWorldZ = camZ + buffer;

        int startX = (int) (minVisibleWorldX / spacing) - 1;
        int endX = (int) (maxVisibleWorldX / spacing) + 1;
        int startZ = (int) (minVisibleWorldZ / spacing) - 1;
        int endZ = (int) (maxVisibleWorldZ / spacing) + 1;

        Color gridColor = new Color(100, 100, 100, 150);

        for (int i = startX; i <= endX; i++) draw3DLine(g2, i * spacing, 0.0, minVisibleWorldZ, i * spacing, 0.0, maxVisibleWorldZ, offX, offY, gridColor);
        for (int i = startZ; i <= endZ; i++) draw3DLine(g2, minVisibleWorldX, 0, i * spacing, maxVisibleWorldX, 0, i * spacing, offX, offY, gridColor);
        
        g2.dispose();
    }
    
    private void draw3DLine(Graphics2D g2, double x1, double y1, double z1, double x2, double y2, double z2, int offX, int offY, Color color) {
        Vertex v1 = new Vertex(x1, y1, z1), v2 = new Vertex(x2, y2, z2);
        v1.setViewX(x1 - camX); v1.setViewY(y1 - camY); v1.setViewZ(z1 - camZ);
        v2.setViewX(x2 - camX); v2.setViewY(y2 - camY); v2.setViewZ(z2 - camZ);

        if (perspectiveMode) {
            v1.setViewZ(v1.getViewZ() + focalLength);
            v2.setViewZ(v2.getViewZ() + focalLength);
        }

        v1.rotateViewY(totalAngleY); v1.rotateViewX(totalAngleX); v1.rotateViewZ(totalAngleZ);
        v2.rotateViewY(totalAngleY); v2.rotateViewX(totalAngleX); v2.rotateViewZ(totalAngleZ);

        if (perspectiveMode) {
            v1.setViewZ(v1.getViewZ() - focalLength);
            v2.setViewZ(v2.getViewZ() - focalLength);
        }

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
            double projX1 = (vx1 * focalLength / w1 * zoomFactor) + offX;
            double projY1 = (vy1 * focalLength / w1 * zoomFactor) + offY;
            double projX2 = (vx2 * focalLength / w2 * zoomFactor) + offX;
            double projY2 = (vy2 * focalLength / w2 * zoomFactor) + offY;

            if ((projX1 < -5000 && projX2 < -5000) || 
                (projX1 > 5000 && projX2 > 5000) || 
                (projY1 < -5000 && projY2 < -5000) || 
                (projY1 > 5000 && projY2 > 5000)) {
                return; 
            }

            g2.setColor(color);
            double[] seg = { projX1, projY1, projX2, projY2 };
            if (clipLineToScreen(seg, offX * 2, offY * 2)) {
                g2.draw(new java.awt.geom.Line2D.Double(seg[0], seg[1], seg[2], seg[3]));
            }
        } else {
            g2.setColor(color);
            g2.draw(new java.awt.geom.Line2D.Double((vx1 * zoomFactor) + offX, (vy1 * zoomFactor) + offY, (vx2 * zoomFactor) + offX, (vy2 * zoomFactor) + offY));
        }
    }
    private boolean clipLineToScreen(double[] seg, double screenW, double screenH) {
        double x1 = seg[0], y1 = seg[1], x2 = seg[2], y2 = seg[3];
        while (true) {
            int c1 = screenOutCode(x1, y1, screenW, screenH);
            int c2 = screenOutCode(x2, y2, screenW, screenH);
            if ((c1 | c2) == 0) {
                seg[0]=x1; seg[1]=y1; seg[2]=x2; seg[3]=y2;
                return true;
            }
            
            if ((c1 & c2) != 0) 
                return false;

            int c = (c1 != 0) ? c1 : c2;
            double x, y;

            if ((c & 8) != 0) {
                x = x1 + (x2-x1)*(screenH-y1)/(y2-y1); y = screenH;
            } else if ((c & 4) != 0) {
                x = x1 + (x2-x1)*(0-y1)/(y2-y1); y = 0; 
            } else if ((c & 2) != 0) {
                y = y1 + (y2-y1)*(screenW-x1)/(x2-x1); x = screenW;
            } else { 
                y = y1 + (y2-y1)*(0-x1)/(x2-x1);
                x = 0;
            }

            if (c == c1) {
                x1 = x; y1 = y;
            } else {
                x2 = x; y2 = y;
            }
        }
    }

    private int screenOutCode(double x, double y, double screenW, double screenH) {
        int c = 0;
        if (x < 0) c |= 1; else if (x > screenW) c |= 2;
        if (y < 0) c |= 4; else if (y > screenH) c |= 8;
        return c;
    }

    private static Shape getObjectAt(double x, double y) {
        ArrayList<Shape> localShapes = new ArrayList<>();
        int index = 0;
        for (Shape shape : shapes) {
            for (Triangle t : shape.getTriangles()) {
                if (t.isInside(x, y)) {
                    localShapes.add(shape);
                    index++;
                }
            }
        }
        if (localShapes.size() > 0) return localShapes.get(0);
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
        } else return null;
    }

    private static void deleteObject(Shape s) {
        shapes.remove(s);
    }
}