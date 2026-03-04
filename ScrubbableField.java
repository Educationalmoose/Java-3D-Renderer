import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ScrubbableField extends JTextField {
    private double min, max, sensitivity;
    private int lastX;
    private Consumer<Double> onValueChange;

    public ScrubbableField(double initialValue, double min, double max, double sensitivity) {
        super(String.valueOf(initialValue), 5);
        this.min = min;
        this.max = max;
        this.sensitivity = sensitivity;
    
        MouseAdapter scrubber = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                requestFocusInWindow();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

                    double currentVal = Double.parseDouble(getText());
                    int dx = e.getX() - lastX;
                    
                    double newVal = currentVal + (dx * sensitivity);
                    
                    // clamp the value between min and max
                    newVal = Math.max(min, Math.min(max, newVal));

                    setText(String.format("%.2f", newVal));
                    lastX = e.getX();

                    if (onValueChange != null) {
                        onValueChange.accept(newVal);
                    }
                } catch (NumberFormatException ex) {
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        };

        this.addMouseListener(scrubber);
        this.addMouseMotionListener(scrubber);
    }

    public void setOnValueChange(Consumer<Double> listener) {
        this.onValueChange = listener;
    }

    public double getValue() {
        try {
            return Double.parseDouble(getText());
        } catch (NumberFormatException e) {
            return min;
        }
    }
}