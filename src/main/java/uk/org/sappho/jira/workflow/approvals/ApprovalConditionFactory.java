package uk.org.sappho.jira.workflow.approvals;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;

public class ApprovalConditionFactory implements WorkflowPluginConditionFactory {

    public Map getDescriptorParams(Map arg0) {
        return new HashMap();
    }

    public Map getVelocityParams(String arg0, AbstractDescriptor arg1) {
        return new HashMap();
    }
}
