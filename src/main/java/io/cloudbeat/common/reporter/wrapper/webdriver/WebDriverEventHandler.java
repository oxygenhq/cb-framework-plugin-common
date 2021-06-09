package io.cloudbeat.common.reporter.wrapper.webdriver;

import io.cloudbeat.common.reporter.CbTestReporter;
import io.cloudbeat.common.reporter.model.FailureResult;
import org.openqa.selenium.*;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class WebDriverEventHandler implements WebDriverEventListener {

    private CbTestReporter reporter;
    private final Hashtable<Integer, String> elementHash = new Hashtable();
    private String lastStepId = null;

    public WebDriverEventHandler(CbTestReporter reporter)
    {
        this.reporter = reporter;
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
        JavascriptExecutor js = (JavascriptExecutor)webDriver;

        long loadEvent = (long) js.executeScript("return (window.performance.timing.loadEventEnd - window.performance.timing.loadEventStart)");
        long domContentLoadedEvent = (long) js.executeScript("return (window.performance.timing.domContentLoadedEventEnd - window.performance.timing.domContentLoadedEventStart)");

        System.out.println("Load event time:" + loadEvent);
        System.out.println("Dom content load event time:" + domContentLoadedEvent);

        Map<String, Number> stats = new HashMap<>();
        stats.put("loadEvent", loadEvent);
        stats.put("domContentLoadedEvent", domContentLoadedEvent);

        reporter.passStep(lastStepId, stats);
        lastStepId = null;
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
        reporter.passStep(lastStepId);
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
}
