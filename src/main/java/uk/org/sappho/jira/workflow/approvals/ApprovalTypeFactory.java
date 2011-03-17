package uk.org.sappho.jira.workflow.approvals;

@SuppressWarnings("unchecked")
public class ApprovalTypeFactory extends SingleStringFactory {

    public static final String approvalTypeKey = "approvalType";

    public ApprovalTypeFactory() {

        super(approvalTypeKey);
    }
}
