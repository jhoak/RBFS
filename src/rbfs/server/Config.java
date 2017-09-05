package rbfs.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Function;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rbfs.util.GeneralUtils;

/**
 * This class describes the server's configuration. It contains methods that allow for reading and
 * updating the fields that make up the configuration itself.
 *
 * Default values for each config item are specified in code here. However, a setting's default
 * value can be overridden if an assignment of the form "settingName=newValue" is specified in a
 * special file, "server.ini", located in the root folder. See getSettingsFromConfigFile() to see
 * how this file works.
 *
 * All config items are specified as class fields here. Each field F must be initialized when the
 * class is loaded, and each should have non-private accessor and mutator methods as appropriate.
 *
 * TODO dank logger for this and all other files. unified or class-specific
 * TODO also actually log the errors and whatever else needs logging; debug vs normal info?
 * TODO extend to client side?
 *
 * @author James Hoak
 * @version 1.0
 */
final class Config {
    /* The port on which the server should start. */
    private static final Setting<Integer> port;

    static {
        /*
         * Fields should be initialized here, preferably using a method like makeF() where F is the
         * name of the field. That method should also take a String argument that corresponds to
         * the value of the overridden setting in the config file (if one was found).
         */
        HashMap<String, String> overrides = getSettingsFromConfigFile("./server.ini");
        port = makePort(overrides.get("port"));
    }

    /**
     * Initializes the config setting for the port.
     * @param portVal The override value for the port, found in the config file, or null.
     * @return A Setting corresponding to the port's config setting.
     */
    private static Setting<Integer> makePort(String portVal) {
        Function<Integer, Boolean> isValidPort = (i) -> i >= 0 && i <= 65535;
        Integer overridePort = (portVal == null) ? null : Integer.parseInt(portVal);
        return new Setting<>("port", isValidPort, 1337, overridePort);
    }

    /**
     * Returns the current value of the port config setting.
     * @return Returns the port as an Integer.
     */
    static int getPort() { return port.getValue(); }

    /**
     * Sets the port to a new value. ***Currently unsupported because the behavior in this
     * situation has not been decided!!!***
     * TODO decide this
     * @param overrideVal The new port.
     * @return True if the operation succeeded, and false otherwise.
     * @throws UnsupportedOperationException On every invocation of this call, since v1.0.
     */
    static boolean setPort(int overrideVal) {
        throw new UnsupportedOperationException("Cannot change value of port once assigned.");
    }

    /**
     * Using the server's config file (if it exists), initializes "override values" for each config
     * setting. Otherwise, no overrides are used (until some part of the server subsystem sets a
     * config item to a new value).
     *
     * The config file should only set config items specified in this class. However, it does
     * not have to contain all (or even any) of these items. Besides blank lines and lines starting
     * with a '#' character (lines which are skipped), the file should only have lines like
     * "configItem=value" (omit the quotes!!), i.e. "port=65535". Lines that assign to nonexistent
     * config items don't actually change anything as of v1.0.
     * TODO update to use JSON
     * @param cfgPath The path where the server's config file is located (including its name).
     * @return A HashMap containing the setting names and new-values of the settings to override.
     */
    private static HashMap<String, String> getSettingsFromConfigFile(String cfgPath) {
        if (cfgPath == null) {
            // TODO log this!
            return new HashMap<>();
        }
        File serverConfig = new File(cfgPath);
        if (!serverConfig.exists()) {
            // TODO log this? also log createConfigFile
            try {
                createConfigFile(cfgPath);
            }
            catch (IOException x) { /* whatever! I tried... */ }
            return new HashMap<>();
        }
        else {
            // Let's try to get overrides from the file we have...
            HashMap<String, String> fileSettings = new HashMap<>();
            String configContents = readConfigFile(cfgPath);
            String[] lines = configContents.split("\n");
            for (String line : lines) {
                // Skip blank lines and lines starting with a hash, '#'
                if (!Pattern.matches("^(|#.*)$", line)) {
                    // ... but try to match others against a typical "assignment" line
                    Pattern assignmentPattern = Pattern.compile("^([a-zA-Z]+?)=(.*)$");
                    Matcher assignmentMatcher = assignmentPattern.matcher(line);
                    if (assignmentMatcher.matches()) {
                        fileSettings.put(
                                assignmentMatcher.group(1), // name
                                assignmentMatcher.group(2)  // value
                        );
                    }
                    else {
                        // TODO log this! ("Bad assignment in config file '" + line + "'");
                    }
                }
            }
            return fileSettings;
        }
    }

    /**
     * Initializes a brand new config file for the server.
     * @param cfgPath The path where the config file should be created (including its name).
     * @throws IllegalArgumentException If cfgPath is null
     * @throws IOException If an IOException occurs during the creation of the file.
     */
    private static void createConfigFile(String cfgPath) throws IOException {
        if (cfgPath == null)
            throw new IllegalArgumentException("Null cfgPath given");
        PrintWriter cfgWriter = new PrintWriter(cfgPath, "UTF-8");
        String header = String.join(
                "\n",
                "# Here you can override the default server configuration.",
                "# Lines starting with a # character, as well as blank lines,",
                "# will be ignored.",
                ""
        );
        cfgWriter.write(header);
        cfgWriter.close();
    }

