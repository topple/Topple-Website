package org.drupal.project.async_command.deprecated;

import bsh.EvalError;
import bsh.Interpreter;
import org.apache.commons.cli.*;
import org.drupal.project.async_command.DrupalConnection;
import org.drupal.project.async_command.DrupalUtils;
import org.drupal.project.async_command.EncryptedFieldAdapter;
import org.drupal.project.async_command.exception.DrupalAppException;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * all external program/script should extends this class for easier access.
 * If using SQL queries, please follow SQL-92 standard to allow maximum compatibility.
 */
@Deprecated
abstract public class AbstractDrupalAppDeprecated {
    private final String VERSION = "7_1_0";

    protected DataSource dataSource;
    protected Logger logger = Logger.getLogger(this.getClass().getName());
    protected DrupalConnection drupalConnection;

    protected RunningMode runningMode = RunningMode.ONCE;
    protected Properties config = new Properties();

    private String configFilePath = getDefaultConfigFilePath();
    private String dbPrefix;
    protected int maxBatchSize;

    /**
     * @return The name of the drupal application.
     */
    abstract public String identifier();

    public enum RunningMode {
        ONCE,
        CONTINUOUS,
        LISTENING,
        FIRST
    }

    @Deprecated
    public static enum EncryptionMethod {
        NONE,
        BASE64,
        MCRYPT,
    }

    /**
     * run the Drupal application using the default settings
     */
    public void runApp() {
        // first, try to establish drupal database connection
        logger.info("Initialize Drupal connection.");
        initDrupalConnection();

        // prepare to run App.
        logger.info("Preparing Drupal applet.");
        prepareApp();

        // retrieve a list of command.
        List<Map<String, Object>> commandList = null;
        try {
            commandList = query("SELECT id, command, uid, eid, created FROM {async_command} " +
                    "WHERE app=? AND status IS NULL ORDER BY created", identifier());
        } catch (SQLException e) {
            logger.severe("Cannot query async_command table.");
            throw new DrupalAppException(e);
        }

        // TODO: also print out a list of command.
        logger.info("Total commands to be executed: " + commandList.size());
        for (Map<String, Object> commandRecord : commandList) {
            int id = ((Long)(commandRecord.get("id"))).intValue();
            String command = (String)(commandRecord.get("command"));

            logger.info("Running async_command: " + command);
            Result result = null;
            try {
                // prepare the command
                prepareCommand((Integer)commandRecord.get("uid"), (Integer)commandRecord.get("eid"), (Integer)commandRecord.get("created"), command);
                // execute the command
                result = runCommand(command);
            } catch (EvaluationFailureException e) {
                updateRecord(id, false, "Command evaluation error. See script log for details. Error: " + e.getMessage());
                e.printStackTrace();
                continue;
            } catch (DrupalAppException e) {
                updateRecord(id, false, e.getMessage());
                e.printStackTrace();
                continue;
            }
            updateRecord(id, result.getStatus(), result.getMessage());
            //logger.info("Result: " + result.getStatus() + " " + result.getMessage().substring(0, 100));
            logger.info("Result: " + result.getStatus());
        }
    }

    // get a list of commands not finished from an app.
    public List<CommandDeprecated> retrievePendingCommand(String app) {
        assert drupalConnection != null;
        ArrayList<CommandDeprecated> commandList = new ArrayList<CommandDeprecated>();

        List<Map<String, Object>> dbResults = null;
        try {
            dbResults = query("SELECT id, command, uid, eid, created FROM {async_command} " +
                    "WHERE app=? AND status IS NULL ORDER BY created", identifier());
        } catch (SQLException e) {
            logger.severe("Cannot query async_command table.");
            throw new DrupalAppException(e);
        }

        for (Map<String, Object> commandRecord : dbResults) {
            CommandDeprecated command = new CommandDeprecated(
                    ((Long)(commandRecord.get("id"))).intValue(),
                    (String)(commandRecord.get("command")),
                    (Integer)commandRecord.get("uid"),
                    (Integer)commandRecord.get("eid"),
                    (Integer)commandRecord.get("created")
                    );
            commandList.add(command);
        }

        return commandList;
    }


