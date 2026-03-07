import java.awt.Color;

public class Light {
    Vertex origin;
    double intensity = 500;
    Color color = Color.WHITE;
    double distanceScale = 100;

    public Light(Vertex origin) {
        this.origin = origin;
    }

    public Light(Vertex origin, double intensity) {
        this.origin = origin;
        this.intensity = intensity;
    }

     public Light(Vertex origin, double intensity, Color color) {
        this.origin = origin;
        this.intensity = intensity;
        this.color = color;
    }

    public double getIntensityAt(Vertex v) {
        v.subtract(origin);
        double distance = Math.sqrt(v.getX() * v.getX() + v.getX() * v.getX() + v.getX() * v.getX()) / distanceScale;
        double tempIntensity = 1/(distance * distance);
        if (tempIntensity < 0.1)
            return 0;
        else
            return tempIntensity;
    }
}
