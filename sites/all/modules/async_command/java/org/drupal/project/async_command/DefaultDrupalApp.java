package org.drupal.project.async_command;


/**
 * This DrupalApp is mostly used for testing/demo purposes.
 */
@Identifier("default")
public class DefaultDrupalApp extends GenericDrupalApp {

    public DefaultDrupalApp(DrupalConnection drupalConnection) {
        super(drupalConnection);
    }

    public static void main(String[] args) {
        logger.finest("You have set the log level to be finest. Expect to see many messages.");
        CommandLineLauncher launcher = new CommandLineLauncher(DefaultDrupalApp.class);
        launcher.launch(args);
    }
}
