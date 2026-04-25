package org.nudtopt.api.algorithm.operator;

import org.nudtopt.api.algorithm.localsearch.HillClimbing;
import org.nudtopt.api.constraint.Score;
import org.nudtopt.api.model.Solution;
import org.nudtopt.api.tool.comparator.SolutionComparator;
import org.nudtopt.api.tool.function.Tool;

import java.util.List;

public class Mutation {


    // 变异
    public static List<Solution> mutation(List<Solution> solutionList, double probability, int num, Solution realSolution) {
        for(int i = 0 ; i < solutionList.size() ; i ++) {                                         // 0. 遍历所有solution
            if(Tool.random() >= probability)    continue;                                         // 1. 是否变异
            double[][] decisionMatrix = solutionList.get(i).getDecisionMatrix();                  // 2. 当前决策矩阵
            double[][] mutatedMatrix = climbingMutation(decisionMatrix, num, realSolution);       // 3. 变异后的决策矩阵
            Solution mutatedSolution = realSolution.matrixClone(mutatedMatrix);                   // 4. 基于决策矩阵创建新的shadow solution
            if (mutatedSolution.getScore().compareTo(solutionList.get(i).getScore()) > 0) solutionList.set(i, mutatedSolution);  // 5. 判断是否相同，加入到种群
        }
        return solutionList;
    }



    // 变异(数字编码)
    public static double[][] onePointMutation(double[][] decisionMatrix) {
        return RandomMove.move(decisionMatrix);                           // 单点随机变异即move (传参未改, 返回新值)
    }

    // 爬山变异
    public static double[][] climbingMutation(double[][] decisionMatrix, int iteration, Solution realSolution) {
        realSolution.decode(decisionMatrix);                              // 1. 基于决策矩阵编码, 获得实例solution
        realSolution.updateScore();
        realSolution.setDecisionMatrix(null);
        new HillClimbing().run(realSolution, iteration, 0);        // 2. 调用爬山算法, solution不断优化(传参不变)
        realSolution.encode();
        return Tool.clone(realSolution.getDecisionMatrix());              // 3. 爬山结束, 基于当前solution, 返回最新决策矩阵(克隆)
    }


    public static double[][] differentialMutation(List<Solution> population, double f_min, double f_max) {
        // 1. 从种群中提取3个互不相同的解, 获取决策矩阵
        List<Solution> solutionList = Tool.randomFromList(population, 3, true);
        solutionList.sort(new SolutionComparator());
        double[][] matrix_1 = solutionList.get(0).getDecisionMatrix(); // 评分第一
        double[][] matrix_2 = solutionList.get(1).getDecisionMatrix(); // 评分第二
        double[][] matrix_3 = solutionList.get(2).getDecisionMatrix(); // 评分第三
        // 2. 新建变异后的决策矩阵
        int rowNum = matrix_1.length;
        int columnNum = matrix_1[0].length;
        double[][] newMatrix = new double[rowNum][columnNum];
        // 3. 计算差分(缩放)系数
        Score score_1 = solutionList.get(0).getScore();
        Score score_2 = solutionList.get(1).getScore();
        Score score_3 = solutionList.get(2).getScore();
        double ratio = score_2.cutScore(score_1).divideScore(score_3.cutScore(score_1));
        double f = f_min + (f_max - f_min) * ratio;
        // 4. 差分操作
        for(int i = 0 ; i < rowNum ; i ++) {
            for(int j = 0 ; j < columnNum ; j ++) {
                newMatrix[i][j] = matrix_1[i][j] + f * (matrix_2[i][j] - matrix_3[i][j]);
                if(newMatrix[i][j] > 1)         newMatrix[i][j] = 1;
                else if(newMatrix[i][j] < 0)    newMatrix[i][j] = 0;
            }
        }
        /* function ends */
        return newMatrix;
    }



/* class ends */
}
