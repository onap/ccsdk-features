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

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataMigrationReport;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.MavenDatabasePluginInitFile;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;

/**
 * @author Michael Dürre
 *
 */
public class Program {

	private static final String CMD_INITDB = "init";
	private static final String CMD_CLEAR_DB = "delete";
	private static final String CMD_CREATE_PLUGIN_INIT_FILE = "pluginfile";
	private static final String CMD_IMPORT = "import";
	private static final String CMD_EXPORT = "export";
	private static final String CMD_LIST_VERSION = "list";
	private static final String CMD_INITDB_DESCRIPTION = "initialize databse indices and aliases";
	private static final String CMD_CLEAR_DB_DESCRIPTION = "clear database indices and aliases";
	private static final String CMD_CREATE_PLUGIN_INIT_FILE_DESCRIPTION = "create maven plugin file";
	private static final String CMD_IMPORT_DESCRIPTION = "import data into database";
	private static final String CMD_EXPORT_DESCRIPTION = "export data from database";
	private static final String CMD_LIST_VERSION_DESCRIPTION = "list release versions";

	private static final List<String[]> commands = Arrays.asList(new String[] { CMD_INITDB, CMD_INITDB_DESCRIPTION },
			new String[] { CMD_CLEAR_DB, CMD_CLEAR_DB_DESCRIPTION },
			new String[] { CMD_CREATE_PLUGIN_INIT_FILE, CMD_CREATE_PLUGIN_INIT_FILE_DESCRIPTION },
			new String[] { CMD_IMPORT, CMD_IMPORT_DESCRIPTION }, new String[] { CMD_EXPORT, CMD_EXPORT_DESCRIPTION },
			new String[] { CMD_LIST_VERSION, CMD_LIST_VERSION_DESCRIPTION });
	private static final String APPLICATION_NAME = "SDNR DataMigrationTool";
	private static final int DEFAULT_SHARDS = 5;
	private static final int DEFAULT_REPLICAS = 1;
	private static final String DEFAULT_DBURL = "http://sdnrdb:9200";
	private static final String DEFAULT_DBPREFIX = "";
	private static final String OPTION_FORCE_RECREATE_SHORT = "f";
	private static final String OPTION_SILENT_SHORT = "n";
	private static final String OPTION_SILENT = "silent";
	private static final String OPTION_VERSION_SHORT = "v";
	private static final String OPTION_SHARDS_SHORT = "s";
	private static final String OPTION_REPLICAS_SHORT = "r";
	private static final String OPTION_OUTPUTFILE_SHORT = "of";
	private static final String OPTION_INPUTFILE_SHORT = "if";
	private static final String OPTION_DEBUG_SHORT = "x";
	private static final String OPTION_TRUSTINSECURESSL_SHORT = "k";

	private static Options options = init();

	private static Log LOG = null;

	@SuppressWarnings("unchecked")
	private static <T> T getOptionOrDefault(CommandLine cmd, String option, T def) throws ParseException {
		if (def instanceof Boolean) {
			return cmd.hasOption(option) ? (T) Boolean.TRUE : def;
		}
		if (cmd.hasOption(option) && cmd.getOptionValue(option) != null) {
			if (option.equals(OPTION_VERSION_SHORT)) {
				String v = cmd.getOptionValue(option);
				return (T) Release.getValueBySuffix(v.startsWith("-") ? v : ("-" + v));
			} else {
				return (T) cmd.getParsedOptionValue(option);
			}
		}
		return def;
	}

