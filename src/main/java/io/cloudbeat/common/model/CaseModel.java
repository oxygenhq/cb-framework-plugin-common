package io.cloudbeat.common.model;

import io.cloudbeat.common.reporter.model.LogEntryResult;
import org.openqa.selenium.logging.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CaseModel extends TestResultEntityWithId {
    public int iterationNum;
    public ArrayList<LogEntryResult> logs;
    public Map<String, Object> сontext;
    public Map<String, String> har;
    public ArrayList<StepModel> steps;
    public short order;

    public void setLogs(List<LogEntry> logs) {
        logs = new ArrayList();

        //logs.forEach(x -> new LogEntryResult(x, "browser"));
    }
}
