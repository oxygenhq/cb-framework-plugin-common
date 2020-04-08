package io.cloudbeat.common;

import io.cloudbeat.common.CloudBeatTest;
import io.cloudbeat.common.model.EndStepModel;
import io.cloudbeat.common.model.FailureModel;
import org.openqa.selenium.*;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.util.Hashtable;

public class WebDriverEventHandler implements WebDriverEventListener {

    private CloudBeatTest currentTest;
    private final Hashtable<Integer, String> elementHash = new Hashtable();

    public WebDriverEventHandler(CloudBeatTest test)
    {
        currentTest = test;
    }

    @Override
    public void beforeAlertAccept(final WebDriver webDriver) {
        currentTest.startStep("Alert accepting");
    }

    @Override
    public void afterAlertAccept(final WebDriver webDriver) {
        currentTest.endStep("Alert accepting");
    }

    @Override
    public void afterAlertDismiss(final WebDriver webDriver) {
        currentTest.startStep("Alert dismissing");
    }

    @Override
    public void beforeAlertDismiss(final WebDriver webDriver) {
        currentTest.endStep("Alert dismissing");
    }

    @Override
    public void beforeNavigateTo(final String s, final WebDriver webDriver) {
        currentTest.startStepInner("Navigate to " + s, currentTest.getCurrentTestName(), s);
    }

    @Override
    public void afterNavigateTo(final String s, final WebDriver webDriver) {
        String stepName = "Navigate to " + s;
        EndStepModel endStepModel = new EndStepModel(stepName, currentTest.getCurrentTestName(), true);
        JavascriptExecutor js = (JavascriptExecutor)webDriver;

        long loadEvent = (long) js.executeScript("return (window.performance.timing.loadEventEnd - window.performance.timing.loadEventStart)");
        long domContentLoadedEvent = (long) js.executeScript("return (window.performance.timing.domContentLoadedEventEnd - window.performance.timing.domContentLoadedEventStart)");

        System.out.println("Load event time:" + loadEvent);
        System.out.println("Dom content load event time:" + domContentLoadedEvent);

        endStepModel.loadEvent = loadEvent;
        endStepModel.domContentLoadedEvent = domContentLoadedEvent;

        currentTest.endStepInner(endStepModel);
    }

    @Override
    public void beforeNavigateBack(final WebDriver webDriver) {
        currentTest.startStep("Navigate back");
    }

    @Override
    public void afterNavigateBack(final WebDriver webDriver) {
        currentTest.endStep("Navigate back");
    }

    @Override
    public void beforeNavigateForward(final WebDriver webDriver) {
        currentTest.startStep("Navigate forward");
    }

    @Override
    public void afterNavigateForward(final WebDriver webDriver) {
        currentTest.endStep("Navigate forward");
    }

    @Override
    public void beforeNavigateRefresh(final WebDriver webDriver) {
        currentTest.startStep("Navigate refresh");
    }

    @Override
    public void afterNavigateRefresh(final WebDriver webDriver) {
        currentTest.endStep("Navigate refresh");
    }

    @Override
    public void beforeFindBy(final By by, final WebElement webElement, final WebDriver webDriver) {
        final String locatorDispName = this.getLocatorDisplayName(by);
        currentTest.startStep(String.format("Find element %s",  locatorDispName));
    }

    @Override
    public void afterFindBy(final By by, final WebElement webElement, final WebDriver webDriver) {
        elementHash.put(webElement.hashCode(), by.toString());
        final String locatorDispName = this.getLocatorDisplayName(by);
        currentTest.endStep(String.format("Find element %s",  locatorDispName));
    }

    @Override
    public void beforeClickOn(final WebElement webElement, final WebDriver webDriver) {
        final String elmName = this.getElementDisplayName(webElement);
        currentTest.startStep(String.format("Click on %s", elmName));
    }

    @Override
    public void afterClickOn(final WebElement webElement, final WebDriver webDriver) {
        currentTest.endStep(currentTest.getCurrentStepName());
    }

    @Override
    public void beforeChangeValueOf(final WebElement webElement, final WebDriver webDriver,
            final CharSequence[] charSequences) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence charSequence : charSequences) {
            sb.append(charSequence.toString());
        }
        currentTest.startStep(String.format("Set value \"%s\"", sb.toString()));
    }

    @Override
    public void afterChangeValueOf(final WebElement webElement, final WebDriver webDriver,
            final CharSequence[] charSequences) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence charSequence : charSequences) {
            sb.append(charSequence.toString());
        }
        currentTest.endStep(String.format("Set value \"%s\"", sb.toString()));
    }

    @Override
    public void beforeScript(final String s, final WebDriver webDriver) {
        currentTest.startStep("Executing script " + s);
    }

    @Override
    public void afterScript(final String s, final WebDriver webDriver) {
        currentTest.endStep("Executing script " + s);
    }

    @Override
    public void beforeSwitchToWindow(final String s, final WebDriver webDriver) {
        currentTest.startStep("Switch to window " + s);
    }

    @Override
    public void afterSwitchToWindow(final String s, final WebDriver webDriver) {
        currentTest.endStep("Switch to window " + s);
    }

    @Override
    public void onException(final Throwable throwable, final WebDriver webDriver) {
        final FailureModel failureModel = new FailureModel(throwable, this.currentTest.getCurrentTestPackageName());
        currentTest.failCurrentStep(failureModel);
    }

    @Override
    public <X> void beforeGetScreenshotAs(final OutputType<X> outputType) {

    }

    @Override
    public <X> void afterGetScreenshotAs(final OutputType<X> outputType, final X x) {

    }

    @Override
    public void beforeGetText(final WebElement webElement, final WebDriver webDriver) {
        currentTest.startStep("Getting text of  " + webElement.getText());
    }

    @Override
    public void afterGetText(final WebElement webElement, final WebDriver webDriver, final String s) {
        currentTest.endStep("Getting text of  " + webElement.getText());
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
