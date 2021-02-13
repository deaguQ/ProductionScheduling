package main.java.algorithm;

import main.java.util.FileReadUtil;

import java.util.*;

public class Base {
    /**
     * 工件信息
     * 1.重量
     * 2.熔炼类型
     * 3.铸造类型
     * 4.热处理类型
     */
    public int[][] workpiece;

    /**
     * 工件数
     */
    public int wN;

    /**
     * 工序阶段数
     */
    public int oN;

    /**
     * 机器加工速度
     */
    public ArrayList<Integer>[] v;

    /**
     * 批处理每一批次的加工时间固定
     */
    public double[] bT;
    /**
     * 各道工序的机器数
     */
    public int[] m;

    /**
     * 调整时间
     */
    public int[][][] adjustT;

    /**
     * 种群
     */
    public int[][] population;

    /**
     * 适应度
     */
    public double[] fitness;

    /**
     * 排序后的索引
     */
    public int[] sortIndex;

    /**
     * 批处理机器容量
     */
    public int[] wLimit;
    /**
     * 当前迭代最优和全局最优
     */
    public int bestI;
    public int[] globalBest;
    public double globalBestF=Double.MIN_VALUE;

    /**
     * @param scale  种群规模
     * @param maxGen 运行代数
     * @param mp 变异概率
     * @param cp 交叉概率
     */

    public int scale;
    public int maxGen;
    public double mp;
    public double cp;


    /**
     * 初始化工件及机器数据
     */
    public void init(String path) {

        workpiece = FileReadUtil.readW(path);
        wN = workpiece[0].length;
        oN = 3;
        v = FileReadUtil.readV(path);
        adjustT = FileReadUtil.readT(path);
        population = new int[scale][wN * 6];
        fitness = new double[scale];
        m = new int[oN];
        m[0] = v[0].size();
        m[1] = v[1].size();
        m[2] = v[2].size();
        wLimit = new int[m[2]];
        bT = new double[m[2]];
        Arrays.fill(wLimit, 100);
        for (int i = 0; i < bT.length; i++) {
            bT[i] = round((double) wLimit[i] / v[2].get(i));
        }
    }
    /**
     * 种群初始化
     * 1.随机
     * 2.考虑机器负载
     */
    public void initGroup() {
        for (int i = 0; i < scale; i++) {


            if (i < scale/2) {

                //工序部分随机初始化
                for (int j = 0; j < wN * 3; j++) {
                    if (j < wN)
                        population[i][j] = j + 1;
                    else if (j < wN * 2)
                        population[i][j] = j + 1 - wN;
                    else
                        population[i][j] = j + 1 - 2 * wN;
                }
                //打散
                shuffle(population[i], wN * 3);

                //机器分配
                int[] tmp = new int[wN];
                for (int j = 0; j < wN * 3; j++) {
                    population[i][j + wN * 3] = randInt(1, m[tmp[population[i][j]-1]]);
                    tmp[population[i][j]-1]++;
                }
                fitness[i] = evaluate(population[i]);


            } else {
                //1.工序部分随机初始化
                for (int j = 0; j < wN * 3; j++) {
                    if (j < wN)
                        population[i][j] = j + 1;
                    else if (j < wN * 2)
                        population[i][j] = j + 1 - wN;
                    else
                        population[i][j] = j + 1 - 2 * wN;
                }
                //打散
                shuffle(population[i], wN * 3);

                //2.贪心方法选择机器：熔铸阶段选择完工时间最小的机器作为当前工序分配的机器
                int[] oS = new int[wN];   //当前工序所处阶段
                int[][] preW = new int[2][MAX(m)];    //各机器上道处理工件
                double[][] mT = new double[3][MAX(m)];  //各机器时间
                double[] wT = new double[wN]; //各工件时间
                for (int j = 0; j < wN * 3; j++) {
                    int curO=oS[population[i][j]-1];    //当前工序
                    if(curO==2){
                        population[i][j + wN * 3] = randInt(1, m[oS[population[i][j]-1]]);
                    }
                    else {
                        double min=Double.MAX_VALUE;
                        int choose=0;
                        for(int k=0;k<m[curO];k++){
                            double finish=
                                    Math.max(mT[curO][k]+ (preW[curO][k]==0?0:adjustT[curO][workpiece[curO + 1][population[i][j]-1]-1][workpiece[curO + 1][preW[curO][k]-1]-1]),wT[population[i][j]-1])
                                            +round((double) workpiece[0][population[i][j]-1] / v[curO].get(k)); //完工时间
                            if(finish<min){
                                min=finish;
                                choose=k;
                            }
                        }
                        population[i][j + wN * 3] =choose+1;
                        //更新
                        mT[curO][choose]=wT[population[i][j]-1]=min;
                        preW[curO][choose]=population[i][j];
                        oS[population[i][j]-1]++;
                    }

                }

            }
        }


    }
    public double evaluate(int[] chromosome) {
        double[][] mT = new double[3][MAX(m)];  //各机器时间
        double[] wT = new double[wN]; //各工件时间
        int[][] preW = new int[2][MAX(m)];    //各机器上道处理工件
        int[] oS = new int[wN];   //当前工序所处阶段
        LinkedList<Integer>[] wb = new LinkedList[m[2]];   //批处理机器上安排的工件
        for (int i = 0; i < wb.length; i++) {
            wb[i] = new LinkedList();
        }
        for (int i = 0; i < wN * 3; i++) {
            if (oS[chromosome[i]-1] != 2) {   //熔铸
                int curM = chromosome[i + wN * 3];
                double curT = round((double) workpiece[0][chromosome[i] - 1] / v[oS[chromosome[i]-1]].get(curM - 1));   //加工时间
                double aT = preW[oS[chromosome[i]-1]][curM - 1] == 0 ? 0 : adjustT[oS[chromosome[i]-1]][workpiece[oS[chromosome[i]-1] + 1][chromosome[i] - 1]-1][workpiece[oS[chromosome[i]-1] + 1][preW[oS[chromosome[i]-1]][curM - 1] - 1]-1];  //调整时间
                //更新机器时间,工件时间
                mT[oS[chromosome[i]-1]][curM - 1] = (Math.max(mT[oS[chromosome[i]-1]][curM - 1]+curT,wT[chromosome[i] - 1]) + aT);
                wT[chromosome[i] - 1] = mT[oS[chromosome[i]-1]][curM - 1];
                oS[chromosome[i]-1]++;
            } else {  //热处理
                int curM = chromosome[i + wN * 3];
                wb[curM - 1].add(chromosome[i]);
            }
        }
        //开始对每个批处理机器上的工件进行分批处理
        for (int i = 0; i < m[2]; i++) {
            while (!wb[i].isEmpty()) {
                int fw = wb[i].pollFirst();   //每个批次的第一个工件
                int curT = workpiece[3][fw - 1];    //当前批次类型
                double aT = wT[fw - 1]; //开始时间
                int curW = workpiece[0][fw - 1];
                //进行扫描，扫描完一圈,下一个达到机器容量限制或不相容不加入到当前批
                Iterator<Integer> iterator = wb[i].iterator();
                while (iterator.hasNext()) {
                    int cur = iterator.next();
                    if (workpiece[3][cur - 1] == curT && curW + workpiece[0][cur - 1] < wLimit[i]) {
                        //更新
                        aT = Math.max(aT, wT[cur - 1]);
                        iterator.remove();
                    }
                }
                //时间更新
                mT[2][i] += (Math.max(aT,mT[2][i] ) + bT[i]);
            }
        }

        //获得最后完工时间
        double maxT = MAX(mT[2]);
        return 1/maxT;

    }

