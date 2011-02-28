package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

abstract public class ApprovalAction extends Approval implements FunctionProvider {

    private final ComponentManager componentManager;
    private final CustomFieldManager customFieldManager;
    private final CommentManager commentManager;

    public ApprovalAction() {

        componentManager = ComponentManager.getInstance();
        customFieldManager = componentManager.getCustomFieldManager();
        commentManager = componentManager.getCommentManager();
    }

    @SuppressWarnings("unchecked")
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

        if (isLiveApprovalIssue(transientVars)) {
            String grade = typeMatcher.group(1);
            String kind = typeMatcher.group(2);
            String issueKey = issue.getKey();
            MutableIssue parentIssue = (MutableIssue) issue.getParentObject();
            User user = componentManager.getJiraAuthenticationContext().getUser();
            String userName = user.getName();
            String userFullName = user.getFullName();
            commentManager.create(parentIssue, userName, userFullName + " " + getAction() + " " + grade.toLowerCase()
                    + " "
                    + kind.toLowerCase() + " approval. See " + issueKey + ".", true);
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
