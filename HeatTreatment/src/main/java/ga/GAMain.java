package main.java.ga;

import main.java.entity.Machine;
import main.java.entity.Workpiece;
import main.java.util.FileReadUtil;

import java.util.ArrayList;
import java.util.Collections;

public class GAMain {
    public static void main(String[] args) {
        testTwo();
    }

    private static void testOne() {
        System.out.println("begin");
        long beginT = System.currentTimeMillis();
        long beginM = Runtime.getRuntime().freeMemory();
        ArrayList<Machine> machineList = FileReadUtil.getHeatMachine();
        ArrayList<Workpiece> workpieceList = FileReadUtil.getHeatWorkpiece();
        //机器容积从大到小排列
        Collections.sort(machineList, (o1, o2) -> o2.getCapacity()-o1.getCapacity());
        HeatGA ga = new HeatGA(500, 500, 0.9, 0.9);
        ga.init(workpieceList,machineList);
        ga.run();

        System.out.println("-----split-------");

        long tmpDelay = System.currentTimeMillis() - beginT;
        long tmpMem = (beginM - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        System.out.println("耗时：" + tmpDelay + "ms  内存：" + tmpMem + "M");
    }
    private static void testTwo(){
        System.out.println("begin");
        long beginT = System.currentTimeMillis();
        long beginM = Runtime.getRuntime().freeMemory();
        ArrayList<Workpiece> workpieceList = FileReadUtil.getAllWorkpiece();
        ArrayList<Machine>[] machines=FileReadUtil.getAllMachine();
        CastHeatGA ga=new CastHeatGA(500, 500, 0.9, 0.9);
        ga.init(workpieceList,machines);
        ga.run();
        System.out.println("-----split-------");
        long tmpDelay = System.currentTimeMillis() - beginT;
        long tmpMem = (beginM - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        System.out.println("耗时：" + tmpDelay + "ms  内存：" + tmpMem + "M");
    }

}