    //随机生成1-n的排列
    public static int[] random(int n) {
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            res[i] = i + 1;
        }
        shuffle(res, res.length);
        return res;
    }

    //洗牌算法
    public static void shuffle(int[] ids, int length) {
        for (int i = 0; i < length; i++) {
            swap(ids, i, randInt(i, length - 1));
        }
    }

    public static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static int MAX(int[] arr) {
        return Arrays.stream(arr).max().getAsInt();
    }

    public static double MAX(double[] arr) {
        return Arrays.stream(arr).max().getAsDouble();
    }

    /**
     * 保留小数点后两位
     *
     * @param a
     * @return
     */
    public static double round(double a) {
        return (double) (Math.round(a * 100) / 100.0);
    }

    /**
     * 获得排序后的索引
     *
     * @param a
     * @return
     */
    public static int[] argsort(final double[] a) {
        return argsort(a, true);
    }

    public static int[] argsort(final double[] a, final boolean ascending) {
        Integer[] indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Double.compare(a[i1], a[i2]);
            }
        });
        return asArray(indexes);
    }

    public static <T extends Number> int[] asArray(final T... a) {
        int[] b = new int[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[i].intValue();
        }
        return b;
    }

    public static int argmax(final double[] vec) {
        assert (vec != null);
        double max = vec[0];
        int argmax = 0;
        for (int i = 1; i < vec.length; i++) {
            final double tmp = vec[i];
            if (tmp > max) {
                max = tmp;
                argmax = i;
            }
        }
        return argmax;
    }
}
