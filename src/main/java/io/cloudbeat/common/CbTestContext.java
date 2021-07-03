package io.cloudbeat.common;

import io.cloudbeat.common.config.CbConfig;
import io.cloudbeat.common.config.CbConfigLoader;
import io.cloudbeat.common.reporter.CbTestReporter;
import io.cloudbeat.common.config.PropertiesConfigLoader;

import java.io.IOException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class CbTestContext {
    /**
     * Thread local context that stores information about not finished tests and steps.
     */
    private static class ThreadContext extends InheritableThreadLocal<CbTestContext> {
        @Override
        public CbTestContext initialValue() {
            return new CbTestContext();
        }

        @Override
        protected CbTestContext childValue(final CbTestContext parentStepContext) {
            return parentStepContext;
        }
    }
    //private static final Logger LOGGER = LoggerFactory.getLogger(CbTestContext.class);
    private static final ThreadContext CURRENT_CONTEXT = new ThreadContext();

            //InheritableThreadLocal.withInitial(() -> new CbTestContext());
    /*new ThreadLocal<CbTestContext>() {
        @Override
        protected CbTestContext initialValue() {
            return new CbTestContext();
        }
    };*/
            //new InheritableThreadLocal<>();

    private CbTestReporter reporter;
    private CbConfig config;
    private boolean isActive;
    private Class currentTestClass;

    public CbTestContext() {
        isActive = false;
        try {
            config = CbConfigLoader.load();
            if (config != null) {
                reporter = new CbTestReporter(config);
                isActive = true;
            }
        }
        catch (IOException e) {
            System.err.println("Failed to initialize CbTestContext: " + e.toString());
        }
        CURRENT_CONTEXT.set(this);
    }
    /**
     * Returns a current test context linked to the current thread.
     *
     * @return test context instance
     */
    public static CbTestContext getInstance() {
        if (CURRENT_CONTEXT.get() == null)
            return new CbTestContext();
        return CURRENT_CONTEXT.get();
    }

    public void setCurrentTestClass(Class testClass) { this.currentTestClass = testClass; }

    public CbTestReporter getReporter() {
        return this.reporter;
    }

    public CbConfig getConfig() { return config; }

    public Class getCurrentTestClass() { return currentTestClass; }

    public boolean isActive() { return isActive; }
}
