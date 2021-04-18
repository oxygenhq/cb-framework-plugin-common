package io.cloudbeat.common;

import com.google.sitebricks.conversion.generics.CaptureType;
import io.appium.java_client.AppiumDriver;
import io.cloudbeat.common.model.*;
import io.cloudbeat.common.restassured.RestAssuredFailureListener;
import io.cloudbeat.common.restassured.RestAssuredRequestLogger;
import io.restassured.RestAssured;
import io.restassured.config.FailureConfig;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.ProxyServer;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public abstract class CloudBeatTest {
    public static final String DEFAULT_WEBDRIVER_URL = "http://localhost:4444/wd/hub";
    public static final String DEFAULT_APPIUM_URL = "http://localhost:4723/wd/hub";

    public WebDriver driver;

    private Map<String, ArrayList<StepModel>> _steps = new HashMap();
    protected String currentStepName;
    protected String currentTestPackage;
    private ArrayList<LogResult> logEntries = new ArrayList();

    private List<String> excludeCapabilityKeys = Arrays.asList(new String[] { "technologyName", "goog:chromeOptions", "browserName" });

    public void setupTest() {
        this.setupTest(null);
    }

    public void setupTest(String testPackageName) {
        if (StringUtils.isEmpty(testPackageName)) {
            this.currentTestPackage = guessCurrentTestPackage();
        }
        else {
            this.currentTestPackage = testPackageName;
        }
    }

    public WebDriver setupDriver(WebDriver driver) {
        EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);
        WebDriverEventHandler handler = new WebDriverEventHandler(this);
        eventDriver.register(handler);
        this.driver = eventDriver;
        return driver;
    }

    protected String guessCurrentTestPackage() {
        // we assume that setupTest method is directly called from the main test class
        StackTraceElement[] stackTrace = Helper.getStackTrace();
        StackTraceElement[] noCbCalls = Helper.filterStackTrace(stackTrace);
        //StackTraceElement[] noCbCalls = Arrays.stream(stackTrace).filter(call -> !call.toString().startsWith("io.cloudbeat")).toArray(StackTraceElement[]::new);
        if (noCbCalls.length > 0) {
            return Helper.extractPackageFromClassName(noCbCalls[0].getClassName());
        }
        return null;
    }

    public void setupRestAssured() {
        RestAssured.filters(new RestAssuredRequestLogger(this));
        RestAssured.config = RestAssured.config()
                .failureConfig(FailureConfig.failureConfig().with().failureListeners(new RestAssuredFailureListener(this)));
    }

    public WebDriver setupWebDriver() throws Exception {
        return this.initWebDriver(null);
    }

    public WebDriver setupMobileDriver() throws Exception {
        return this.initMobileDriver(null);
    }

    public WebDriver initWebDriver(DesiredCapabilities userCapabilities) throws Exception {
        DesiredCapabilities capabilities = mergeUserAndCloudbeatCapabilities(userCapabilities);
        RemoteWebDriver driver = new RemoteWebDriver(getDriverUrl(DEFAULT_WEBDRIVER_URL), capabilities);
        EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);
        WebDriverEventHandler handler = new WebDriverEventHandler(this);
        eventDriver.register(handler);
        this.driver = eventDriver;
        return driver;
    };

    public WebDriver initMobileDriver(DesiredCapabilities userCapabilities) throws Exception {
        DesiredCapabilities capabilities = mergeUserAndCloudbeatCapabilities(userCapabilities);
        AppiumDriver driver = new AppiumDriver(getDriverUrl(DEFAULT_APPIUM_URL), capabilities);
        EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);
        WebDriverEventHandler handler = new WebDriverEventHandler(this);
        eventDriver.register(handler);
        this.driver = eventDriver;
        return this.driver;
    }

    protected DesiredCapabilities mergeUserAndCloudbeatCapabilities(DesiredCapabilities userCapabilities) throws Exception {
        String browserName = System.getProperty("browserName");

        DesiredCapabilities capabilities = null;
        if (StringUtils.isEmpty(browserName)) {
            if (userCapabilities != null)
                capabilities = userCapabilities;
            else
                capabilities = DesiredCapabilities.chrome();
        }
        else if ("firefox".equalsIgnoreCase(browserName)) {
            capabilities = DesiredCapabilities.firefox();
        }
        else if ("ie".equalsIgnoreCase(browserName)) {
            capabilities = DesiredCapabilities.internetExplorer();
        }
        else if ("chrome".equalsIgnoreCase(browserName)) {
            capabilities = DesiredCapabilities.chrome();
        }
        else {
            capabilities = DesiredCapabilities.chrome();
        }

        String payloadPath = System.getProperty("payloadpath");
        PayloadModel payloadModel = null;
        try {
            payloadModel = PayloadModel.Load(payloadPath);
        }
        catch (Exception exception) { }

        if(payloadModel != null && payloadModel.capabilities != null) {
            for (Map.Entry<String, String> pair : payloadModel.capabilities.entrySet()) {
                if (excludeCapabilityKeys.contains(pair.getKey())) {
                    continue;
                }

                capabilities.setCapability(pair.getKey(), pair.getValue());
            }
        }

        if(payloadModel != null && payloadModel.options != null
                && ((payloadModel.options.containsKey("collectBrowserLogs") && Boolean.parseBoolean(payloadModel.options.get("collectBrowserLogs")))
                || ((payloadModel.options.containsKey("collectDeviceLogs") && Boolean.parseBoolean(payloadModel.options.get("collectDeviceLogs")))))) {

            try {
                LoggingPreferences logPrefs = new LoggingPreferences();
                logPrefs.enable(LogType.BROWSER, Level.ALL);
                capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
                System.out.println("Browser logs turn on");
            }
            catch (Exception e) {}
        }

        // merge capabilities received from CloudBeat with user provided capabilities
        if (userCapabilities != null && capabilities != null) {
            capabilities = userCapabilities.merge(capabilities);
        }

        return capabilities;
    }

    public abstract String getCurrentTestName();

    public void failCurrentStep(FailureModel failureModel) {
        endStepInner(new EndStepModel(currentStepName, getCurrentTestName(), false, failureModel));
    }

    public String getCurrentStepName() {
        return this.currentStepName;
    }

    public String getCurrentTestPackageName() {
        return this.currentTestPackage;
    }

    public void startStep(String name) {
        startStepInner(name, getCurrentTestName(), null);
    }

    void startStepInner(String name, String testName, String pageRef) {
        System.out.println("Start step with name:" + name + " for method" + testName);
        currentStepName = name;
        StepModel newStep = new StepModel();
        newStep.name = name;
        newStep.location = getCurrentLocation();
        newStep.steps = new ArrayList<>();
        newStep.startTime = new Date();
        newStep.isFinished = false;
        if (_steps.containsKey(testName)) {
            ArrayList<StepModel> steps = _steps.get(testName);
            StepModel currentStep = getFirstNotFinishedStep(steps);

            while (currentStep != null) {
                steps = currentStep.steps;
                newStep.parent = currentStep;
                currentStep = getFirstNotFinishedStep(steps);
            }

            steps.add(newStep);
            return;
        }

        ArrayList steps = new ArrayList<StepModel>();
        steps.add(newStep);
        _steps.put(testName, steps);
    }

    public void endStep(String name) { endStepInner(new EndStepModel(name, getCurrentTestName(), true)); }

    protected String getCurrentLocation() {
        StackTraceElement firstTestRelatedCall = this.getFirstTestRelatedCall(Helper.getStackTrace());
        if (firstTestRelatedCall != null) {
            return firstTestRelatedCall.toString();
        }
        return null;
    }

    protected StackTraceElement getFirstTestRelatedCall(StackTraceElement[] stackTrace) {
        // if currentTestPackage is defined, then find the first call in the stack trace that matches package name
        if (!StringUtils.isEmpty(this.currentTestPackage)) {
            Optional<StackTraceElement> relatedCall = Arrays.stream(stackTrace).filter(call -> call.toString().startsWith(this.currentTestPackage)).findFirst();
            if(relatedCall.isPresent()) {
                return relatedCall.get();
            }
        }

        StackTraceElement[] filtered = Helper.filterStackTrace(stackTrace);
        if (filtered.length > 0) {
            return filtered[0];
        }

        return null;
    }

    void endStepInner(EndStepModel endStepModel) {
        System.out.println("End step with name:" + endStepModel.stepName + " with is success:" + endStepModel.isSuccess + " for method" + endStepModel.testName);
        if (!_steps.containsKey(endStepModel.testName)) {
            return;
        }

        ArrayList<StepModel> steps = _steps.get(endStepModel.testName);
        StepModel currentStep = getFirstNotFinishedStep(steps);

        if (currentStep == null) {
            return;
        }

        while (!currentStep.name.equalsIgnoreCase(endStepModel.stepName)) {
            steps = currentStep.steps;
            currentStep = getFirstNotFinishedStep(steps);

            if (currentStep == null) {
                return;
            }
        }

        finishStep(currentStep, endStepModel);

        while (currentStep != null) {
            finishStep(currentStep, endStepModel);
            steps = currentStep.steps;
            currentStep = getFirstNotFinishedStep(steps);
        }
    }

    private void finishStep(StepModel currentStep, EndStepModel endStepModel) {
        currentStep.status = endStepModel.isSuccess ? ResultStatus.Passed : ResultStatus.Failed;
        currentStep.isFinished = true;
        currentStep.failure = endStepModel.failureModel;
        currentStep.duration = (new Date().toInstant().toEpochMilli() - currentStep.startTime.toInstant().toEpochMilli());
        currentStep.loadEvent = endStepModel.loadEvent;
        currentStep.domContentLoadedEvent = endStepModel.domContentLoadedEvent;

        if(!endStepModel.isSuccess && driver != null && driver instanceof TakesScreenshot) {
            currentStep.screenShot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64);
        }
    }

    private StepModel getFirstNotFinishedStep(ArrayList<StepModel> steps) {
        return steps.stream()
                .filter((step) -> !step.isFinished)
                .findFirst()
                .orElse(null);
    }

    protected URL getDriverUrl(String defaultUrl) throws MalformedURLException {
        String payloadPath = System.getProperty("payloadpath");
        String webDriverUrl = new String();
        PayloadModel payloadModel = null;
        try {
            payloadModel = PayloadModel.Load(payloadPath);
            if(payloadModel != null && payloadModel.metadata != null) {
                if(payloadModel.metadata.containsKey("seleniumUrl")) {
                    webDriverUrl = payloadModel.metadata.get("seleniumUrl");
                }
                else if(payloadModel.metadata.containsKey("appiumUrl")) {
                    webDriverUrl =  payloadModel.metadata.get("appiumUrl");
                }
            }
        }
        catch (Exception exception) { }
        
        if (StringUtils.isEmpty(webDriverUrl))
            webDriverUrl = defaultUrl;

        return new URL(webDriverUrl);
    }

    public ArrayList<StepModel> getStepsForMethod(String methodName, boolean isSuccess, FailureModel failureModel) {
        if (_steps.containsKey(methodName))
        {
            if(isSuccess) return _steps.get(methodName);

            ArrayList<StepModel> steps = _steps.get(methodName);
            StepModel firstNotEndedStep = getFirstNotFinishedStep(steps);

            while (firstNotEndedStep != null && firstNotEndedStep.steps != null && !firstNotEndedStep.steps.isEmpty()) {
                steps = firstNotEndedStep.steps;
                firstNotEndedStep = getFirstNotFinishedStep(steps);
            }


            Boolean isAnyFailSteps = steps.stream().anyMatch(stepModel -> stepModel.status == ResultStatus.Failed);

            if(!isAnyFailSteps) {
                firstNotEndedStep = getFirstNotFinishedStep(steps);

                if (firstNotEndedStep == null) {
                    startStepInner("Assertion", methodName, null);
                    endStepInner(new EndStepModel("Assertion", methodName, false, failureModel));
                }
            }

            steps = _steps.get(methodName);
            steps.stream()
                .filter(x -> !x.isFinished)
                .forEach(x -> endStepInner(new EndStepModel(x.name, methodName, false, failureModel)));


            steps = _steps.get(methodName);
            if(steps != null && !steps.isEmpty()) {
                return steps;
            }
        }

        ArrayList<StepModel> steps = new ArrayList();

        if(!isSuccess) {
            StepModel failureStep = new StepModel();
            failureStep.isFinished = true;
            failureStep.failure = failureModel;
            failureStep.status = ResultStatus.Failed;
            failureStep.name = failureModel.message == null ? "Error" : failureModel.message.split("\n")[0];

            steps.add(failureStep);
        }
        
        return steps;
    }

    public ArrayList<LogResult> getLastLogEntries() {
        if(driver == null) {
            return new ArrayList();
        }

        ArrayList<LogResult> result = new ArrayList();
        driver.manage().logs().getAvailableLogTypes().stream().forEach(type -> {
            List<LogEntry> logs = driver.manage().logs().get(type).getAll();
            logs.stream().forEach(x -> result.add(new LogResult(x, type)));
        });

        if(logEntries != null) {
            result.removeAll(logEntries);
        }

        logEntries = result;
        return result;
    }

    protected void afterTest() {
        try {
            if (driver != null) {
                driver.close();
                driver.quit();
            }
        }
        catch (Exception e){}
    }
}
