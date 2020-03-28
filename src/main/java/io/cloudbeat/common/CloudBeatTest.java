package io.cloudbeat.common;

import com.smartbear.har.model.HarEntry;
import com.smartbear.har.model.HarLog;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.cloudbeat.common.model.FailureModel;
import io.cloudbeat.common.model.PayloadModel;
import io.cloudbeat.common.model.ResultStatus;
import io.cloudbeat.common.model.StepModel;
import io.cloudbeat.common.restassured.RestAssuredFailureListener;
import io.cloudbeat.common.restassured.RestAssuredRequestLogger;
import io.cloudbeat.common.webdriver.WebDriverEventHandler;
import io.restassured.RestAssured;
import io.restassured.config.FailureConfig;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class CloudBeatTest {
    public static final String DEFAULT_WEBDRIVER_URL = "http://localhost:4444/wd/hub";
    public static final String DEFAULT_APPIUM_URL = "http://localhost:4723/wd/hub";

    protected WebDriver driver;

    private Map<String, ArrayList<StepModel>> _steps = new HashMap<>();
    protected String currentStepName;
    protected String currentTestPackage;

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

    public WebDriver initWebDriver(DesiredCapabilities userCapabilities) throws MalformedURLException {
        MutableCapabilities capabilities = mergeUserAndCloudbeatCapabilities(userCapabilities);
        RemoteWebDriver driver = new RemoteWebDriver(getDriverUrl(DEFAULT_WEBDRIVER_URL), capabilities);
        EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);
        WebDriverEventHandler handler = new WebDriverEventHandler(this);
        eventDriver.register(handler);
        this.driver = eventDriver;
        return driver;
    };

    public WebDriver initMobileDriver(DesiredCapabilities userCapabilities) throws MalformedURLException {
        MutableCapabilities capabilities = mergeUserAndCloudbeatCapabilities(userCapabilities);
        AppiumDriver driver = new AppiumDriver(getDriverUrl(DEFAULT_APPIUM_URL), capabilities);
        EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);
        WebDriverEventHandler handler = new WebDriverEventHandler(this);
        eventDriver.register(handler);
        this.driver = eventDriver;
        return this.driver;
    }

    protected MutableCapabilities mergeUserAndCloudbeatCapabilities(DesiredCapabilities userCapabilities) {
        String browserName = System.getProperty("browserName");

        MutableCapabilities capabilities = null;
        if (StringUtils.isEmpty(browserName)) {
            if (userCapabilities != null)
                capabilities = userCapabilities;
            else
                capabilities = new ChromeOptions();
        }
        else if ("firefox".equalsIgnoreCase(browserName)) {
            capabilities = DesiredCapabilities.firefox();
        }
        else if ("ie".equalsIgnoreCase(browserName)) {
            capabilities = DesiredCapabilities.internetExplorer();
        }
        else if ("chrome".equalsIgnoreCase(browserName)) {
            capabilities = new ChromeOptions();
        }
        else {
            capabilities = new ChromeOptions();
        }

        String payloadPath = System.getProperty("payloadpath");
        PayloadModel payloadModel = null;
        try {
            payloadModel = PayloadModel.Load(payloadPath);
        }
        catch (Exception exception) { }

        if(payloadModel != null && payloadModel.capabilities != null) {
            for (Map.Entry<String, String> pair : payloadModel.capabilities.entrySet()) {
                capabilities.setCapability(pair.getKey(), pair.getValue());
            }
        }

        // merge capabilities received from CloudBeat with user provided capabilities
        if (userCapabilities != null && capabilities != null) {
            capabilities = userCapabilities.merge(capabilities);
        }

        return capabilities;
    }

    public abstract String getCurrentTestName();

    public void failCurrentStep(FailureModel failureModel) {
        endStepInner(currentStepName, getCurrentTestName(), false, failureModel);
    }

    public String getCurrentStepName() {
        return this.currentStepName;
    }

    public String getCurrentTestPackageName() {
        return this.currentTestPackage;
    }

    public void startStep(String name) {
        startStepInner(name, getCurrentTestName());
    }

    private void startStepInner(String name, String testName) {
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
                currentStep = getFirstNotFinishedStep(steps);
            }

            steps.add(newStep);
            return;
        }

        ArrayList steps = new ArrayList<StepModel>();
        steps.add(newStep);
        _steps.put(testName, steps);
    }

    public void endStep(String name) {
        endStepInner(name, getCurrentTestName(), true, null);
    }

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
            return Arrays.stream(stackTrace).filter(call -> call.toString().startsWith(this.currentTestPackage)).findFirst().get();
        }
        // alternatively, filter out unrelated calls (e.g. java, selenium, cloudbeat internals, etc.)
        else {
            StackTraceElement[] filtered = Helper.filterStackTrace(stackTrace);
            if (filtered.length > 0) {
                return filtered[0];
            }
            return null;
        }
    }

    private void endStepInner(String name, String testName, boolean isSuccess, FailureModel failureModel) {
        System.out.println("End step with name:" + name + " with is success:" + isSuccess + " for method" + testName);
        if (!_steps.containsKey(testName)) {
            return;
        }

        ArrayList<StepModel> steps = _steps.get(testName);
        StepModel currentStep = getFirstNotFinishedStep(steps);

        if (currentStep == null) {
            return;
        }

        while (!currentStep.name.equalsIgnoreCase(name)) {
            steps = currentStep.steps;
            currentStep = getFirstNotFinishedStep(steps);

            if (currentStep == null) {
                return;
            }
        }

        finishStep(currentStep, isSuccess, failureModel);

        while (currentStep != null) {
            finishStep(currentStep, isSuccess, failureModel);
            steps = currentStep.steps;
            currentStep = getFirstNotFinishedStep(steps);
        }
    }

    private void finishStep(StepModel currentStep, boolean isSuccess, FailureModel failureModel) {
        currentStep.status = isSuccess ? ResultStatus.Passed : ResultStatus.Failed;
        currentStep.isFinished = true;
        currentStep.failure = failureModel;
        currentStep.duration = (new Date().toInstant().toEpochMilli() - currentStep.startTime.toInstant().toEpochMilli());

        if(!isSuccess && driver != null && driver instanceof TakesScreenshot) {
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
        String webDriverUrl = System.getProperty("webdriverUrl");
        if (StringUtils.isEmpty(webDriverUrl))
            webDriverUrl = defaultUrl;

        return new URL(webDriverUrl);
    }

    public ArrayList<StepModel> getStepsForMethod(String methodName, boolean isSuccess, FailureModel failureModel) {
        if (_steps.containsKey(methodName))
        {
            ArrayList<StepModel> steps = _steps.get(methodName);
            ArrayList<StepModel> notEndedSteps = new ArrayList<>(steps.stream().filter((stepModel -> !stepModel.isFinished)).collect(Collectors.toList()));
            Boolean isAnyFailSteps = steps.stream().anyMatch(stepModel -> stepModel.status == ResultStatus.Failed);
            
            if (notEndedSteps.isEmpty() && !isSuccess && !isAnyFailSteps) {
                startStepInner("Assertion", methodName);
                endStepInner("Assertion", methodName, false, failureModel);
            } else {
                for (StepModel step : notEndedSteps) {
                    endStepInner(step.name, methodName, isSuccess, failureModel);
                }
            }

            return _steps.get(methodName);
        }

        return null;
    }
    public void addHar(HarLog har) {
        /*StepModel currentStep = getFirstNotFinishedStep(steps);
        if (currentStep == null)
            return;     // this method must be called on unfinished step
        if (currentStep.extra == null)
            currentStep.extra = new ExtraModel();
        if (currentStep.extra.har == null)
            currentStep.extra.har = new HarLog()*/
    }

    public void addHarEntry(HarEntry entry) {

    }

    protected void afterTest() {
        if (driver != null) {
            driver.close();
            driver.quit();
        }
    }
}
