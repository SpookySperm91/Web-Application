package john.server.common.dto;

import lombok.Getter;

@Getter
public enum ResponseType {
    //Error
    SIGNUP_ERROR, LOGIN_ERROR, RESET_PASSWORD_ERROR,
    //Success
    SIGNUP_SUCCESS, LOGIN_SUCCESS, RESET_PASSWORD_SUCCESS,
    //Exception
    SIGNUP_EXCEPTION, LOGIN_EXCEPTION, RESET_PASSWORD_EXCEPTION
}
