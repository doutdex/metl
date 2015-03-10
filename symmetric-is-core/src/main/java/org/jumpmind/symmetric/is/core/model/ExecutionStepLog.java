package org.jumpmind.symmetric.is.core.model;

import java.util.Date;

public class ExecutionStepLog extends AbstractObject {

    private static final long serialVersionUID = 1L;

    private String executionStepId;
    
    private String category;
    
    private String level;
    
    private String logText;
    
    private Date createTime;
	
    @Override
    public void setName(String name) {
    }

    @Override
    public String getName() {
        return id;
    }

	public String getExecutionStepId() {
		return executionStepId;
	}

	public void setExecutionStepId(String executionStepId) {
		this.executionStepId = executionStepId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLogText() {
		return logText;
	}

	public void setLogText(String logText) {
		this.logText = logText;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}