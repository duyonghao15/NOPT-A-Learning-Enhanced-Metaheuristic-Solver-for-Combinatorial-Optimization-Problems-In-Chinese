package org.nudtopt.api.algorithm.learning;

import org.apache.commons.lang3.SerializationUtils;
import org.nudtopt.api.algorithm.Algorithm;
import org.nudtopt.api.algorithm.operator.Operator;
import org.nudtopt.api.constraint.Score;
import org.nudtopt.api.model.Solution;
import org.nudtopt.api.tool.function.Tool;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RL_LateAcceptance extends Algorithm {

    private String name             = "学习增强的变邻域算法(RL-VNS)";
    private String type             = "学习型算法";
    private Boolean highlight       = true;
    private long iteration          = 5000;
    private List<Operator> tabuList = new ArrayList<>();
    private List<Score> lateList    = new ArrayList<>();    // 逾期表, 存储最近历史评分的list
    private int tabuSize            = 5;
    private int lateSize            = 5;                    // 逾期表长度

    /**
     * 训练策略
     * @param trainData   训练数据
     * @param actionSpace 动作空间（与操作算子数量保持一致）
     */
    public void train(List<Solution> trainData, Integer actionSpace, int steps, int epoch) throws IOException {
        // 0. 状态空间 迭代进度（0-4）  算子数量
        QTable qtable = new QTable(4 * actionSpace, actionSpace);
        // 1. 循环训练次数
        for (int i = 0; i < epoch; i++) {
            // 2. 循环训练场景（重置）
            for (Solution solutionO : trainData) {
                Solution solution = SerializationUtils.clone(solutionO);
                tabuList.clear();
                historyOperatorList.clear();

                int trappedIteration = 0;
                if(lateList.isEmpty()) {
                    for(int temp = 0 ; temp < lateSize ; temp ++) {                // 初始化late list
                        lateList.add(solution.getScore().clone());
                    }
                }

                int currentState;
                int currentAction;
                int nextState = 0;
                // 2.2 迭代训练
                for(int j = 0 ; j < steps ; j ++) {
                    // 1. move and score
                    Score oldScore = solution.getScore().clone();          // 注意: 记录旧score
                    trappedIteration ++;

                    currentState = nextState;
                    currentAction = qtable.getAction(currentState);
                    Operator operator = operatorList.get(currentAction).moveAndScore(solution, tabuList);

                    // 2. accept?
                    boolean accept = solution.getScore().compareTo(oldScore) >= 0 ||
                            solution.getScore().compareTo(lateList.get(0)) >= 0;
                    boolean isImprove = solution.getScore().compareTo(oldScore) == 0 ||
                            solution.getScore().compareTo(lateList.get(0)) == 0;

                    if (operator == null) {
                        qtable.update(currentState, currentAction, -0.2, nextState);
                        j--;
                        continue;
                    }

                    /** 陷入局部最优情况 **/
                    double a;
                    if (trappedIteration < 100) a = 0.1;
                    else if (trappedIteration < 300) a = 0.4;
                    else if (trappedIteration < 500) a = 0.7;
                    else a = 0.9;

                    if (trappedIteration < 300) nextState = 0;
                    else if (trappedIteration < 500) nextState = 1;
                    else if (trappedIteration < 800) nextState = 2;
                    else nextState = 3;


//                    nextState = QTable.getStates(a, 4, currentAction, operatorList.size());
                    if (isImprove) {
                        qtable.update(currentState, currentAction, 0.0001, nextState);
                    } else if (accept) {
                        qtable.update(currentState, currentAction, 1, nextState);
                    } else {
                        qtable.update(currentState, currentAction, 0, nextState);
                    }

                    // 3. accept or reject
                    if(accept) {
                        tabuList.add(operator);
                        historyOperatorList.add(operator);
                        Tool.listFIFO(tabuList, tabuSize);
                        Tool.listFIFO(historyOperatorList, historySize);
                        logger.debug("\t" + this + "\t" + j + "th\tobtains score " + solution.getScore() + "\tby (1/" + trappedIteration + ") operator\t" + operator.getMoveList());
                        trappedIteration = 0;
                    } else {
                        Operator.undo(solution, operator);                // 撤回: 实例编码(逆序撤销move), 数字编码(赋值原矩阵)
                        if(solution.getConstraint().isIncremental() && solution.getScore().compareTo(oldScore) != 0) {
                            logger.error("\t" + this + "\t" + j + "th\t增量式约束: " + solution.getScore() + " != 上一步: " + oldScore);
                        }
                        solution.getScore().clone(oldScore);              // solution赋旧score(注意, 只是值, score对象不能变)
                    }

                    // 4. update late score list
                    lateList.add(solution.getScore().clone());            // 当前解插入FIFO的lateList
                    Tool.listFIFO(lateList, lateSize);
                }

            }
        }

        // 2. 保存训练好的策略到指定路径
    }



    /**
     * 在线强化学习的逾期接受算法
     * @param solution
     * @param iteration
     * @param tabuSize
     * @param lateSize
     * @return
     */
    public List<Operator> run(Solution solution, long iteration, int tabuSize, int lateSize) {
        int actionSpace = operatorList.size();
        // 0. 状态空间 迭代进度（0-4）  算子数量
        QTable qtable = new QTable(4 * actionSpace, actionSpace);
        this.solution = solution;
        int trappedIteration = 0;
        if(lateList.size() == 0) {
            for(int i = 0 ; i < lateSize ; i ++) {                // 初始化late list
                lateList.add(solution.getScore().clone());
            }
        }

        int currentState;
        int currentAction;
        int nextState = 0;
        for(int i = 0 ; i < iteration; i ++) {
            // 1. move and score
            Score oldScore = solution.getScore().clone();          // 注意: 记录旧score
            trappedIteration ++;
            currentState = nextState;
            currentAction = qtable.getAction(currentState);
            Operator operator = operatorList.get(currentAction).moveAndScore(solution, tabuList);
            while (operator == null) {
                currentAction = new Random().nextInt(operatorList.size());
                operator = operatorList.get(currentAction).moveAndScore(solution, tabuList);
            }

            // 2. accept?
//            System.out.println(i + "  " + currentState + "  " + currentAction + "  " + solution.getScore().getMeanScore());
            boolean accept = solution.getScore().compareTo(oldScore) >= 0 ||
                    solution.getScore().compareTo(lateList.get(0)) >= 0;
            boolean isEqual = solution.getScore().compareTo(oldScore) == 0 ||
                    solution.getScore().compareTo(lateList.get(0)) == 0;


            double a;
            if (trappedIteration < 100) a = 0.1;
            else if (trappedIteration < 300) a = 0.4;
            else if (trappedIteration < 500) a = 0.7;
            else a = 0.9;


            nextState = QTable.getStates(a, 4, currentAction, operatorList.size());
//            nextState = QTable.getStates(1.0 * i / iteration, 4, currentAction, operatorList.size());

            if (isEqual) {
                qtable.update(currentState, currentAction, 0.1, nextState);
            } else if (accept) {
                qtable.update(currentState, currentAction, 1, nextState);
            } else {
                qtable.update(currentState, currentAction, 0, nextState);
            }

            // 3. accept or reject
            if(accept) {
                tabuList.add(operator);
                historyOperatorList.add(operator);
                Tool.listFIFO(tabuList, tabuSize);
                Tool.listFIFO(historyOperatorList, historySize);
                logger.debug("\t" + this + "\t" + i + "th\tobtains score " + solution.getScore() + "\tby (1/" + trappedIteration + ") operator\t" + operator.getMoveList());
                trappedIteration = 0;
            } else {
                Operator.undo(solution, operator);                // 撤回: 实例编码(逆序撤销move), 数字编码(赋值原矩阵)
                if(solution.getConstraint().isIncremental() && solution.getScore().compareTo(oldScore) != 0) {
                    logger.error("\t" + this + "\t" + i + "th\t增量式约束: " + solution.getScore() + " != 上一步: " + oldScore);
                }
                solution.getScore().clone(oldScore);              // solution赋旧score(注意, 只是值, score对象不能变)
            }

            // 4. update late score list
            lateList.add(solution.getScore().clone());            // 当前解插入FIFO的lateList
            Tool.listFIFO(lateList, lateSize);
            /* loop ends */
            if(update().equals("stop"))     break;
        }
        /* function ends */
        return historyOperatorList;
    }


    @Override
    public void run() {
        historyOperatorList = run(solution, iteration, tabuSize, lateSize);
    }


    public int getLateIndex(int lateSize, int iteration, long maxIteration, int trappedIteration) {
        // 迭代进程越多, 逾期表应该缩短, index->size, (减少回退, 快速收敛)
        // trap越多, 逾期表应该拉长, index->0, 帮助回退
        double iterCoefficient = iteration * 1.0 / maxIteration;
        double trapCoefficient = Math.max((10 - trappedIteration) * 1.0 / 10, 0); // 不小于0
        double coefficient = (iterCoefficient + trapCoefficient) / 2;
        int lateIndex = (int) Math.round(lateSize * coefficient);
        return Math.min(Math.max(0, lateIndex), lateSize - 1);
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

    public Boolean getHighlight() {
        return highlight;
    }
    public void setHighlight(Boolean highlight) {
        this.highlight = highlight;
    }

    public long getIteration() {
        return iteration;
    }
    public void setIteration(long iteration) {
        this.iteration = iteration;
    }

    public List<Operator> getTabuList() {
        return tabuList;
    }
    public void setTabuList(List<Operator> tabuList) {
        this.tabuList = tabuList;
    }

    public int getTabuSize() {
        return tabuSize;
    }
    public void setTabuSize(int tabuSize) {
        this.tabuSize = tabuSize;
    }

    public List<Score> getLateList() {
        return lateList;
    }
    public void setLateList(List<Score> lateList) {
        this.lateList = lateList;
    }

    public int getLateSize() {
        return lateSize;
    }
    public void setLateSize(int lateSize) {
        this.lateSize = lateSize;
    }

}
