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
    protected void bumpWorkflow(MutableIssue approvalIssue, Map params) throws WorkflowException {

        ApprovalsConfiguration approvalsConfiguration = null;
        try {
            approvalsConfiguration = ApprovalsConfiguration.getInstance();
        } catch (Throwable e) {
            throw new WorkflowException("Unable to get plugin configuration!", e);
        }
        String approvedStatus = approvalsConfiguration.getProperty("approvals.status.approved",
                ApprovalsConfiguration.undefined);
        String approvalType = (String) params.get(ApprovalTypeFactory.approvalTypeKey);
        MutableIssue parentIssue = (MutableIssue) approvalIssue.getParentObject();
        boolean isApproved = true;
        for (MutableIssue subTask : parentIssue.getSubTaskObjects())
            if (approvalsConfiguration.isApprovalIssueType(subTask.getIssueTypeObject().getName(), approvalType)
                    && !subTask.getStatusObject().getName().equals(approvedStatus)) {
                isApproved = false;
                break;
            }
        if (isApproved) {
            int transitionActionId;
            try {
                transitionActionId = Integer.parseInt(approvalsConfiguration.getProperty(
                        "approvals.auto.transition.action." + approvalType, "-999"));
            } catch (Throwable t) {
                throw new WorkflowException("Unable to get auto-transition configuration!", t);
            }
            log.warn("Running workflow transition action id " + transitionActionId + " on " + parentIssue.getKey());
            WorkflowTransitionUtil workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
            workflowTransitionUtil.setIssue(parentIssue);
            workflowTransitionUtil.setUsername(ComponentManager.getInstance().getJiraAuthenticationContext().getUser()
                    .getName());
            workflowTransitionUtil.setAction(transitionActionId);
            ErrorCollection errors = workflowTransitionUtil.validate();
            if (errors.hasAnyErrors())
                throw new WorkflowException("Unable to transition " + parentIssue.getKey() + "! Caused by " + errors);
            errors = workflowTransitionUtil.progress();
            if (errors.hasAnyErrors())
                throw new WorkflowException("Unable to transition " + parentIssue.getKey() + "! Caused by " + errors);
        }
    }

    @Override
    protected String getAction() {
        return "have granted";
    }
}
