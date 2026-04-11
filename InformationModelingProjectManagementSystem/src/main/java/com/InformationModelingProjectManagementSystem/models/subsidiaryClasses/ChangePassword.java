package com.InformationModelingProjectManagementSystem.models.subsidiaryClasses;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ChangePassword {
    
    private String lastPassword;
    private String newPassword;
    private String newPasswordRepeat;

    public ChangePassword() {}

    public String getLastPassword() {
        return lastPassword;
    }
    public void setLastPassword(String lastPassword) {
        this.lastPassword = lastPassword;
    }
    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    public String getNewPasswordRepeat() {
        return newPasswordRepeat;
    }
    public void setNewPasswordRepeat(String newPasswordRepeat) {
        this.newPasswordRepeat = newPasswordRepeat;
    }

    public String getNewPasswordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(this.newPassword);
    }

    public boolean checkLastPassword(String currentPassword) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(this.lastPassword, currentPassword);
    }
    
}
