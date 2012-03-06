package org.drupal.project.async_command.test;

import org.drupal.project.async_command.*;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class DefaultDrupalAppTest {

    @Test
    public void testPingMe() throws SQLException {
        // create a pingme command.
        // attention: this drupal connection is the one Drush is using. could be different from the one in the drupalConnection created earlier
        String output = DrupalUtils.executeDrush("async-command", "default", "PingMe", "From UnitTest", "string1=Hello");
        System.out.println("Drush output: " + output);

        DrupalConnection drupalConnection = DrupalConnection.create();
        drupalConnection.connect();
        Long id = DrupalUtils.getLong(drupalConnection.queryValue("SELECT max(id) FROM {async_command}"));
        DefaultDrupalApp drupalApp = new DefaultDrupalApp(drupalConnection);
        drupalApp.run();

        // run() closes the drupal connection, so we recreate again.
        drupalConnection.connect(true);
        assertTrue(output.trim().endsWith(id.toString()));
        CommandRecord record = drupalConnection.retrieveCommandRecord(id);
        assertTrue(record.getStatus().equals(AsyncCommand.Status.SUCCESS));
        assertTrue(record.getMessage().endsWith("Hello"));
    }

    @Test
    public void testParallel() throws SQLException {
        DrupalConnection drupalConnection = DrupalConnection.create();
        drupalConnection.connect();

        // create a pingme command.
        // attention: this drupal connection is the one Drush is using. could be different from the one in the drupalConnection created earlier
        DrupalUtils.executeDrush("async-command", "default", "PingMe", "Test parallel (from UnitTest)", "string1=medium|number1=5000|number3=-1");
        Long id1 = DrupalUtils.getLong(drupalConnection.queryValue("SELECT max(id) FROM {async_command}"));
        DrupalUtils.executeDrush("async-command", "default", "PingMe", "Test parallel (from UnitTest)", "string1=short|number1=1000|number3=-1");
        Long id2 = DrupalUtils.getLong(drupalConnection.queryValue("SELECT max(id) FROM {async_command}"));
        DrupalUtils.executeDrush("async-command", "default", "PingMe", "Test parallel (from UnitTest)", "string1=long|number1=10000|number3=-1");
        Long id3 = DrupalUtils.getLong(drupalConnection.queryValue("SELECT max(id) FROM {async_command}"));

        DefaultDrupalApp drupalApp = new DefaultDrupalApp(drupalConnection);
        drupalApp.setRunningMode(GenericDrupalApp.RunningMode.PARALLEL);
        drupalApp.run();

        // run() closes the drupal connection, so we recreate again.
        drupalConnection.connect(true);
        List<Object[]> results = drupalConnection.queryArray("SELECT id FROM {async_command} WHERE end IS NOT NULL AND app='default' AND command='PingMe' AND number3=-1 ORDER BY end DESC");
        //System.out.println((results.get(0))[0] + ", " + (results.get(1))[0] + ", " + (results.get(2))[0]);
        //System.out.println(id3 + ", " + id1 + ", " + id2);
        // since there's waiting time, this should be the correct order
        assertTrue(id3.equals(DrupalUtils.getLong((results.get(0))[0])));
        assertTrue(id1.equals(DrupalUtils.getLong((results.get(1))[0])));
        assertTrue(id2.equals(DrupalUtils.getLong((results.get(2))[0])));
    }
}
