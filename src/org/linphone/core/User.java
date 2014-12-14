package org.linphone.core;

public class User {

    private String email;
    private String activationCode;
    private boolean isActivated;
    private boolean isLoggedIn;

    public User() {
    }

    public User(String email, String activationCode, boolean isActivated, boolean isLoggedIn) {
        this.email = email;
        this.activationCode = activationCode;
        this.isActivated = isActivated;
        this.isLoggedIn = isLoggedIn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public boolean getIsActivated() { return isActivated; }

    public void setIsActivated(boolean isActivated) { this.isActivated = isActivated; }

    public boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    @Override
    public int hashCode() {
        int result = 31 * (email != null ? email.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "User{" + '\n'
                + " email=" + email + '\n'
                + " activationCode=" + activationCode + '\n'
                + ",isActivated=" + isActivated + '\n'
                + ",isLoggedIn=" + isLoggedIn + '\n'
                + "}";
    }
}