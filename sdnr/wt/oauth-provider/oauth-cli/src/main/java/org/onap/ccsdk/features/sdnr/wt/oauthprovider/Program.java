package org.onap.ccsdk.features.sdnr.wt.oauthprovider;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.InvalidConfigurationException;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.TokenCreator;

public class Program {

    private static Log LOG = null;

    private static void initLog(boolean silent, String logfile, Level loglvl) {
        org.apache.log4j.Logger.getRootLogger().getLoggerRepository().resetConfiguration();
        LOG = LogFactory.getLog(Program.class);
        if (!silent) {
            ConsoleAppender console = new ConsoleAppender(); // create appender
            // configure the appender
            String PATTERN = "%d [%p|%C{1}] %m%n";
            console.setLayout(new PatternLayout(PATTERN));
            console.setThreshold(loglvl);
            console.activateOptions();
            // add appender to any Logger (here is root)
            org.apache.log4j.Logger.getRootLogger().addAppender(console);
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
            org.apache.log4j.Logger.getRootLogger().addAppender(fa);
        }
        // repeat with all other desired appenders
    }

    public static void main(String[] args) {
        initLog(true, null, Level.INFO);
        CliArgs parsedArgs = null;
        try {
            parsedArgs = parseArgs(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (parsedArgs.help) {
            printHelp();
            return;
        }
        long tokenLifetime = parsedArgs.tokenLifetime;
        Instant now = Instant.now();
        UserTokenPayload userData = new UserTokenPayload();
        userData.setRoles(List.of(parsedArgs.role.split(",")));
        userData.setPreferredUsername(parsedArgs.username);
        userData.setIat(now.getEpochSecond() * 1000);
        userData.setExp(now.plusSeconds(tokenLifetime).getEpochSecond() * 1000);
        try {
            final var tokenCreator = TokenCreator.getInstance(parsedArgs.alg, parsedArgs.secret, parsedArgs.publicKey,
                    parsedArgs.issuer, parsedArgs.tokenLifetime);
            final var token = tokenCreator.createNewJWT(userData);
            System.out.println(token.getToken());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void printHelp() {
        System.out.println("SDNR JWT TokenCreator Tool");
        System.out.println("==========================");
        System.out.println("usage: java -jar sdnr-wt-oauth-cli-$VERSION.jar [OPTIONS]");
        System.out.println("");
        System.out.println("OPITONS:");
        System.out.println("  -u [--username]   token username      (mandatory)");
        System.out.println("  -r [--roles]      token roles         (mandatory)");
        System.out.println("  -a [--algorithm]  token creation algorithm  ");
        System.out.println("  -s [--secret]     token secret or private key");
        System.out.println("  -p [--public-key] token public key");
        System.out.println("  -i [--issuer]     token issuer");
        System.out.println("  -l [--lifetime]   token lifetime in seconds (format 2y and 5d is also allowed)");
        System.out.println("  -h [--help]       print help");
        System.out.println("");
        System.out.println(
                "HINT: if non mandatory options are not given they tried to be loaded from default SDNR oauth config file");
    }

    private static CliArgs parseArgs(String[] args) throws IOException, InvalidConfigurationException {

        CliArgs result = new CliArgs();
        result.help = false;
        int i = 0;
        while (i < args.length) {
            var arg = args[i];

            if (arg.equals("-r") || arg.equals("--roles")) {
                i++;
                result.role = i < args.length ? args[i] : null;
            } else if (arg.equals("-u") || arg.equals("--username")) {
                i++;
                result.username = i < args.length ? args[i] : null;
            } else if (arg.equals("-a") || arg.equals("--algorithm")) {
                i++;
                result.alg = i < args.length ? args[i] : null;
            } else if (arg.equals("-s") || arg.equals("--secret")) {
                i++;
                result.secret = i < args.length ? args[i] : null;
            } else if (arg.equals("-p") || arg.equals("--public-key")) {
                i++;
                result.publicKey = i < args.length ? args[i] : null;
            } else if (arg.equals("-i") || arg.equals("--issuer")) {
                i++;
                result.issuer = i < args.length ? args[i] : null;
            } else if (arg.equals("-l") || arg.equals("--lifetime")) {
                i++;
                result.tokenLifetime = i < args.length ? parseTimepan(args[i]) : null;
            } else if (arg.equals("-h") || arg.equals("--help")) {
                result.help = true;
                return result;
            }
            i++;

        }

        if (result.secret == null || result.alg == null || result.issuer == null || result.tokenLifetime == null) {
            Config localConfig = Config.getInstance();

            if (result.alg == null) {
                result.alg = localConfig.getAlgorithm();
            }
            if (result.issuer == null) {
                result.issuer = localConfig.getTokenIssuer();
            }
            if (result.secret == null) {
                result.secret = localConfig.getTokenSecret();
            }
            if (result.publicKey == null) {
                result.publicKey = localConfig.getPublicKey();
            }
            if (result.tokenLifetime == null) {
                result.tokenLifetime = localConfig.getTokenLifetime();
            }
        }
        if (result.role == null || result.username == null) {
            throw new InvalidConfigurationException("role and username arguments are mandatory");
        }

        return result;
    }

    private static Long parseTimepan(String arg) {
        if (arg == null || arg.isBlank()) {
            return null;
        }
        if (arg.matches("\\d+y")) {
            return Long.parseLong(arg.substring(0, arg.length() - 1)) * 60 * 60 * 24 * 365;
        }
        if (arg.matches("\\d+d")) {
            return Long.parseLong(arg.substring(0, arg.length() - 1)) * 60 * 60 * 24;
        }
        return Long.parseLong(arg);
    }

    private static class CliArgs {

        private Long tokenLifetime;
        private String alg;
        private String issuer;
        private String secret;
        private String publicKey;
        private String role;
        private String username;

        private boolean help;

    }
}
