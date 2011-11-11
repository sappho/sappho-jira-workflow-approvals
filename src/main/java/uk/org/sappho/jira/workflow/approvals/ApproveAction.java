package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.opensymphony.workflow.WorkflowException;

public class ApproveAction extends DecideAction {

    private static final Logger log = Logger.getLogger(ApproveAction.class);

    @SuppressWarnings("rawtypes")
    @Override
    protected String bumpWorkflow(Issue approvalIssue, Map params) throws WorkflowException {

        PluginConfiguration pluginConfiguration = PluginConfiguration.getInstance();
        Issue parentIssue = approvalIssue.getParentObject();
        String project = parentIssue.getProjectObject().getKey();
        String approvalType = (String) params.get(ApprovalTypeFactory.approvalTypeKey);
        boolean isApproved = true;
        for (Issue subTask : parentIssue.getSubTaskObjects())
            if (pluginConfiguration.isIssueType(project, approvalType, subTask.getIssueTypeObject().getName())
                    && pluginConfiguration.isNotApproved(project, subTask)) {
                isApproved = false;
                break;
            }
        if (isApproved) {
            WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration(project, approvalType, parentIssue);
            int transitionActionId = workflowConfiguration.getTransitionActionId();
            log.warn("Running workflow transition action id " + transitionActionId + " on " + parentIssue.getKey());
            boolean wasIndexing = ImportUtils.isIndexIssues();
            ImportUtils.setIndexIssues(true);
            WorkflowTransitionUtil workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
            workflowTransitionUtil.setIssue((MutableIssue) parentIssue);
            workflowTransitionUtil.setUsername(
                    ComponentManager.getInstance().getJiraAuthenticationContext().getUser().getName());
            workflowTransitionUtil.setAction(transitionActionId);
            ErrorCollection errors = workflowTransitionUtil.validate();
            if (errors.hasAnyErrors())
                throw new WorkflowException("Unable to validate transition " + parentIssue.getKey() + "! Caused by "
                        + errors);
            errors = workflowTransitionUtil.progress();
            ImportUtils.setIndexIssues(wasIndexing);
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
