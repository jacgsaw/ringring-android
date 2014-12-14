package org.linphone.core;

// import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

// @JsonIgnoreProperties(ignoreUnknown=true)
public class UserListResult {
    private List<User> users;
    private Status status;

    public UserListResult() {
    }

    public UserListResult(List<User> users, Status status) {
        this.users = users;
        this.status = status;
    }

    public List<User> getUsers() {
        return this.users;
    }
    public void setUserList(List<User> users) {
        this.users = users;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

        String userListString = null;
        for (User user : getUsers()) {
            if (userListString == null) {
                userListString = user.toString();
            }
            else {
                userListString = userListString + user.toString();
            }
        }

        return statusString + '\n'
                + "Result {" + '\n'
                + userListString + '\n'
                + "}";
    }

}