    /**
     * Subclass can choose to run some code in order to prepare for the DrupalApp.
     */
    protected void prepareApp() {
        // do nothing in default settings.
    }

    /**
     * Run the async command. Derived classes should handle exceptions.
     */
    protected Result runCommand(String command) throws EvaluationFailureException {
        try {
            // run each command
            Interpreter interpreter = new Interpreter();
            interpreter.set("app", this);
            Result result = (Result) interpreter.eval("app."+ command);
            return result;
        } catch (EvalError e) {
            logger.severe("Cannot execute the command: " + command);
            throw new EvaluationFailureException(e);
        } catch (ClassCastException e) {
            logger.severe("The command should return a Result object.");
            throw new EvaluationFailureException(e);
        }
    }


    protected void prepareCommand(int uid, int eid, int created, String command) {
        // do nothing in default settings.
        // subclass could do some preparation for the command based on uid and eid.
    }


    protected void updateRecord(int id, boolean status, String message) {
        long changed = new Date().getTime() / 1000;
        try {
            update("UPDATE {async_command} SET status=?, message=?, changed=? WHERE id=?", status, message, changed, id);
        } catch (SQLException e) {
            logger.severe("Cannot update status in async_command.");
            throw new DrupalAppException(e);
        }
    }

    // This is a dummy command to test connection.
    public Result pingMe() {
        return new Result(true, "Ping successful.");
    }

    @Deprecated
    public void initDrupalConnection() {
        //assert dataSource == null;  // assert that it hasn't been initialized yet.
        if (drupalConnection != null) {
            logger.warning("Drupal connection exists already. Check your code and make sure things are OK.");
            return;
        }
        // if config already set, then skip reading config file.
        if (!config.containsKey("username") && !config.containsKey("password")) {
            try {
                File configFile = new File(configFilePath);
                File settingsPhpFile = getDefaultSettingsPhpFile();
                if (configFile.exists()) {
                    // read configuration file.
                    Reader configReader = new FileReader(configFile);
                    config.load(configReader);
                } else if (settingsPhpFile.exists()) {
                    // read settings.php file instead
                    logger.info("Fallback using Drupal setting.php file.");
                    loadSettingsPhp(settingsPhpFile);
                } else {
                    throw new DrupalAppException("Database configuration file " + configFilePath + " doesn't exists, and cannot find settings.php either. Please see documentation on how to configure the module.");
                }
            } catch (IOException e) {
                logger.severe("Error reading configuration file.");
                throw new DrupalAppException(e);
            }
        }

        drupalConnection = new DrupalConnection(config);
        drupalConnection.connect();
    }

    @Deprecated
    public void initConfig(String configString) {
        Reader configReader = new StringReader(configString);
        try {
            config.load(configReader);
        } catch (IOException e) {
            throw new DrupalAppException("Cannot read config string.");
        }
    }


    @Deprecated
    protected File getDefaultSettingsPhpFile() {
        try {
            return DrupalUtils.getDrupalSettingsFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new DrupalAppException(e);
        }
    }

    // FIXME: this is absorbed in initDrupalConnection.
    @Deprecated
    public void testConnection() {
        initDrupalConnection();
        try {
            //QueryRunner q = new QueryRunner(dataSource);
            //List<Map<String, Object>> rows = q.query(d("SELECT * FROM {async_command} WHERE app=?"), new MapListHandler(), identifier());
            //for (Map<String, Object> row : rows) {
            //    System.out.println(row.get("id") + ":" + row.get("command"));
            //}
            // TODO-postponed: should use more sophisticated test on user privileges.
            Connection conn = dataSource.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            logger.info("Database connection successful: " + metaData.getDatabaseProductName() + metaData.getDatabaseProductVersion());
            //ResultSet rs = metaData.getColumnPrivileges(null, null, d("{async_command}"), "id");
        } catch (SQLException e) {
            logger.severe("SQL error during testing connection.");
            throw new DrupalAppException(e);
        }
    }

