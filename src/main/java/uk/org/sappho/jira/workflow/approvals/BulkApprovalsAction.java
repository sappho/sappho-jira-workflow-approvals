package uk.org.sappho.jira.workflow.approvals;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public abstract class BulkApprovalsAction extends AbstractJiraFunctionProvider {

    private static final Logger log = Logger.getLogger(BulkApprovalsAction.class);

    public void transitionIssue(Issue subTask, int transitionActionId, User user) throws WorkflowException {

        log.warn("Running workflow transition action id " + transitionActionId + " on " + subTask.getKey());
        boolean wasIndexing = ImportUtils.isIndexIssues();
        try {
            ImportUtils.setIndexIssues(true);
            WorkflowTransitionUtil workflowTransitionUtil = JiraUtils
                    .loadComponent(WorkflowTransitionUtilImpl.class);
            workflowTransitionUtil.setIssue((MutableIssue) subTask);
            workflowTransitionUtil.setUsername(user.getName());
            workflowTransitionUtil.setAction(transitionActionId);
            ErrorCollection errors = workflowTransitionUtil.validate();
            if (errors.hasAnyErrors())
                throw new WorkflowException("Unable to validate transition " + subTask.getKey()
                        + "! Caused by "
                        + errors);
            errors = workflowTransitionUtil.progress();
            if (errors.hasAnyErrors())
                throw new WorkflowException("Unable to progress transition " + subTask.getKey()
                        + "! Caused by "
                        + errors);
        } finally {
            ImportUtils.setIndexIssues(wasIndexing);
        }
    }

    public void transitionIssueWithAssignment(Issue subTask, int transitionActionId, User user)
            throws WorkflowException {

        User assignee = subTask.getAssignee();
        ((MutableIssue) subTask).setAssignee(user);
        subTask.store();
        transitionIssue(subTask, transitionActionId, user);
        ((MutableIssue) subTask).setAssignee(assignee);
        subTask.store();
    }
}
