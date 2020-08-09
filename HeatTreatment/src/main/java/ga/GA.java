package main.java.ga;

import main.java.entity.Machine;
import main.java.entity.Workpiece;

import java.util.*;


public class GA {

    /**
     * 种群规模
     */
    private int scale;

    /**
     * 工件数量
     */
    private int workpiecesNum;
    /**
     * 工件集合
     */
    private ArrayList<Workpiece> workpieces;
    /**
     * 机器数量
     */
    private int machineNum;
    /**
     * 机器集合
     */
    private  ArrayList<Machine> machines;

    /**
     * 最大运行代数
     */
    private int maxGen;

    /**
     * 当前运行代数
     */
    private int curGen;

    /**
     * 交叉概率
     */
    private double pc;

    /**
     * 变异概率
     */
    private double pm;

    /**
     * 种群中个体的累计概率
     */
    private double[] pi;


    /**
     *  初始种群，父代种群
     */
    private int[][] oldPopulation;

    /**
     * 新的种群，子代种群
     */
    private int[][] newPopulation;

    /**
     * 种群适应度，表示种群中各个个体的适应度
     */
    private double[] fitness;

    /**
     * 随机数
     */
    private Random random;

    /**
     *
     * @param scale 种群规模
     * @param maxGen 运行代数
     * @param pc 交叉概率
     * @param pm 变异概率
     */
    public GA(int scale, int maxGen, double pc, double pm){
        this.scale = scale;
        this.maxGen = maxGen;
        this.pc = pc;
        this.pm = pm;
        this.machines=new ArrayList<Machine>();
        this.workpieces=new ArrayList<Workpiece>();
        init();
    }

    /**
     * 从文件中加载工件信息和机器信息
     */
    private void init(){
        this.workpiecesNum=5;
        this.machineNum=2;
        //机器体积从大到小排序
        machines.add(new Machine(0,5));
        machines.add(new Machine(1,5));
        workpieces.add(new Workpiece(0,1,1));
        workpieces.add(new Workpiece(1,2,2));
        workpieces.add(new Workpiece(2,3,3));
        workpieces.add(new Workpiece(2,4,4));
        workpieces.add(new Workpiece(2,5,5));
        this.oldPopulation=new int[scale][workpiecesNum];
        this.fitness=new double[scale];
    }

    /**
     * 初始化种群
     */
    public void initGroup(){
        for (int i = 0; i < scale; i++) {
            for(int j=0;j<workpiecesNum;j++){//工件初始排列0，1，2，3，4...
                oldPopulation[i][j]=j;
            }
            //将工件序号洗牌,例：0，1，2，3，4->2，3，1，4，0
            shuffle(oldPopulation[i]);
            for(int j=0;j<workpiecesNum;j++){
                System.out.print(oldPopulation[i][j]+" ");
            }
            System.out.println();

        }
    }
    //洗牌算法
    private void shuffle(int[] ids) {
        int len=ids.length;
        for(int i=0;i<len;i++){
            swap(ids,i,randInt(i,len-1));
        }
    }
    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
    private  int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    /**
     * 计算适应度
     * @param index 表示某个体下标
     */
    public double evaluate(int index){
        int[] chromosome=oldPopulation[index];//当前染色体
        HashSet<Integer> set=new HashSet<>();
        for(int i:chromosome)
            set.add(i);
        int curBatch=1;//当前批次
        int curMachine=1;//当前机器号
        int[] curMachineTime=new int[machineNum];//当前机器工作时间
        int curLimit=machines.get(curMachine-1).getCapacity();//当前批次还能容许的体积
        while(!set.isEmpty()){//当存在未安排的工件时不断进行分批处理
            //按顺序扫描所有工件，若能加入当前批次，则从工件集合中删去，并更新染色体，否则继续循环
            int curMaxTime=Integer.MIN_VALUE;//当前批次所需加工时间
            for(int i:chromosome) {//遍历一边当前未安排工件
                if (set.contains(i)&&curLimit >= workpieces.get(i).getV()) {//当前机器还能容纳下工件
                    set.remove(i);
                    System.out.println("工件"+i+"在第"+curBatch+"批次，在"+curMachine+"号机器上");
                    curLimit -= workpieces.get(i).getV();
                    curMaxTime=Math.max(curMaxTime,workpieces.get(i).getT());//批次的加工时间等于批次中工件的最长加工时间
                }
            }
            curMachineTime[curMachine-1]+=curMaxTime;
            //下一批次选择最先空闲的机器
            if(!set.isEmpty()){
                curBatch++;
                for(int j=0;j<machineNum;j++){
                    if(curMachineTime[j]<curMachineTime[curMachine-1])
                        curMachine=j+1;
                }
                curLimit=machines.get(curMachine-1).getCapacity();
            }

        }
        //寻找最大完工时间,即所有机器的最大工作时间
        int maxT=Integer.MIN_VALUE;
        for(int j=0;j<curMachineTime.length;j++){
            maxT=Math.max(maxT,curMachineTime[j]);
        }
        System.out.println("最大完工时间为"+maxT);
        fitness[index]=1/(double)maxT;
        return fitness[index];
    }
}
