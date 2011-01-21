package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.issue.MutableIssue;

abstract public class Approval {

    protected final static Pattern typeRegex = Pattern.compile("^Approval : ([a-zA-Z]+?) : ([a-zA-Z]+?)$");
    protected MutableIssue issue;
    protected Matcher typeMatcher;

    protected boolean isApprovalIssue(Map<String, MutableIssue> transientVars) {

        issue = transientVars.get("issue");
        String issueType = issue.getIssueTypeObject().getName();
        typeMatcher = typeRegex.matcher(issueType);
        return typeMatcher.matches();
    }
}
