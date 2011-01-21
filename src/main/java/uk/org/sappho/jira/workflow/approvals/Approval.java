package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

abstract public class Approval implements FunctionProvider {

    private final ComponentManager componentManager;
    private final CustomFieldManager customFieldManager;
    private final CommentManager commentManager;
    private final static Pattern typeRegex = Pattern.compile("^Approval : ([a-zA-Z]+?) : ([a-zA-Z]+?)$");

    public Approval() {

        componentManager = ComponentManager.getInstance();
        customFieldManager = componentManager.getCustomFieldManager();
        commentManager = componentManager.getCommentManager();
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        String issueType = issue.getIssueTypeObject().getName();
        Matcher matcher = typeRegex.matcher(issueType);
        if (matcher.matches()) {
            String grade = matcher.group(1);
            String kind = matcher.group(2);
            String issueKey = issue.getKey();
            MutableIssue parentIssue = (MutableIssue) issue.getParentObject();
            User user = componentManager.getJiraAuthenticationContext().getUser();
            String userName = user.getName();
            String userFullName = user.getFullName();
            commentManager.create(parentIssue, userFullName + " " + getAction() + " " + grade + " " + kind
                    + " approval. See " + issueKey + ".", null, false);
            String type = grade + " : " + kind;
            Object flag = customFieldManager.getCustomFieldObjectByName("Approval : " + type);
            Object approver = customFieldManager.getCustomFieldObjectByName("Approver : " + type);
            if (flag != null && approver != null) {
                parentIssue.setCustomFieldValue((CustomField) flag, getFlag());
                parentIssue.setCustomFieldValue((CustomField) approver, userName);
            }
        }
    }

    abstract protected String getAction();

    abstract protected String getFlag();
}
