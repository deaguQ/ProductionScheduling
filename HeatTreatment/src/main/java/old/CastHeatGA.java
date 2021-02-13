package main.java.old;

import main.java.old.entity.Machine;
import main.java.old.entity.Workpiece;

import java.util.*;


public class CastHeatGA {

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
    private List<Pair> workpiecesPair;



    /**
     * 机器集合数组
     */
    private ArrayList<Machine>[] machines;

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
     * 工序数：默认为3
     */
    private int orderNumber=3;

    /**
     *
     * @param scale 种群规模
     * @param maxGen 运行代数
     * @param pc 交叉概率
     * @param pm 变异概率
     */
    public CastHeatGA(int scale, int maxGen, double pc, double pm){
        this.scale = scale;
        this.maxGen = maxGen;
        this.pc = pc;
        this.pm = pm;
    }

    /**
     * 从文件中加载工件信息和机器信息
     */
    public void init(ArrayList<Workpiece> workpieces,ArrayList<Machine>[] machines){
        this.workpieces = workpieces;
        this.machines = machines;
        this.workpiecesNum = workpieces.size();
        this.oldPopulation = new int[scale][workpiecesNum];
        this.newPopulation = new int[scale][workpiecesNum];
        this.fitness = new double[scale];
        this.pi=new double[scale];
        this.bestChromosome=new int[workpiecesNum];
        this.random = new Random(System.currentTimeMillis());
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
//            printChromosome(oldPopulation[i]);

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
    static class Pair {
        int index;
        int time;
        private Pair(int workPieceID,int finishTime){
            this.index =workPieceID;
            this.time =finishTime;
        }

    }
    /**
     * 计算适应度
     * @param index 表示某个体下标
     * @param chromosome 染色体
     */
    public double evaluate(int index, int[] chromosome) {

        //熔铸解码过程->得到每个工件的铸造工序完工时间
        int[] copy=Arrays.copyOf(chromosome,chromosome.length);//记录工件优先级
        for(int i=0;i<orderNumber-1;i++){
            workpiecesPair=new ArrayList<>();
            for(int j:copy){
                //选择出当前工序最先空闲的机器
                int curMachineIndex=getFreeMachine(machines[i]);
                //更新当前机器时间和工件时间
                machines[i].get(curMachineIndex).curT+=workpieces.get(j).times[i];
                workpieces.get(j).curT+=workpieces.get(j).times[i];
                workpiecesPair.add(new Pair(j,workpieces.get(j).curT));
            }
            //根据工件当前工序的完成时间更新优先级
            copy=refresh();
        }

        //热处理过程
        Set<Integer> unHandle = new HashSet<>();//当前未处理工件集合
        for (int i : copy)
            unHandle.add(i);
        int curBatch = 1;//当前批次
        int curMachine = 1;//当前机器号
        int curLimit = machines[orderNumber-1].get(curMachine - 1).getCapacity();//当前批次还能容许的体积
        int lastFinishTime=0;
        while (!unHandle.isEmpty()) {//当存在未安排的工件时不断进行分批处理
            //按顺序扫描所有工件，若能加入当前批次，则从工件集合中删去，并更新染色体，否则继续循环
            int curMaxTime = Integer.MIN_VALUE;//当前批次所需加工时间
            for (int i : copy) {//遍历一边当前未安排工件
                if (unHandle.contains(i) && curLimit >= workpieces.get(i).getV()) {//当前机器还能容纳下工件
                    unHandle.remove(i);
//                    System.out.println("工件"+i+"在第"+curBatch+"批次，在"+curMachine+"号机器上");
                    curLimit -= workpieces.get(i).getV();
                    curMaxTime = Math.max(curMaxTime, workpieces.get(i).getT());//批次的加工时间等于批次中工件的最长加工时间
                    lastFinishTime=workpieces.get(i).curT;//批次中最后一个工件的前道工序的完工时间
                }
            }
            //机器的开始加工时间为当前空闲时间和批次中最后一个工件的前道工序的完工时间取最大值
            machines[orderNumber-1].get(curMachine-1).curT=Math.max(lastFinishTime,machines[orderNumber-1].get(curMachine-1).curT)+curMaxTime;
            if (!unHandle.isEmpty()) {
                curBatch++;
                curMachine=getFreeMachine(machines[orderNumber-1])+1;
                curLimit = machines[orderNumber-1].get(curMachine - 1).getCapacity();

            }
        }
        //寻找最大完工时间,即所有机器的最大工作时间
        int maxT=Integer.MIN_VALUE;
        for(int j = 0; j< machines[orderNumber-1].size(); j++){
            maxT=Math.max(maxT, machines[orderNumber-1].get(j).curT);
        }
//        System.out.println("最大完工时间为"+maxT);
        fitness[index]=1/(double)maxT;
        //重置工件，机器的当前时间
        reset();
        return fitness[index];
    }

    /**
     * 重置工件，机器的当前时间
     */
    private void reset(){
        for(Workpiece w:workpieces){
            w.curT=0;
        }
        for(int i = 0; i< machines.length; i++){
            for(Machine m: machines[i]){
                m.curT=0;
            }
        }
    }

    /**
     * 根据当前工件的完工时间确定新的优先级数组
     * @return
     */
    private int[] refresh() {
        Collections.sort(workpiecesPair, new Comparator<Pair>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                return o1.time-o2.time;
            }
        });
        int[] res=new int[workpiecesPair.size()];
        for(int i=0;i<res.length;i++){
            res[i]=workpiecesPair.get(i).index;
        }
        return res;
    }

    /**
     * 获得最先空闲机器的下标位置
     * @param machines 当前工序机器列表
     * @return 列表下标位置
     */
    private int getFreeMachine(ArrayList<Machine> machines) {
        int res=0;
        int curT=machines.get(res).getCurT();
        for(int i=1;i<machines.size();i++){
            int tmp=machines.get(i).getCurT();
            if(tmp<curT){
                res=i;
                curT=machines.get(i).getCurT();
            }
        }
        return res;
    }

    /**
     * 计算种群中各个个体的累积概率，
     * 前提是已经计算出各个个体的适应度fitness[max]，
     * 作为赌轮选择策略一部分，Pi[max]
     */
    private void countRate () {
        double sumFitness = 0;
        for (int i = 0; i < scale; i++) {
            sumFitness += fitness[i];
        }

        //计算概率
        for (int i = 0; i < scale; i++) {
            this.pi[i] = fitness[i] / sumFitness;
        }
    }

    /**
     *  挑选某代种群中适应度最高的个体，直接复制到子代中，
     *  前提是已经计算出各个个体的适应度Fitness[max]
     */
    private void selectBest () {
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
    private void select () {
        int selectId = 0;
        double tmpRan;
        double tmpSum;
        for (int i = 1; i < scale; i++) {
            tmpRan = (double) ((getRandomNum() % 1000) / 1000.0);
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
    private void copyChromosome ( int curP, int oldP){
        for (int i = 0; i < workpiecesNum; i++) {
            newPopulation[curP][i] = oldPopulation[oldP][i];
        }
    }

    /**
     * 生成一个0-65535之间的随机数
     * @return
     */
    private int getRandomNum () {
        return this.random.nextInt(65535);
    }


    /**
     * 交叉算子，两点交叉,相邻染色体交叉产生不同子代染色体
     * 这里存在问题
     * @param k1 染色体编号 14|653|72 ->     14|371|72->54|371|62
     * @param k2 染色体编号 26|371|45 ->     26|653|45->21|653|45
     */
    private void crossover ( int k1, int k2){
//        printChromosome(newPopulation[k1]);
//        printChromosome(newPopulation[k2]);
//        System.out.println("*******");
        //随机发生交叉的位置[pos1,pos2]
        int pos1 = getRandomNum() % workpiecesNum;
        int pos2 = getRandomNum() % workpiecesNum;
        //确保pos1和pos2两个位置不同
//        while(pos1 == pos2){
//            pos2 = getRandomNum() % workpiecesNum;
//        }
//        if (pos1 == pos2)
//            pos2 = pos1 + 1;

        //确保pos1小于等于pos2
        if (pos1 > pos2) {
            int tmpPos = pos1;
            pos1 = pos2;
            pos2 = tmpPos;
        }
        boolean[] flag1 = new boolean[workpiecesNum];
        boolean[] flag2 = new boolean[workpiecesNum];
        //交换两条染色体中间部分
        for (int i = pos1; i <= pos2; i++) {
            int t = newPopulation[k1][i];
            newPopulation[k1][i] = newPopulation[k2][i];
            flag1[newPopulation[k1][i]] = true;
            newPopulation[k2][i] = t;
            flag2[newPopulation[k2][i]] = true;
        }
        //检查染色体中重复工件号并替换
        for (int i = 0; i < workpiecesNum; i++) {
            if ((i < pos1 || i > pos2) && flag1[newPopulation[k1][i]])//非交叉点位间出现重复
                for (int j = 0; i < workpiecesNum; j++) {
                    if (!flag1[j]) {
                        newPopulation[k1][i] = j;
                        flag1[j] = true;
                        break;
                    }
                }
            else//忘记加这一步导致程序错误
                flag1[newPopulation[k1][i]]=true;
            if ((i < pos1 || i > pos2) && flag2[newPopulation[k2][i]])//非交叉点位间出现重复
                for (int j = 0; i < workpiecesNum; j++) {
                    if (!flag2[j]) {
                        newPopulation[k2][i] = j;
                        flag2[j] = true;
                        break;
                    }
                }
            else//忘记加这一步导致程序错误
                flag2[newPopulation[k2][i]]=true;

        }
//        printChromosome(newPopulation[k1]);
//        printChromosome(newPopulation[k2]);
//        System.out.println("------------");

    }
    /**
     * 变异操作,两点变异，随机生成两个基因位，并交换两个基因的位置
     * @param k 染色体标号
     */
    public void mutation ( int k){
        int pos1 = getRandomNum() % workpiecesNum;
        int pos2 = getRandomNum() % workpiecesNum;
        while (pos1 == pos2) {
            pos2 = getRandomNum() % workpiecesNum;
        }
        swap(newPopulation[k], pos1, pos2);

    }
    /**
     * 进化函数，正常交叉变异
     */
    private void evolution () {
        // 挑选某代种群中适应度最高的个体
        selectBest();
        // 赌轮选择策略挑选scale-1个下一代个体
        select();

        double ran;
        for (int i = 0; i < scale; i = i + 2) {
            ran = random.nextDouble();
            if (ran < this.pc) {
                //如果小于pc，则进行交叉
                crossover(i, i + 1);
//                System.out.println("-----");
            }
            else {
                //否则，进行变异
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
     * 打印染色体
     */
    public void printArray(int[] arr){
        for(int i=0;i<arr.length;i++){
            System.out.print(arr[i]+" ");
        }
        System.out.println();
    }
    /**
     * 解决问题
     */
    public void run () {
        //初始化种群
        initGroup();
        //计算初始适度
        for (int i = 0; i < scale; i++) {
            evaluate(i, oldPopulation[i]);
        }
        // 计算初始化种群中各个个体的累积概率，pi[max]
        countRate();
        //开始进化
        for (curGen = 0; curGen < maxGen; curGen++) {
            //do select, crossover and mutation operator
            evolution();
            //计算当前代的适度
            for (int i = 0; i < scale; i++) {
                evaluate(i, newPopulation[i]);
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
     * 解码
     */
    private void decodeChromosome () {
        HashSet<Integer> set = new HashSet<>();
        for (int i : bestChromosome){
            set.add(i);
        }
        printArray(bestChromosome);
        //熔铸解码过程->得到每个工件的铸造工序完工时间
        int[] copy=Arrays.copyOf(bestChromosome,bestChromosome.length);//记录工件优先级
        for(int i=0;i<orderNumber-1;i++){
            workpiecesPair=new ArrayList<>();
            for(int j:copy){
                //选择出当前工序最先空闲的机器
                int curMachineIndex=getFreeMachine(machines[i]);
                //更新当前机器时间和工件时间
                machines[i].get(curMachineIndex).curT+=workpieces.get(j).v/1;
                workpieces.get(j).curT+=workpieces.get(j).times[i];
                workpiecesPair.add(new Pair(j,workpieces.get(j).curT));
            }
            //根据工件当前工序的完成时间更新优先级
            copy=refresh();
        }

        //热处理过程
        Set<Integer> unHandle = new HashSet<>();//当前未处理工件集合
        for (int i : copy)
            unHandle.add(i);
        int curBatch = 1;//当前批次
        int curMachine = 1;//当前机器号
        int curLimit = machines[orderNumber-1].get(curMachine - 1).getCapacity();//当前批次还能容许的体积
        int lastFinishTime=0;
        while (!unHandle.isEmpty()) {//当存在未安排的工件时不断进行分批处理
            //按顺序扫描所有工件，若能加入当前批次，则从工件集合中删去，并更新染色体，否则继续循环
            int curMaxTime = Integer.MIN_VALUE;//当前批次所需加工时间
            for (int i : copy) {//遍历一边当前未安排工件
                if (unHandle.contains(i) && curLimit >= workpieces.get(i).getV()) {//当前机器还能容纳下工件
                    unHandle.remove(i);
//                    System.out.println("工件"+i+"在第"+curBatch+"批次，在"+curMachine+"号机器上");
                    curLimit -= workpieces.get(i).getV();
                    curMaxTime = Math.max(curMaxTime, workpieces.get(i).getT());//批次的加工时间等于批次中工件的最长加工时间
                    lastFinishTime=workpieces.get(i).curT;//批次中最后一个工件的前道工序的完工时间
                }
            }
            //机器的开始加工时间为当前空闲时间和批次中最后一个工件的前道工序的完工时间取最大值
            machines[orderNumber-1].get(curMachine-1).curT=Math.max(lastFinishTime,machines[orderNumber-1].get(curMachine-1).curT)+curMaxTime;
            if (!unHandle.isEmpty()) {
                curBatch++;
                curMachine=getFreeMachine(machines[orderNumber-1])+1;
                curLimit = machines[orderNumber-1].get(curMachine - 1).getCapacity();

            }
        }
        //寻找最大完工时间,即所有机器的最大工作时间
        int maxT=Integer.MIN_VALUE;
        for(int j = 0; j< machines[orderNumber-1].size(); j++){
            maxT=Math.max(maxT, machines[orderNumber-1].get(j).curT);
        }
        System.out.println("最大完工时间为"+maxT);

/*
        int curBatch = 1;//当前批次
        int curMachine = 1;//当前机器号
        int[] curMachineTime = new int[machineNum];//当前机器工作时间
        int curLimit = machines.get(curMachine - 1).getCapacity();//当前批次还能容许的体积
        List<Integer> start_time=new ArrayList<>();//每一个批次的开始时间
        List<Integer> duration_time=new ArrayList<>();//每一个批次的持续时间
        List<Integer> machine_start=new ArrayList<>();//每一个批次对应机器
        List<String> p_g=new ArrayList<>();//每一个批次的所有加工工件

        while (!set.isEmpty()) {//当存在未安排的工件时不断进行分批处理
            //按顺序扫描所有工件，若能加入当前批次，则从工件集合中删去，并更新染色体，否则继续循环
            int curMaxTime = Integer.MIN_VALUE;//当前批次所需加工时间
            start_time.add(curMachineTime[curMachine-1]);
            machine_start.add(curMachine);
            String curWorkpieces="' ";
            for (int i : bestChromosome) {//遍历一边当前未安排工件
                if (set.contains(i) && curLimit >= workpieces.get(i).getV()) {//当前机器还能容纳下工件
                    set.remove(i);
                    curWorkpieces=curWorkpieces+(i+1)+" ";
                    System.out.println( "机器"+curMachine +"---"+"批次"+curBatch +"---工件" + (i + 1));
                    curLimit -= workpieces.get(i).getV();
                    curMaxTime = Math.max(curMaxTime, workpieces.get(i).getT());//批次的加工时间等于批次中工件的最长加工时间
                }
            }
            curWorkpieces+="'";
            p_g.add(curWorkpieces);
            duration_time.add(curMaxTime);
            curMachineTime[curMachine - 1] += curMaxTime;
            System.out.println("批次"+curBatch+"在机器"+curMachine+"完工时间:"+curMachineTime[curMachine - 1]);
            System.out.println("-----------");
            //下一批次选择最先空闲的机器,这里存在一个问题可能会出现机器一个都装不下的情况
            if (!set.isEmpty()) {
                curBatch++;
                int curBound = 0;
                for (int i : bestChromosome) {//遍历一边当前未安排工件,找到curBound
                    if (set.contains(i)) {
                        curBound = workpieces.get(i).getV();
                        break;
                    }

                }
                for (int j = 0; j < machineNum; j++) {
                    if (curMachineTime[j] < curMachineTime[curMachine - 1] && machines.get(j).getCapacity() >= curBound)//防止一个都装不下的情况
                        curMachine = j + 1;
                }
                curLimit = machines.get(curMachine - 1).getCapacity();

            }

        }
        //寻找最大完工时间,即所有机器的最大工作时间
        int maxT = Integer.MIN_VALUE;
        for (int j = 0; j < curMachineTime.length; j++) {
            maxT = Math.max(maxT, curMachineTime[j]);
        }
        System.out.println("最大完工时间为" + maxT);
        printList(start_time);
        printList(duration_time);
        printList(machine_start);
        printList(p_g);*/
    }

    private void printList(List list) {
        for(Object i:list)
            System.out.print(i+",");
        System.out.println();
    }
    private void printList2(List list) {
        for(Object i:list)
            System.out.print(i+",");
        System.out.println();
    }

    /**
     * 甘特图
     */
    public void gant(){

    }
}
