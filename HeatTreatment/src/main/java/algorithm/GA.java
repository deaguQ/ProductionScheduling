package main.java.algorithm;

import main.java.util.FileReadUtil;

import java.util.Arrays;
import java.util.HashSet;

import static org.apache.commons.lang3.ArrayUtils.reverse;

public class GA extends Base{

    /**
     * 种群中个体的累计概率
     */
    private double[] pi;

    /**
     * 新的种群，子代种群
     */
    private int[][] newPopulation;

    /**
     * 当前运行代数
     */
    private int curGen;
    /**
     * 最佳适应度
     */
    private double best;
    /**
     * 最佳出现代数
     */
    private int bestGen;

    public GA(int scale, int maxGen,  double mp,double cp){
        this.scale = scale;
        this.maxGen = maxGen;
        this.mp = mp;
        this.cp = cp;
        pi=new double[scale];
    }

    @Override
    public void init(String path) {
        super.init(path);
        newPopulation=new int[scale][wN * 6];
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
     * 复制染色体，将oldPopulation复制到newPopulation
     * @param curP 新染色体在种群中的位置
     * @param oldP 旧的染色体在种群中的位置
     */
    private void copyChromosome ( int curP, int oldP){
        for (int i = 0; i < wN*6; i++) {
            newPopulation[curP][i] = population[oldP][i];
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
            globalBest= Arrays.copyOf(population[maxId],population[0].length);
        }
        // copy the best chromosome into new population and put on the first of population
        copyChromosome(0, maxId);
    }
    /**
     * 选择算子，赌轮选择策略挑选scale-1个下一代个体
     */
    private void select () {
        int selectId = 0;
        double tmpRan;
        double tmpSum;
        for (int i = 1; i < scale; i++) {
            tmpRan = Math.random();
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
    private void crossover(int p1, int p2) {
        int[] c1 = new int[newPopulation[p1].length];
        int[] c2 = new int[newPopulation[p1].length];

        //随机划分工件集为两个非空的子集j1 j2
        int numP1 = randInt(1, wN);
        while (numP1 == wN)
            numP1 = randInt(1, wN);
        int[] rw = random(wN);
        HashSet<Integer> j1 = new HashSet<>();
        HashSet<Integer> j2 = new HashSet<>();
        for (int i = 0; i < wN; i++) {
            if (i < numP1)
                j1.add(rw[i]);
            else
                j2.add(rw[i]);
        }
        //复制p1包含在J1的工件到C1，P2包含在J1的工件到C2 保留位置,
        // p1包含在j2的工件到c2剩余位置，p2包含在j2的工件到c1剩余位置
        for (int i = 0; i < wN * 3; i++) {
            if (j1.contains(newPopulation[p1][i])) {
                c1[i] = newPopulation[p1][i];
                c1[i + 3 * wN] = newPopulation[p1][i + 3 * wN];
            }
            if (j1.contains(newPopulation[p2][i])) {
                c2[i] =newPopulation[p2][i];
                c2[i + 3 * wN] = newPopulation[p2][i + 3 * wN];
            }
        }
        for (int i = 0; i < wN * 3; i++) {
            if (j2.contains(newPopulation[p1][i])) {
                for (int j = 0; j < wN * 3; j++) {
                    if (c2[j] == 0) {
                        c2[j] = newPopulation[p1][i];
                        c2[j + wN * 3] = newPopulation[p1][i + wN * 3];
                        break;
                    }
                }
            }
            if (j2.contains(newPopulation[p2][i])) {
                for (int j = 0; j < wN * 3; j++) {
                    if (c1[j] == 0) {
                        c1[j] = newPopulation[p2][i];
                        c1[j + wN * 3] = newPopulation[p2][i + wN * 3];
                        break;
                    }
                }
            }
        }

        newPopulation[p1]=c1;
        newPopulation[p2]=c2;

    }
    /**
     * 变异
     * 在机器分配部分采用单点变异法,在工序排序部分采用逆序变异法
     *
     * @param id
     */
    private void mutation(int id) {

        //工序部分
        int i1 = randInt(0, 3 * wN - 1);
        int i2 = randInt(0, 3 * wN - 1);
        while (i2 == i1)
            i2 = randInt(0, 3 * wN - 1);
        reverse(newPopulation[id], Math.min(i1, i2), Math.max(i1, i2));
        //机器分配部分
        int wid = randInt(0, 3 * wN - 1);
        int mid = newPopulation[id][wid + 3 * wN];
        //确认是哪个阶段的工序
        int o = 0;
        for (int j = 0; j < wid; j++) {
            if (newPopulation[id][j] == newPopulation[id][wid])
                o++;
        }
        if (m[o] != 1) {
            int newId = randInt(1, m[o]);
            while (newId == mid) {
                newId = randInt(1, m[o]);
            }
            newPopulation[id][wid + 3 * wN] = newId;
        }
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
            ran = Math.random();
            if (ran < cp) {
                //如果小于pc，则进行交叉
                crossover(i, i + 1);
            }
            else {
                //否则，进行变异
                ran = Math.random();
                if (ran < mp) {
                    //变异染色体i
                    mutation(i);
                }

                ran = Math.random();
                if (ran < mp) {
                    //变异染色体i+1
                    mutation(i + 1);
                }
            }
        }
    }


    public void run () {
        //初始化种群
        initGroup();

        for(int i=0;i<scale;i++){
            fitness[i] = evaluate(population[i]);
        }
        // 计算初始化种群中各个个体的累积概率，pi[max]
        countRate();
        //开始迭代
        for (curGen = 0; curGen < maxGen; curGen++) {
            //do select, crossover and mutation operator
            evolution();
            for(int i=0;i<scale;i++){
                fitness[i] = evaluate(newPopulation[i]);

            }
            //更新全局最优
            int k = argmax(fitness);
            if(fitness[k]>globalBestF){
                globalBestF = fitness[k];
            }
            System.out.println(""+curGen+":"+round(1/globalBestF));
            //calculate the probability of each chromosome in population
            countRate();
            // 将新种群newGroup复制到旧种群中，准备下一代进化
            for (int i = 0; i < scale; i++) {
                for (int j = 0; j < wN*6; j++) {
                    population[i][j] = newPopulation[i][j];
                }
            }
        }

    }

    public static void main(String[] args) {
        GA ga = new GA(1000,1000,0.6,0.6);
        ga.init("03");
        ga.run();
    }
}
