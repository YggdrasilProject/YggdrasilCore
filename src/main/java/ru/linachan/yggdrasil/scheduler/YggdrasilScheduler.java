package ru.linachan.yggdrasil.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class YggdrasilScheduler {

    private final Map<String, YggdrasilTask> taskMap = new HashMap<>();

    private final ScheduledExecutorService executorService;

    private static final Logger logger = LoggerFactory.getLogger(YggdrasilScheduler.class);

    public YggdrasilScheduler() {
        executorService = Executors.newScheduledThreadPool(10);
    }

    public boolean scheduleTask(YggdrasilTask task) {
        ScheduledFuture<?> taskHandle;
        if (!taskMap.containsKey(task.getTaskName())) {
            if (task.isPeriodic()) {
                taskHandle = executorService.scheduleAtFixedRate(
                    task.getRunnableTask(),
                    task.getInitialDelay(),
                    task.getExecutionPeriod(),
                    task.getTimeUnit()
                );
                task.setTaskHandle(taskHandle);
                taskMap.put(task.getTaskName(), task);
                logger.info("Periodic task '" + task.getTaskName() + "' scheduled with delay: " + task.getInitialDelay());
            } else {
                taskHandle = executorService.schedule(
                    task.getRunnableTask(),
                    task.getInitialDelay(),
                    task.getTimeUnit()
                );
                task.setTaskHandle(taskHandle);
                taskMap.put(task.getTaskName(), task);
                logger.info("Task '" + task.getTaskName() + "' scheduled with delay: " + task.getInitialDelay());
            }
            return true;
        }
        return false;
    }

    public YggdrasilTask getTask(String taskName) {
        if (this.taskMap.containsKey(taskName)) {
            return this.taskMap.get(taskName);
        }
        return null;
    }

    public void shutdown() {
        for (String taskName: taskMap.keySet()) {
            taskMap.get(taskName).cancelTask();
        }
    }
}
