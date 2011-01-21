package uk.org.sappho.jira.workflow.approvals;

public class DeclineAction extends ApprovalAction {

    protected String getAction() {
        return "declined";
    }

    protected String getFlag() {
        return "declined";
    }
}
