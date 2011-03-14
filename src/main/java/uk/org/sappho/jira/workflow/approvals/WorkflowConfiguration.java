package uk.org.sappho.jira.workflow.approvals;

import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.workflow.WorkflowException;

public class WorkflowConfiguration {

    private int transitionActionId;
    private String requiredStatus;
    private static final Logger log = Logger.getLogger(WorkflowConfiguration.class);

    public WorkflowConfiguration(String project, String approvalType, MutableIssue parentIssue)
            throws WorkflowException {

        ApprovalsConfiguration approvalsConfiguration = ApprovalsConfiguration.getInstance();
        List<String> workflowNames =
                approvalsConfiguration.getPropertyList(project, "parent.issue.workflow.name." + approvalType);
        List<String> workflowActions =
                approvalsConfiguration.getPropertyList(project, "parent.issue.workflow.auto.transition.action.id."
                        + approvalType);
        List<String> workflowStatuses =
                approvalsConfiguration.getPropertyList(project, "parent.issue.workflow.status." + approvalType);
        String issueWorkflowName =
                ComponentManager.getInstance().getWorkflowManager().getWorkflow(parentIssue).getName();
        log.warn("Found workflow " + issueWorkflowName + " on " + parentIssue.getKey());
        int index = 0;
        boolean workflowFound = false;
        for (String workflowName : workflowNames) {
            if (workflowName.equals(issueWorkflowName)) {
                try {
                    transitionActionId = Integer.parseInt(workflowActions.get(index));
                } catch (Throwable t) {
                    throw new WorkflowException("Auto-transition action ID for workflow " + issueWorkflowName
                            + " and approval type " + approvalType + " is incorrect!", t);
                }
                try {
                    requiredStatus = workflowStatuses.get(index);
                } catch (Throwable t) {
                    throw new WorkflowException("Auto-transition action ID for workflow " + issueWorkflowName
                            + " and approval type " + approvalType + " is incorrect!", t);
                }
                workflowFound = true;
                break;
            }
            index++;
        }
        if (!workflowFound)
            throw new WorkflowException("Configuration for workflow " + issueWorkflowName + " and approval type "
                    + approvalType + " is incorrect!");
    }

    public int getTransitionActionId() {

        return transitionActionId;
    }

    public String getRequiredStatus() {

        return requiredStatus;
    }
}