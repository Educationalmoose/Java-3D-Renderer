public class Vertex {
    private double x,y,z;
    private double viewx, viewy, viewz;
    
    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        
        resetView();
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

    public void setViewX(double x) {
        this.viewx = x;
    }
    public void setViewY(double y) {
        this.viewy = y;
    }
    public void setViewZ(double z) {
        this.viewz = z;
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

    public double getViewX() {
        return this.viewx;
    }
    public double getViewY() {
        return this.viewy;
    }
    public double getViewZ() {
        return this.viewz;
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
        double r = this.z * cos - this.x * sin;
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

    public void rotateViewX(double angle) {
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);

        double t = this.viewy * cos - this.viewz * sin;
        double r = this.viewz * cos + this.viewy * sin;
        this.viewy = t;
        this.viewz = r;
    }

    public void rotateViewY(double angle) {
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);

        double t = this.viewx * cos + this.viewz * sin;
        double r = this.viewz * cos - this.viewx * sin;
        this.viewx = t;
        this.viewz = r;
    }

    public void rotateViewZ(double angle) {
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);

        double t = this.viewx * cos - this.viewy * sin;
        double r = this.viewy * cos + this.viewx * sin;
        this.viewx = t;
        this.viewy = r;
    }

    public void resetView() {
        this.viewx = this.x;
        this.viewy = this.y;
        this.viewz = this.z;
    }

    public void subtract(Vertex v) {
        this.x = this.x - v.getX();
        this.y = this.y - v.getY();
        this.z = this.z - v.getZ();
        //return new Vertex(x, y, z);
    }

    public void add(Vertex v) {
        this.x = this.x + v.getX();
        this.y = this.y + v.getY();
        this.z = this.z + v.getZ();
        //return new Vertex(x, y, z);
    }

    public void scale(double factor) {
        this.x = this.x * factor;
        this.y = this.y * factor;
        this.z = this.z * factor;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

}
