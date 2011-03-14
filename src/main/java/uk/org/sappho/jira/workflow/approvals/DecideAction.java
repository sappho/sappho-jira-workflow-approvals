package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

abstract public class DecideAction implements FunctionProvider {

    @SuppressWarnings("unchecked")
    public void execute(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        MutableIssue approvalIssue = (MutableIssue) transientVars.get("issue");
        String approvalType = bumpWorkflow(approvalIssue, params);
        ComponentManager componentManager = ComponentManager.getInstance();
        componentManager
                .getCommentManager()
                .create(
                        approvalIssue.getParentObject(),
                        componentManager.getJiraAuthenticationContext().getUser().getName(),
                        "I "
                                + getAction()
                                + " "
                                + approvalIssue.getIssueTypeObject().getName()
                                + " from sub-task "
                                + approvalIssue.getKey()
                                + "."
                                + (approvalType != null ? "\nAll "
                                        + approvalType
                                        + " approvals now granted so this issue has been auto-transitioned to the next workflow step"
                                        : ""), true);
    }

    @SuppressWarnings("unchecked")
    protected String bumpWorkflow(@SuppressWarnings("unused") MutableIssue approvalIssue,
            @SuppressWarnings("unused") Map params) throws WorkflowException {

        // do nothing unless this is an ApproveAction
        return null;
    }

    abstract protected String getAction();
}
