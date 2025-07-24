/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.SdnrDbType;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataMigrationReport;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.MavenDatabasePluginInitFile;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;

/**
 * @author Michael Dürre
 *
 */
public class Program {

    // constants
    private static final String CMD_INITDB = "init";
    private static final String CMD_CLEAR_DB = "delete";
    private static final String CMD_CLEAR_DB_COMPLETE = "clear";
    private static final String CMD_CREATE_PLUGIN_INIT_FILE = "pluginfile";
    private static final String CMD_IMPORT = "import";
    private static final String CMD_EXPORT = "export";
    private static final String CMD_LIST_VERSION = "list";

    private static final String CMD_INITDB_DESCRIPTION = "initialize databse indices and aliases";
    private static final String CMD_CLEAR_DB_DESCRIPTION = "delete database indices and aliases for current release";
    private static final String CMD_CLEAR_DB_COMPLETE_DESCRIPTION = "delete all database indices and aliases";

    private static final String CMD_CREATE_PLUGIN_INIT_FILE_DESCRIPTION = "create maven plugin file";
    private static final String CMD_IMPORT_DESCRIPTION = "import data into database";
    private static final String CMD_EXPORT_DESCRIPTION = "export data from database";
    private static final String CMD_LIST_VERSION_DESCRIPTION = "list release versions";

    private static final List<String[]> commands = Arrays.asList(new String[] {CMD_INITDB, CMD_INITDB_DESCRIPTION},
            new String[] {CMD_CLEAR_DB, CMD_CLEAR_DB_DESCRIPTION},
            new String[] {CMD_CLEAR_DB_COMPLETE, CMD_CLEAR_DB_COMPLETE_DESCRIPTION},
            new String[] {CMD_CREATE_PLUGIN_INIT_FILE, CMD_CREATE_PLUGIN_INIT_FILE_DESCRIPTION},
            new String[] {CMD_IMPORT, CMD_IMPORT_DESCRIPTION}, new String[] {CMD_EXPORT, CMD_EXPORT_DESCRIPTION},
            new String[] {CMD_LIST_VERSION, CMD_LIST_VERSION_DESCRIPTION});
    private static final String APPLICATION_NAME = "SDNR DataMigrationTool";

    private static final int DEFAULT_SHARDS = 5;
    private static final int DEFAULT_REPLICAS = 1;
    private static final int DEFAULT_DATABASEWAIT_SECONDS = 30;
    private static final String DEFAULT_DBURL_ELASTICSEARCH = "http://sdnrdb:9200";
    private static final String DEFAULT_DBURL_MARIADB = "jdbc:mysql://sdnrdb:3306/sdnrdb";
    private static final String DEFAULT_DBPREFIX = "";
    private static final boolean DEFAULT_TRUSTINSECURESSL = false;

