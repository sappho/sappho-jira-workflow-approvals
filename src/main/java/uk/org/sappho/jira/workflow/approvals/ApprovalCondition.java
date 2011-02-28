package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

public class ApprovalCondition extends Approval implements Condition {

    protected final static Pattern statusRegex = Pattern.compile("^(Submitted|Awaiting.+Approval)$");
    protected static final Logger log = Logger.getLogger(ApprovalCondition.class);

    @SuppressWarnings("unchecked")
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

        boolean passes = false;
        if (isLiveApprovalIssue(transientVars)) {
            Issue parentIssue = issue.getParentObject();
            if (parentIssue != null) {
                String parentStatus = parentIssue.getStatusObject().getName();
                passes = statusRegex.matcher(parentStatus).matches();
                log.warn(parentIssue.getKey() + " has status \"" + parentStatus + "\" - approval okay? = " + passes);
            }
        }
        return passes;
    }
}
