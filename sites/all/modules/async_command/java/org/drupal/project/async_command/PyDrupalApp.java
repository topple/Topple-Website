package org.drupal.project.async_command;

public class PyDrupalApp extends GenericDrupalApp {

    /**
     * This calls a function initialize() for other initialization stuff.
     * Jython doesn't support function overloading, so we can only do this.
     *
     * @param drupalConnection Connection to a Drupal database that has the {async_command} table.
     */
    public PyDrupalApp(DrupalConnection drupalConnection) {
        super(drupalConnection);
        logger.fine("Constructor called for PyDrupalApp");
        initialize();
    }

    protected void initialize() {
        // do nothing here. for overriding purpose. Usually register command here.
    }

    /**
     * anually set DrupalConnection and call initialize() required.
     */
    protected PyDrupalApp() {
        super();
        logger.fine("Default constructor called for PyDrupalApp. Manually set DrupalConnection and call initialize() required.");
    }

    /**
     * Derived function has to override it.
     */
    @Override
    public String getIdentifier() {
        throw new IllegalArgumentException("Please override getIdentifier()");
    }

    /**
     * Has to use a different name for the function because Jython/Python doesn't support function overloading.
     * @param identifier
     * @param commandClass
     */
    protected void registerCommandClassWithIdentifier(String identifier, Class<? extends AsyncCommand> commandClass) {
        registerCommandClass(identifier, commandClass);
    }
}