	private static void initLog(boolean silent, String logfile) {
		initLog(silent, logfile, Level.INFO);
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

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			printHelp(formatter);
			System.exit(1);
		}
		if (cmd == null) {
			printHelp(formatter);
			System.exit(1);
		}
		try {
			initLog(getOptionOrDefault(cmd, OPTION_SILENT_SHORT, false), null,
					getOptionOrDefault(cmd, OPTION_DEBUG_SHORT, false) ? Level.DEBUG : Level.INFO);
		} catch (ParseException e2) {

		}
		switch (cmd.getOptionValue("c")) {
		case CMD_INITDB:
			try {
				cmd_init_db(cmd);
			} catch (Exception e1) {
				exit(e1);
			}
			break;
		case CMD_CLEAR_DB:
			try {
				cmd_clear_db(cmd);
			} catch (Exception e1) {
				exit(e1);
			}
			break;
		case CMD_CREATE_PLUGIN_INIT_FILE:
			try {
				String of = getOptionOrDefault(cmd, "of", null);
				if (of == null) {
					throw new Exception("please add the parameter output-file");
				}
				MavenDatabasePluginInitFile.create(Release.CURRENT_RELEASE, of);
			} catch (Exception e) {
				exit(e);
			}
			break;
		case CMD_IMPORT:
			try {
				cmd_dbimport(cmd);
			} catch (Exception e1) {
				exit(e1);
			}
			break;
		case CMD_EXPORT:
			try {
				cmd_dbexport(cmd);
			} catch (Exception e) {
				exit(e);
			}
			break;
		case CMD_LIST_VERSION:
			cmd_listversion();
			break;
		default:
			printHelp(formatter);
			break;
		}
		System.exit(0);
	}

	/**
	 * @param formatter
	 */
	private static void printHelp(HelpFormatter formatter) {
		formatter.printHelp(APPLICATION_NAME, options);
		System.out.println("\nCommands:");
		for (String[] c : commands) {
			System.out.println(String.format("%10s\t%s", c[0], c[1]));
		}
	}

	/**
	 * 
	 */
	private static void cmd_listversion() {

		System.out.println("Database Releases:");
		final String format = "%15s\t%8s";
		System.out.println(String.format(format, "Name", "Version"));
		for (Release r : Release.values()) {

			System.out.println(String.format(format, r.getValue(),
					r.getDBSuffix() != null && r.getDBSuffix().length() > 1 ? r.getDBSuffix().substring(1) : ""));
		}

	}

	/**
	 * @throws Exception 
	 * 
	 */
	private static void cmd_dbimport(CommandLine cmd) throws Exception {
		String dbUrl = getOptionOrDefault(cmd, "db", DEFAULT_DBURL);
		String username = getOptionOrDefault(cmd, "dbu", null);
		String password = getOptionOrDefault(cmd, "dbp", null);
		String filename = getOptionOrDefault(cmd, OPTION_OUTPUTFILE_SHORT, null);
		boolean trustAll = getOptionOrDefault(cmd, OPTION_TRUSTINSECURESSL_SHORT, false);
		if (filename == null) {
			throw new Exception("please add output file parameter");
		}
		DataMigrationProviderImpl service = new DataMigrationProviderImpl(new HostInfo[] { HostInfo.parse(dbUrl) },
				username, password, trustAll);
		DataMigrationReport report = service.importData(filename, false);
		LOG.info(report);
		if(!report.completed()) {
			throw new Exception("db import seems to be not executed completed");
		}
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private static void cmd_dbexport(CommandLine cmd) throws Exception {
		String dbUrl = getOptionOrDefault(cmd, "db", DEFAULT_DBURL);
		String username = getOptionOrDefault(cmd, "dbu", null);
		String password = getOptionOrDefault(cmd, "dbp", null);
		String filename = getOptionOrDefault(cmd, OPTION_OUTPUTFILE_SHORT, null);
		boolean trustAll = getOptionOrDefault(cmd, OPTION_TRUSTINSECURESSL_SHORT, false);
		if (filename == null) {
			throw new Exception("please add output file parameter");
		}
		DataMigrationProviderImpl service = new DataMigrationProviderImpl(new HostInfo[] { HostInfo.parse(dbUrl) },
				username, password, trustAll);
		DataMigrationReport report = service.exportData(filename);
		LOG.info(report);
		if(!report.completed()) {
			throw new Exception("db export seems to be not executed completed");
		}

	}

	/**
	 * @param e
	 */
	private static void exit(Exception e) {
		if (LOG != null) {
			LOG.error("Error during execution: {}", e);
		} else {
			System.err.println(e);
		}
		System.exit(1);
	}

	/**
	 * @param cmd
	 * @throws java.text.ParseException 
	 * @throws Exception 
	 */
	private static void cmd_clear_db(CommandLine cmd) throws Exception {
		Release r = getOptionOrDefault(cmd, OPTION_VERSION_SHORT, Release.CURRENT_RELEASE);
		String dbUrl = getOptionOrDefault(cmd, "db", DEFAULT_DBURL);
		String dbPrefix = getOptionOrDefault(cmd, "p", DEFAULT_DBPREFIX);
		String username = getOptionOrDefault(cmd, "dbu", null);
		String password = getOptionOrDefault(cmd, "dbp", null);
		boolean trustAll = getOptionOrDefault(cmd, OPTION_TRUSTINSECURESSL_SHORT, false);
		DataMigrationProviderImpl service = new DataMigrationProviderImpl(new HostInfo[] { HostInfo.parse(dbUrl) },
				username, password, trustAll);
		if (!service.clearDatabase(r, dbPrefix)) {
			throw new Exception("failed to init database");
		}
	}

	/**
	 * @param cmd
	 * @throws java.text.ParseException 
	 * @throws Exception 
	 */
	private static void cmd_init_db(CommandLine cmd) throws Exception {
		Release r = getOptionOrDefault(cmd, OPTION_VERSION_SHORT, Release.CURRENT_RELEASE);
		int numShards = getOptionOrDefault(cmd, OPTION_SHARDS_SHORT, DEFAULT_SHARDS);
		int numReplicas = getOptionOrDefault(cmd, OPTION_REPLICAS_SHORT, DEFAULT_REPLICAS);
		String dbUrl = getOptionOrDefault(cmd, "db", DEFAULT_DBURL);
		String dbPrefix = getOptionOrDefault(cmd, "p", DEFAULT_DBPREFIX);
		String username = getOptionOrDefault(cmd, "dbu", null);
		String password = getOptionOrDefault(cmd, "dbp", null);
		boolean trustAll = getOptionOrDefault(cmd, OPTION_TRUSTINSECURESSL_SHORT, false);
		DataMigrationProviderImpl service = new DataMigrationProviderImpl(new HostInfo[] { HostInfo.parse(dbUrl) },
				username, password, trustAll);
		boolean forceRecreate = cmd.hasOption(OPTION_FORCE_RECREATE_SHORT);
		if (!service.initDatabase(r, numShards, numReplicas, dbPrefix, forceRecreate)) {
			throw new Exception("failed to init database");
		}

	}

	/**
	 * @return
	 */
	private static Options init() {
		Options options = new Options();
		options.addOption(createOption("c", "cmd", true, "command to execute", true));
		options.addOption(createOption("db", "dburl", true, "database url", false));
		options.addOption(createOption("dbu", "db-username", true, "database basic auth username", false));
		options.addOption(createOption("dbp", "db-password", true, "database basic auth password", false));
		options.addOption(createOption(OPTION_REPLICAS_SHORT, "replicas", true, "amount of replicas", false));
		options.addOption(createOption(OPTION_SHARDS_SHORT, "shards", true, "amount of shards", false));
		options.addOption(createOption("p", "prefix", true, "prefix for db indices", false));
		options.addOption(createOption(OPTION_VERSION_SHORT, "version", true, "version", false));
		options.addOption(createOption(OPTION_DEBUG_SHORT, "verbose", false, "verbose mode", false));
		options.addOption(createOption(OPTION_TRUSTINSECURESSL_SHORT, "trust-insecure", false,
				"trust insecure ssl certs", false));
		options.addOption(createOption("w", "wait", true, "wait delay for yellow status", false));
		options.addOption(
				createOption(OPTION_FORCE_RECREATE_SHORT, "force-recreate", false, "delete if sth exists", false));
		options.addOption(createOption(OPTION_SILENT_SHORT, OPTION_SILENT, false, "prevent console output", false));
		options.addOption(createOption(OPTION_OUTPUTFILE_SHORT, "output-file", true, "file to write into", false));
		options.addOption(createOption(OPTION_INPUTFILE_SHORT, "input-file", true, "file to read from", false));

		return options;
	}

	/**
	 * @param opt 
	 * @param longOpt 
	 * @param hasArg 
	 * @param description 
	 * @param required 
	 * @return
	 */
	private static Option createOption(String opt, String longOpt, boolean hasArg, String description,
			boolean required) {
		Option o = new Option(opt, longOpt, hasArg, description);
		o.setRequired(required);
		return o;
	}
}
