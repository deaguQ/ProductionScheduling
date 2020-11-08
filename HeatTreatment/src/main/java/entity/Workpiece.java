package main.java.entity;

public class Workpiece {
    //工件id
    public int id;
    //工件体积
    public int v;
    //加工所需时间
    public int t;
    //各工序加工所需时间
    public int[] times;
    //当前时间
    public int curT;
    public Workpiece(int id, int v, int t) {
        this.id = id;
        this.v = v;
        this.t = t;
    }
    public Workpiece(int id, int v, int... t) {
        this.id = id;
        this.v = v;
        this.times = t;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }
}
