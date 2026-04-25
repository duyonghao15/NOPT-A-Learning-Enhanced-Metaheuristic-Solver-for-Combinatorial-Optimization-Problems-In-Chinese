package org.nudtopt.api.algorithm.operator;


import org.nudtopt.api.algorithm.localsearch.HillClimbing;
import org.nudtopt.api.model.Solution;
import org.nudtopt.api.tool.comparator.SolutionComparator;
import org.nudtopt.api.tool.function.Tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Crossover {

    // 0. 交叉
    public static List<Solution> crossover(List<Solution> parentList, double probability, int populationSize, Solution realSolution) {
        List<Solution> solutionList = new ArrayList<>();
        // 1. 每个个体都进行一次交叉
        for (int i = 0; i < parentList.size(); i++) {
            // a. 根据概率判断是否进行交叉,不交叉直接选择现有的父代
            if (Tool.random() < probability) {
                solutionList.add(parentList.get(i));
                continue;
            }

            // b. 选择不同的父代进行交叉
            Solution parentSolution_1 = parentList.get(i);
            Solution parentSolution_2 = parentSolution_1;
            if (parentList.size() > 1) {
                while (parentSolution_2 == parentSolution_1) {
                    parentSolution_2 = Tool.randomFromList(parentList);
                }
            }

            // c. 交叉, 获得父子代共3个,排除父代2
            List<Solution> offspringSolutionList = crossover(parentSolution_1, parentSolution_2, realSolution, 3);

            // d. 选择最好的个体
            Collections.sort(offspringSolutionList, new SolutionComparator());
            solutionList.add(offspringSolutionList.get(0));
        }

        // e. 补充不足的子代(基于现有个体随机扰动)
        while (solutionList.size() < populationSize) {
            // e.a. 随机选择1个个代
            Solution superSolution = Tool.randomFromList(parentList);
            double[][] decisionMatrix = superSolution.getDecisionMatrix();
            realSolution.decode(decisionMatrix);
            realSolution.updateScore();
            realSolution.setDecisionMatrix(null);
            for (int i = 0; i < 0.4 * realSolution.getDecisionEntityList().size(); i++) {
                new RandomMove().moveAndScore(realSolution, null);
            }
            realSolution.encode();
            double[][] newMatrix = realSolution.getDecisionMatrix();
            Solution newSolution = realSolution.matrixClone(newMatrix);

            solutionList.add(newSolution);
        }
        parentList.clear();
        /* function ends */
        return solutionList;
    }


    public static List<Solution> crossover(Solution parentSolution_1, Solution parentSolution_2, Solution realSolution) {
        return crossover(parentSolution_1, parentSolution_2, realSolution, 4);
    }

    // 1. 交叉
    public static List<Solution> crossover(Solution parentSolution_1, Solution parentSolution_2, Solution realSolution, int saveNum) {
        // a. 2个父代的决策矩阵
        double[][] parentMatrix_1 = parentSolution_1.getDecisionMatrix();
        double[][] parentMatrix_2 = parentSolution_2.getDecisionMatrix();

        // b. 交叉父代的决策矩阵, 获得2个子代决策矩阵
        List<double[][]> offspringMatrixList = Crossover.onePointCrossover(parentMatrix_1, parentMatrix_2);
        double[][] offspringMatrix_1 = offspringMatrixList.get(0);
        double[][] offspringMatrix_2 = offspringMatrixList.get(1);

        // c. 子代决策矩阵 -> solution (影子, 只有矩阵和评分, 没有属性)
        Solution offspringSolution_1 = realSolution.matrixClone(offspringMatrix_1);
        Solution offspringSolution_2 = realSolution.matrixClone(offspringMatrix_2);

        // d. 输出父子代共4个
        List<Solution> fourSolutionList = new ArrayList<>();
        if (saveNum >= 2) {
            fourSolutionList.add(offspringSolution_1);
            fourSolutionList.add(offspringSolution_2);
        }
        if (saveNum >= 3) fourSolutionList.add(parentSolution_1);
        if (saveNum >= 4) fourSolutionList.add(parentSolution_2);

        /* function ends */
        return fourSolutionList;
    }


    // 2.1 单点矩阵交叉(数字编码)
    public static List<double[][]> onePointCrossover(double[][] parentMatrix_1, double[][] parentMatrix_2) {
        int index = (int) (Tool.random() * parentMatrix_1.length);
        int[] indexArray = new int[index];
        for(int i = 0 ; i < indexArray.length ; i ++) {
            indexArray[i] = i;
        }
        return Swap.swap(parentMatrix_1, parentMatrix_2, indexArray);   // 注意: 传参未改, 返回新值
    }


    // 2.2 双点矩阵交叉(数字编码)
    public static List<double[][]> twoPointCrossover(double[][] parentMatrix_1, double[][] parentMatrix_2) {
        int index_1 = (int) (Tool.random() * parentMatrix_1.length);
        int index_2 = (int) (Tool.random() * parentMatrix_2.length);
        int index_small = index_1 < index_2 ? index_1 : index_2;
        int index_large = index_1 > index_2 ? index_1 : index_2;
        int[] indexArray = new int[index_large - index_small];
        for(int i = 0 ; i < indexArray.length ; i ++) {
            indexArray[i] = i + index_small;
        }
        return Swap.swap(parentMatrix_1, parentMatrix_2, indexArray);   // 注意: 传参未改, 返回新值
    }


/* class ends */
}
