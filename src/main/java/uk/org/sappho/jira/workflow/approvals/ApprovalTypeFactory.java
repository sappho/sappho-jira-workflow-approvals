package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

public class ApprovalTypeFactory extends AbstractWorkflowPluginFactory
        implements WorkflowPluginFunctionFactory, WorkflowPluginConditionFactory {

    public static final String approvalTypeKey = "approvalType";

    @SuppressWarnings("unchecked")
    @Override
    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor) {

        velocityParams.put(approvalTypeKey, getApprovalType(descriptor));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void getVelocityParamsForInput(Map velocityParams) {

        velocityParams.put(approvalTypeKey, ApprovalsConfiguration.undefined);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor) {

        velocityParams.put(approvalTypeKey, getApprovalType(descriptor));
    }

    @SuppressWarnings("unchecked")
    public Map getDescriptorParams(Map<String, Object> conditionParams) {

        String param = ApprovalsConfiguration.undefined;
        if (conditionParams != null && conditionParams.containsKey(approvalTypeKey))
            param = extractSingleParam(conditionParams, approvalTypeKey);
        return EasyMap.build(approvalTypeKey, param);
    }

    private String getApprovalType(AbstractDescriptor descriptor) {

        String approvalType = (String) ((FunctionDescriptor) descriptor).getArgs().get(approvalTypeKey);
        if (approvalType == null)
            approvalType = ApprovalsConfiguration.undefined;
        approvalType = approvalType.trim();
        if (approvalType.length() < 1)
            approvalType = ApprovalsConfiguration.undefined;
        return approvalType;
    }
}
