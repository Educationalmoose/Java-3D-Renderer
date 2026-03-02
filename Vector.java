public class Vector {
    private Vertex start;
    private Vertex end;

    private double x;
    private double y;
    private double z;

    private double magnitude;

    public Vector(Vertex start, Vertex end) {
        this.end = end;
        this.start = start;
        this.x = end.getX() - start.getX();
        this.y = end.getY() - start.getY();
        this.z = end.getZ() - start.getZ();
        this.magnitude = Math.sqrt(x * x + y * y + z * z);
    }

    public Vector(double x, double y, double z) {
        this.start = new Vertex(0,0,0);
        this.end = new Vertex(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
        this.magnitude = Math.sqrt(x * x + y * y + z * z);

    }

    public Vector crossProduct(Vector v) {
        double i = (y * v.z) - (z * v.y);
        double j = (z * v.x) - (x * v.z);
        double k = (x * v.y) - (y * v.x);
        return new Vector(i, j, k);
    }

    public Vector normalize() {
        if (magnitude == 0) 
            return new Vector(0, 0, 0);
        return new Vector(x / magnitude, y / magnitude, z / magnitude);
    }

    public double dotProduct(Vector other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public double getMagnitude() {
        return this.magnitude;
    }

    public double getX() {
        return this.x;
    }
    public double getY() {
        return this.y;
    }
    public double getZ() {
        return this.z;
    }
}
