package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

public class ServiceAndTypeValidCondition implements Condition {

    @SuppressWarnings("unchecked")
    public boolean passesCondition(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        ApprovalsKey approvalsKey = new ServiceTypeRegion();
        try {
            approvalsKey.init((Issue) transientVars.get("issue"));
        } catch (Throwable t) {
            return false;
        }
        return true;
    }
}
