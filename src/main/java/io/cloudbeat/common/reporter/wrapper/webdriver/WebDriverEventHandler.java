package io.cloudbeat.common.reporter.wrapper.webdriver;

import io.cloudbeat.common.reporter.CbTestReporter;
import io.cloudbeat.common.reporter.model.FailureResult;
import io.cloudbeat.common.reporter.model.LogMessage;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.util.*;
import java.util.stream.Stream;

public class WebDriverEventHandler implements WebDriverEventListener {

    private final CbTestReporter reporter;
    private final WebDriver webDriver;
    private final Hashtable<Integer, String> elementHash = new Hashtable();
    private String lastStepId = null;
    private final boolean isWeb;
    private final boolean isMobile;
    private final boolean isAndroid;
    private final boolean isChrome;
    private final boolean isPerformanceLoggingOn;

    public WebDriverEventHandler(CbTestReporter reporter, WebDriver webDriver)
    {
        this.reporter = reporter;
        this.webDriver = webDriver;
        // identify if the provided webdriver is of mobile or web type
        Capabilities caps = ((HasCapabilities) webDriver).getCapabilities();
        isWeb = StringUtils.isNotEmpty(caps.getBrowserName());
        isMobile = caps.getPlatform() != null && (
                caps.getPlatform().name().equalsIgnoreCase("android") ||
                caps.getPlatform().name().equalsIgnoreCase("ios"));
        isAndroid = caps.getPlatform() != null && caps.getPlatform().name().equalsIgnoreCase("android");
        isChrome = isChromeDriver(webDriver);
        isPerformanceLoggingOn = isPerformanceLoggingOn(webDriver);
    }

    private static boolean isChromeDriver(WebDriver webDriver) {
        if (webDriver instanceof ChromeDriver)
            return true;
        Capabilities caps = ((HasCapabilities) webDriver).getCapabilities();
        return StringUtils.isNotEmpty(caps.getBrowserName()) && caps.getBrowserName().equalsIgnoreCase("chrome");
    }

    private static boolean isPerformanceLoggingOn(WebDriver webDriver) {
        Capabilities caps = ((HasCapabilities) webDriver).getCapabilities();
        return caps.getCapability("goog:loggingPrefs") != null;
    }

    @Override
    public void beforeAlertAccept(final WebDriver webDriver) {
        lastStepId = reporter.startStep("Accept alert");
    }

