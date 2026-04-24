package org.nudtopt.realworldproblems.apiforsatellite.resource.platform;

import org.nudtopt.realworldproblems.apiforsatellite.resource.Resource;
import org.nudtopt.realworldproblems.apiforsatellite.resource.payload.Payload;
import org.nudtopt.realworldproblems.apiforsatellite.resource.window.DownlinkWindow;
import org.nudtopt.realworldproblems.apiforsatellite.resource.window.Orbit;
import org.nudtopt.realworldproblems.apiforsatellite.resource.window.Window;
import org.nudtopt.realworldproblems.apiforsatellite.task.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Action extends Resource {

    private long beginTime;         // 开始时间
    private long endTime;           // 结束时间
    private double memory;          // 数据量
    private double currentMemory;   // 动作执行完后当前星上固存量
    private boolean rubber;         // 是否进行擦除固存(约定动作结束后)
    private Task task;              // 所属任务
    private Window window;          // 窗口
    private Orbit orbit;            // 某圈
    private Payload payload;        // 执行动作的载荷(相机或天线)
    private Platform platform;      // 执行动作的平台(卫星或测站)
    private Action lastAction;      // 上一个动作
    private Action nextAction;      // 下一个动作

    // getter & setter
    public long getBeginTime() {
        return beginTime;
    }
    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getMemory() {
        return memory;
    }
    public void setMemory(double memory) {
        this.memory = memory;
    }

    public double getCurrentMemory() {
        return currentMemory;
    }
    public void setCurrentMemory(double currentMemory) {
        this.currentMemory = currentMemory;
    }

    public boolean isRubber() {
        return rubber;
    }
    public void setRubber(boolean rubber) {
        this.rubber = rubber;
    }

    public Task getTask() {
        return task;
    }
    public void setTask(Task task) {
        this.task = task;
    }

    public Window getWindow() {
        return window;
    }
    public void setWindow(Window window) {
        this.window = window;
    }

    public Orbit getOrbit() {
        return orbit;
    }
    public void setOrbit(Orbit orbit) {
        this.orbit = orbit;
    }

    public Payload getPayload() {
        return payload;
    }
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public Platform getPlatform() {
        return platform;
    }
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Action getLastAction() {
        return lastAction;
    }
    public void setLastAction(Action lastAction) {
        this.lastAction = lastAction;
    }

    public Action getNextAction() {
        return nextAction;
    }
    public void setNextAction(Action nextAction) {
        this.nextAction = nextAction;
    }

    /**
     * 根据任务信息创建/更新动作
     * @param type 动作类型
     */
    public void update(String type) {
        if(task == null)        return;
        name = type;
        if(task.getMode() <= 0) return;
        create(type);
    }
    public void create(String type) {
        if(task == null)    return;
        name  = type;
        switch (type) {
            default:
            case "成像":
                beginTime  = task.getImageBeginTime();
                endTime    = task.getImageEndTime();
                capability = task.getImageDuration();
                memory     = task.getImageMemory();
                window     = task.getImageChance().getImageWindow();
                orbit      = task.getImageChance().getImageWindow().getOrbit();
                payload    = task.getImageChance().getImageWindow().getCamera();
                break;
            case "数传":
                beginTime  = task.getDownlinkBeginTime();
                endTime    = task.getDownlinkEndTime();
                capability = task.getDownlinkDuration();
                memory     = task.getImageMemory();
                window     = task.getDownlinkChance().getDownlinkWindow();
                orbit      = task.getDownlinkChance().getDownlinkWindow().getOrbit();
                payload    = task.getDownlinkChance().getDownlinkWindow().getAntenna();
                rubber     = task.isRubber();
                break;
        }
        id = beginTime;
        platform = orbit.getSatellite();
    }


    /**
     * 根据任务, 创建动作
     * @param task 任务
     * @param type 动作: 成像/数传
     * @return     返回动作
     */
    public static Action create(Task task, String type) {
        Action action = new Action();
        action.setTask(task);
        action.create(type);
        return action;
    }
    public static <T extends Task> List<Action> create(List<T> taskList, String type) {
        List<Action> actionList = new ArrayList<>();
        for(T task : taskList) {
            Action action = create(task, type);
            actionList.add(action);
        }
        return actionList;
    }



    // 根据任务集获取卫星动作序列
    public static List<Action> getActionList(List<Task> taskList) {
        List<Action> actionList = new ArrayList<>();
        for(Task task : taskList) {
            if(task.getMode() <= 0) continue;               // 只记成功任务
            Action imageAction = new Action();              // 创建成像动作
            imageAction.setTask(task);
            imageAction.update("成像");
            actionList.add(imageAction);
            if(!task.isNoDownlink()) {
                Action downlinkAction = new Action();       // 创建数传动作
                downlinkAction.setTask(task);
                downlinkAction.update("数传");
                actionList.add(downlinkAction);
            }
        }
        actionList.sort(Comparator.comparingLong(Action::getBeginTime));
        return actionList;
    }


    // 检查动作序列基本约束
    public static boolean checkActionList(List<Action> actionList) {
        List<Task> memoryTaskList = new ArrayList<>();      // 固存中存储的任务集合
        double memory = 0;                                  // 记录固存量
        int file = 0;                                       // 记录文件数量

        Action lastImageAction = null;
        Action lastDownlinkAction = null;
        for(int i = 0 ; i < actionList.size() - 1 ; i ++) {
            // a. 动作关联
            Action action = actionList.get(i);
            Action nextAction = actionList.get(i + 1);
            action.setNextAction(nextAction);
            nextAction.setLastAction(action);

            // b. 检查前后转换时间约束
            Satellite satellite = action.getTask().getImageChance().getImageWindow().getOrbit().getSatellite();
            boolean checkTransition = satellite.getConstraint().check(action);  // todo 重写间隔时间约束
            if(!checkTransition) {
                return false;
            }

            // c. 连续数传约束判断
            if(action.getName().equals("成像")) {
                lastImageAction = action;
                memoryTaskList.add(action.getTask());
                memory += action.getTask().getImageMemory();
                file ++;
            }
            if(action.getName().equals("数传")) {
                if(lastImageAction != null && lastDownlinkAction != null) {
                    if(!checkContinuousDownlink(lastDownlinkAction, lastImageAction, action)) {
                        return false;   // 非连续数传
                    }
                }
                lastDownlinkAction = action;
            }

            // d. 固存约束
            if(file   >  satellite.getMemory().getFileCapability())         return false;   // 超过文件数量上限
            if(memory >  satellite.getMemory().getCapability())             return false;   // 固存容量判断
            if(memory >= satellite.getMemory().getCapability() * 0.75 &&                    // 超过75%则擦
                action.getName().equals("数传") && nextAction.getName().equals("成像")) {   // 在下一次记录前擦
                long rubberTime = action.getEndTime();                                      // 记当前数传动作结束时间为擦除
                // 判断固存擦除时约束是否满足, 需分两种情况
                // 1 指定文件号擦除, 只擦已回传的
                if(satellite.getMemory().isRubberByFile()) {
                    for(int j = memoryTaskList.size() - 1 ; j >= 0 ; j --) {
                        Task memoryTask = memoryTaskList.get(j);
                        long downlinkTime = memoryTask.getDownlinkEndTime();
                        if(downlinkTime <= rubberTime) {                    // 若当前时间以前已回传
                            memoryTaskList.remove(j);                       // 则移除该任务
                            memory -= memoryTask.getImageMemory();          // 扣除任务数据
                            file --;                                        // 文件号-1;
                            memoryTask.setSelfRubberTime(rubberTime);       // 记录该文件的擦除时间
                        }
                    }
                } else { // 2 整块擦除, 全部擦除, 且需回传完
                    for(Task memoryTask : memoryTaskList) {
                        long downlinkTime = memoryTask.getDownlinkEndTime();
                        if(downlinkTime > rubberTime) {                     // 若当前时间前尚未回传
                            return false;                                   // 则违反约束
                        }
                        memoryTask.setSelfRubberTime(rubberTime);           // 记录该文件的擦除时间
                    }
                    memoryTaskList.clear();
                    memory = 0;                                             // 固存清零
                    file = 0;                                               // 文件数清零
                }
                action.getTask().setRubber(true);                           // 记录此次擦除动作
            }

            // e. 顺序回放判断
            if(satellite.getMemory().isSequentialDownlink()) {

            }
            action.setCurrentMemory(memory);                                // 记录当前固存量
        }
        return true;
    }


    // 判断同窗口内数传动作是否连续？
    public static boolean checkContinuousDownlink(Action downlinkAction_1, Action imageAction, Action downlinkAction_2) {
        DownlinkWindow downlinkWindow_1 = downlinkAction_1.getTask().getDownlinkChance().getDownlinkWindow();
        DownlinkWindow downlinkWindow_2 = downlinkAction_2.getTask().getDownlinkChance().getDownlinkWindow();
        Satellite satellite = downlinkAction_1.getOrbit().getSatellite();
        if(satellite.getName().contains("B17"))                 return true;    // 17无所谓
        if(downlinkWindow_1 != downlinkWindow_2)                return true;    // 只有同窗口才判断
        if(imageAction.getBeginTime() >  downlinkAction_1.getBeginTime() &&     // 成像动作在两个数传动作之间
           imageAction.getEndTime()   <= downlinkAction_2.getBeginTime()) {
            return false;
        }
        return true;
    }


/* class ends */
}
