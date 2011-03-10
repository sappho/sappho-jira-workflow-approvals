package uk.org.sappho.jira.workflow.approvals;

public class ReconsiderAction extends DecideAction {

    @Override
    protected String getAction() {
        return "am reconsidering";
    }
}
