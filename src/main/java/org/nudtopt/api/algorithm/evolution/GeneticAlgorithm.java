package org.nudtopt.api.algorithm.evolution;

import org.nudtopt.api.algorithm.Algorithm;
import org.nudtopt.api.algorithm.operator.*;
import org.nudtopt.api.constraint.Score;
import org.nudtopt.api.model.Solution;
import org.nudtopt.api.tool.comparator.SolutionComparator;
import org.nudtopt.api.tool.function.Tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneticAlgorithm extends Algorithm {

    private String name         = "遗传算法(GA)";
    private String type         = "进化算法";
    private long iteration      = 100;
    private int populationSize  = 50;
    private double selectionPro = 0.5;
    private double crossoverPro = 0.7;
    private double mutationPro  = 0.2;
    private int mutationNum     = 100;


    public List<Solution> run(Solution solution, List<Solution> population, long iteration, double selectionPro, double crossoverPro, double mutationPro, int mutationNum) {
        this.solution = solution;
        if(population == null)  population = defaultInitialize(solution, populationSize);
        else                    populationSize = population.size();

        for(int i = 0 ; i < iteration ; i ++) {
            // 1 selection
            population = Selection.selection(population, selectionPro);
            logger.debug("\t" + this + "\t" + i + "th\tselecting\t" + population.size() + "\toutperforming parents, the best " + population.get(0).getScore() + ", average " + calAverageScore(population));

            // 2 crossover
            population = Crossover.crossover(population, crossoverPro, populationSize, solution);
            logger.debug("\t" + this + "\t\treproducing\t" + population.size() + "\toffspring, with average score " + calAverageScore(population));


            // 3 mutation
            population = Mutation.mutation(population, mutationPro, mutationNum, solution);
            logger.debug("\t" + this + "\t==========================================  第 " + i + " 代完成 !  ==========================================");

            // loop ends
            Collections.sort(population, new SolutionComparator());             // 种群按评分降序
            historySolutionList.addAll(population);                             // 存入历史解集
            Tool.listFIFO(historySolutionList, historySize);
            if(update().equals("stop"))     break;
        }
        // function ends
        updateInputSolution();
        return historySolutionList;
    }



    // 1. 初始化种群
    public List<Solution> defaultInitialize(Solution solution, int populationSize) {
        if(progressBar != null) {
            progressBar.setIndeterminate(true);
            progressBar.setString("Initializing ......");
        }

        List<Solution> population = new ArrayList<>();
        Score totalScore = new Score();
        /*Score initialScore = solution.getScore().clone();
        int iterationPerRun = 1000;
        for(int i = 0 ; i < populationSize ; i ++) {
            logger.debug("\tgenerating\tthe initial solution\t(" + (i + 1) + "/" + populationSize + ") ......");
            List<Move> moveList = new HillClimbing().run(solution, iterationPerRun);  // 1. 迭代100获得一个solution
            double[][] newMatrix = solution.encode();                                 // 2. 该solution的决策矩阵
            Solution newSolution = solution.matrixClone(newMatrix);                   // 3. 根据决策矩阵创建一个new solution
            population.add(newSolution);

            solution.setDecisionMatrix(null);                                         // 4. 撤回
            for(int j = moveList.size() - 1 ; j >= 0 ; j --) {
                Move move = moveList.get(j);
                move.undo();
                solution.updateScore(move);
            }
            if(solution.getScore().compareTo(initialScore) != 0)    logger.error("\tscore error!\n\n");
        }*/
        for(int i = 0 ; i < populationSize ; i ++) {
            double[][] newMatrix = RandomInitialize.randomInitializeDigital(solution);// 1. 随机初始化决策矩阵
            Solution newSolution = solution.matrixClone(newMatrix);                   // 2. 根据决策矩阵创建一个new solution
            Score newScore = newSolution.getScore();
            totalScore = totalScore.addScore(newScore);
            population.add(newSolution);
        }
        Score averageScore = calAverageScore(population);
        logger.debug("\tinitialized\tthe " + populationSize + "-" + solution.getClass().getName() + "population !");
        logger.debug("\twhere\tthe average score of the population is " + averageScore + " !");

        if(progressBar != null) {
            progressBar.setValue(0);
            progressBar.setString(null);
            progressBar.setIndeterminate(false);
            progressBar.setMaximum((int) (iteration * populationSize * mutationPro * mutationNum));
        }
        return population;
    }


    /**
     * 计算种群平均收益
     * @param population 种群(解的list)
     * @return 平均score
     */
    public Score calAverageScore(List<Solution> population) {
        Score score = new Score();
        int populationSize = population.size();
        for(Solution solution : population) {
            score = score.addScore(solution.getScore());         // 收益累加
        }
        score.setHardScore(score.getHardScore() / populationSize);
        score.setMeanScore(score.getMeanScore() / populationSize);
        score.setSoftScore(score.getSoftScore() / populationSize);
        return score;
    }


    @Override
    public void run() {
         run(solution, null, iteration, selectionPro, crossoverPro, mutationPro, mutationNum);
    }


    // getter & setter
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public long getIteration() {
        return iteration;
    }
    public void setIteration(long iteration) {
        this.iteration = iteration;
    }

    public int getPopulationSize() {
        return populationSize;
    }
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public double getSelectionPro() {
        return selectionPro;
    }
    public void setSelectionPro(double selectionPro) {
        this.selectionPro = selectionPro;
    }

    public double getCrossoverPro() {
        return crossoverPro;
    }
    public void setCrossoverPro(double crossoverPro) {
        this.crossoverPro = crossoverPro;
    }

    public double getMutationPro() {
        return mutationPro;
    }
    public void setMutationPro(double mutationPro) {
        this.mutationPro = mutationPro;
    }

    public int getMutationNum() {
        return mutationNum;
    }
    public void setMutationNum(int mutationNum) {
        this.mutationNum = mutationNum;
    }

/* class ends */
}
