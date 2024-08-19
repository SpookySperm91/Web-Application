package john.LOGIN_SYSTEM.monolith.register;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import john.LOGIN_SYSTEM.common.components.AccountLock;
import john.LOGIN_SYSTEM.common.components.PasswordStrength;
import john.LOGIN_SYSTEM.common.config.PropertiesConfig;
import john.LOGIN_SYSTEM.common.dto.UserDTO;
import john.LOGIN_SYSTEM.common.response.ResponseClient;
import john.LOGIN_SYSTEM.common.response.ResponseLayer;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.common.response.ResponseUtil;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink.LinkToken;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink.LinkTokenService;
import john.LOGIN_SYSTEM.session.SessionAttr;
import john.LOGIN_SYSTEM.session.SessionService;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/register")
class RegisterController {
    private final RegisterService serviceLayer;
    private final PasswordStrength passwordStrength;
    private final LinkTokenService tokenService;
    private final AccountLock accountLock;
    private final SessionService redisSession;
    private final PropertiesConfig propertiesConfig;
    private final ResponseUtil responseUtil;

    @Autowired
    public RegisterController(RegisterService signupService,
                              PasswordStrength passwordStrength,
                              LinkTokenService tokenService,
                              AccountLock accountLock, SessionService redisSession,
                              PropertiesConfig propertiesConfig,
                              ResponseUtil responseUtil) {
        this.serviceLayer = signupService;
        this.passwordStrength = passwordStrength;
        this.tokenService = tokenService;
        this.accountLock = accountLock;
        this.redisSession = redisSession;
        this.propertiesConfig = propertiesConfig;
        this.responseUtil = responseUtil;
    }


    // CHECK USER'S INPUTS BEFORE SIGNUP
    // Sanitize user inputs from malicious attempt
    // Check email and username format; Return bad response if error occurs
    // Proceed to create new account
    // Return response
    @PostMapping("/")
    public ResponseEntity<ResponseClient> signupUser(@Valid @RequestBody UserDTO request, HttpSession session) throws Exception {
        String sanitizedEmail = Encode.forHtml(request.getEmail());
        String sanitizedUsername = Encode.forHtml(request.getUsername());
        String sanitizedPassword = Encode.forHtml(request.getPassword());

        ResponseLayer email = serviceLayer.checkEmail(sanitizedEmail);
        ResponseLayer username = serviceLayer.checkUsername(sanitizedUsername);
        ResponseLayer password = passwordStrength.checkPassword(sanitizedPassword);

        if (!email.isSuccess() || !username.isSuccess() || !password.isSuccess()) {
            List<String> errorMessages = Stream.of(email.getMessage(), username.getMessage(), password.getMessage())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return responseUtil.buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ResponseType.SIGNUP_ERROR,
                    "ERROR: " + String.join(", ", errorMessages));
        }

        // Create token for email verification
        var signupResponse = serviceLayer.verificationProcess(sanitizedUsername, sanitizedEmail, sanitizedPassword);

        // set session
        Map<String, Object> sessionData = redisSession.convertToMap(signupResponse.getDataAccess().getObjects().getFirst());
        redisSession.setSessionHash(session,                                            // Create(hash)
                SessionAttr.VERIFICATION_LINK_SESSION,                                  // attr = VerificationLink
                sessionData,                                                            // body
                signupResponse.getDataAccess().getDates().getFirst().getTime());        // expire


        return responseUtil.buildResponse(
                signupResponse.getHttpStatus(),
                ResponseType.SIGNUP_PENDING,
                signupResponse.getMessage());
    }


    // VERIFY ACCOUNT
    // Check if link is expire or valid
    // Enable locked account
    // Delete the link
    @GetMapping("/{verification}")
    public ModelAndView verificationProcess(@PathVariable String verification, Model model, HttpSession session) {
        Map<String, Object> value = redisSession.getSessionHash(session, SessionAttr.VERIFICATION_LINK_SESSION);
        LinkToken retrievedToken;

        if (!value.isEmpty()) {
            retrievedToken = new ObjectMapper().convertValue(value, LinkToken.class);
            System.out.println(retrievedToken);

            if (retrievedToken.getToken().equals(verification)) {
                accountLock.enableAccount(retrievedToken);
                tokenService.deleteVerificationLink(retrievedToken);
                redisSession.removeSession(session, SessionAttr.VERIFICATION_LINK_SESSION);

                model.addAttribute("baseUrl", propertiesConfig.getBaseUrl());
                return new ModelAndView("verification-success", model.asMap());
            } else {  // persistence if value not found in redis
                return tokenService.getVerificationLink(verification)
                        .map(token -> {
                            accountLock.enableAccount(token);
                            tokenService.deleteVerificationLink(token);
                            redisSession.removeSession(session, SessionAttr.VERIFICATION_LINK_SESSION);

                            model.addAttribute("baseUrl", propertiesConfig.getBaseUrl());
                            return new ModelAndView("verification-success", model.asMap());
                        }).orElseGet(() -> {
                            model.addAttribute("baseUrl", propertiesConfig.getBaseUrl());
                            return new ModelAndView("verification-expired", model.asMap());
                        });
            }
        }
        model.addAttribute("baseUrl", propertiesConfig.getBaseUrl());
        return new ModelAndView("InvalidSession", model.asMap());
    }
}


