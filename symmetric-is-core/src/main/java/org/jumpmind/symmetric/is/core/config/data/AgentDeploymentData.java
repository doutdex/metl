package org.jumpmind.symmetric.is.core.config.data;

public class AgentDeploymentData extends AbstractData {

    private static final long serialVersionUID = 1L;

    String agentId;

    String componentFlowVersionId;

    String startMode;

    String status;

    String message;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getComponentFlowVersionId() {
        return componentFlowVersionId;
    }

    public void setComponentFlowVersionId(String componentFlowVersionId) {
        this.componentFlowVersionId = componentFlowVersionId;
    }

    public String getStartMode() {
        return startMode;
    }

    public void setStartMode(String startMode) {
        this.startMode = startMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String errorMessage) {
        this.message = errorMessage;
    }

    public String getMessage() {
        return message;
    }

}
