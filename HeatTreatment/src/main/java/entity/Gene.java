package main.java.entity;

public class Gene {

    private double fitness;
    //染色体,行数为工件号，一共有两列，第一列为分配的批次号，第二列为分配的机器号
    private int[][] chromosome;
    public double getfitness(){
        return fitness;
    }
    public void setfitness(double x){
        this.fitness = x;
    }
    public int[][] getchromosome(){
        return chromosome;
    }
    public void setchromosome(int[][] x){
        this.chromosome = x;
    }
}
