package io.cloudbeat.common.reporter;

import io.cloudbeat.common.client.CbApiClient;
import io.cloudbeat.common.config.CbConfig;
import io.cloudbeat.common.reporter.model.*;
import io.cloudbeat.common.reporter.wrapper.webdriver.WebDriverWrapper;
import io.cloudbeat.common.writer.ResultWriter;
import org.apache.commons.lang3.SystemUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Function;

public class CbTestReporter {
    //private static final Logger LOGGER = LoggerFactory.getLogger(CbTestReporter.class);
    private final static String TEST_RESULTS_FILENAME = ".CB_TEST_RESULTS";
    private final String language = "java";

    private CbApiClient cbClient;
    private CbConfig config;
    private Instance instance;
    private String frameworkName;
    private String frameworkVersion;
    private TestResult result;
    private boolean isStarted = false;

    private SuiteResult lastSuite = null;
    private CaseResult lastCase = null;
    private StepResult lastStep = null;

    public CbTestReporter(CbConfig config) {
        this.config = config;
        this.cbClient = new CbApiClient();
    }

    public Optional<Instance> getInstance() {
        if (instance == null)
            return Optional.empty();
        return Optional.of(instance);
    }

    public boolean isStarted() {
        return  isStarted;
    }

    public void setFramework(final String frameworkName) {
        this.setFramework(frameworkName, null);
    }

    public void setFramework(final String frameworkName, final String frameworkVersion) {
        this.frameworkName = frameworkName;
        this.frameworkVersion = frameworkVersion;
        if (result != null && !result.getMetaData().containsKey("framework.name")) {
            result.addAttribute("framework.name", frameworkName);
            result.addAttribute("framework.version", frameworkVersion);
        }
    }

    public void startInstance() {
        // ignore this call if instance has been already initialized
        if (instance != null)
            return;
        if (config.getInstanceId() != null)
            instance = new Instance(config.getInstanceId(), config.getRunId(), config.getCapabilities());
        else
            instance = new Instance(null, config.getRunId(), config.getCapabilities());
        instance.setStatus(RunStatus.RUNNING);
        if (instance.isRunIdAutoGenerated())
            cbClient.startRun(instance.getRunId(), config.getProjectId(), RunStatus.RUNNING);

        if (instance.isInstanceIdAutoGenerated())
            cbClient.startInstance(instance.getId(), instance.getRunId(), RunStatus.RUNNING, instance.getAttributes());
        else
            cbClient.postInstanceStatus(instance.getRunId(), instance.getId(), instance.getStatus());
        result = new TestResult(instance.getRunId(), instance.getId(), config.getCapabilities(), config.getOptions(), config.getMetadata(), config.getEnvironmentVariables());
        // add system attributes (e.g. agent information)
        addSystemAttributes();
        // add framework details, if provided by framework implementation
        if (frameworkName != null)
            result.addAttribute("framework.name", frameworkName);
        if (frameworkVersion != null)
            result.addAttribute("framework.version", frameworkVersion);
        // make sure to indicate we have already started the instance
        // so if of any reason we call this method again, we won't re-initialize the instance
        isStarted = true;
    }

    public void endInstance() {
        if (instance == null)
            return;
        TestStatus finalStatus = calculateFinalTestStatus();
        result.end(finalStatus);
        instance.setStatus(RunStatus.FINISHED);
        cbClient.endInstance(instance.getId(), instance.getRunId(), finalStatus);
        if (instance.isRunIdAutoGenerated())
            cbClient.endRun(instance.getRunId());
        isStarted = false;
        ResultWriter.writeResult(result, TEST_RESULTS_FILENAME);
    }

    private TestStatus calculateFinalTestStatus() {
        return TestStatus.PASSED;
    }

    public void startSuite(final String name, final String fqn) {
        final SuiteResult suite = result.addNewSuite(name);
        suite.setFqn(fqn);
    }

    public void endSuite(String fqn) {
        result.lastSuite(fqn).ifPresent(suite -> {
           suite.end();
        });
    }

    public void startCase(final String name, final String fqn, final String suiteFqn) throws Exception {
        SuiteResult suiteResult = result.lastSuite(suiteFqn).orElseThrow(
                () -> new Exception("No started suite was found. You must call startSuite first.")
        );
        CaseResult caseResult = suiteResult.addNewCaseResult(name);
        caseResult.setFqn(fqn);
        this.lastCase = caseResult;
    }

    public void endCase(final String caseFqn, final TestStatus status, final Throwable throwable) throws Exception {
        if (lastCase == null || lastCase.getFqn() == null)
            return;
        if (!lastCase.getFqn().equals(caseFqn))
            throw new Exception("Cannot find started case: " + caseFqn);

        lastCase.end(status, throwable);
        lastCase = null;
    }

    public void passCase(final String caseFqn) throws Exception {
        endCase(caseFqn, TestStatus.PASSED, null);
    }

