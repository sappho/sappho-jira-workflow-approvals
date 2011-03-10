package uk.org.sappho.jira.workflow.approvals;

public class DeclineAction extends DecideAction {

    @Override
    protected String getAction() {
        return "have declined";
    }
}
