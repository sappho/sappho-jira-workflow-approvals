package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

abstract public class CommentToParentAction implements FunctionProvider {

    @SuppressWarnings("rawtypes")
    public void execute(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        MutableIssue subTask = (MutableIssue) transientVars.get("issue");
        Issue parentIssue = subTask.getParentObject();
        if (parentIssue != null) {
            ComponentManager componentManager = ComponentManager.getInstance();
            String commentSourceFieldName = (String) params.get(CommentSourceFieldNameFactory.commentSourceFieldName);
            CustomField commentSourceField = componentManager.getCustomFieldManager().getCustomFieldObjectByName(
                    commentSourceFieldName);
            if (commentSourceField != null) {
                componentManager.getCommentManager().create(parentIssue,
                        componentManager.getJiraAuthenticationContext().getUser().getName(),
                        (String) subTask.getCustomFieldValue(commentSourceField), true);
            }
        }
    }
}
