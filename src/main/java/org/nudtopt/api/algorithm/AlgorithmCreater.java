//package org.nudtopt.api.algorithm;
//
//import org.nudtopt.api.algorithm.evolution.*;
//import org.nudtopt.api.algorithm.hybrid.*;
//import org.nudtopt.api.algorithm.localsearch.*;
//import org.nudtopt.api.algorithm.rule.*;
//import org.nudtopt.api.model.MainLogger;
//import org.nudtopt.api.tool.function.Tool;
//
//import java.util.Random;
//
//public class AlgorithmCreater extends MainLogger {
//
//
//    //  **************************** 创建算法 ******************************
//    public Algorithm run(long id, String algorithmName, long iteration, long maxRunTime, int taskNum, int resourceNum) {
//        // 1. 准备, 计算参数
//        int tabuSize = taskNum / 50;
//        iteration = iteration > 0 ? iteration : Math.max(taskNum * resourceNum * 100, 10000);   // 根据入参和默认值, 修改算法迭代次数
//        if(taskNum < 150)           iteration = iteration * 4;                                  // 小规模任务多跑一段时间
//        // if(taskNum < 10 && !algorithmName.contains("规则"))   algorithmName = "爬山算法";        // 极小任务规模默认调用爬山算法
//        Tool.setRandom(new Random(30)); // 固定随机种子
//        logger.info("Scenario-" + id + ": 正在创建算法 ......");
//
//        // 2. 创建算法
//        Algorithm algorithm;
//        switch (algorithmName) {
//            default:
//            case "逾期接受算法":
//                // todo
//                if(!algorithmName.equals("逾期接受算法")) {
//                    Tool.setRandom(new Random(algorithmName.hashCode()));
//                    iteration = iteration * 4 / 5;
//                }
//                algorithm = new LateAcceptance();
//                algorithm.setIteration(iteration);
//                ((LateAcceptance)algorithm).setTabuSize(5);
//                ((LateAcceptance)algorithm).setLateSize(Math.max(Math.min(taskNum, 5), 2));    // 不超过5, 不小于2
//                break;
//            case "模拟退火算法":
//                algorithm = new SimulatedAnnealing();
//                algorithm.setIteration(iteration);
//                ((SimulatedAnnealing)algorithm).setAnnealType("等比");
//                break;
//            case "爬山算法":
//                algorithm = new HillClimbing();
//                algorithm.setIteration(iteration);
//                break;
//            case "迭代局部搜索算法":
//                algorithm = new IteratedLocalSearch();
//                algorithm.setIteration(10);
//                ((IteratedLocalSearch)algorithm).setTabuSize(tabuSize);
//                ((IteratedLocalSearch)algorithm).setInnerIteration(iteration / algorithm.getIteration());
//                break;
//            case "禁忌搜索算法":
//                algorithm = new TabuSearch();
//                algorithm.setIteration(10);
//                ((TabuSearch)algorithm).setTabuSize(tabuSize);
//                ((TabuSearch)algorithm).setInnerIteration(iteration / algorithm.getIteration());
//                break;
//            case "大洪水算法":
//                algorithm = new GreatDeluge();
//                ((GreatDeluge)algorithm).setDelugeRatio(0.5);
//                break;
//            case "遗传算法":
//                algorithm = new GeneticAlgorithm();
//                algorithm.setIteration(50);
//                ((GeneticAlgorithm)algorithm).setPopulationSize(30);
//                break;
//            case "差分进化算法":
//                algorithm = new DifferentialEvolution();
//                algorithm.setIteration(50);
//                ((DifferentialEvolution)algorithm).setPopulationSize(30);
//                break;
//            case "模因算法":
//                algorithm = new MemeticAlgorithm();
//                algorithm.setIteration(10);
//                ((MemeticAlgorithm)algorithm).setPopulationSize(30);
//                break;
//            case "紧前排序算法":      algorithm = new FirstFit();         break;
//            case "优选排序算法":      algorithm = new BestFit();          break;
//            case "随机分配算法":      algorithm = new RandomFit();        break;
//            case "随机无约束分配":    algorithm = new RandomAllocate();   break;
//        }
//        maxRunTime = (long)(maxRunTime * (1 - Tool.random() * 0.15));   // 根据最大时间概率折减
//        algorithm.setMaxRunTime(maxRunTime);
//        logger.info("Scenario-" + id + ": 算法创建成功！当前算法: " + algorithmName + ", 迭代次数: " + algorithm.getIteration() +
//                    " (即有效任务数 " + taskNum + " * 资源数 " + resourceNum + " * " + algorithm.getIteration() / taskNum / resourceNum + ")" +
//                    (maxRunTime == 0 ? "" : ", 最大运行时间: " + maxRunTime + "s") + ".\n");
//        return algorithm;
//    }
//
//
///* class ends */
//}
