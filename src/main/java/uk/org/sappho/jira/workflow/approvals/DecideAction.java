package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

abstract public class DecideAction implements FunctionProvider {

    @SuppressWarnings("rawtypes")
    public void execute(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        MutableIssue approvalIssue = (MutableIssue) transientVars.get("issue");
        String message = bumpWorkflow(approvalIssue, params);
        ComponentManager componentManager = ComponentManager.getInstance();
        componentManager
                .getCommentManager()
                .create(
                        approvalIssue.getParentObject(),
                        componentManager.getJiraAuthenticationContext().getUser().getName(),
                        "I " + getAction() + " _" + approvalIssue.getIssueTypeObject().getName().toLowerCase()
                                + "_ using approval subtask " + approvalIssue.getKey() + "." + message, true);
    }

    @SuppressWarnings("rawtypes")
    protected String bumpWorkflow(MutableIssue approvalIssue, Map params) throws WorkflowException {

        // do nothing unless this is an ApproveAction
        return "";
    }

    abstract protected String getAction();
}
