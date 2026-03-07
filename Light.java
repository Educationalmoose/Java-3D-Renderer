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
        double dx = v.getX() - origin.getX();
        double dy = v.getY() - origin.getY();
        double dz = v.getZ() - origin.getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz) / distanceScale;
        
        if (distance < 0.001) distance = 0.001;

        double tempIntensity = 1 / (distance * distance);
        
        return tempIntensity; 
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public double getIntensity() {
        return this.intensity;
    }
}
