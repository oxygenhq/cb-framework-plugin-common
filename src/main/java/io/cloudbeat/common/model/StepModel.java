package io.cloudbeat.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.cloudbeat.common.reporter.model.FailureResult;
import net.lightbody.bmp.core.har.Har;

import java.util.ArrayList;
import java.util.Dictionary;

@JsonIgnoreProperties(value = { "isFinished", "parent"})
public class StepModel extends TestResultBase {
    public String location;
    public Dictionary<String, String> stats;
    public FailureResult failure;
    public String screenShot;
    public StepModel parent;
    public ArrayList<StepModel> steps;
    public boolean isFinished;
    public String pageRef;
    public long loadEvent;
    public Har hars;
    public long domContentLoadedEvent;
}
