package org.nudtopt.api.algorithm.learning;

import org.nudtopt.api.constraint.Score;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QTable{
    private final int stateCount;          // 状态数量
    private final int actionCount;         // 动作数量
    private double[][] qTable;             // Q表
    private final double EPSILON;          // 贪心系数
    private final double LEARNING_RATE;    // 学习率α
    private final double DISCOUNT_FACTOR;  // 折扣因子γ（
    private final Random random;           // 全局随机数实例

    /**
     * 构造函数（增加参数校验，初始化核心参数）
     * @param state 状态数量（必须>0）
     * @param action 动作数量（必须>0）
     */
    public QTable(int state, int action) {
        if (state <= 0 || action <= 0) {
            throw new IllegalArgumentException("状态数和动作数必须大于0！");
        }
        this.stateCount = state;
        this.actionCount = action;
        this.qTable = new double[state][action];
        this.EPSILON = 0.3;                // 贪心系数：10%探索，90%利用
        this.LEARNING_RATE = 0.2;          // 学习率α
        this.DISCOUNT_FACTOR = 0.95;        // 折扣因子γ
        this.random = new Random();        // 全局Random实例

        // 2. Q表初始化
        for (int i = 0; i < state; i++) {
            for (int j = 0; j < action; j++) {
                qTable[i][j] = 0.0;
            }
        }
    }

    /**
     * 标准化Q值更新（遵循Q-learning核心公式）
     * Q(s,a) = Q(s,a) + α * [r + γ * maxQ(s',a') - Q(s,a)]
     * @param stateIndex 当前状态索引
     * @param actionIndex 当前动作索引
     * @param nextState 下一个状态
     */
    public void update(int stateIndex, int actionIndex, double reward, int nextState) {
        // 1. 边界校验，避免数组越界
        if (!isValidState(stateIndex) || !isValidAction(actionIndex) || !isValidState(nextState)) {
            throw new ArrayIndexOutOfBoundsException("状态/动作索引超出范围！");
        }
        // 2. 计算当前Q值和下一个状态的最大Q值
        double currentQ = qTable[stateIndex][actionIndex];
        double maxNextQ = getMaxQValue(nextState);
        // 3. Q-learning更新公式
        double newQ = currentQ + LEARNING_RATE * (reward + DISCOUNT_FACTOR * maxNextQ - currentQ);
        qTable[stateIndex][actionIndex] = newQ;
    }

    public void update(int stateIndex, int actionIndex, Score oldScore, Score newScore, int nextState) {
        double reward;
        double compare = oldScore.compareTo(newScore);
        if (compare > 0) reward = 1.0;
        else if (compare == 0) reward = 0.02;
        else reward = 0.0;
        update(stateIndex, actionIndex, reward, nextState);
    }


    /**
     * 选择动作
     * @param state 当前状态
     * @return 选中的动作索引
     */
    public int getAction(int state) {
        if (!isValidState(state)) {
            throw new ArrayIndexOutOfBoundsException("状态索引超出范围！");
        }
        // ε-贪心：EPSILON概率探索，1-EPSILON概率利用
        if (random.nextDouble() < EPSILON) {
            // 探索：随机选择动作
            return random.nextInt(actionCount);
        } else {
            // 利用：选择最优动作（处理平局）
            return getOptimalAction(state);
        }
    }

    /**
     * 校验状态索引是否有效
     */
    private boolean isValidState(int stateIndex) {
        return stateIndex >= 0 && stateIndex < stateCount;
    }

    /**
     * 校验动作索引是否有效
     */
    private boolean isValidAction(int actionIndex) {
        return actionIndex >= 0 && actionIndex < actionCount;
    }

    /**
     * 获取指定状态下的最大Q值
     */
    private double getMaxQValue(int state) {
        double maxQ = Double.NEGATIVE_INFINITY;
        for (int action = 0; action < actionCount; action++) {
            maxQ = Math.max(maxQ, qTable[state][action]);
        }
        return maxQ;
    }

    /**
     * 选择最优动作
     */
    private int getOptimalAction(int state) {
        double maxQ = getMaxQValue(state);
        List<Integer> optimalActions = new ArrayList<>();
        // 收集所有Q值等于最大值的动作
        for (int action = 0; action < actionCount; action++) {
            if (Math.abs(qTable[state][action] - maxQ) < 1e-6) {
                optimalActions.add(action);
            }
        }
        // 随机选择一个最优动作（避免策略僵化）
        return optimalActions.get(random.nextInt(optimalActions.size()));
    }

    public static Integer getStates(double iterationRate, int slice, int action, int actionNum) {
        double rate = 1.0 / slice;
        for (int i = 1; i <= slice; i++) {
            if (iterationRate < i * rate) {
                return (i - 1) * actionNum + action;
            }
        }
        return (slice - 1) * actionNum + action;
    }
}


