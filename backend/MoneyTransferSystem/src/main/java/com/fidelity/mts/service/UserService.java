package com.fidelity.mts.service;

import com.fidelity.mts.application.dto.UserLoginRequest;
import com.fidelity.mts.application.dto.UserLoginResponse;
import com.fidelity.mts.application.dto.UserRegistrationRequest;

public interface UserService {

    /** Registers a new user and creates a linked account with balance 0. */
    String register(UserRegistrationRequest request);

    /** Authenticates a user by email/password, returns their accountId. */
    UserLoginResponse login(UserLoginRequest request);
}
