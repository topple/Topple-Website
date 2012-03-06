package org.drupal.project.async_command.test;

import org.drupal.project.async_command.*;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DrupalUtilsTest {

    @Test
    public void testEvalPhp() throws Exception {
        String str = "Hello, world";
        assertTrue(DrupalUtils.evalPhp("echo \"{0}\";", str).equals(str));
        assertTrue(DrupalUtils.evalPhp("echo 1;").equals("1"));
    }

    @Test
    public void testDecryption() throws Exception {
        Properties config = DrupalUtils.loadProperties(DrupalUtils.getConfigPropertiesFile());
        String key = config.getProperty("mcrypt_secret_key");
        assertNotNull(key);
        EncryptedFieldAdapter encset = new EncryptedFieldAdapter(EncryptedFieldAdapter.Method.MCRYPT, key);

        String orig = "abc=def";
        //System.out.println(MessageFormat.format("echo base64_encode(mcrypt_encrypt(MCRYPT_3DES, \"{0}\", '{1}', 'ecb'));", key, orig));
        String enc = DrupalUtils.evalPhp("echo base64_encode(mcrypt_encrypt(MCRYPT_3DES, \"{0}\", \"{1}\", 'ecb'));", key, orig);
        //System.out.println("Encrypted message: " + enc);
        Properties p = encset.readSettings(enc);
        //p.list(System.out);
        assertTrue(p.getProperty("abc").trim().equals("def"));
    }

    @Test
    public void testReadSettingsFile() throws Exception {
        File settingsFile = DrupalUtils.getDrupalSettingsFile();
        System.out.println(settingsFile.getPath());
        Properties config = DrupalUtils.convertSettingsToConfig(settingsFile);
        assertEquals(config.getProperty("database_type"), "mysql");
        DrupalUtils.prepareConfig(config);
        assertEquals(config.getProperty("url"), "jdbc:mysql://localhost/drupal7");
    }

    @Test
    public void testDrush() throws Exception {
        DrupalUtils.executeDrush("vset", "async_command_drush_test", "Hello", "--always-set");
        String output = DrupalUtils.executeDrush("vget", "async_command_drush_test");
        assertTrue(output.trim().endsWith("\"Hello\""));
        DrupalUtils.executeDrush("vdel", "async_command_drush_test", "--yes");
    }

    @Test
    public void testIdentifier() throws Exception {
        assertTrue(DrupalUtils.getIdentifier(DefaultDrupalApp.class).equals("default"));
        assertTrue(DrupalUtils.getIdentifier(GenericDrupalApp.class).equals("GenericDrupalApp"));
        assertTrue(DrupalUtils.getIdentifier(PingMe.class).equals("PingMe"));
    }

}
