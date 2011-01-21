package uk.org.sappho.jira.workflow.approvals;

public class ReconsiderAction extends ApprovalAction {

    protected String getAction() {
        return "is reconsidering";
    }

    protected String getFlag() {
        return "waiting";
    }
}
