public class Vertex {
    private int x, y, z;
    public Vertex(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    public void setZ(int z) {
        this.z = z;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getZ() {
        return z;
    }

    public Vertex rotateX(double angle) {
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);

        int x = this.x;
        int y = (int) (this.y * cos - this.z * sin);
        int z = (int) (this.z * cos + this.y * sin);
        return new Vertex(x, y, z);
    }

    public Vertex rotateY(double angle) {
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);

        int x = (int) (this.x * cos + this.z * sin);
        int y = this.y;
        int z = (int) (this.z * cos - this.x * sin);
        return new Vertex(x, y, z);
    }

    public Vertex rotateZ(double angle) {
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);

        int x = (int) (this.x * cos - this.y * sin);
        int y = (int) (this.y * cos + this.x * sin);
        int z = this.z;
        return new Vertex(x, y, z);
    }
}
