package org.drupal.project.async_command.exception;

/**
 * Handles any Drupal related exceptions.
 */
public class DrupalAppException extends RuntimeException {

    public DrupalAppException(Throwable t) {
        super(t);
    }

    public DrupalAppException(String msg) {
        super(msg);
    }

    public DrupalAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
