package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

abstract public class SingleStringFactory extends AbstractWorkflowPluginFactory
        implements WorkflowPluginFunctionFactory, WorkflowPluginConditionFactory {

    private final String key;

    public SingleStringFactory(String key) {

        this.key = key;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor) {

        velocityParams.put(key, getValue(descriptor));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void getVelocityParamsForInput(Map velocityParams) {

        velocityParams.put(key, PluginConfiguration.undefined);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor) {

        velocityParams.put(key, getValue(descriptor));
    }

    @SuppressWarnings("unchecked")
    public Map getDescriptorParams(Map<String, Object> conditionParams) {

        String param = PluginConfiguration.undefined;
        if (conditionParams != null && conditionParams.containsKey(key))
            param = extractSingleParam(conditionParams, key);
        return EasyMap.build(key, param);
    }

    private String getValue(AbstractDescriptor descriptor) {

        String value = (String) ((FunctionDescriptor) descriptor).getArgs().get(key);
        if (value == null)
            value = PluginConfiguration.undefined;
        value = value.trim();
        if (value.length() < 1)
            value = PluginConfiguration.undefined;
        return value;
    }

    @SuppressWarnings("unchecked")
    public String getValue(Map params) {

        return (String) params.get(key);
    }
}
