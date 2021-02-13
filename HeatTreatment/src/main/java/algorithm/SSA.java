package main.java.algorithm;

import main.java.util.FileReadUtil;

import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.reverse;

public class SSA extends Base{
    /**
     * @param p 发现者个数
     * @param ST 发现者预警值
     * @param d 意识到危险者个数
     * @param jn 锦标赛选择个数
     **/
    public int p;
    public double ST;
    public int d;
    public int jn;

    public SSA(int scale, int maxGen, double pn, double ST, double dn, double mp,double cp,int jn) {
        this.scale = scale;
        this.maxGen = maxGen;
        this.p = (int) Math.round(pn * scale);
        this.ST = ST;
        this.d = (int) Math.round(dn * scale);
        this.mp = mp;
        this.cp = cp;
        this.jn=jn;
    }



    /**
     * 主程序
     */
    public void run() {
        //1.种群初始化
        initGroup();
        for(int i=0;i<scale;i++){
            fitness[i] = evaluate(population[i]);
        }
        for (int i = 0; i < maxGen; i++) {
            //2.种群适应度排序,获得排序后的索引
            sortIndex = argsort(fitness, false);
            //3.发现者位置更新
            pUpdate();
            //获得当前迭代最优解
            bestI = argmax(fitness);
            //4.加入者位置更新
            jUpdate();

            //更新全局最优
            int j = argmax(fitness);
            if(fitness[j]>globalBestF){
                globalBestF = fitness[j];
                globalBest = Arrays.copyOf(population[j], population[j].length);
            }
            //5.意识到危险的麻雀位置更新
            dUpdate();

            //6.更新全局最优
            j = argmax(fitness);
            if(fitness[j]>globalBestF){
                globalBestF = fitness[j];
                globalBest = Arrays.copyOf(population[j], population[j].length);
            }
            System.out.println(""+i+":"+round(1/globalBestF));
        }
        //输出
        System.out.println("最优适应度为"+globalBestF+"最大完工时间："+round(1/globalBestF));

    }

    /**
     * 意识到危险的麻雀位置更新
     * 1.全局最优变异再与之交叉
     * 2.当前个体变异再随机与种群中的个体交叉
     */
    private void dUpdate() {
        //随机选择d个个体
        int[] r = random(scale);
        for (int i = 0; i < d; i++) {
            if (fitness[r[i] - 1] < globalBestF) {
                //全局最优变异再与之交叉
                int[] mut = mutation(globalBest);

                if(Math.random()<cp){
                    mut = crossover(population[r[i]-1], mut);

                }
                population[r[i]-1]=mut;
                fitness[r[i]-1] = evaluate(population[r[i]-1]);
            } else {
                int[] mut = mutation(population[r[i]-1]);
                if(Math.random()<cp){
                    mut = crossover(population[randInt(0,scale-1)], mut);

                }
                population[r[i]-1]=mut;
                fitness[r[i]-1] = evaluate(population[r[i]-1]);
            }
        }

    }

