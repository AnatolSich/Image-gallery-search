package com.agile.engine.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TaskSchedulerUtils {

    public static ThreadPoolTaskScheduler getThreadPoolTaskScheduler(String namePrefix) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setDaemon(true);
        scheduler.setThreadNamePrefix(namePrefix);
        scheduler.afterPropertiesSet();
        return scheduler;
    }
}
