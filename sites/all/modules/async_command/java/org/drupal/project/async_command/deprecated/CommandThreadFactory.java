package org.drupal.project.async_command.deprecated;

import org.drupal.project.async_command.AsyncCommand;
import org.drupal.project.async_command.DrupalUtils;

import java.util.concurrent.ThreadFactory;

/**
 * Creates default thread for command.
 * TODO: this won't work with ThreadPoolExecutor because the Runnable is not AsyncCommand, but a adapter.
 */
public class CommandThreadFactory implements ThreadFactory {

    private ThreadGroup defaultThreadGroup = new ThreadGroup("CommandThreadGroup");

    private final int WEIGHT_LOWER_BOUND = -1000;
    private final int WEIGHT_UPPER_BOUND = 1000;


    public static class Thread extends java.lang.Thread {
        private AsyncCommand command;

        public Thread(ThreadGroup group, AsyncCommand command) {
            super(group, command);
            this.command = command;
        }

        public AsyncCommand getCommand() {
            return command;
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        assert r.getClass().isAssignableFrom(AsyncCommand.class) : "CommandThreadFactory can only creates AsyncCommand runnables.";
        AsyncCommand command = (AsyncCommand) r;
        Thread thread = new Thread(defaultThreadGroup, command);

        // do some settings.
        thread.setDaemon(false);
        // name is CommandName-CommandID-ThreadCreatedTime
        thread.setName(command.getIdentifier() + "-" + command.getRecord().getId() + "-" + DrupalUtils.getLocalUnixTimestamp());

        // map weight to priority.
        int priority = Thread.NORM_PRIORITY;
        if (command.getRecord().getWeight() < WEIGHT_LOWER_BOUND) {
            // the smaller the weight, the higher priority it gets.
            priority = Thread.MAX_PRIORITY;
        } else if (command.getRecord().getWeight() > WEIGHT_UPPER_BOUND) {
            priority = Thread.MIN_PRIORITY;
        }
        thread.setPriority(priority);

        thread.setUncaughtExceptionHandler(new java.lang.Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(java.lang.Thread t, Throwable e) {
                assert t.getClass().isAssignableFrom(Thread.class);
                AsyncCommand command = ((Thread) t).getCommand();
                // delegate to the DrupalApp to handle exception.
                //command.getDrupalApp().handleException(command.getRecord(), e);
            }
        });

        return thread;
    }

}
