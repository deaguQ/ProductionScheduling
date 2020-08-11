package main.java.ga;

import main.java.entity.Machine;
import main.java.entity.Workpiece;
import main.java.util.FileReadUtil;

import java.util.ArrayList;

public class GAMain {
    public static void main(String[] args) {
        testOne();
    }

    private static void testOne() {
        System.out.println("begin");
        long beginT = System.currentTimeMillis();
        long beginM = Runtime.getRuntime().freeMemory();

        ArrayList<Machine> machineList = FileReadUtil.getAllMachine();
        ArrayList<Workpiece> workpieceList = FileReadUtil.getAllWorkpiece();
        //sort all the scenery according to viewCount
//		Collections.sort(sceneryList);
//		Collections.reverse(sceneryList);

        GA ga = new GA(300, 1000, 0.9, 0.9);
        ga.init(workpieceList,machineList);
        ga.run();

        System.out.println("-----split-------");

        long tmpDelay = System.currentTimeMillis() - beginT;
        long tmpMem = (beginM - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        System.out.println("耗时：" + tmpDelay + "ms  内存：" + tmpMem + "M");
    }
}
