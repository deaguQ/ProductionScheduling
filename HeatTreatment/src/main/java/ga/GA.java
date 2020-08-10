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
     * 最佳适应度
     */
    private double best;
    /**
     * 最佳出现代数
     */
    private int bestGen;
    /**
     * 最佳染色体
     */
    private int[] bestChromosome;

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
    }

    /**
     * 从文件中加载工件信息和机器信息
     */
    public void init(ArrayList<Workpiece> workpieces,ArrayList<Machine> machines){
        this.workpiecesNum=5;
        this.machineNum=2;
        //机器体积要从大到小排序
        this.workpieces=workpieces;
        this.machines=machines;
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
     * @param chromosome 染色体
     */
    public double evaluate(int index,int[] chromosome){
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
    /**
     * 计算种群中各个个体的累积概率，
     * 前提是已经计算出各个个体的适应度fitness[max]，
     * 作为赌轮选择策略一部分，Pi[max]
     */
    private void countRate(){
        double sumFitness = 0;
        for (int i = 0; i < scale; i++) {
            sumFitness += fitness[i];
        }

        //计算概率
        for (int i = 0; i < scale; i++) {
            this.pi[i] = fitness[i] / sumFitness ;
        }
    }
    /**
     *  挑选某代种群中适应度最高的个体，直接复制到子代中，
     *  前提是已经计算出各个个体的适应度Fitness[max]
     */
    private void selectBest(){
        int maxId = 0;
        double maxFitness = fitness[0];

        //save the best  id and fitness
        for (int i = 1; i < scale; i++) {
            //save the best chromosome
            if (maxFitness < fitness[i]) {
                maxFitness = fitness[i];
                maxId = i;
            }
        }

        //save the globally best chromosome
        if (best < maxFitness) {
            best = maxFitness;
            bestGen = curGen;
            for (int i = 0; i < workpiecesNum; i++) {
                bestChromosome[i] = oldPopulation[maxId][i];
            }
        }
        // copy the best chromosome into new population and put on the first of population
        this.copyChromosome(0, maxId);

    }
    /**
     * 选择算子，赌轮选择策略挑选scale-1个下一代个体
     */
    private void select(){
        int selectId = 0;
        double tmpRan;
        double tmpSum;
        for (int i = 1; i < scale; i++) {
            tmpRan = (double)((getRandomNum() % 1000) / 1000.0);
            tmpSum = 0.0;
            for (int j = 0; j < scale; j++) {
                selectId = j;
                tmpSum += this.pi[j];
                if (tmpSum > tmpRan) {
                    break;
                }
            }
            copyChromosome(i, selectId);
        }
    }
    /**
     * 复制染色体，将oldPopulation复制到newPopulation
     * @param curP 新染色体在种群中的位置
     * @param oldP 旧的染色体在种群中的位置
     */
    private void copyChromosome(int curP, int oldP){
        for (int i = 0; i < workpiecesNum; i++) {
            newPopulation[curP][i] = oldPopulation[oldP][i];
        }
    }

    /**
     * 生成一个0-65535之间的随机数
     * @return
     */
    private int getRandomNum(){
        return this.random.nextInt(65535);
    }
    // TODO: 2020/8/10 需要改进
    /**
     * 交叉算子，两点交叉,相邻染色体交叉产生不同子代染色体
     * @param k1 染色体编号 14|653|72 ->     46|371|52
     * @param k2 染色体编号 26|371|45 ->     27|653|14
     */
    private void crossover(int k1, int k2){
        //随机发生交叉的位置
        int pos1 = getRandomNum() % workpiecesNum;
        int pos2 = getRandomNum() % workpiecesNum;
        //确保pos1和pos2两个位置不同
        while(pos1 == pos2){
            pos2 = getRandomNum() % workpiecesNum;
        }

        //确保pos1小于pos2
        if (pos1 > pos2) {
            int tmpPos = pos1;
            pos1 = pos2;
            pos2 = tmpPos;
        }

        //交换两条染色体中间部分
        for (int i = pos1; i < pos2; i++) {
            int t = newPopulation[k1][i];
            newPopulation[k1][i] = newPopulation[k2][i];
            newPopulation[k2][i] = t;
        }
    }
    /**
     * 变异操作,两点变异，随机生成两个基因位，并交换两个基因的位置
     * @param k 染色体标号
     */
    public void mutation(int k){

    }
    /**
     * 进化函数，正常交叉变异
     */
    private void evolution(){
        // 挑选某代种群中适应度最高的个体
        selectBest();
        // 赌轮选择策略挑选scale-1个下一代个体
        select();

        double ran;
        for (int i = 0; i < scale; i = i+2) {
            ran = random.nextDouble();
            if (ran < this.pc) {
                //如果小于pc，则进行交叉
                crossover(i, i+1);
            }else{
                //否者，进行变异
                ran = random.nextDouble();
                if (ran < this.pm) {
                    //变异染色体i
                    mutation(i);
                }

                ran = random.nextDouble();
                if (ran < this.pm) {
                    //变异染色体i+1
                    mutation(i + 1);
                }
            }
        }
    }
    /**
     * 解决问题
     */
    public void run(){
        //初始化种群
        initGroup();
        //计算初始适度
        for (int i = 0; i < scale; i++) {
            fitness[i] = this.evaluate(i, oldPopulation[i]);
        }
        // 计算初始化种群中各个个体的累积概率，pi[max]
        countRate();
        //开始进化
        for (curGen = 0; curGen < maxGen; curGen++) {
            //do select, crossover and mutation operator
            evolution();
            //计算当前代的适度
            for (int i = 0; i < scale; i++) {
                fitness[i] = this.evaluate(i, newPopulation[i]);
            }
            //calculate the probability of each chromosome in population
            countRate();
            // 将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
            for (int i = 0; i < scale; i++) {
                for (int j = 0; j < workpiecesNum; j++) {
                    oldPopulation[i][j] = newPopulation[i][j];
                }
            }
        }
        //select the best
        selectBest();
        //解码，生成甘特图
        decodeChromosome();
    }

    /**
     * 解码，生成甘特图
     */
    private void decodeChromosome() {
    }



}
