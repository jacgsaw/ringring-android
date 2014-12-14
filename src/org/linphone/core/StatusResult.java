package org.linphone.core;

// import org.codehaus.jackson.annotate.JsonIgnoreProperties;

// @JsonIgnoreProperties(ignoreUnknown=true)
public class StatusResult {
    private Status status;

    public StatusResult() {
    }

    public StatusResult(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public boolean isSuccess() {
        return Status.OKAY.equals(this.getStatus());
    }

    @Override
    public String toString() {
        return  "Status {" + '\n'
                + " status=" + this.status + '\n'
                + ",isSuccess=" + this.isSuccess() + '\n'
                + "}";
    }
}