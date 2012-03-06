package org.drupal.project.async_command.deprecated;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Class for Jython script
 */
@Deprecated
abstract public class JythonDrupalApp extends AbstractDrupalAppDeprecated {
    /**
     * Run the async command. This is different from the base class because BSH reflection doesn't work for Jython files.
     * Need to use the PythonInterpreter to evaluate the method.
     */
    @Override
    protected Result runCommand(String command) throws EvaluationFailureException {
        try {
            // run each command
            PythonInterpreter interpreter = new PythonInterpreter();
            interpreter.set("app", this);
            PyObject obj = interpreter.eval("app."+ command);
            Result result = (Result)(obj.__tojava__(Result.class));
            return result;
        } catch (Throwable e) {
            logger.severe("Cannot evaluate Jython command: " + command);
            throw new EvaluationFailureException(e);
        }
    }
}
