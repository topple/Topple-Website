package org.drupal.project.async_command;

import org.drupal.project.async_command.exception.CommandExecutionException;
import org.drupal.project.async_command.exception.CommandParseException;
import org.drupal.project.async_command.exception.DrupalAppException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This is the new DrupalApp class. A launcher has a field to launch DrupalApp, rather than DrupalApp has a field of launcher.
 */
public class GenericDrupalApp implements Runnable {

    /**
     * Various running mode of the DrupalApp
     */
    public static enum RunningMode {
        /**
         * Only execute the next available command in the queue, and then exit.
         */
        FIRST,

        /**
         * Retrieve a list of commands, execute them in serial, and then exit.
         */
        SERIAL,

        /**
         * Retrieves a list of commands, execute them in parallel, and then exit.
         */
        PARALLEL,

        /**
         * Continuously retrieve new commands from the queue, execute them in parallel (serial just means thread=1), and exit only when told to.
         */
        NONSTOP;
    }

    private RunningMode runningMode = RunningMode.SERIAL;

    protected DrupalConnection drupalConnection;

    protected Properties config;

    protected static Logger logger = DrupalUtils.getPackageLogger();

    /**
     * Default executor, run single thread.
     * This is useful for command that will create another command to run (either in 1 thread or in multiple thread)
     */
    protected ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Class name: the class object.
     */
    protected Map<String, Class> acceptableCommandClass = new HashMap<String, Class>();

    /**
     * Register acceptable AsyncCommand classes in constructor. By default, this registers PingMe command.
     * Any DrupalApp should have at least one drupal database connection. Otherwise it's not "DrupalApp" anymore.
     *
     * @param drupalConnection Connection to a Drupal database that has the {async_command} table.
     */
    public GenericDrupalApp(DrupalConnection drupalConnection) {
        logger.fine("Constructor called for GenericDrupalApp");
        assert drupalConnection != null;
        setDrupalConnection(drupalConnection);
        // TODO: the logic should be that DrupalApp would initiate config and create DrupalConnection, not the other way around.
        config = drupalConnection.config;
        // register command classes
        registerCommandClass(PingMe.class);
    }

    /**
     * This constructor requires manually set DrupalConnection.
     * Access is "protected".
     */
    protected GenericDrupalApp() {
        logger.fine("Default constructor called for GenericDrupalApp. Manually set DrupalConnection required.");
    }


    /////////////////////////////////////////////////


    public void setRunningMode(RunningMode runningMode) {
        this.runningMode = runningMode;
    }

    /**
     * Only derived class can change drupal connection.
     *
     * @param drupalConnection
     */
    protected void setDrupalConnection(DrupalConnection drupalConnection) {
        assert drupalConnection != null;
        this.drupalConnection = drupalConnection;
    }

    public DrupalConnection getDrupalConnection() {
        return this.drupalConnection;
    }

    /**
     * Specifies the name this DrupalApp is known as. By default is the class name. You can override default value too.
     * @return The identifier of the app.
     */
    public String getIdentifier() {
        return DrupalUtils.getIdentifier(this.getClass());
    }


    /**
     * Create an object of AsyncCommand based on the CommandRecord.
     * This function can't be moved into CommandRecord because CommandRecord is not award of different AsyncCommand classes.
     *
     * @param record
     * @return
     */
    AsyncCommand parseCommand(CommandRecord record) throws CommandParseException {
        if (acceptableCommandClass.containsKey(record.getCommand())) {
            Class commandClass = acceptableCommandClass.get(record.getCommand());
            try {
                Constructor<AsyncCommand> constructor = commandClass.getConstructor(CommandRecord.class, GenericDrupalApp.class);
                return constructor.newInstance(record, this);
            } catch (NoSuchMethodException e) {
                throw new DrupalAppException(e);
            } catch (InvocationTargetException e) {
                throw new DrupalAppException(e);
            } catch (InstantiationException e) {
                throw new DrupalAppException(e);
            } catch (IllegalAccessException e) {
                throw new DrupalAppException(e);
            }
        } else {
            throw new CommandParseException("Invalid command or not registered with the DrupalApp. Command: " + record.getCommand());
        }
    }

    /**
     * Register a command with the Drupal application.
     *
     * @param commandClass
     */
    public void registerCommandClass(Class<? extends AsyncCommand> commandClass) {
        // the command class has to be a subclass of AsyncCommand
        // assert AsyncCommand.class.isAssignableFrom(commandClass);
        String id = DrupalUtils.getIdentifier(commandClass);
        acceptableCommandClass.put(id, commandClass);
    }

    /**
     * Register a command with arbitrary identifier.
     *
     * @param identifier
     * @param commandClass
     */
    public void registerCommandClass(String identifier, Class commandClass) {
        // the command class has to be a subclass of AsyncCommand
        assert AsyncCommand.class.isAssignableFrom(commandClass);
        acceptableCommandClass.put(identifier, commandClass);
    }


    @Override
    public void run() {
        assert drupalConnection != null;  // this shouldn't be a problem in runtime.
        drupalConnection.connect();  // if it's not connected yet. make the connection.

        switch (runningMode) {
            case FIRST:
                throw new UnsupportedOperationException("PARALLEL running mode not supported yet.");
                //break;
            case SERIAL:
                runSerial();
                break;
            case PARALLEL:
                runParallel();
                break;
            case NONSTOP:
                throw new UnsupportedOperationException("NONSTOP running mode not supported yet.");
                //break;
        }
        // even though serial didn't use executor, we still shut it down in case any commands used it.
        logger.info("Shutdown parallel executor.");
        executor.shutdown();

        // close drupalConnection here is odd because DrupalApp didn't create the connection.
        // whoever create the connection should be responsible closing it.
        drupalConnection.close();
        drupalConnection = null;
        logger.info("Running the DrupalApp is accomplished.");
    }

