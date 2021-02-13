package main.java.util;

import main.java.old.entity.Machine;
import main.java.old.entity.Workpiece;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
public class FileReadUtil {
    static String rootP="C:/Users/DELL/Desktop/硕毕设/顺利毕业/code/HeatTreatment/src/main/java/dataset";
    public static ArrayList<Machine> getHeatMachine() {
        ArrayList<Machine> res=new ArrayList<>();
        res.add(new Machine(1,50));
        res.add(new Machine(2,18));
        res.add(new Machine(3,20));
        return res;
    }
    public static ArrayList<Machine> getCastMachine() {
        ArrayList<Machine> res=new ArrayList<>();
        res.add(new Machine(1,450));
        res.add(new Machine(2,450));
        res.add(new Machine(3,450));
        return res;
    }
    public static ArrayList<Machine>[] getAllMachine() {
        ArrayList<Machine>[] res=new ArrayList[3];
        res[0]=getCastMachine();
        res[1]=getCastMachine();
        res[2]=getHeatMachine();
        return res;
    }
    public static ArrayList<Workpiece> getHeatWorkpiece() {
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
        res.add(new Workpiece(16,10,5));
        res.add(new Workpiece(17,9,7));
        res.add(new Workpiece(18,14,5));
        res.add(new Workpiece(19,12,9));
        res.add(new Workpiece(20,15,8));
        res.add(new Workpiece(21,5,3));
        res.add(new Workpiece(22,12,2));
        res.add(new Workpiece(23,4,5));
        res.add(new Workpiece(25,9,8));
        res.add(new Workpiece(26,12,9));
        res.add(new Workpiece(27,5,9));
        res.add(new Workpiece(28,13,6));
        res.add(new Workpiece(29,13,5));
        res.add(new Workpiece(30,4,9));
        return res;
    }
    public static ArrayList<Workpiece> getAllWorkpiece() {
        ArrayList<Workpiece> res=new ArrayList<>();
        res.add(new Workpiece(1,3,4,7,5));
        res.add(new Workpiece(2,2,4,8,12));
        res.add(new Workpiece(3,5,3,6,4));
        res.add(new Workpiece(4,5,6,11,4));
        res.add(new Workpiece(5,8,4,8,9));
        res.add(new Workpiece(6,9,4,6,12));
        res.add(new Workpiece(7,9,5,9,5));
        res.add(new Workpiece(8,6,4,7,13));
        res.add(new Workpiece(9,5,4,8,13));
        res.add(new Workpiece(10,9,3,6,4));
        res.add(new Workpiece(11,5,3,6,10));
        res.add(new Workpiece(12,7,5,9,9));
        res.add(new Workpiece(13,5,4,7,14));
        res.add(new Workpiece(14,9,3,6,12));
        res.add(new Workpiece(15,8,4,7,15));
        return res;
    }
//    C:\Users\DELL\Desktop\硕毕设\顺利毕业\code\HeatTreatment\src\main\java\dataset
    public static int[][] readW(String path) {
        int[][] result=null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(rootP+"/"+path+"/weight_"+path+".txt"));
            String[] s_w=in.readLine().split(" ");
            in.close();
            result=new int[4][s_w.length];
            for(int i=0;i<s_w.length;i++){
                result[0][i]=Integer.parseInt(s_w[i]);
            }
            in=new BufferedReader(new FileReader(rootP+"/"+path+"/type_"+path+".txt"));
            for(int i=1;i<4;i++){
                String[] s_t=in.readLine().split(" ");
                for(int j=0;j<s_w.length;j++){
                    result[i][j]=Integer.parseInt(s_t[j]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        int[][] ints = readW("01");
        ArrayList<Integer>[] arrayLists = readV("01");
        int[][][] ints1 = readT("01");
    }

    public static ArrayList<Integer>[] readV(String path) {
        ArrayList<Integer>[] result=new ArrayList[3];
        try {
            BufferedReader in = new BufferedReader(new FileReader(rootP+"/"+path+"/v_"+path+".txt"));
            for(int i=0;i<3;i++){
                String[] s_v=in.readLine().split(" ");
                result[i]=new ArrayList<>();
                for(int j=0;j<s_v.length;j++){
                    result[i].add(Integer.valueOf(s_v[j]));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int[][][] readT(String path) {
        int[][][] result=new int[2][3][3];
        try {
            BufferedReader in = new BufferedReader(new FileReader(rootP+"/"+path+"/weight_"+path+".txt"));
            for(int i=0;i<1;i++){
                String[] s_t=in.readLine().split(" ");
                result[i][0][1]=result[i][1][0]=Integer.parseInt(s_t[0]);
                result[i][0][2]=result[i][2][0]=Integer.parseInt(s_t[1]);
                result[i][1][2]=result[i][2][1]=Integer.parseInt(s_t[2]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
