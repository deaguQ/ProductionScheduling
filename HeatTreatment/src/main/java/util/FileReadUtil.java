package main.java.util;

import main.java.entity.Machine;
import main.java.entity.Workpiece;

import java.util.ArrayList;
//todo 下一步真正实现从文件中读数据
public class FileReadUtil {
    public static ArrayList<Machine> getAllMachine() {
        ArrayList<Machine> res=new ArrayList<>();
        res.add(new Machine(1,50));
        res.add(new Machine(2,18));
        return res;
    }

    public static ArrayList<Workpiece> getAllWorkpiece() {
        ArrayList<Workpiece> res=new ArrayList<>();
        res.add(new Workpiece(1,3,5));
        res.add(new Workpiece(2,2,12));
        res.add(new Workpiece(3,5,4));
        res.add(new Workpiece(4,5,4));
        res.add(new Workpiece(5,8,9));
        res.add(new Workpiece(6,9,12));
        res.add(new Workpiece(7,9,5));
        res.add(new Workpiece(8,6,13));
        res.add(new Workpiece(9,5,13));
        res.add(new Workpiece(10,9,4));
        res.add(new Workpiece(11,5,10));
        res.add(new Workpiece(12,7,9));
        res.add(new Workpiece(13,5,14));
        res.add(new Workpiece(14,9,12));
        res.add(new Workpiece(15,8,15));
        return res;
    }
}
