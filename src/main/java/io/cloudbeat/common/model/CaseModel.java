package io.cloudbeat.common.model;

import net.lightbody.bmp.core.har.Har;
import org.openqa.selenium.logging.LogEntry;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

public class CaseModel extends TestResultEntityWithId {
    public int iterationNum;
    public ArrayList<LogResult> logs;
    public Map<String, Object> сontext;
    public Map<String, String> har;
    public ArrayList<StepModel> steps;
    public short order;

    public void setLogs(List<LogEntry> logs) {
        logs = new ArrayList();

        logs.forEach(x -> new LogResult(x, "browser"));
    }
}