    @Override
    public void afterAlertAccept(final WebDriver webDriver) {
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    @Override
    public void beforeAlertDismiss(final WebDriver webDriver) {
        lastStepId = reporter.startStep("Dismiss alert");
    }

    @Override
    public void afterAlertDismiss(final WebDriver webDriver) {
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    @Override
    public void beforeNavigateTo(final String s, final WebDriver webDriver) {
        lastStepId = reporter.startStep("Navigate to " + s);
    }

    @Override
    public void afterNavigateTo(final String s, final WebDriver webDriver) {
        if (lastStepId == null)      // not suppose to happen
            return;

        String stepName = "Navigate to " + s;

        // get navigation timing metrics
        final Map<String, Number> stats = getNavigationTimingStats(webDriver);

        // get browser or device logs
        final List<LogMessage> logs = collectLogs(webDriver);

        collectPerformanceData(webDriver);

        reporter.passStep(lastStepId, stats, logs.size() == 0 ? null : logs);

        lastStepId = null;
    }

    private void collectPerformanceData(WebDriver webDriver) {
        //if (isChrome && isPerformanceLoggingOn)

        /*ArrayList<Object> results = (ArrayList<Object>) ((JavascriptExecutor)webDriver).executeScript(
                //"return window.performance.getEntries();");
                "return await chrome.devtools.network.getHAR();");
        results.forEach((url)->System.out.println(url.toString()));*/

        /*boolean hasPerformanceLogs = webDriver.manage().logs().getAvailableLogTypes()
                .stream().anyMatch(t -> t.equals("performance"));
        if (!hasPerformanceLogs)
            return;
        LogEntries perfLogs = webDriver.manage().logs().get("performance");*/
    }

    @Override
    public void beforeNavigateBack(final WebDriver webDriver) {
        lastStepId = reporter.startStep("Navigate back");
    }

    @Override
    public void afterNavigateBack(final WebDriver webDriver) {
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    @Override
    public void beforeNavigateForward(final WebDriver webDriver) {
        lastStepId = reporter.startStep("Navigate forward");
    }

    @Override
    public void afterNavigateForward(final WebDriver webDriver) {
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    @Override
    public void beforeNavigateRefresh(final WebDriver webDriver) {
        lastStepId = reporter.startStep("Navigate refresh");
    }

    @Override
    public void afterNavigateRefresh(final WebDriver webDriver) {
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    @Override
    public void beforeFindBy(final By by, final WebElement webElement, final WebDriver webDriver) {
        final String locatorDispName = this.getLocatorDisplayName(by);
        lastStepId = reporter.startStep(String.format("Find element %s",  locatorDispName));
    }

    @Override
    public void afterFindBy(final By by, final WebElement webElement, final WebDriver webDriver) {
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    @Override
    public void beforeClickOn(final WebElement webElement, final WebDriver webDriver) {
        final String elmName = this.getElementDisplayName(webElement);
        lastStepId = reporter.startStep(String.format("Click on %s", elmName));
    }

    @Override
    public void afterClickOn(final WebElement webElement, final WebDriver webDriver) {
        // get navigation timing metrics
        final Map<String, Number> stats = getNavigationTimingStats(webDriver);

        reporter.passStep(lastStepId, stats);
        lastStepId = null;
    }

    @Override
    public void beforeChangeValueOf(final WebElement webElement, final WebDriver webDriver,
            final CharSequence[] charSequences) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence charSequence : charSequences) {
            sb.append(charSequence.toString());
        }
        lastStepId = reporter.startStep(String.format("Set value \"%s\"", sb.toString()));
    }

    @Override
    public void afterChangeValueOf(final WebElement webElement, final WebDriver webDriver,
            final CharSequence[] charSequences) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence charSequence : charSequences) {
            sb.append(charSequence.toString());
        }
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    @Override
    public void beforeScript(final String s, final WebDriver webDriver) {
        lastStepId = reporter.startStep("Executing script " + s);
    }

    @Override
    public void afterScript(final String s, final WebDriver webDriver) {
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    @Override
    public void beforeSwitchToWindow(final String s, final WebDriver webDriver) {
        lastStepId = reporter.startStep("Switch to window " + s);
    }

    @Override
    public void afterSwitchToWindow(final String s, final WebDriver webDriver) {
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    @Override
    public void onException(final Throwable throwable, final WebDriver webDriver) {
        //final FailureResult failureModel = new FailureResult(throwable, this.reporter.getCurrentTestPackageName());
        if (lastStepId != null)
            reporter.failStep(lastStepId, throwable);
    }

    @Override
    public <X> void beforeGetScreenshotAs(final OutputType<X> outputType) {

    }

    @Override
    public <X> void afterGetScreenshotAs(final OutputType<X> outputType, final X x) {

    }

    @Override
    public void beforeGetText(final WebElement webElement, final WebDriver webDriver) {
        lastStepId = reporter.startStep("Getting text of  " + webElement.getText());
    }

    @Override
    public void afterGetText(final WebElement webElement, final WebDriver webDriver, final String s) {
        reporter.passStep(lastStepId);
        lastStepId = null;
    }

    private String getLocatorDisplayName(final By by) {
        final String byLocatorStr = by.toString();
        return String.format("%s", byLocatorStr.replace("By.", "by "));
    }

    private String getElementDisplayName(final WebElement webElement) {
        final String text = webElement.getText();
        final String tagName = webElement.getTagName();
        final String elmType = webElement.getAttribute("type");
        String elmTypeLabel = "";
        // determine element type (link, button or other)
        if (tagName.equals("a")) {
            elmTypeLabel = "link ";
        }            
        else if (tagName.equals("button")) {
            elmTypeLabel = "button ";
        }
        else if (tagName.equals("option")) {
            elmTypeLabel = "option ";
        }
        else if (tagName.equals("label")) {
            elmTypeLabel = "label ";
        }
        else if (tagName.equals("input") && elmType != null && (elmType.equals("button") || elmType.equals("submit"))) {
            elmTypeLabel = "button ";
        }
        else if (tagName.equals("input") && elmType != null && elmType.equals("url")) {
            elmTypeLabel = "link ";
        }

        if (text != null && !text.isEmpty()) {
            return String.format("%s\"%s\"", elmTypeLabel, text);
        }
        else {
            return String.format("<%s>", tagName);
        }
    }

    private List<LogMessage> collectLogs(final WebDriver webDriver) {
        ArrayList<LogMessage> logs = new ArrayList<>();
        if (isWeb)
            collectLogs(logs, webDriver, LogType.BROWSER);
        if (isAndroid)
            collectLogs(logs, webDriver, "logcat");

        return logs;
    }

    private void collectLogs(final List<LogMessage> logMessages, final WebDriver webDriver, final String logType) {
        final LogEntries logEntries = webDriver.manage().logs().get(logType);
        logEntries.forEach(logEntry -> logMessages.add(toLogMessage(logEntry)));
    }

    private static LogMessage toLogMessage(LogEntry logEntry) {
        LogMessage logMessage = new LogMessage();
        logMessage.setMessage(logEntry.getMessage());
        logMessage.setLevel(logEntry.getLevel().getName());
        logMessage.setTimestamp(logEntry.getTimestamp());

        return logMessage;
    }

    private Map<String, Number> getNavigationTimingStats(WebDriver webDriver) {
        // navigation timing is relevant only for browser based tests
        if (!isWeb)
            return null;

        JavascriptExecutor js = (JavascriptExecutor)webDriver;
        long loadEvent = (long) js.executeScript("return (window.performance.timing.loadEventEnd - window.performance.timing.loadEventStart)");
        long domContentLoadedEvent = (long) js.executeScript("return (window.performance.timing.domContentLoadedEventEnd - window.performance.timing.domContentLoadedEventStart)");

        Map<String, Number> stats = new HashMap<>();
        if (loadEvent > 0)
            stats.put("loadEvent", loadEvent);
        if (domContentLoadedEvent > 0)
            stats.put("domContentLoadedEvent", domContentLoadedEvent);
        // return null if both loadEvent and domContentLoadedEvent are 0
        if (stats.keySet().size() > 0)
            return stats;
        return null;
    }
}