    private static final String OPTION_FORCE_RECREATE_SHORT = "f";
    private static final String OPTION_FORCE_RECREATE_LONG = "force-recreate";
    private static final String OPTION_SILENT_SHORT = "n";
    private static final String OPTION_SILENT = "silent";
    private static final String OPTION_VERSION_SHORT = "v";
    private static final String OPTION_VERSION_LONG = "version";
    private static final String OPTION_SHARDS_SHORT = "s";
    private static final String OPTION_SHARDS_LONG = "shards";
    private static final String OPTION_REPLICAS_SHORT = "r";
    private static final String OPTION_REPLICAS_LONG = "replicas";
    private static final String OPTION_OUTPUTFILE_SHORT = "of";
    private static final String OPTION_OUTPUTFILE_LONG = "output-file";
    private static final String OPTION_INPUTFILE_SHORT = "if";
    private static final String OPTION_INPUTFILE_LONG = "input-file";
    private static final String OPTION_DEBUG_SHORT = "x";
    private static final String OPTION_DEBUG_LONG = "verbose";
    private static final String OPTION_TRUSTINSECURESSL_SHORT = "k";
    private static final String OPTION_TRUSTINSECURESSL_LONG = "trust-insecure";
    private static final String OPTION_DATABASE_SHORT = "db";
    private static final String OPTION_DATABASE_LONG = "dburl";
    private static final String OPTION_COMMAND_SHORT = "c";
    private static final String OPTION_COMMAND_LONG = "cmd";
    private static final String OPTION_DATABASETYPE_SHORT = "dbt";
    private static final String OPTION_DATABASETYPE_LONG = "db-type";
    private static final String OPTION_DATABASEUSER_SHORT = "dbu";
    private static final String OPTION_DATABASEUSER_LONG = "db-username";
    private static final String OPTION_DATABASEPASSWORD_SHORT = "dbp";
    private static final String OPTION_DATABASEPASSWORD_LONG = "db-password";
    private static final String OPTION_DATABASEPREFIX_SHORT = "p";
    private static final String OPTION_DATABASEPREFIX_LONG = "prefix";
    private static final String OPTION_DATABASEWAIT_SHORT = "w";
    private static final String OPTION_DATABASEWAIT_LONG = "wait";
    private static final String OPTION_HELP_SHORT = "h";
    private static final String OPTION_HELP_LONG = "help";
    // end of constants

    // variables
    private static Options options = init();
    private static Log LOG = null;
    // end of variables

    // public methods
    public static void main(String[] args) {
        System.exit(main2(args));
    }
    // end of public methods

    // private methods
    @SuppressWarnings("unchecked")
    private static <T> T getOptionOrDefault(CommandLine cmd, String option, T def) throws ParseException {
        if (def instanceof Boolean) {
            return cmd.hasOption(option) ? (T) Boolean.TRUE : def;
        }
        if (def instanceof Integer) {
            return cmd.hasOption(option) ? (T) Integer.valueOf(cmd.getOptionValue(option)) : def;
        }
        if (def instanceof Long) {
            return cmd.hasOption(option) ? (T) Long.valueOf(cmd.getOptionValue(option)) : def;
        }
        if (def instanceof Release) {
            return cmd.hasOption(option) ? (T) Release.getValueBySuffix(cmd.getOptionValue(option)) : def;
        }
        if (def instanceof SdnrDbType) {
            return cmd.hasOption(option) ? (T) SdnrDbType.valueOf(cmd.getOptionValue(option).toUpperCase()) : def;
        }
        if (cmd.hasOption(option) && cmd.getOptionValue(option) != null) {
            if (option.equals(OPTION_VERSION_SHORT)) {
                String v = cmd.getOptionValue(option);
                return (T) Release.getValueBySuffix(v.startsWith("-") ? v : "-" + v);
            } else {
                return (T) cmd.getParsedOptionValue(option);
            }
        }
        return def;
    }

    private static void initLog(boolean silent, String logfile, Level loglvl) {
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();
        LOG = LogFactory.getLog(Program.class);
        if (!silent) {
            ConsoleAppender console = new ConsoleAppender(); // create appender
            // configure the appender
            String PATTERN = "%d [%p|%C{1}] %m%n";
            console.setLayout(new PatternLayout(PATTERN));
            console.setThreshold(loglvl);
            console.activateOptions();
            // add appender to any Logger (here is root)
            Logger.getRootLogger().addAppender(console);
        }
        if (logfile != null) {
            RollingFileAppender fa = new RollingFileAppender();
            fa.setName("FileLogger");
            fa.setFile(logfile);
            fa.setLayout(new PatternLayout("%d %-5p [%c] %m%n"));
            fa.setThreshold(loglvl);
            fa.setMaximumFileSize(10000000);
            fa.setAppend(true);
            fa.setMaxBackupIndex(5);
            fa.activateOptions();
            // add appender to any Logger (here is root)
            Logger.getRootLogger().addAppender(fa);
        }
        // repeat with all other desired appenders
    }