    /**
     * Handle any exception (Throwable) occurred during command execution.
     * AsyncCommand should delegate exception handling to DrupalApp.
     *
     * @param record
     * @param exception
     */
    protected void handleException(CommandRecord record, Throwable exception) {
        if (CommandParseException.class.isInstance(exception)) {
                record.setStatus(AsyncCommand.Status.UNRECOGNIZED);
                record.setMessage(exception.getMessage());
        } else if (CommandExecutionException.class.isInstance(exception)) {
            logger.severe("Cannot run command '" + record.getCommand() + "' for application '" + this.getIdentifier() + "'. Error message: " + exception.getMessage());
            // assume status and message are already set in the command execution
            if (record.getStatus() == null) {
                record.setStatus(AsyncCommand.Status.FAILURE);
            }
            if (record.getMessage() == null) {
                record.setMessage(exception.getMessage());
            }
        } else if (DrupalAppException.class.isInstance(exception)) {
            logger.severe("Unexpected error happens for command: " + record.getCommand());
            record.setStatus(AsyncCommand.Status.FAILURE);
            record.setMessage(exception.getMessage());
        } else {
            logger.severe("System error or programming bug.");
            record.setStatus(AsyncCommand.Status.INTERNAL_ERROR);
            record.setMessage("System error or programming bug. See execution log for more details. Error type: " + exception.getClass().getName());
        }
        exception.printStackTrace();
    }

    /**
     * Run in serial mode. We don't use the single thread "executor" here to have more simplicity.
     */
    protected void runSerial() {
        List<CommandRecord> records = drupalConnection.retrievePendingCommandRecord(this.getIdentifier());
        logger.info("Total number of commands to run: " + records.size());
        logger.fine("Sorting commands.");
        Collections.sort(records);
        for (CommandRecord record : records) {
            AsyncCommand command = null;
            try {
                // attention: might need to set "records.start" here in order to handle UREC case.
                command = parseCommand(record);
                logger.info("Executing command: " + record.getCommand());
                command.run();
            } catch (Throwable e) {
                handleException(record, e);
            }

            if (record.getEnd() == null) {
                record.setEnd(DrupalUtils.getLocalUnixTimestamp());
            }
            logger.info("Command finished running with status: " + record.getStatus().toString());
            // if persistResult throws exception, then we simply will exit running the DrupalApp unless we fix the problem.
            // that's why we don't catch exception here and have launcher handle the exception.
            record.persistResult();
        }
    }

    protected void runParallel() {
        int coreThreadNum = Integer.parseInt(config.getProperty("thread_core_number", "2"));
        int maxThreadNum = Integer.parseInt(config.getProperty("thread_max_number", "2"));
        if (maxThreadNum < coreThreadNum) {
            logger.warning("thread_max_number cannot be smaller than thread_core_number.");
            maxThreadNum = coreThreadNum;
        }
        // set the executor to be pooled executor.
        //executor = new ThreadPoolExecutor(coreThreadNum, maxThreadNum, 1, TimeUnit.SECONDS,
        //        new PriorityBlockingQueue<Runnable>(), new CommandThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        executor = new ThreadPoolExecutor(coreThreadNum, maxThreadNum, 0, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy()) {
            @Override
            protected void afterExecute(Runnable runnable, Throwable throwable) {
                super.afterExecute(runnable, throwable);
                if (throwable != null) {
                    assert runnable instanceof Future<?>;
                    CommandRecord record = null;
                    try {
                        record = (CommandRecord) (((Future<?>) runnable).get());
                    } catch (InterruptedException e) {
                        assert false;
                    } catch (ExecutionException e) {
                        assert false;
                    }
                    // delegate to the DrupalApp to handle exception.
                    handleException(record, throwable);
                }
            }
        };

        List<CommandRecord> records = drupalConnection.retrievePendingCommandRecord(this.getIdentifier());
        //logger.fine("Sorting commands.");
        //Collections.sort(records);
        List<Future<CommandRecord>> futuresList = new ArrayList<Future<CommandRecord>>();
        // this is to save the map if future.get generates no results.
        Map<Future<CommandRecord>, CommandRecord> futuresMap = new HashMap<Future<CommandRecord>, CommandRecord>();

        for (CommandRecord record : records) {
            try {
                AsyncCommand command = parseCommand(record);
                Future<CommandRecord> future = executor.submit(command, record);
                futuresList.add(future);
                futuresMap.put(future, record);
            } catch (CommandParseException e) {
                record.setStatus(AsyncCommand.Status.UNRECOGNIZED);
                record.setMessage(e.getMessage());
                // if any record generate errors when persisting, we stop the main program by not handling any RuntimeException.
                record.persistResult();
                e.printStackTrace();
            }
        }
        logger.info("Total number of commands to run in parallel: " + futuresList.size());

        for (Future<CommandRecord> future : futuresList) {
            CommandRecord record = null;
            try {
                // TODO: could enforce timeout here.
                record = future.get();
            } catch (Throwable e) {
                if (record == null) {
                    record = futuresMap.get(future);
                }
                handleException(record, e);
            }
            record.persistResult();
        }
    }

    public Properties getConfig() {
        return config;
    }
}
