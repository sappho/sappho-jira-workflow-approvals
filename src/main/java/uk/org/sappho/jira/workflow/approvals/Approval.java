package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.MutableIssue;

abstract public class Approval {

    protected final static Pattern typeRegex = Pattern.compile("^Approval : ([a-zA-Z]+) : ([a-zA-Z]+)$");
    protected MutableIssue issue;
    protected Matcher typeMatcher;
    protected static final Logger log = Logger.getLogger(Approval.class);

    protected boolean isLiveApprovalIssue(Map<String, MutableIssue> transientVars) {

        issue = transientVars.get("issue");
        String issueType = issue.getIssueTypeObject().getName();
        typeMatcher = typeRegex.matcher(issueType);
        boolean isLiveApprovalIssue = typeMatcher.matches();
        log.warn(issue.getKey() + " has type \"" + issueType + "\" - is live approvals issue? = "
                        + isLiveApprovalIssue);
        return isLiveApprovalIssue;
    }
}
