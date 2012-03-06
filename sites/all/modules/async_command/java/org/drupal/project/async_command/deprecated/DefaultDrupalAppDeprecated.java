package org.drupal.project.async_command.deprecated;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

@Deprecated
public class DefaultDrupalAppDeprecated extends AbstractDrupalAppDeprecated {
    @Override
    public String identifier() {
        return "default";
    }

    public Result dummy() {
        return dummy(null);
    }

    public Result dummy(String message) {
        if (message == null) {
            return new Result(true, "no message");
        } else if (message.equals("fail me")) {
            return new Result(false, "mission failed.");
        } else {
            String msg = "You said " + message;
            return new Result(true, msg);
        }
    }

    public static void main(String[] args) {
        DefaultDrupalAppDeprecated app = new DefaultDrupalAppDeprecated();
        app.handleCLI(args);
    }


    //////////////////////////////////////////////// test class ////////////////////////////////////////////

    public static class TestMe {
        DefaultDrupalAppDeprecated app;

        @Before
        public void setUp() throws Exception {
            app = new DefaultDrupalAppDeprecated();
            //app.initDrupalConnection();
        }

        //@Test
        public void testBasic() throws Exception {
            app.testConnection();
            app.config.list(System.out);
        }

        @Test
        public void testIdentifier() throws Exception {
            assertTrue(app.identifier().equals("default"));
        }


        //@Test
        public void testEvalPhp() throws Exception {
            String str = "Hello, world";
            assertTrue(app.evalPhp("echo \"{0}\";", str).equals(str));
            assertTrue(app.evalPhp("echo 1;").equals("1"));

            app.config.put("php_cli", "/usr/bin/php");
            assertTrue(app.evalPhp("echo \"{0}\";", str).equals(str));
            app.config.remove("php_cli");
        }

        //@Test(expected=DrupalAppException.class)
        public void testEvalPhpFail() throws Exception {
            app.config.put("php_cli", "/usr/bin/phpx");
            assertTrue(app.evalPhp("echo 1;").equals("1"));
            app.config.remove("php_cli");
        }

        @Test
        public void testDecryption() throws Exception {
            app.initDrupalConnection();
            //app.config.list(System.out);
            String orig = "abc=def";
            String key = app.config.getProperty("mcrypt_secret_key");
            assertNotNull(key);

            //System.out.println(MessageFormat.format("echo base64_encode(mcrypt_encrypt(MCRYPT_3DES, \"{0}\", '{1}', 'ecb'));", key, orig));
            String enc = app.evalPhp("echo base64_encode(mcrypt_encrypt(MCRYPT_3DES, \"{0}\", \"{1}\", 'ecb'));", key, orig);
            //System.out.println("Encrypted message: " + enc);
            Properties p = app.readEncryptedSettingsField(enc, AbstractDrupalAppDeprecated.EncryptionMethod.MCRYPT);
            //p.list(System.out);
            assertTrue(p.getProperty("abc").trim().equals("def"));
        }

        @Test
        public void testDrupalVariable() throws Exception {
            app.initDrupalConnection();

            Object value = app.drupalVariableGet("anonymous");
            assertTrue(value.getClass() == String.class);
            assertTrue(((String) value).equals("Anonymous"));

            String siteName = (String) app.drupalVariableGet("site_name");
            //System.out.println(siteName);
            String testSiteName = "site name in test";
            app.drupalVariableSet("site_name", testSiteName);
            assertTrue(((String) app.drupalVariableGet("site_name")).equals(testSiteName));
            app.drupalVariableSet("site_name", siteName);
        }

        @Test
        public void testDbUtils() throws Exception {
            app.initDrupalConnection();
            long uid = (Long) app.queryValue("SELECT uid FROM {users} WHERE uid=1");
            assertTrue(uid == 1L);
            Object result = app.queryValue("SELECT uid FROM {users} WHERE uid=-1");
            assertNull(result);
        }

        @Test
        public void testBatchUpdate() throws Exception {
            app.initDrupalConnection();
            app.maxBatchSize = 2;
            String s1 = app.evalPhp("echo serialize(2);");
            Object[][] params1 = {{"async_command_test1", "1".getBytes()}, {"async_command_test2", s1.getBytes()}, {"async_command_test3", "3".getBytes()}};
            app.batch("INSERT INTO {variable}(name, value) VALUES(?, ?)", params1);

            long num1 = (Long) app.queryValue("SELECT COUNT(*) FROM {variable} WHERE name LIKE 'async_command_test%'");
            assertEquals(num1, 3);

            int v1 = (Integer) app.drupalVariableGet("async_command_test2");
            assertEquals(v1, 2);

            String s2 = app.evalPhp("echo serialize('abc');");
            Object[][] params2 = {{s2.getBytes(), "async_command_test1"}};
            app.batch("UPDATE {variable} SET value=? WHERE name=?", params2);
            String v2 = (String) app.drupalVariableGet("async_command_test1");
            assertEquals(v2, "abc");

            app.maxBatchSize = 0;
            Object[][] params3 = {{"async_command_test1"}, {"async_command_test2"}, {"async_command_test3"}};
            app.batch("DELETE FROM {variable} WHERE name=?", params3);
            long num2 = (Long) app.queryValue("SELECT COUNT(*) FROM {variable} WHERE name LIKE 'async_command_test%'");
            assertEquals(num2, 0);
        }

        @Test
        public void testQueryValue() throws Exception {
            app.initDrupalConnection();
            Object o = app.queryValue("SELECT nid FROM {node} WHERE nid=0");
            assertNull(o);
            long i = (Long) app.queryValue("SELECT COUNT(*) FROM {variable}");
            assertTrue(i > 0);
        }

        //@Test
        public void testReadSettingsPHP() throws Exception {
            System.out.println(app.getDefaultSettingsPhpFile());
            assertTrue("/opt/drupal7/sites/default/settings.php".equals(app.getDefaultSettingsPhpFile().getAbsolutePath()));
            app.loadSettingsPhp(app.getDefaultSettingsPhpFile());
            String url = app.config.getProperty("url");
            assertTrue(url.equals("jdbc:mysql://localhost/drupal7"));
        }

        @Test
        public void testRetrievePendingCommand() {
            // TODO: write me.
        }

        // native decryption program.
        /*public Result encryptionTest() throws DrupalAppException {
            try {
                String base64Str = (String) queryValue("SELECT field_encset_1_value FROM {field_data_field_encset_1} WHERE entity_id=8");
                byte[] encrypted = Base64.decodeBase64(base64Str);
                Cipher cipher = Cipher.getInstance("TripleDES/ECB/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec("6x37hX".getBytes(), "TripleDES"));
                byte[] decrypted = cipher.doFinal(encrypted);
                Properties properties = new Properties();
                properties.load(new ByteArrayInputStream(decrypted));
                return new Result(true, properties.toString());
            } catch (Exception e) {
                e.printStackTrace();
                throw new DrupalAppException(e);
            }
        }*/

        /*public Result decodeTest() throws SQLException, DrupalAppException {
            //String value = (String) queryValue("SELECT field_encset_base64_value FROM {field_data_field_encset_base64} WHERE entity_id=7");
            String value = (String) queryValue("SELECT field_encset_1_value FROM {field_data_field_encset_1} WHERE entity_id=8");
            Properties properties = readEncryptedSettingsField(value, EncryptionMethod.MCRYPT);
            return new Result(true, properties.toString());
        }*/

    }

}
