package io.cloudbeat.common;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class CbTest {
    private Map<String, ArrayList<StepModel>> _steps = new HashMap<>();
    public WebDriver driver;
    private String currentStepName;

    public WebDriver createWebDriverBasedOnCbCapabilities() throws Exception {
        String browserName = System.getProperty("browserName");
        DesiredCapabilities capabilities = null;
        if("firefox".equalsIgnoreCase(browserName)){
            capabilities = DesiredCapabilities.firefox();
        } else if ("ie".equalsIgnoreCase(browserName)) {
            capabilities = DesiredCapabilities.internetExplorer();
        } else {
            capabilities = DesiredCapabilities.chrome();
        }

        return new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capabilities);
    }

    public void setWebDriver(WebDriver driver) {
        EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);
        CbEventHandler handler = new CbEventHandler(this);
        eventDriver.register(handler);
        this.driver = eventDriver;
        this.driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    public abstract String getCurrentTestName();

    public void failCurrentStep(FailureModel failureModel) {endStepInner(currentStepName, getCurrentTestName(), false, failureModel);}

    public void startStep(String name) {
        System.out.println("Start step with name:" + name);
        currentStepName = name;
        StepModel newStep = new StepModel();
        newStep.name = name;
        newStep.steps = new ArrayList<>();
        newStep.startTime = new Date();
        newStep.isFinished = false;

        String testName = getCurrentTestName();
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

    private void endStepInner(String name, String testName, boolean isSuccess, FailureModel failureModel) {
        System.out.println("End step with name:" + name + " with is success:" + isSuccess);
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

    public ArrayList<StepModel> getStepsForMethod(String methodName, boolean isSuccess, FailureModel failureModel) {
        if (_steps.containsKey(methodName))
        {
            ArrayList<StepModel> steps = _steps.get(methodName);
            ArrayList<StepModel> notEndedSteps = new ArrayList<>(steps.stream().filter((stepModel -> !stepModel.isFinished)).collect(Collectors.toList()));
            for (StepModel step : notEndedSteps) {
                endStepInner(step.name, methodName, isSuccess, failureModel);
            }

            return _steps.get(methodName);
        }

        return null;
    }

    public void afterTest() {
        driver.close();
        driver.quit();
    }
}
