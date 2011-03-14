package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.opensymphony.workflow.WorkflowException;

public class ApproveAction extends DecideAction {

    private static final Logger log = Logger.getLogger(ApproveAction.class);

    @SuppressWarnings("unchecked")
    @Override
    protected String bumpWorkflow(MutableIssue approvalIssue, Map params) throws WorkflowException {

        ApprovalsConfiguration approvalsConfiguration = ApprovalsConfiguration.getInstance();
        MutableIssue parentIssue = (MutableIssue) approvalIssue.getParentObject();
        String project = parentIssue.getProjectObject().getKey();
        String approvalType = (String) params.get(ApprovalTypeFactory.approvalTypeKey);
        boolean isApproved = true;
        for (MutableIssue subTask : parentIssue.getSubTaskObjects())
            if (approvalsConfiguration.isIssueType(project, approvalType, subTask.getIssueTypeObject().getName())
                    && !approvalsConfiguration.isRegexMatch(project, "statuses.approved", subTask.getStatusObject()
                            .getName())) {
                isApproved = false;
                break;
            }
        if (isApproved) {
            WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration(project, approvalType, parentIssue);
            int transitionActionId = workflowConfiguration.getTransitionActionId();
            log.warn("Running workflow transition action id " + transitionActionId + " on " + parentIssue.getKey());
            WorkflowTransitionUtil workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
            workflowTransitionUtil.setIssue(parentIssue);
            workflowTransitionUtil.setUsername(
                    ComponentManager.getInstance().getJiraAuthenticationContext().getUser().getName());
            workflowTransitionUtil.setAction(transitionActionId);
            ErrorCollection errors = workflowTransitionUtil.validate();
            if (errors.hasAnyErrors())
                throw new WorkflowException("Unable to validate transition " + parentIssue.getKey() + "! Caused by "
                        + errors);
            errors = workflowTransitionUtil.progress();
            if (errors.hasAnyErrors())
                throw new WorkflowException("Unable to progress transition " + parentIssue.getKey() + "! Caused by "
                        + errors);
            return "\nAll " + approvalType
                    + " approvals now granted so this issue has been auto-transitioned to "
                    + parentIssue.getStatusObject().getName().toLowerCase() + ".";
        } else
            return "";
    }

    @Override
    protected String getAction() {
        return "have granted";
    }
}
