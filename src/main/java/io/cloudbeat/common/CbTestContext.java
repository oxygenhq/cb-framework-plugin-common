package io.cloudbeat.common;

import io.cloudbeat.common.config.CbConfig;
import io.cloudbeat.common.reporter.CbTestReporter;
import io.cloudbeat.common.config.PropertiesLoader;

import java.util.Properties;
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

    private final CbTestReporter reporter;
    private final CbConfig config;

    public CbTestContext() {
        config = new CbConfig(PropertiesLoader.load());
        reporter = new CbTestReporter(config);
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

    public CbTestReporter getReporter() {
        return this.reporter;
    }

    public CbConfig getConfig() { return config; }
}
