package main.java.ga;

import main.java.entity.Gene;
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
    private Gene[] oldPopulation;

    /**
     * 新的种群，子代种群
     */
    private Gene[] newPopulation;

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
    }

    /**
     * 从文件中加载工件信息和机器信息
     */
    public void init(){

    }

    /**
     * 初始化种群
     */
    private void initGroup(){
        for (int i = 0; i < scale; i++) {
            oldPopulation[i]=new Gene();
            int[][] chromosome=new int[workpiecesNum][2];//第一列批次号，第二列机器号
            //未安排工件集合
            LinkedList<Workpiece> curWorkpieces=new LinkedList(workpieces);
            //将工件随机排序,例：0，1，2，3，4->2，3，1，4，0
            Collections.shuffle(curWorkpieces);
            int curBatch=1;//当前批次
            int curMachine=1;//当前机器号
            int[] curMachineTime=new int[machineNum];//当前机器工作时间
            int curLimit=machines.get(curMachine-1).getCapacity();//当前批次还能容许的体积
            while(!curWorkpieces.isEmpty()){//当存在未安排的工件时不断进行分批处理
                //按顺序扫描所有工件，若能加入当前批次，则从工件集合中删去，并更新染色体，否则继续循环
                Iterator<Workpiece> iterator = curWorkpieces.iterator();
                int curMaxTime=Integer.MIN_VALUE;//当前批次所需加工时间
                while (iterator.hasNext()) {
                    Workpiece curWorkpiece = iterator.next();
                    if (curLimit >= curWorkpiece.getV()) {
                        iterator.remove();
                        curLimit -= curWorkpiece.getV();
                        chromosome[curWorkpiece.getId()][0] = curBatch;
                        chromosome[curWorkpiece.getId()][1] = curMachine;
                        curMaxTime=Math.max(curMaxTime,curWorkpiece.getT());
                    }
                }
                curMachineTime[curMachine-1]+=curMaxTime;
                //下一批次选择最先空闲的机器
                if(!curWorkpieces.isEmpty()){
                    curBatch++;
                    for(int j=0;j<machineNum;j++){
                        if(curMachineTime[j]<curMachineTime[curMachine-1])
                            curMachine=j+1;
                    }
                }

            }
            oldPopulation[i].setchromosome(chromosome);
            //寻找最大完工时间
            int maxT=Integer.MIN_VALUE;
            for(int j=0;j<curMachineTime.length;j++){
                maxT=Math.max(maxT,curMachineTime[j]);
            }
            oldPopulation[i].setfitness(1/maxT);
        }
    }

    /**
     * 计算适应度
     */
    private double caculate(Gene gene){

        return gene.getfitness();
    }
}
