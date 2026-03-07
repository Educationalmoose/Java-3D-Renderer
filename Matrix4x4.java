public class Matrix4x4 {
    public double[][] m = new double[4][4];

    public Matrix4x4() {
        m[0][0] = 1; m[1][1] = 1; m[2][2] = 1; m[3][3] = 1;
    }

    public Matrix4x4 multiply(Matrix4x4 other) {
        Matrix4x4 result = new Matrix4x4();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                result.m[r][c] = this.m[r][0] * other.m[0][c] + this.m[r][1] * other.m[1][c] + this.m[r][2] * other.m[2][c] + this.m[r][3] * other.m[3][c];
            }
        }
        return result;
    }

    public Vertex multiplyVertex(Vertex v) {
        double x = v.getX() * m[0][0] + v.getY() * m[1][0] + v.getZ() * m[2][0] + m[3][0];
        double y = v.getX() * m[0][1] + v.getY() * m[1][1] + v.getZ() * m[2][1] + m[3][1];
        double z = v.getX() * m[0][2] + v.getY() * m[1][2] + v.getZ() * m[2][2] + m[3][2];
        double w = v.getX() * m[0][3] + v.getY() * m[1][3] + v.getZ() * m[2][3] + m[3][3];

        if (w != 0) {
            x /= w;
            y /= w;
            z /= w;
        }

        Vertex result = new Vertex(x, y, z);
        return result;
    }

    public static Matrix4x4 makeTranslation(double tx, double ty, double tz) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.m[3][0] = tx;
        matrix.m[3][1] = ty;
        matrix.m[3][2] = tz;
        return matrix;
    }

    public static Matrix4x4 makeScale(double sx, double sy, double sz) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.m[0][0] = sx;
        matrix.m[1][1] = sy;
        matrix.m[2][2] = sz;
        return matrix;
    }

    public static Matrix4x4 makeRotationX(double angle) {
        Matrix4x4 matrix = new Matrix4x4();
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);
        matrix.m[1][1] = cos;
        matrix.m[1][2] = sin;
        matrix.m[2][1] = -sin;
        matrix.m[2][2] = cos;
        return matrix;
    }

    public static Matrix4x4 makeRotationY(double angle) {
        Matrix4x4 matrix = new Matrix4x4();
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);
        matrix.m[0][0] = cos;
        matrix.m[0][2] = -sin;
        matrix.m[2][0] = sin;
        matrix.m[2][2] = cos;
        return matrix;
    }

    public static Matrix4x4 makeRotationZ(double angle) {
        Matrix4x4 matrix = new Matrix4x4();
        double cos = Math.cos(angle * Math.PI / 180);
        double sin = Math.sin(angle * Math.PI / 180);
        matrix.m[0][0] = cos;
        matrix.m[0][1] = sin;
        matrix.m[1][0] = -sin;
        matrix.m[1][1] = cos;
        return matrix;
    }

    public static Matrix4x4 makeProjection(double focalLength, boolean isPerspective) {
    Matrix4x4 matrix = new Matrix4x4();
    if (isPerspective && focalLength != 0) {
        matrix.m[2][3] = 1.0 / focalLength; 
        matrix.m[3][3] = 1.0;               
    }
    return matrix;
} 
}