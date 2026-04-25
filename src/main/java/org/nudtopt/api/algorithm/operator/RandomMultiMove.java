package org.nudtopt.api.algorithm.operator;

import org.nudtopt.api.model.DecisionEntity;
import org.nudtopt.api.model.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RandomMultiMove extends RandomMove {


    private int moveNum = 2;

    /**
     * 同时进行多次随机move
     * @param solution 原解
     * @param tabuList 禁忌表
     * @return         元算子集合
     */
    @Override
    public Operator moveAndScore(Solution solution, List<Operator> tabuList) {
        List<Move> moveList = new ArrayList<>();
        List<DecisionEntity> entityList = solution.getRandomDecisionEntity(moveNum);
        for(DecisionEntity decisionEntity : entityList) {
            Move move = move(decisionEntity);
            if(move != null) {
                moveList.add(move);
            }
        }
        if(moveList.size() == 0) {
            return moveAndScore(solution, tabuList);
            // return super.moveAndScore(solution, tabuList);
        }
        return createAndUpdate(moveList, solution);      // 返回: 创建一个综合算子, 并更新解的约束收益
    }

    public RandomMultiMove(int moveNum) {
        this.moveNum = moveNum; // Tool.randomFromList(Arrays.asList(2, 3));
    }

    public RandomMultiMove() {
        this.moveNum = 2; // Tool.randomFromList(Arrays.asList(2, 3));
    }



}
