package main.java.util;

import main.java.entity.Machine;
import main.java.entity.Workpiece;

import java.util.ArrayList;

public class FileReadUtil {
    public static ArrayList<Machine> getAllMachine() {
        ArrayList<Machine> res=new ArrayList<>();
        res.add(new Machine(1,50));
        res.add(new Machine(2,18));
        return res;
    }

    public static ArrayList<Workpiece> getAllWorkpiece() {
        ArrayList<Workpiece> res=new ArrayList<>();
        res.add(new Workpiece(1,16,5));
        res.add(new Workpiece(2,24,7));
        res.add(new Workpiece(3,26,8));
        res.add(new Workpiece(4,14,6));
        res.add(new Workpiece(5,9,4));
        res.add(new Workpiece(6,18,8));
        return res;
    }
}
