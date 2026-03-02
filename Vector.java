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
        this.magnitude = Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z));
    }

    public Vector(double x, double y, double z) {
        this.start = new Vertex(0,0,0);
        this.end = new Vertex(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
        this.magnitude = Math.sqrt((x * x) + (y * y) + (z * z));

    }

    public Vector crossProduct(Vector v) {
        double i = (this.y * v.getZ()) - (this.z * v.getY());
        double j = (this.z * v.getX()) - (this.x * v.getZ());
        double k = (this.x * v.getY()) - (this.y * v.getX());
        return new Vector(i, j, k);
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
