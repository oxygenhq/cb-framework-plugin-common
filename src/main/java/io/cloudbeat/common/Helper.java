package io.cloudbeat.common;

import io.cloudbeat.common.config.CbConfig;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;

public class Helper {
    public static final String[] STACKTRACE_IGNORE_LIST = {
            "io.cloudbeat",
            "org.openqa.selenium",
            "java."
    };

    public static StackTraceElement[] filterStackTrace(StackTraceElement[] stackTrace) {
        return Arrays.stream(stackTrace).filter(e -> {
            final String callStr = e.toString();
            return !Arrays.stream(STACKTRACE_IGNORE_LIST).anyMatch(f -> callStr.startsWith(f));
        }).toArray(StackTraceElement[]::new);
    }

    public static String extractPackageFromClassName(String className) {
        if (StringUtils.isEmpty(className)) {
            return null;
        }
        String[] classNameParts = className.split("\\.");
        if (classNameParts.length <= 1) {
            return StringUtils.EMPTY;
        }
        // remove the last part (e.g. actual class name without package prefix)
        String[] packagePartsOnly = Arrays.copyOf(classNameParts,classNameParts.length - 1);
        return String.join(".", packagePartsOnly);
    }

    public static StackTraceElement[] getStackTrace() {

        return Thread.currentThread().getStackTrace();
    }

    public static StackTraceElement[] getStackTraceStartingFromPackage(StackTraceElement[] stackTrace, String testPackageName) {
        if (StringUtils.isEmpty(testPackageName)) {
            return filterStackTrace(stackTrace);
        }
        int firstTestCallIndex = -1;
        for (int i=0; i < stackTrace.length; i++) {
            if (stackTrace[i].toString().startsWith(testPackageName)) {
                firstTestCallIndex = i;
                break;
            }
        }
        // if we found the index of the first test method call, then return the stacktrace from that call downwards
        if (firstTestCallIndex > -1) {
            return Arrays.copyOfRange(stackTrace, firstTestCallIndex, stackTrace.length - 1);
        }
        // we are not suppose to be here, as if testPackageName is correct,
        // then we will always find related call in the trace
        return filterStackTrace(stackTrace);
    }

    public static String[] stackTraceToStringArray(StackTraceElement[] stackTrace) {
        return Arrays.stream(stackTrace).map(call -> call.toString()).toArray(String[]::new);
    }

    public static DesiredCapabilities castMapToDesiredCapabilities(Map<String, String> capsMap) {
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        // filter out "technologyName" capability as it's internal in CB and not part of Web Driver standard
        capsMap.keySet().stream().filter((key) -> key != "technologyName").forEach((key) -> capabilities.setCapability(key, capsMap.get(key)));

        return capabilities;
    }

    public static DesiredCapabilities mergeUserAndCloudbeatCapabilities(DesiredCapabilities extraCapabilities) {
        CbConfig config = CbTestContext.getInstance().getConfig();
        final DesiredCapabilities capabilities = config != null && config.getCapabilities() != null ? castMapToDesiredCapabilities(config.getCapabilities()) : new DesiredCapabilities();
        if (extraCapabilities != null)
            return capabilities.merge(extraCapabilities);
        return capabilities;
    }

    public static void addSpecialCapabilities(DesiredCapabilities capabilities) {
        CbConfig config = CbTestContext.getInstance().getConfig();
        if (config != null && capabilities != null && config.getOptions() != null &&
                capabilities.getBrowserName() != null && capabilities.getBrowserName().equals("chrome")
        ) {
            if (
                    (config.getOptions().containsKey("collectBrowserLogs") && Boolean.parseBoolean(config.getOptions().get("collectBrowserLogs"))) ||
                    (config.getOptions().containsKey("collectDeviceLogs") && Boolean.parseBoolean(config.getOptions().get("collectDeviceLogs")))
            ) {
                try {
                    LoggingPreferences logPrefs = new LoggingPreferences();
                    logPrefs.enable(LogType.BROWSER, Level.ALL);
                    capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
                    System.out.println("Browser logs turn on");
                }
                catch (Exception e) {}
            }
        }
    }
}
