package org.drupal.project.async_command.deprecated;

@Deprecated
public class EvaluationFailureException extends Exception {
    public EvaluationFailureException(Throwable e) {
        super(e);
    }
    public EvaluationFailureException(String msg) {
        super(msg);
    }
}
