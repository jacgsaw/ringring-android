package org.linphone.core;

// import org.codehaus.jackson.annotate.JsonIgnoreProperties;

// @JsonIgnoreProperties(ignoreUnknown=true)
public class UserResult {
    private User user;
    private Status status;

    public UserResult() {
    }

    public UserResult(User user, Status status) {
        this.user = user;
        this.status = status;
    }

    public User getUser() {
        return this.user;
    }

    public Status getStatus() {
        return this.status;
    }

    public boolean isSuccess() {
        return Status.OKAY.equals(this.getStatus());
    }

    @Override
    public String toString() {
        String statusString =  "Status {" + '\n'
                + " status=" + this.status + '\n'
                + ",isSuccess=" + this.isSuccess() + '\n'
                + "}";

        String userString = null;
        if(getUser() != null) {
            userString = getUser().toString();
        }

        return statusString + '\n'
                + "User {" + '\n'
                + userString + '\n'
                + "}";
    }
}