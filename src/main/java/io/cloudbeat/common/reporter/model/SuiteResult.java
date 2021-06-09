package io.cloudbeat.common.reporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.cloudbeat.common.reporter.serializer.EpochTimeSerializer;
import io.cloudbeat.common.reporter.serializer.TestStatusSerializer;

import java.util.*;

public class SuiteResult {
    String id;
    String name;
    @JsonSerialize(using = EpochTimeSerializer.class)
    long startTime;
    @JsonSerialize(using = EpochTimeSerializer.class)
    long endTime;
    long duration;
    String fqn;
    @JsonSerialize(using = TestStatusSerializer.class)
    TestStatus status;
    ArrayList<String> args;
    ArrayList<CaseResult> cases = new ArrayList<>();

    public SuiteResult(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.startTime = Calendar.getInstance().getTimeInMillis();
    }

    public void end() {
        this.endTime = Calendar.getInstance().getTimeInMillis();
        this.duration = endTime - startTime;
        // determine if at least one of the cases has failed
        boolean hasFailedCase = cases.stream().anyMatch(x -> x.status == TestStatus.FAILED);
        // set suite's status to Failed if at least one case has failed
        this.status = hasFailedCase ? TestStatus.FAILED : TestStatus.PASSED;
    }

    public CaseResult addNewCaseResult(String name) {
        CaseResult newCase = new CaseResult(name);
        cases.add(newCase);
        return newCase;
    }

    public Optional<CaseResult> lastCase(String fqn) {
        for (int i = cases.size(); i-- > 0; ) {
            final CaseResult caseResult = cases.get(i);
            final String caseFqn = caseResult.getFqn();
            if (caseFqn != null && caseFqn.equals(fqn))
                return Optional.of(caseResult);
        }
        return Optional.empty();

    }
    /* Setters */
    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    /* Getters */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getDuration() { return duration; }

    public String getFqn() {
        return fqn;
    }

    public TestStatus getStatus() {
        return status;
    }

    public List<String> getArgs() {
        return args;
    }

    public List<CaseResult> getCases() {
        return cases;
    }
}
