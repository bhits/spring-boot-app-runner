package gov.samhsa.bhits.runner;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public final class ArgsUtils {

    private static final Logger logger = LoggerFactory.getLogger(ArgsUtils.class);

    private ArgsUtils() {
    }

    public static final Map<String, String> filterPortArgs(Map<String, String> args) {
        Map<String, String> filteredArgs = args.entrySet().stream().filter(entry -> isNotPortArg(entry.getKey())).collect(toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        return filteredArgs;
    }

    private static final boolean isNotPortArg(String arg) {
        boolean isNotPort = !"--server.port".equals(arg) && !"-Dserver.port".equals(arg);
        if (!isNotPort) {
            logger.info("arg: " + arg + " is a port configuration, it will be removed from the arguments");
        }
        return isNotPort;
    }
}