    /**
     * pox交叉
     *
     * @param p1
     * @param p2
     */
    private int[] crossover(final int[] p1, final int[] p2) {
        int[] c1 = new int[p1.length];
        int[] c2 = new int[p1.length];

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
            if (j1.contains(p1[i])) {
                c1[i] = p1[i];
                c1[i + 3 * wN] = p1[i + 3 * wN];
            }
            if (j1.contains(p2[i])) {
                c2[i] = p2[i];
                c2[i + 3 * wN] = p2[i + 3 * wN];
            }
        }
        for (int i = 0; i < wN * 3; i++) {
            if (j2.contains(p1[i])) {
                for (int j = 0; j < wN * 3; j++) {
                    if (c2[j] == 0) {
                        c2[j] = p1[i];
                        c2[j + wN * 3] = p1[i + wN * 3];
                        break;
                    }
                }
            }
            if (j2.contains(p2[i])) {
                for (int j = 0; j < wN * 3; j++) {
                    if (c1[j] == 0) {
                        c1[j] = p2[i];
                        c1[j + wN * 3] = p2[i + wN * 3];
                        break;
                    }
                }
            }
        }
        return evaluate(c1) > evaluate(c2) ? c1 : c2;
    }

    /**
     * 加入者位置更新
     * 1.与锦标赛法选出的个体随机交叉
     * 2.当前迭代最优个体随机变异再与之随机交叉
     */
    private void jUpdate() {
        for (int i = p; i < scale; i++) {
            if (i > scale / 2) {
                //与锦标赛法选出的个体交叉
                if(Math.random()<cp){
                    population[sortIndex[i]]=crossover(population[sortIndex[i]], population[match(jn)]);
                    fitness[sortIndex[i]] = evaluate(population[sortIndex[i]]);
                }

            } else {
                //当前迭代最优个体随机变异再与之随机交叉
                int[] mut = mutation(population[bestI]);
                if(Math.random()<cp){

                    mut = crossover(population[sortIndex[i]], mut);

                }
                population[sortIndex[i]]=mut;
                fitness[sortIndex[i]] = evaluate(population[sortIndex[i]]);
            }
        }
    }

    /**
     * 锦标赛选择，每次从种群随机选n个个体，随机选择其中最好的一个
     * @return
     */
    private int match(int n) {
        int[] r = random(wN);
        int res=0;
        double bestV=Double.MIN_VALUE;
        for(int i=0;i<n;i++){
            if(fitness[r[i]-1]>bestV)
            {
                bestV=fitness[r[i]-1];
                res=r[i]-1;
            }
        }
        return res;
    }

    /**
     * 发现者位置更新
     * 1.邻域搜索
     * 2.以一定的概率进行变异
     */
    private void pUpdate() {
        double r = Math.random();
        if (r < ST) {
            for (int i = 0; i < p; i++) {
                //邻域搜索
                neighborSearch(sortIndex[i]);
            }
        } else {
            for (int i = 0; i < p; i++) {
                //变异
                if(Math.random()<mp)
                    mutation(sortIndex[i]);
            }
        }
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
        reverse(population[id], Math.min(i1, i2), Math.max(i1, i2));
        //机器分配部分
        int wid = randInt(0, 3 * wN - 1);
        int mid = population[id][wid + 3 * wN];
        //确认是哪个阶段的工序
        int o = 0;
        for (int j = 0; j < wid; j++) {
            if (population[id][j] == population[id][wid])
                o++;
        }
        if (m[o] != 1) {
            int newId = randInt(1, m[o]);
            while (newId == mid) {
                newId = randInt(1, m[o]);
            }
            population[id][wid + 3 * wN] = newId;
        }
        //更新适应度
        fitness[id] = evaluate(population[id]);
    }

    private int[] mutation(int[] p) {
        if(Math.random()<mp){
            int[] res = Arrays.copyOf(p, p.length);
            //工序部分
            int i1 = randInt(0, 3 * wN - 1);
            int i2 = randInt(0, 3 * wN - 1);
            while (i2 == i1)
                i2 = randInt(0, 3 * wN - 1);
            reverse(res, Math.min(i1, i2), Math.max(i1, i2));
            //机器分配部分
            int wid = randInt(0, 3 * wN - 1);
            int mid = res[wid + 3 * wN];
            //确认是哪个阶段的工序
            int o = 0;
            for (int j = 0; j < wid; j++) {
                if (res[j] == res[wid])
                    o++;
            }
            if (m[o] != 1) {
                int newId = randInt(1, m[o]);
                while (newId == mid) {
                    newId = randInt(1, m[o]);
                }
                res[wid + 3 * wN] = newId;
            }
            return res;
        }else
            return p;

    }

    /**
     * 对id个体进行邻域搜索
     * 1.在工序部分随机选出两个元素值不同的元素,然后互换所选元素的位置
     * 2.在工序排序部分随机选出两个元素,然后将后面元素插入到前面元素之前的位置
     * 3.在机器分配部分随机选出一个元素,该元素对应工序的可选机器应多于一台,然后将此工序分配到其可加工机器集中的随机其他机器上
     *
     * @param id
     */
    private void neighborSearch(int id) {
        double bestF = fitness[id];
        int[] best = population[id];
        for (int i = 0; i < 20; i++) {
            int[] copy = Arrays.copyOf(population[id], population[id].length);
            int r = randInt(1, 3);
            if (r == 1) {
                int fid = randInt(0, 3 * wN - 1);
                int sid = randInt(0, 3 * wN - 1);
                while (copy[sid] == copy[fid])
                    sid = randInt(0, 3 * wN - 1);
                swap(copy, fid, sid);
                double curF = evaluate(copy);
                if (curF > bestF) {
                    best = copy;
                    bestF = curF;
                }
            } else if (r == 2) {
                int fid = randInt(0, 3 * wN - 1);
                int sid = randInt(0, 3 * wN - 1);
                while (fid == sid)
                    sid = randInt(0, 3 * wN - 1);
                int bid = Math.max(fid, sid);
                int tmp = copy[bid];
                for (int j = bid; j > fid; j--) {
                    copy[j] = copy[j - 1];
                }
                copy[fid] = tmp;
                double curF = evaluate(copy);
                if (curF > bestF) {
                    best = copy;
                    bestF = curF;
                }
            } else {
                int rid = randInt(0, 3 * wN - 1);
                int mid = copy[rid + 3 * wN];
                //确认是哪个阶段的工序
                int o = 0;
                for (int j = 0; j < rid; j++) {
                    if (copy[j] == copy[rid])
                        o++;
                }
                int rm = randInt(1, m[o]);
                while (mid == rm) {
                    rm = randInt(1, m[o]);
                }
                copy[rid + 3 * wN] = rm;
                double curF = evaluate(copy);
                if (curF > bestF) {
                    best = copy;
                    bestF = curF;
                }
            }
        }
        population[id] = best;
        fitness[id] = bestF;
    }







    public static void main(String[] args) {
        SSA ssa = new SSA(1000,1000,0.2,0.8,0.1,0.6,0.6,5);
        ssa.init("03");
        ssa.run();
    }
}
