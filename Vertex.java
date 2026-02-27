public class Vertex {
    private double x, y, z;
    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
    public void setZ(double z) {
        this.z = z;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }

    public void translate(double dx, double dy, double dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    public void rotateX(double angle) {
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);

        double t = this.y * cos - this.z * sin;
        double r = this.z * cos + this.y * sin;
        this.y = t;
        this.z = r;
    }

    public void rotateY(double angle) {
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);

        double t = this.x * cos + this.z * sin;
        double r = this.z * cos - this.x * sin;;
        this.x = t;
        this.z = r;
    }

    public void rotateZ(double angle) {
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);

        double t = this.x * cos - this.y * sin;
        double r = this.y * cos + this.x * sin;
        this.x = t;
        this.y = r;
    }

    public void subtract(Vertex v) {
        this.x -= v.getX();
        this.y -= v.getY();
        this.z -= v.getZ();
    }

    public void add(Vertex v) {
        this.x += v.getX();
        this.y += v.getY();
        this.z += v.getZ();
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

}
