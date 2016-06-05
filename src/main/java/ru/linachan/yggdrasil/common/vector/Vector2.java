package ru.linachan.yggdrasil.common.vector;

public class Vector2<X, Y> {

    private X x;
    private Y y;

    public Vector2(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }

    public void setX(X x) {
        this.x = x;
    }

    public void setY(Y y) {
        this.y = y;
    }
}