    private static int main2(String[] args) {

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelp(formatter);
            return 1;
        }
        if (cmd == null) {
            printHelp(formatter);
            return 1;
        }
        try {
            initLog(getOptionOrDefault(cmd, OPTION_SILENT_SHORT, false), null,
                    getOptionOrDefault(cmd, OPTION_DEBUG_SHORT, false) ? Level.DEBUG : Level.INFO);
        } catch (ParseException e2) {

        }
        try {
            if (getOptionOrDefault(cmd, OPTION_HELP_SHORT, false)) {
                printHelp(formatter);
                return 0;
            }
        } catch (ParseException e2) {
            return exit(e2);
        }
        final String command = cmd.getOptionValue(OPTION_COMMAND_SHORT);
        if (command == null) {
            printHelp(formatter);
            return 1;
        }
        switch (command) {
            case CMD_INITDB:
                try {
                    cmd_init_db(cmd);
                } catch (Exception e1) {
                    return exit(e1);
                }
                break;
            case CMD_CLEAR_DB:
                try {
                    cmd_clear_db(cmd);
                } catch (Exception e1) {
                    return exit(e1);
                }
                break;
            case CMD_CLEAR_DB_COMPLETE:
                try {
                    cmd_clear_db_complete(cmd);
                } catch (Exception e1) {
                    return exit(e1);
                }
                break;
            case CMD_CREATE_PLUGIN_INIT_FILE:
                try {
                    String of = getOptionOrDefault(cmd, OPTION_OUTPUTFILE_SHORT, null);
                    if (of == null) {
                        throw new Exception("please add the parameter output-file");
                    }
                    MavenDatabasePluginInitFile
                            .create(getOptionOrDefault(cmd, OPTION_VERSION_SHORT, Release.CURRENT_RELEASE), of);
                } catch (Exception e) {
                    return exit(e);
                }
                break;
            case CMD_IMPORT:
                try {
                    cmd_dbimport(cmd);
                } catch (Exception e1) {
                    return exit(e1);
                }
                break;
            case CMD_EXPORT:
                try {
                    cmd_dbexport(cmd);
                } catch (Exception e) {
                    return exit(e);
                }
                break;
            case CMD_LIST_VERSION:
                cmd_listversion();
                break;

            default:
                printHelp(formatter);
                return 1;
        }
        return 0;
    }

    private static void printHelp(HelpFormatter formatter) {
        formatter.printHelp(APPLICATION_NAME, options);
        System.out.println("\nCommands:");
        for (String[] c : commands) {
            System.out.println(String.format("%10s\t%s", c[0], c[1]));
        }
    }

    private static void cmd_listversion() {

        System.out.println("Database Releases:");
        final String format = "%15s\t%8s";
        System.out.println(String.format(format, "Name", "Version"));
        for (Release r : Release.values()) {

            System.out.println(String.format(format, r.getValue(),
                    r.getDbSuffix() != null && r.getDbSuffix().length() > 1 ? r.getDbSuffix().substring(1) : ""));
        }

    }

    private static void cmd_dbimport(CommandLine cmd) throws Exception {
        DatabaseOptions options = new DatabaseOptions(cmd);
        String filename = getOptionOrDefault(cmd, OPTION_OUTPUTFILE_SHORT, null);
        if (filename == null) {
            throw new Exception("please add output file parameter");
        }
        DataMigrationProviderImpl service = new DataMigrationProviderImpl(options.getType(), options.getUrl(),
                options.getUsername(), options.getPassword(), options.doTrustAll(), options.getTimeoutMs());
        DataMigrationReport report = service.importData(filename, false);
        LOG.info(report);
        if (!report.completed()) {
            throw new Exception("db import seems to be not executed completed");
        }
        LOG.info("database import completed successfully");
    }

    private static void cmd_dbexport(CommandLine cmd) throws Exception {
        DatabaseOptions options = new DatabaseOptions(cmd);
        String filename = getOptionOrDefault(cmd, OPTION_OUTPUTFILE_SHORT, null);
        if (filename == null) {
            throw new Exception("please add output file parameter");
        }
        DataMigrationProviderImpl service = new DataMigrationProviderImpl(options.getType(), options.getUrl(),
                options.getUsername(), options.getPassword(), options.doTrustAll(), options.getTimeoutMs());
        DataMigrationReport report = service.exportData(filename);
        LOG.info(report);
        if (!report.completed()) {
            throw new Exception("db export seems to be not executed completed");
        }
        LOG.info("database export completed successfully");
    }

    private static int exit(Exception e) {
        if (LOG != null) {
            LOG.error("Error during execution: {}", e);
        } else {
            System.err.println(e);
        }
        return 1;
    }

    private static void cmd_clear_db(CommandLine cmd) throws Exception {
        Release r = getOptionOrDefault(cmd, OPTION_VERSION_SHORT, (Release) null);
        DatabaseOptions options = new DatabaseOptions(cmd);
        String dbPrefix = getOptionOrDefault(cmd, OPTION_DATABASEPREFIX_SHORT, DEFAULT_DBPREFIX);
        DataMigrationProviderImpl service = new DataMigrationProviderImpl(options.getType(), options.getUrl(),
                options.getUsername(), options.getPassword(), options.doTrustAll(), options.getTimeoutMs());
        if (!service.clearDatabase(r, dbPrefix, options.getTimeoutMs())) {
            throw new Exception("failed to init database");
        }
        LOG.info("database clear completed successfully");
    }

    private static void cmd_clear_db_complete(CommandLine cmd) throws Exception {
        DatabaseOptions options = new DatabaseOptions(cmd);
        DataMigrationProviderImpl service = new DataMigrationProviderImpl(options.getType(), options.getUrl(),
                options.getUsername(), options.getPassword(), options.doTrustAll(), options.getTimeoutMs());
        if (!service.clearCompleteDatabase(options.getTimeoutMs())) {
            throw new Exception("failed to init database");
        }
        LOG.info("database complete clear completed successfully");
    }

    private static void cmd_init_db(CommandLine cmd) throws Exception {
        Release r = getOptionOrDefault(cmd, OPTION_VERSION_SHORT, (Release) null);
        int numShards = getOptionOrDefault(cmd, OPTION_SHARDS_SHORT, DEFAULT_SHARDS);
        int numReplicas = getOptionOrDefault(cmd, OPTION_REPLICAS_SHORT, DEFAULT_REPLICAS);
        DatabaseOptions options = new DatabaseOptions(cmd);
        String dbPrefix = getOptionOrDefault(cmd, OPTION_DATABASEPREFIX_SHORT, DEFAULT_DBPREFIX);
        DataMigrationProviderImpl service = new DataMigrationProviderImpl(options.getType(),options.getUrl(),
                options.getUsername(), options.getPassword(), options.doTrustAll(), options.getTimeoutMs());
        boolean forceRecreate = cmd.hasOption(OPTION_FORCE_RECREATE_SHORT);
        if (!service.initDatabase(r, numShards, numReplicas, dbPrefix, forceRecreate, options.getTimeoutMs())) {
            throw new Exception("failed to init database");
        }
        LOG.info("database init completed successfully");

    }

    private static Options init() {
        Options result = new Options();
        result.addOption(createOption(OPTION_COMMAND_SHORT, OPTION_COMMAND_LONG, true, "command to execute", false));
        result.addOption(createOption(OPTION_DATABASE_SHORT, OPTION_DATABASE_LONG, true, "database url", false));
        result.addOption(createOption(OPTION_DATABASETYPE_SHORT, OPTION_DATABASETYPE_LONG, true,
                "database type (elasticsearch|mariadb)", false));
        result.addOption(createOption(OPTION_DATABASEUSER_SHORT, OPTION_DATABASEUSER_LONG, true,
                "database basic auth username", false));
        result.addOption(createOption(OPTION_DATABASEPASSWORD_SHORT, OPTION_DATABASEPASSWORD_LONG, true,
                "database basic auth password", false));
        result.addOption(createOption(OPTION_REPLICAS_SHORT, OPTION_REPLICAS_LONG, true, "amount of replicas", false));
        result.addOption(createOption(OPTION_SHARDS_SHORT, OPTION_SHARDS_LONG, true, "amount of shards", false));
        result.addOption(createOption(OPTION_DATABASEPREFIX_SHORT, OPTION_DATABASEPREFIX_LONG, true,
                "prefix for db indices", false));
        result.addOption(createOption(OPTION_VERSION_SHORT, OPTION_VERSION_LONG, true, "version", false));
        result.addOption(createOption(OPTION_DEBUG_SHORT, OPTION_DEBUG_LONG, false, "verbose mode", false));
        result.addOption(createOption(OPTION_TRUSTINSECURESSL_SHORT, OPTION_TRUSTINSECURESSL_LONG, false,
                "trust insecure ssl certs", false));
        result.addOption(createOption(OPTION_DATABASEWAIT_SHORT, OPTION_DATABASEWAIT_LONG, true,
                "wait for yellow status with timeout in seconds", false));
        result.addOption(createOption(OPTION_FORCE_RECREATE_SHORT, OPTION_FORCE_RECREATE_LONG, false,
                "delete if sth exists", false));
        result.addOption(createOption(OPTION_SILENT_SHORT, OPTION_SILENT, false, "prevent console output", false));
        result.addOption(
                createOption(OPTION_OUTPUTFILE_SHORT, OPTION_OUTPUTFILE_LONG, true, "file to write into", false));
        result.addOption(createOption(OPTION_INPUTFILE_SHORT, OPTION_INPUTFILE_LONG, true, "file to read from", false));
        result.addOption(createOption(OPTION_HELP_SHORT, OPTION_HELP_LONG, false, "show help", false));
        return result;
    }

    private static long getTimeoutOptionMillis(CommandLine cmd) throws ParseException {
        return getOptionOrDefault(cmd, OPTION_DATABASEWAIT_SHORT, DEFAULT_DATABASEWAIT_SECONDS) * 1000L;
    }

    /**
     * create option for argparse lib
     *
     * @param opt short option string
     * @param longOpt long option string
     * @param hasArg flag if has a parameter after option tag
     * @param description description for help output
     * @param required flag if is required for program
     * @return option object for argparse lib
     */
    private static Option createOption(String opt, String longOpt, boolean hasArg, String description,
            boolean required) {
        Option o = new Option(opt, longOpt, hasArg, description);
        o.setRequired(required);
        return o;
    }
    // end of private methods

    private static class DatabaseOptions{
        private final String url;
        private final String username;
        private final String password;
        private final boolean trustAll;
        private final long timeoutMs;
        private final SdnrDbType type;

        public String getUrl() {
            return this.url;
        }
        public SdnrDbType getType() {
            return this.type;
        }
        public String getUsername() {
            return this.username;
        }
        public String getPassword() {
            return this.password;
        }
        public boolean doTrustAll() {
            return this.trustAll;
        }
        public long getTimeoutMs() {
            return this.timeoutMs;
        }

        public DatabaseOptions(CommandLine cmd) throws ParseException {
            this.type = getOptionOrDefault(cmd, OPTION_DATABASETYPE_LONG, SdnrDbType.ELASTICSEARCH);
            this.url = getOptionOrDefault(cmd, OPTION_DATABASE_SHORT,
                    this.type == SdnrDbType.ELASTICSEARCH ? DEFAULT_DBURL_ELASTICSEARCH : DEFAULT_DBURL_MARIADB);
            this.username = getOptionOrDefault(cmd, OPTION_DATABASEUSER_SHORT, null);
            this.password = getOptionOrDefault(cmd, OPTION_DATABASEPASSWORD_SHORT, null);
            this.trustAll = getOptionOrDefault(cmd, OPTION_TRUSTINSECURESSL_SHORT, DEFAULT_TRUSTINSECURESSL);
            this.timeoutMs = getTimeoutOptionMillis(cmd);
        }
    }
}
