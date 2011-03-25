package uk.org.sappho.jira.workflow.approvals;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
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

        // Find out what approvals are needed
        // TODO: Work out a way of making this bit pluggable - it's way too specific to the CRM project
        ApprovalsKey approvalsKey = new ServiceTypeRegion();
        approvalsKey.init(parentIssue);
        List<String> approvers = approvalsKey.getAllowedApprovers(approvalIssue.getIssueTypeObject().getName());

        String project = parentIssue.getProjectObject().getKey();
        String approvalType = (String) params.get(ApprovalTypeFactory.approvalTypeKey);
        WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration(project, approvalType, parentIssue);

        String status = parentIssue.getStatusObject().getName();
        String username = ComponentManager.getInstance().getJiraAuthenticationContext().getUser().getName();
        log.warn("Status of " + parentIssue.getKey() + " is " + status);
        log.warn("Logged in user is " + username);
        boolean passes = workflowConfiguration.getRequiredStatus().equals(status) && approvers.contains(username);
        log.warn("Approval buttons will be " + (passes ? "visible" : "hidden"));
        return passes;
    }
}
