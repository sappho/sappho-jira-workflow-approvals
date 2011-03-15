package uk.org.sappho.jira.workflow.approvals;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;

@SuppressWarnings("unchecked")
public class ParentCustomField extends CalculatedCFType {

    public Object getSingularObjectFromString(String customFieldObject) throws FieldValidationException {

        return customFieldObject;
    }

    public String getStringFromSingularObject(Object customFieldObject) {

        return customFieldObject.toString();
    }

    public Object getValueFromIssue(CustomField field, Issue issue) {

        return issue.isSubTask() ? issue.getParentObject().getKey() : issue.getKey();
    }
}
