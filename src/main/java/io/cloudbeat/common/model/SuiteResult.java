package io.cloudbeat.common.model;

import org.aspectj.weaver.ast.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class SuiteResult {
    String id;
    String name;
    long startTime;
    long endTime;
    Optional<String> fqn;
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
        // determine if at least one of the cases has failed
        boolean hasFailedCase = cases.stream().anyMatch(x -> x.status == TestStatus.FAILED);
        // set suite's status to Failed if at least one case has failed
        this.status = hasFailedCase ? TestStatus.FAILED : TestStatus.PASSED;
    }

    public void setFqn(String fqn) {
        this.fqn = Optional.of(fqn);
    }

    public Optional<String> getFqn() {
        return fqn;
    }

    public CaseResult addNewCaseResult(String name) {
        CaseResult newCase = new CaseResult(name);
        cases.add(newCase);
        return newCase;
    }

    public Optional<CaseResult> getLastCase(String fqn) {
        for (int i = cases.size(); i-- > 0; ) {
            final CaseResult caseResult = cases.get(i);
            final Optional<String> caseFqn = caseResult.getFqn();
            if (caseFqn.isPresent() && caseFqn.get().equals(fqn))
                return Optional.of(caseResult);
        }
        return Optional.empty();

    }
}
