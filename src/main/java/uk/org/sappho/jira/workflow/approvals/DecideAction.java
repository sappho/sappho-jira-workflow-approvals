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
        bumpWorkflow(approvalIssue, params);
        ComponentManager componentManager = ComponentManager.getInstance();
        componentManager.getCommentManager().create(
                approvalIssue.getParentObject(),
                componentManager.getJiraAuthenticationContext().getUser().getName(),
                "I " + getAction() + " " + approvalIssue.getIssueTypeObject().getName() + " approval. See "
                        + approvalIssue.getKey() + ".", true);
    }

    @SuppressWarnings("unchecked")
    protected void bumpWorkflow(@SuppressWarnings("unused") MutableIssue approvalIssue,
            @SuppressWarnings("unused") Map params)
            throws WorkflowException {

        // do nothing unless this is an ApproveAction
    }

    abstract protected String getAction();
}