    @Deprecated
    private String getDefaultConfigFilePath() {
        try {
            return DrupalUtils.getConfigPropertiesFile().toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new DrupalAppException(e);
        }
    }

    @Deprecated
    protected String d(String sql) {
        return drupalConnection.d(sql);
    }

    @Deprecated
    protected List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        return drupalConnection.query(sql, params);
    }

    @Deprecated
    protected Object queryValue(String sql, Object... params) throws SQLException {
        return drupalConnection.queryValue(sql, params);
    }

    @Deprecated
    protected int update(String sql, Object... params) throws SQLException {
        return drupalConnection.update(sql, params);
    }

    @Deprecated
    protected int[] batch(String sql, Object[][] params) throws SQLException {
        return drupalConnection.batch(sql, params);
    }

    /*protected int[] batch(Connection connection, String sql, Object[][] params) throws SQLException {
        QueryRunner q = new QueryRunner(dataSource);
        String processedSql = d(sql);
        // fix slow problem [#1185100]
        if (maxBatchSize > 0) {
            int start = 0;
            int end = 0;
            int count;
            int[] num = new int[params.length];
            do {
                end += maxBatchSize;
                if (end > params.length) {
                    end = params.length;
                }
                // run batch query
                logger.fine("Database batch processing: " + start + " to " + end);
                int[] batchNum =  q.batch(connection, processedSql, Arrays.copyOfRange(params, start, end));
                for (count=0; count<batchNum.length; count++) {
                    num[start+count] = batchNum[count];
                }
                start = end;
            } while (end < params.length);
            return num;
        } else {
            logger.fine("Batch processing all.");
            return q.batch(connection, processedSql, params);
        }
    }*/

    /**
     * this function handles CLI; users of the class should call this function.
     * @param args arguments from main()
     */
    public void handleCLI(String[] args) {
        logger.info("DrupalApp VERSION: " + VERSION);

        // build command parser
        Options options = buildOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine command = null;
        try {
            command = parser.parse(options, args);
        } catch (ParseException exp) {
            logger.severe("Cannot parse parameters. Please use -h to see help. Error message: " + exp.getMessage());
            return;
        }

        // get configurable settings. non-exclusive.
        handleCommandSettings(command);

        // handle executable options, mutual exclusive
        handleCommandExecutables(command);
    }


    private void handleCommandExecutables(CommandLine command) {
        // mutual exclusive options.
        if (command.hasOption('h')) {
            // print help message and exit.
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Drupal application -- " + identifier(), buildOptions());
        }
        else if (command.hasOption('t')) {
            // test connection and exit.
            testConnection();
        }
        else if (command.hasOption('e')) {
            // run command directly and exit.
            initDrupalConnection();
            prepareApp();
            String evalStr = command.getOptionValue('e');
            try {
                Result result = runCommand(evalStr);
                String successMsg = result.getStatus() ? "succeeds" : "fails";
                logger.info("Running " + successMsg + ". Message: " + result.getMessage());
            } catch (EvaluationFailureException e) {
                logger.severe("Cannot evaluate script from command line.");
                throw new DrupalAppException(e);
            }
        }
        else {
            // run the application command by command. could be time-consuming
            runApp();
        }
    }


    private void handleCommandSettings(CommandLine command) {
        if (command.hasOption('c')) {
            configFilePath = command.getOptionValue('c');
            logger.info("Set configuration file as: " + configFilePath);
        } else {
            configFilePath = getDefaultConfigFilePath();
        }

        if (command.hasOption('r')) {
            String rm = command.getOptionValue('r');
            if (rm.equals("once")) {
                runningMode = RunningMode.ONCE;
            } else if (rm.equals("continuous")) {
                // TODO: not supported yet
                throw new UnsupportedOperationException();
                //runningMode = RunningMode.CONTINUOUS;
            } else if (rm.equals("listening")) {
                // TODO: not supported yet
                throw new UnsupportedOperationException();
                //runningMode = RunningMode.LISTENING;
            } else {
                logger.severe("Cannot parse parameters for -r. Please use -h to see help. Use default running mode.");
            }
        }
    }


    /**
     * If subclass needs to set properties, Use config.properties instead of override the method here.
     */
    private Options buildOptions() {
        Options options = new Options();
        options.addOption("c", true, "database configuration file");
        options.addOption("r", true, "program running mode: 'once' (default), 'continuous', or 'listening'");
        options.addOption("h", "help", false, "print this message");
        options.addOption("t", "test", false, "test connection to Drupal database");
        options.addOption("e", "eval", true, "evaluate a method call directly");
        return options;
    }

    /**
     * This is the same as handleCLI()
     * @param args
     */
    public void run(String[] args) {
        handleCLI(args);
    }

    @Deprecated
    protected Properties readEncryptedSettingsField(String value, EncryptionMethod encryptionMethod) {
        EncryptedFieldAdapter.Method method;
        switch (encryptionMethod) {

            case BASE64:
                method = EncryptedFieldAdapter.Method.BASE64;
                break;
            case MCRYPT:
                method = EncryptedFieldAdapter.Method.MCRYPT;
                break;
            case NONE:
            default:
                method = EncryptedFieldAdapter.Method.NONE;
                break;
        }
        EncryptedFieldAdapter ensetAdapter = new EncryptedFieldAdapter(method, config.getProperty("mcrypt_secret_key"));
        return ensetAdapter.readSettings(value);
    }

    @Deprecated
    protected Properties readEncryptedSettingsField(String value) {
        return readEncryptedSettingsField(value, EncryptionMethod.MCRYPT);
    }

    @Deprecated
    protected void loadSettingsPhp(File settingsPhp) throws IOException {
        Properties config = DrupalUtils.convertSettingsToConfig(settingsPhp);
        DrupalUtils.prepareConfig(config);
        this.config.putAll(config);
    }

    @Deprecated
    protected String evalPhp(String pattern, Object... params) {
        return DrupalUtils.evalPhp(pattern, params);
    }

    @Deprecated
    protected String evalPhp(String phpCode) {
        return DrupalUtils.evalPhp(phpCode);
    }

    @Deprecated
    protected String getReaderContent(Reader input) throws IOException {
        return DrupalUtils.getContent(input);
    }

    @Deprecated
    protected Map<String, Object> unserializePhpArray(String serialized) {
        return DrupalUtils.unserializePhpArray(serialized);
    }

    @Deprecated
    protected String convertBlobValueToString(Object blobValue) {
        return DrupalUtils.convertBlobToString(blobValue);
    }

    @Deprecated
    protected Object drupalVariableGet(String varName) {
        return drupalConnection.variableGet(varName);
    }

    @Deprecated
    protected void drupalVariableSet(String varName, String varValue) {
        drupalConnection.variableSet(varName, varValue);
    }

    /**
     * @see http://code.google.com/p/json-simple/wiki/EncodingExamples
     * @see http://code.google.com/p/json-simple/
     * @param obj The object to be encoded
     */
    /*protected String encodeJSON(Object obj) {
        return JSONValue.toJSONString(obj);
    }*/


    /**
     * @see http://code.google.com/p/json-simple/wiki/DecodingExamples
     * @see http://code.google.com/p/json-simple/
     * @param jsonText json text to be decoded.
     * @return the JSON object, caller should change it to Map of List or other primitives.
     */
    /*protected Object decodeJSON(String jsonText) {
        return JSONValue.parse(jsonText);
    }*/

}
