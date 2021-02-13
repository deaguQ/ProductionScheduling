package main.java.old.entity;

public class Machine {
    public int id;
    public int capacity;
    //机器类型：熔练，铸造，热处理
    public int type;
    public int curT;
    public int v;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Machine(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public int getType() {
        return type;
    }

    public int getCurT() {
        return curT;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCurT(int curT) {
        this.curT = curT;
    }
}
