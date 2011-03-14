package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

public class ApprovalCondition implements Condition {

    protected static final Logger log = Logger.getLogger(ApprovalCondition.class);

    @SuppressWarnings("unchecked")
    public boolean passesCondition(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        MutableIssue approvalIssue = (MutableIssue) transientVars.get("issue");
        MutableIssue parentIssue = (MutableIssue) approvalIssue.getParentObject();
        String project = parentIssue.getProjectObject().getKey();
        String approvalType = (String) params.get(ApprovalTypeFactory.approvalTypeKey);
        WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration(project, approvalType, parentIssue);
        String status = parentIssue.getStatusObject().getName();
        boolean passes = workflowConfiguration.getRequiredStatus().equals(status);
        log.warn("Status of " + parentIssue.getKey() + " is " + status + " - approval buttons will be "
                + (passes ? "visible" : "hidden"));
        return passes;
    }
}