    /**
     * Returns the unmodified contents of the server's config file.
     * TODO perhaps stick file reading in a utility class?
     * @param cfgPath The path where the server's config file is located (including its name).
     * @return A String containing the unmodified contents of the file, or "" if an error occurred.
     * @throws IllegalArgumentException If cfgPath is null
     */
    private static String readConfigFile(String cfgPath) {
        if (cfgPath == null)
            throw new IllegalArgumentException("Null cfgPath given");
        try {
            FileReader cfgReader = new FileReader(cfgPath);
            StringBuilder sb = new StringBuilder();
            int c = cfgReader.read();
            while (c != -1) {
                sb.append((char) c);
                c = cfgReader.read();
            }
            cfgReader.close();
            return sb.toString();
        } catch (Exception x) {
            // TODO log this! ("Error reading config file: " + x.getMessage());
            return "";
        }
    }

    /**
     * The private constructor for Config. There should never be any instances of the class.
     */
    private Config() {}

    /**
     * Represents a config item and the value(s) to which it is set. The item itself has multiple
     * values by containing both a default value and an override value. The default cannot be
     * reset, but the override value may start as a value from the config file and change over the
     * course of the program's runtime. If the override is ever null, the default is used as a
     * backup.
     *
     * A Setting contains a function that allows it to check whether some value would be a valid
     * value for the config item. For example, a port must be an Integer between 0 and 65535
     * inclusive, and the port item has a function that checks that an int is in this range.
     *
     * Settings are thread-safe as get() and set() are synchronized. They need not be synchronized
     * at the outer Config level, since, for example, you don't need to lock the entire Config
     * just to get the port number.
     * @param <T> The type of value this setting represents.
     */
    private static class Setting<T> {
        private final String name;
        private final Function<T, Boolean> isValid;
        private final T defaultVal;
        private T overrideVal;

        /**
         * Initializes a new Setting. This should NOT be used by the outer Config code.
         * @param name The name of the corresponding config item
         * @param isValid A function that tells whether a given value is valid for this config item
         * @param defaultVal The default value for this setting
         * @param overrideVal The override value for this setting
         */
        private Setting(String name, Function<T, Boolean> isValid, T defaultVal, T overrideVal) {
            this.name = name;
            this.isValid = isValid;
            this.defaultVal = defaultVal;

            if (overrideVal == null) {
                // TODO log this! no setting for given name, using default %s
                this.overrideVal = null;
            }
            else if (!isValid.apply(overrideVal)) {
                // TODO log this! invalid config file setting %s or whatever
                this.overrideVal = null;
            }
            else {
                this.overrideVal = overrideVal;
            }
        }

        /**
         * Creates and returns a new Setting. Should be used by the outer Config code.
         * @param name The name of the corresponding config item
         * @param isValid A function that tells whether a given value is valid for this config item
         * @param defaultVal The default value for this setting
         * @param overrideVal The override value for this setting
         * @param <T> The type of value this setting represents.
         * @return The new Setting using the provided params.
         * @throws FailedInitException If name or isValid are null, or if the default value is
         * invalid.
         */
        private static <T> Setting<T> makeSetting(
                String name,
                Function<T, Boolean> isValid,
                T defaultVal,
                T overrideVal
        ) throws FailedInitException {
            // Make sure name and isValid are non-null
            String[] importantArgNames = {"name", "isValid"};
            Object[] importantArgValues = {name, isValid};
            String nullArgName = GeneralUtils.firstNullArg(importantArgNames, importantArgValues);
            if (nullArgName != null)
                throw new FailedInitException("Null parameter '" + nullArgName + "'");

            // Make sure default value is valid
            if (!isValid.apply(defaultVal)) {
                String err = String.format(
                        "Config item '%s' given invalid default value '%s'",
                        name,
                        defaultVal
                );
                throw new FailedInitException(err);
            }
            return new Setting<>(name, isValid, defaultVal, overrideVal);
        }

        /**
         * Retrieves the value to which the corresponding config item is set. Thread-safe.
         * @return The value to which the corresponding config item is set.
         */
        private synchronized T getValue() {
            return (overrideVal != null) ? overrideVal : defaultVal;
        }

        /**
         * Tries to set the corresponding config item to a new value. Returns true if successful.
         * Thread-safe.
         * @param overrideVal The value to which the config item should be set.
         * @return True if the operation succeeded, and false otherwise.
         */
        private synchronized boolean setValue(T overrideVal) {
            // TODO log when null is successfully set
            if (isValid.apply(overrideVal)) {
                this.overrideVal = overrideVal;
                return true;
            }
            else
                return false;
        }

        /**
         * Represents an exception that happened when a Setting failed to initialize properly.
         */
        private static class FailedInitException extends Exception {
            /**
             * Creates a new Exception with the given message.
             * @param msg The message to include in the exception
             */
            private FailedInitException(String msg) { super(msg); }
        }
    }
}
