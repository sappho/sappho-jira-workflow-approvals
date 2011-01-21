package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;
import java.util.regex.Pattern;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

public class ApprovalCondition extends Approval implements Condition {

    private final static Pattern statusRegex = Pattern
            .compile("^Awaiting (Technical Approval|Management Approval|Action)$");

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

        boolean passes = false;
        if (isApprovalIssue(transientVars)) {
            String status = issue.getStatusObject().getName();
            passes = statusRegex.matcher(status).matches();
        }
        return passes;
    }
}
