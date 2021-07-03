package io.cloudbeat.common.reporter.wrapper.webdriver;

import io.appium.java_client.events.EventFiringWebDriverFactory;
import io.appium.java_client.events.api.Listener;
import io.cloudbeat.common.reporter.CbTestReporter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.util.ArrayList;
import java.util.List;

public class WebDriverWrapper {
    private CbTestReporter reporter;

    public WebDriverWrapper(CbTestReporter reporter) {
        this.reporter = reporter;
    }

    public EventFiringWebDriver wrap(WebDriver webDriver) {
        EventFiringWebDriver eventDriver = new EventFiringWebDriver(webDriver);
        WebDriverEventHandler handler = new WebDriverEventHandler(this.reporter, webDriver);
        eventDriver.register(handler);
        return eventDriver;
    }

    public void wrap(EventFiringWebDriver eventFiringWebDriver) {
        WebDriverEventHandler handler = new WebDriverEventHandler(this.reporter, eventFiringWebDriver);
        eventFiringWebDriver.register(handler);
    }
}
