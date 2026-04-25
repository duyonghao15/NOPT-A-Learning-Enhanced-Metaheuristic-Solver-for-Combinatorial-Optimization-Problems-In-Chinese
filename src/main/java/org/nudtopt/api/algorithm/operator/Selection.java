package org.nudtopt.api.algorithm.operator;

import org.nudtopt.api.model.Solution;
import org.nudtopt.api.tool.comparator.SolutionComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Selection {

    // 1. 选择
    public static List<Solution> selection(List<Solution> solutionList, double selectionPro) {
        Collections.sort(solutionList, new SolutionComparator());                // 1. score降序排序
        int populationSize = solutionList.size();                                // 2. 种群规模
        int selectionSize = (int) (selectionPro * populationSize);               // 3. 选择规模
        /*for(int i = 0 ; i < populationSize - selectionSize ; i ++) {             // 4. 剩下的逆序删除
            solutionList.remove(solutionList.get(solutionList.size() - 1));
        }*/
        List<Solution> selectedList = new ArrayList<>(solutionList.subList(0, selectionSize));

        return selectedList;
//        return solutionList;
    }

/* class ends */
}
