package main.java.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class ACO extends Base {
    public ACO(){

    }
    //工序总数
    private int aN;
    //机器总数
    private int allM;
    //各阶段机器集
    private ArrayList<Integer>[] machines;
    //每只蚂蚁上各工序选择机器
    private int[][] oFM;
    //各机器所安排工序集
    private ArrayList<Integer>[] oSeq;
    //排序后的工序集
    private ArrayList<Integer>[] oSeq2;
    //工序分派信息素矩阵
    private double[][] mTau;
    //工序分配启发信息矩阵
    private double[][] mEta;
    //工序排序信息素矩阵
    private double[][][] sTau;
    //信息素重要程度因子
    private double alpha;
    //启发信息重要程度因子
    private double beta;
    //各工件时间
    private double[] wT;
    @Override
    public void init(String path) {
        super.init(path);
        aN=wN*oN;
        oFM=new int[scale][aN];
        machines=new ArrayList[oN];
        int tmp=0;
        for(int i=0;i<oN;i++){
            machines[i]=new ArrayList<>();
            allM+=m[i];
            for(int j=1;j<=m[i];j++){
                machines[i].add(j+tmp);
                if(j==m[i])
                    tmp=j+tmp;
            }
        }
        oSeq=new ArrayList[allM];
        oSeq2=new ArrayList[allM];

        for(int i=0;i<allM;i++){
            oSeq[i]=new ArrayList<>();
            oSeq2[i]=new ArrayList<>();
        }
        //工序分派信息素矩阵初始化
        mTau=new double[aN][allM];
        //工序分派启发信息矩阵初始化
        mEta=new double[aN][allM];
        sTau=new double[allM][aN][aN];
        wT=new double[wN];
    }

    public void run(){
        for(int i=0;i<maxGen;i++){
            for(int j=0;j<scale;j++){   //每只蚂蚁构建一个可行解
                //1.工序分配:每道工序安排到哪个机器上
                for(int k=1;k<=aN;k++){
                    int curS=k%oN==0?oN:k%oN;   //当前工序阶段
                    //计算各机器选择概率
                    double[] pro=new double[machines[curS-1].size()];
                    double sum=0;
                    for(int l=0;l<pro.length;l++){
                        pro[l]=Math.pow(mTau[k-1][machines[curS-1].get(l)],alpha)*Math.pow(mEta[k-1][machines[curS-1].get(l)],beta);
                        sum+=pro[l];
                    }
                    for(int l=0;l<pro.length;l++){
                        pro[l]=pro[l]/sum;
                    }
                    //轮盘赌法选择机器
                    int mid=choose(pro);
                    //更新
                    oFM[j][k-1]=machines[curS-1].get(mid);
                    oSeq[mid].add(k);
                    //局部更新信息素
                }
                //2.工序排序：确定非批处理机上的工序加工顺序
                for(int k=1;k<=allM-m[oN-1];k++){   //遍历机器
                    int curN=oSeq[k-1].size();
                    if(curN==0)
                        continue;
                    for(int l=1;l<=curN;l++){   //确定机器k上第l个加工工序
                        //计算工序计算待选集概率
                        double[] pro=new double[oSeq[k-1].size()];

                        double sum=0;
                        for(int r=0;r<pro.length;r++){
                            //启发信息与调整时间的大小有关
                            int curEta=1;
                            if(l!=1){
                                //前道工件号，工序阶段
                                int preO=oSeq2[k-1].get(oSeq2[k-1].size()-1);
                                int preW=(preO-1)/oN+1;
                                int preS=preO%oN==0?oN:preO%oN;
                                int preT=workpiece[preS][preW-1];
                                int curT=workpiece[oSeq[k-1].get(r)%oN==0?oN:oSeq[k-1].get(r)%oN][(oSeq[k-1].get(r)-1)/oN];
                                curEta=1/(adjustT[preS][preT-1][curT-1]+1);
                                curEta=1/(adjustT[preS][preT-1][curT-1]+1);
                            }
                            pro[r]=Math.pow(sTau[k-1][oSeq[k-1].get(r)-1][l-1],alpha)*Math.pow(curEta,beta);
                            sum+=pro[r];
                        }
                        for(int r=0;l<pro.length;r++){
                            pro[r]=pro[r]/sum;
                        }
                        int ch=choose(pro);
                        oSeq2[k-1].add(oSeq[k-1].get(ch));
                        //更新候选集
                        oSeq[k-1].remove(ch);
                    }
                }
                //3.计算每个工件进行批处理工序前的完工时间
                for(int k=1;k<=2;k++){
                    for(int r:machines[k-1]){ //阶段k的所有机器
                        int mm=0;
                        double preT=0; //机器时间
                        for(int l=0;l<oSeq2[r-1].size();l++){   //机器r上的工序序列
                            int curAT=0;    //调整时间
                            if(l!=0){
                                curAT=adjustT[k-1][getType(oSeq2[r-1].get(r))-1][getType(oSeq2[r-1].get(r-1))-1];
                            }
                            double curStartT=Math.max(preT+curAT,wT[getWId(oSeq2[r-1].get(l)-1)]);
                            //更新
                            double curProcessT=round((double) workpiece[0][getWId(oSeq2[r-1].get(l))]/v[getStage(oSeq2[r-1].get(l))].get(mm));
                            wT[getWId(oSeq2[r-1].get(l)-1)]+=(curStartT+curProcessT);
                            preT=wT[getWId(oSeq2[r-1].get(l)-1)];
                        }
                        mm++;
                    }
                }
                //4.工序组批
                for(int k=allM-m[oN-1]+1;k<=allM;k++){  //遍历所有批处理机器
                    int mm=0;
                    while(oSeq[k-1].size()!=0){ //当候选集不为空，一直组批
                        //构建新批
                        ArrayList<Integer> curBatch=new ArrayList<>();
                        int curCapacity=wLimit[mm]; //批次剩余容量
                        //随机选择一个工序入批
                        int randO=randInt(1,oSeq[k-1].size());
                        curBatch.add(oSeq[k-1].get(randO-1));
                        double curAT=wT[getWId(oSeq[k-1].get(randO-1))];    //批到达时间为所有工件中最晚到达的时间
                        curCapacity-=workpiece[0][getWId(oSeq[k-1].get(randO-1))];
                        int curType=workpiece[3][getWId(oSeq[k-1].get(randO-1))];    //当前批的加工类型
                        //更新候选解
                        oSeq[k-1].remove(randO-1);
                        //获取当前批次候选集
                        ArrayList<Integer> curBatchAllow=getBatchAllow(oSeq[k-1],curType,curCapacity);
                        while (!curBatchAllow.isEmpty()){
                            double[] pro=new double[curBatchAllow.size()];
                            double sum=0;
                            for(int r=0;r<curBatchAllow.size();r++){
                                //工序安排到当前批的信息素
                                double curTau=0;
                                for(int bid:curBatch){
                                    curTau+=(bTau[mm][bid-1][curBatchAllow.get(r)-1]);
                                }
                                curTau/=curBatch.size();
                                //计算工序安排到当前批的启发信息：空闲空间减少量
                                //该工序加入后的开始时间
                                double curAT2=Math.max(curAT,wT[getWId(curBatchAllow.get(r))]);
                                double curEta=wLimit[mm]*(curAT2-curAT)+workpiece[0][getWId(curBatchAllow.get(r))]*bT[mm];
                                curEta=curEta<0?1:1+curEta;
                                pro[r]=Math.pow(curTau,alpha)*Math.pow(curEta,beta);
                                sum+=pro[r];
                            }
                            for(int l=0;l<pro.length;l++){
                                pro[l]=pro[l]/sum;
                            }
                            //轮盘赌法选择工序
                            int oid=choose(pro);
                            //更新
                            curBatch.add(curBatchAllow.get(oid));
                            curAT=Math.max(curAT,wT[getWId(curBatchAllow.get(oid))]);
                            curCapacity-=workpiece[0][getWId(curBatchAllow.get(oid))];
                            oSeq[k-1].remove((Integer)curBatchAllow.get(oid));
                            curBatchAllow=getBatchAllow(oSeq[k-1],curType,curCapacity);

                        }
                        mBatchs[mm].add(curBatch);
                        mBatchsT[mm].add(curAT);
                    }
                    //4.1 批排序：到达时间早的批优先
                    int[] sortIndex=argsort(mBatchsT[mm]);
                    //根据排序结果调整位置
                    mBatchs[mm]=sortBatch(mBatchs[mm],sortIndex);
                    Collections.sort(mBatchsT[mm]);
                    //4.2 计算机器的最后完工时间
                    double curMT=0;
                    for(int r=0;r<sortIndex.length;r++){    //依次遍历当前机器所有批次
                        double curST=Math.max(curMT,mBatchsT[mm].get(sortIndex[r]));
                        curMT+=(curST+bT[mm]);
                    }
                    antBT[j][mm]=curMT;

                    mm++;
                }

            }
            //获得每只蚂蚁的最晚完工时间
            for(int j=0;j<scale;j++){
                fitness[j]=MAX(antBT[j]);
            }
            //6.信息素更新:  只有最优蚂蚁参加更新
            bestI=argmin(fitness);
            //6.1 工序分配信息素更新
            mTauUpdate(oFM[bestI]);
            //6.2 工序排序信息素更新
            //6.3 工序组批信息素更新

            //全局最优解更新
            if(fitness[bestI]<globalBestF){
                globalBestF=fitness[bestI];
            }

            System.out.println(""+i+":"+fitness[bestI]);

        }

    }

    /**
     * 获得工序加工类型
     * @param oid
     * @return
     */
    private int getType(int oid){
        return workpiece[getStage(oid)][getWId(oid)];
    }

    /**
     * 获得工序工件id
     * @param oid
     * @return
     */
    private int getWId(int oid){
        return (oid-1)/oN;
    }

    /**
     * 获得工序所处阶段
     * @param oid
     * @return
     */
    private int getStage(int oid){
        return oid%oN==0?oN:oid%oN;
    }
    /**
     * 轮盘赌法选择机器
     * @param pro
     * @return
     */
    private int choose(double[] pro) {
        double rand=Math.random();
        double sum=0;
        for(int i=0;i<pro.length;i++){
            sum+=pro[i];
            if(sum>=rand)
                return i;
        }
        return -1;
    }
}