    public void failCase(final String caseFqn, Throwable exception) throws Exception {
        endCase(caseFqn, TestStatus.FAILED, exception);
    }

    public void skipCase(final String caseFqn, final String suiteFqn) throws Exception {
        endCase(caseFqn, TestStatus.SKIPPED, null);
    }

    public String startStep(final String name) {
        return startStep(name, null, null);
    }

    public String startStep(final String name, final String fqn, final List<String> args) {
        if (lastStep != null) {
            lastStep = lastStep.addNewSubStep(name);
        }
        else if (lastCase != null) {
            lastStep = lastCase.addNewStep(name);
        }
        else    // we are not suppose to call startStep if not case was started before
            return null;
        if (fqn != null)
            lastStep.setFqn(fqn);
        if (args != null)
            lastStep.setArgs(args);
        return lastStep.getId();
    }

    public void endLastStep() {
        if (lastStep != null) {
            endStep(lastStep.getId(), null, null);
            lastStep = Optional.of(lastStep.getParentStep()).orElse(null);
        }
    }

    public String passLastStep() {
        return null;
    }

    public void passStep(final String stepId) {
        passStep(stepId, null);
    }

    public void passStep(final String stepId, Map<String, Number> stats) {
        final StepResult step = endStep(stepId, TestStatus.PASSED, null);
        if (stats != null)
            step.setStats(stats);
    }

    public void failStep(final String name, Throwable throwable) {
        failStep(name, null, throwable);
    }
    public void failStep(final String stepId, Map<String, Number> stats, Throwable throwable) {
        endStep(stepId, TestStatus.FAILED, throwable);
    }

    public StepResult endStep(final String stepId, TestStatus status, Throwable throwable) {
        if (lastStep == null || stepId == null)
            return null;
        LinkedList<StepResult> stepStack = new LinkedList<>();
        StepResult currentStep = lastStep;
        boolean stepFound = false;
        while (currentStep != null) {
            stepStack.push(currentStep);
            if (currentStep.getId() != null && currentStep.getId().equals(stepId)) {
                stepFound = true;
                break;
            }
            currentStep = currentStep.getParentStep();
        }
        if (!stepFound)
            return null;
        final StepResult endedStep = stepStack.pop();
        endedStep.end(status, throwable);
        // make sure to end all children/parent steps, if they remain open
        stepStack.stream().forEach((step) -> {
            if (step.getEndTime() == 0)
                step.end(status, throwable);
        });
        return endedStep;
    }

    public void step(final String name, Runnable stepFunc) {
        final String stepId = startStep(name);
        try {
            stepFunc.run();
            endStep(stepId, null, null);
        }
        catch (Throwable e) {
            failStep(stepId, e);
        }
    }

    public WebDriverWrapper getWebDriverWrapper() {
        return new WebDriverWrapper(this);
    }

    /*
    private void loadConfig() {
        String payloadpath = System.getProperty("payloadpath");;
        String testmonitorUrl = System.getProperty("testmonitorurl");
        testMonitorToken = System.getProperty("testmonitortoken");

        try {
            if (payloadpath != null && testmonitorUrl != null && testMonitorToken != null) {
                testMonitorStatusUrl = testmonitorUrl + "/status";
                payload = CbConfig.load(payloadpath);
            }
            else {
                logInfo("Plugin will be disabled. One of payloadpath, testmonitorurl, or testmonitortoken parameters is missing.");
            }
        }
        catch (IOException e) {
            logError("Unable to load CloudBeat configuration settings.", e);
            //LOGGER.error("Unable to load CloudBeat configuration settings.", e);
        }
        // TODO: make sure we throw an exception or handle in some other way the situation
        // where no configuration parameters where provided (e.g. when user runs test outside CB environment)
    }*/

    private void logError(String message) {
        System.err.println("[CloudBeat] " + message);
    }

    private void logError(String message, Exception e) {
        System.err.println("[CloudBeat] " + message);
        e.printStackTrace();
    }

    private void logInfo(String message) {
        System.out.println("[CloudBeat] " + message);
    }

    private void addSystemAttributes() {
        result.addAttribute("agent.hostname", getHostName());
        result.addAttribute("agent.java.name", SystemUtils.JAVA_RUNTIME_NAME);
        result.addAttribute("agent.java.version", SystemUtils.JAVA_VERSION);
        result.addAttribute("agent.os.name", SystemUtils.OS_NAME);
        result.addAttribute("agent.os.version", SystemUtils.OS_VERSION);
        result.addAttribute("agent.user.name", SystemUtils.USER_NAME);
        result.addAttribute("agent.user.home", SystemUtils.USER_HOME);
        result.addAttribute("agent.user.timezone", SystemUtils.USER_TIMEZONE);
    }
    private static String getHostName() {
        try {
            return Optional.ofNullable(SystemUtils.getHostName()).orElse(InetAddress.getLocalHost().getHostName());
        }
        catch (UnknownHostException e) {
            return null;
        }
    }

    public StepResult getLastStep() {
        return lastStep;
    }
}
