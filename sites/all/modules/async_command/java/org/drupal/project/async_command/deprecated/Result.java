package org.drupal.project.async_command.deprecated;

/**
 * Each async_command has to return an instance of this class.
 */
@Deprecated
public class Result {
    private boolean successful;
    private String message;

    public Result(boolean successful, String message) {
        this.successful = successful;
        this.message = message;
    }
    /**
     * @return true if successful, false otherwise
     */
    public boolean getStatus() {
        return this.successful;
    }

    public String getMessage() {
        return this.message;
    }
}
