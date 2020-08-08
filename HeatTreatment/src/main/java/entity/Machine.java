package main.java.entity;

public class Machine {
    private int id;
    private int capacity;

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
}
