package org.nudtopt.api.algorithm.learning;

import org.nudtopt.api.algorithm.Algorithm;
import org.nudtopt.api.algorithm.localsearch.HillClimbing;
import org.nudtopt.api.algorithm.operator.Operator;
import org.nudtopt.api.constraint.Score;
import org.nudtopt.api.model.Solution;
import org.nudtopt.api.tool.function.Tool;

import java.util.ArrayList;
import java.util.List;

public class RL_SimulatedAnnealing extends Algorithm {
    private String name = "学习增强的模拟退火算法(RLSA)";
    private String type = "学习型算法";
    private Boolean highlight = true;
    private long iteration = 10000;
    private List<Operator> tabuList = new ArrayList<>();
    private int tabuSize = 4;
    private Score initTemp;                       // 初始退火温度
    private String annealType = "等比";         // 退火类型

    public List<Operator> run(Solution solution, long iteration, int tabuSize, Score initTemp, String annealType) {
        int actionSpace = operatorList.size();
        QTable qtable = new QTable(4 * actionSpace, actionSpace);
        this.solution = solution;
        int trappedIteration = 0;
        // 0. temperature
        if (initTemp == null) initTemp = randomInitialScore(solution, 5, 1000);
        double hardTemp = initTemp.getHardScore();
        double meanTemp = initTemp.getMeanScore();
        double softTemp = initTemp.getSoftScore();


        int currentState;
        int currentAction;
        int nextState = 0;
        for (int i = 0; i < iteration; i++) {
            // 1. move and score(inner loop)
            Score oldScore = solution.getScore().clone();

            currentState = nextState;
            currentAction = qtable.getAction(currentState);
            Operator operator = operatorList.get(currentAction).moveAndScore(solution, tabuList);
            nextState = QTable.getStates((double) i / iteration, 4, currentAction, operatorList.size());
            qtable.update(currentState, currentAction, oldScore, solution.getScore(), nextState);
            if (operator == null) {
                qtable.update(currentState, currentAction, -0.5, nextState);
                i--;
                continue;
            }


            trappedIteration++;

            // 2. annealing (every N iterations)
            if ((i + 1) % 1000 == 0) {
                switch (annealType) {
                    case "等差":
                        hardTemp = initTemp.getHardScore() - iteration * 0.01;
                        meanTemp = initTemp.getMeanScore() - iteration * 0.01;
                        softTemp = initTemp.getSoftScore() - iteration * 0.01;
                        break;
                    case "等比":
                        hardTemp = initTemp.getHardScore() * Math.pow(0.001, i * 5.0 / iteration);
                        meanTemp = initTemp.getMeanScore() * Math.pow(0.001, i * 5.0 / iteration);
                        softTemp = initTemp.getSoftScore() * Math.pow(0.001, i * 5.0 / iteration);
                        break;
                    case "对数":
                        hardTemp = initTemp.getHardScore() * (1 - Math.log(i + 1) / Math.log(iteration));
                        meanTemp = initTemp.getMeanScore() * (1 - Math.log(i + 1) / Math.log(iteration));
                        softTemp = initTemp.getSoftScore() * (1 - Math.log(i + 1) / Math.log(iteration));
                        break;
                    default:
                        logger.error("\t退火类型输入错误 !");
                        break;
                }
                initTemp.setHardScore(Math.max((long) hardTemp, 0L));
                initTemp.setMeanScore(Math.max((long) meanTemp, 0L));
                initTemp.setSoftScore(Math.max((long) softTemp, 0L));
            }

            // 3. accept?
            boolean accept;
            if (solution.getScore().compareTo(oldScore) >= 0) {     // 获得优解, 接受
                accept = true;
            } else {                                               // 获得劣解, 判断
                long hardInterval = solution.getScore().getHardScore() - oldScore.getHardScore();
                long meanInterval = solution.getScore().getMeanScore() - oldScore.getMeanScore();
                long softInterval = solution.getScore().getSoftScore() - oldScore.getSoftScore();

                double hardP = Math.exp(hardInterval / (hardTemp + 0.000001));
                double meanP = Math.exp(meanInterval / (meanTemp + 0.000001));
                double softP = Math.exp(softInterval / (softTemp + 0.000001));
                double minP = (hardP < meanP) ? (hardP < softP ? hardP : softP) : (meanP < softP ? meanP : softP);
                accept = Tool.random() < minP;                     // <p, 接受(劣解); 否则不接受
            }

            // 4. accept or reject
            if (accept) {
                tabuList.add(operator);
                historyOperatorList.add(operator);
                Tool.listFIFO(tabuList, tabuSize);
                Tool.listFIFO(historyOperatorList, historySize);
                logger.debug("\t" + this + "\t" + i + "th\tobtains score " + solution.getScore() + "\tby (1/" + trappedIteration + ") operator\t" + operator.getMoveList());
                trappedIteration = 0;
            } else {
                Operator.undo(solution, operator);                // 撤回: 实例编码(逆序撤销move), 数字编码(赋值原矩阵)
                if (solution.getConstraint().isIncremental() && solution.getScore().compareTo(oldScore) != 0) {
                    logger.error("\t" + this + "\t" + i + "th\t增量式约束: " + solution.getScore() + " != 上一步: " + oldScore);
                }
                solution.getScore().clone(oldScore);              // solution赋旧score(注意, 只是值, score对象不能变)
            }
            /* loop ends */
            if (update().equals("stop")) break;
        }
        /* function ends */
        return historyOperatorList;
    }

    // 随机初始score (随机run n次, 取平均score)
    public Score randomInitialScore(Solution solution, int run, long iterationPerRun) {
        Score score = new Score();
        Score initialScore = solution.getScore().clone();
        for (int i = 0; i < run; i++) {
            logger.debug("\tgenerating\tthe initial solution\t(" + (i + 1) + "/" + run + ") ......");
            List<Operator> operatorList = new HillClimbing().run(solution, iterationPerRun, 0);
            score.addHardScore(solution.getScore().getHardScore());
            score.addMeanScore(solution.getScore().getMeanScore());
            score.addSoftScore(solution.getScore().getSoftScore());
            for (int j = operatorList.size() - 1; j >= 0; j--) {
                Operator operator = operatorList.get(j);
                operator.undo();
                solution.updateScore(operator);
            }
            if (solution.getScore().compareTo(initialScore) != 0) logger.error("\tscore error!\n\n");
        }
        score.setHardScore(Math.abs(score.getHardScore() / run));  // 退火温度应均为正
        score.setMeanScore(Math.abs(score.getMeanScore() / run));
        score.setSoftScore(Math.abs(score.getSoftScore() / run));
        return score;
    }


    @Override
    public void run() {
        historyOperatorList = run(solution, iteration, tabuSize, initTemp, annealType);
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

    public Score getInitTemp() {
        return initTemp;
    }

    public void setInitTemp(Score initTemp) {
        this.initTemp = initTemp;
    }

    public String getAnnealType() {
        return annealType;
    }

    public void setAnnealType(String annealType) {
        this.annealType = annealType;
    }
}
