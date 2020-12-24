package com.glenfordham.webserver.config;

/**
 * Contains all supported application arguments
 */
public enum Arguments {

    HELP(
            "h",
            false,
            false,
            "help",
            false,
            "displays the help text you're seeing now",
            null
    ),
    PORT(
            "p",
            false,
            true,
            "port",
            true,
            "sets the port to listen on  eg. 80",
            "80"),
    TEMP_DIR_PREFIX(
            "t",
            false,
            true,
            "tempDirPrefix",
            true,
            "sets prefix for Tomcat's temporary directory name  eg. prefix",
            "tomcat-base-dir"),
    WEB_APP_ROOT(
            "w",
            true,
            true,
            "webapproot",
            true,
            "sets the web application root. A directory should be located inside named 'webroot', which is where content will be served from",
            null);

    private final String name;
    private final boolean isRequired;
    private final boolean isConfig;
    private final String longName;
    private final boolean isArgValueRequired;
    private final String helpMessage;
    private final String defaultValue;

    Arguments(String name, boolean isRequired, boolean isConfig, String longName, boolean isArgValueRequired, String helpMessage, String defaultValue) {
        this.name = name;
        this.isRequired = isRequired;
        this.isConfig = isConfig;
        this.longName = longName;
        this.isArgValueRequired = isArgValueRequired;
        this.helpMessage = helpMessage;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the name of the parameter
     *
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether is the argument is required or not
     *
     * @return true if the argument is required
     */
    public boolean getIsRequired() {
        return isRequired;
    }

    /**
     * Returns whether is the argument is application config or not
     *
     * @return true if the argument is application config
     */
    public boolean getIsConfig() {
        return isConfig;
    }


    /**
     * Returns the long name variant of the argument
     *
     * @return the argument long name variant
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Returns whether an argument (value) is required for the argument
     *
     * @return true if an argument is required
     */
    public boolean isArgValueRequired() {
        return isArgValueRequired;
    }

    /**
     * Returns the help message for the given argument, used by commons-cli
     *
     * @return help message
     */
    public String getHelpMessage() {
        return helpMessage;
    }

    /**
     * Returns the default value for the argument
     *
     * @return default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }
}
