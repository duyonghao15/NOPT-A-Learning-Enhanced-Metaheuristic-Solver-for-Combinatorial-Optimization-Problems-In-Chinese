package org.nudtopt.realworldproblems.satelliterangescheduling.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TaskRequest implements Serializable {

    private double minElevation = 0;            // 最小天线仰角
    private double maxElevation = 90;           // 最大天线仰角

    private long earlistTime = 0;               // 最早时间
    private long deadline = 9999999;            // 最晚时间

    private List<String> expandList   = new ArrayList<>();    // 要求扩频
    private List<String> functionList = new ArrayList<>();    // 要求测控功能
    private List<String> frequentList = new ArrayList<>();    // 要求测控频段

    private String orbitType;                   // 要求轨道类型(入境圈, 中间圈, 出境圈, 或无要求)

    private long minOrbitGap = 0;               // 两(升/降)轨间隔: 最小圈数
    private long minInterval = 0;               // 两(升/降)轨间隔: 最短时间
    private long maxOrbitGap = 9999999;         // 两(升/降)轨间隔: 最大圈数
    private long maxInterval = 9999999;         // 两(升/降)轨间隔: 最长时间

    // getter & setter
    public double getMinElevation() {
        return minElevation;
    }
    public void setMinElevation(double minElevation) {
        this.minElevation = minElevation;
    }

    public double getMaxElevation() {
        return maxElevation;
    }
    public void setMaxElevation(double maxElevation) {
        this.maxElevation = maxElevation;
    }

    public long getEarlistTime() {
        return earlistTime;
    }
    public void setEarlistTime(long earlistTime) {
        this.earlistTime = earlistTime;
    }

    public long getDeadline() {
        return deadline;
    }
    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public List<String> getExpandList() {
        return expandList;
    }
    public void setExpandList(List<String> expandList) {
        this.expandList = expandList;
    }

    public List<String> getFunctionList() {
        return functionList;
    }
    public void setFunctionList(List<String> functionList) {
        this.functionList = functionList;
    }

    public List<String> getFrequentList() {
        return frequentList;
    }
    public void setFrequentList(List<String> frequentList) {
        this.frequentList = frequentList;
    }

    public String getOrbitType() {
        return orbitType;
    }
    public void setOrbitType(String orbitType) {
        this.orbitType = orbitType;
    }

    public long getMinOrbitGap() {
        return minOrbitGap;
    }
    public void setMinOrbitGap(long minOrbitGap) {
        this.minOrbitGap = minOrbitGap;
    }

    public long getMinInterval() {
        return minInterval;
    }
    public void setMinInterval(long minInterval) {
        this.minInterval = minInterval;
    }

    public long getMaxOrbitGap() {
        return maxOrbitGap;
    }
    public void setMaxOrbitGap(long maxOrbitGap) {
        this.maxOrbitGap = maxOrbitGap;
    }

    public long getMaxInterval() {
        return maxInterval;
    }
    public void setMaxInterval(long maxInterval) {
        this.maxInterval = maxInterval;
    }

/* class ends */
}
