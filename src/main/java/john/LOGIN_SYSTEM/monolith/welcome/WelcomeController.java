package john.LOGIN_SYSTEM.monolith.welcome;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import john.LOGIN_SYSTEM.common.dto.EmailDTO;
import john.LOGIN_SYSTEM.common.dto.PasswordDTO;
import john.LOGIN_SYSTEM.common.response.ResponseClient;
import john.LOGIN_SYSTEM.common.response.ResponseType;
import john.LOGIN_SYSTEM.common.response.ResponseUtil;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink.LinkToken;
import john.LOGIN_SYSTEM.persistenceMongodb.token.verificationLink.LinkTokenService;
import john.LOGIN_SYSTEM.session.SessionAttr;
import john.LOGIN_SYSTEM.session.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/welcome")
public class WelcomeController {
    private final SessionService redisSession;
    private final WelcomeService welcomeService;
    private final LinkTokenService linkService;
    private final ResponseUtil responseUtil;

    @Autowired
    public WelcomeController(SessionService redisSession, WelcomeService welcomeService, LinkTokenService linkService, ResponseUtil responseUtil) {
        this.redisSession = redisSession;
        this.welcomeService = welcomeService;
        this.linkService = linkService;
        this.responseUtil = responseUtil;
    }

    @GetMapping("/")
    public ResponseEntity<String> welcome(HttpSession session) {
        Boolean authenticated = (Boolean) redisSession.getSession(session, SessionAttr.LOGIN_ACCESS_SESSION);
        boolean isSessionValid = authenticated != null && authenticated; // Defense against NullPointerException

        if (isSessionValid) {
            return ResponseEntity.ok("welcome");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not allowed");
    }


    @PutMapping("/change-password")
    public ResponseEntity<ResponseClient> changePassword(HttpSession session, @RequestBody PasswordDTO request) {
        String userSession = (String) redisSession.getSession(session, SessionAttr.USER_ID_SESSION);

        if (userSession == null || userSession.isEmpty()) {
            return responseUtil.buildResponse(HttpStatus.UNAUTHORIZED,
                    ResponseType.RESET_PASSWORD_EXCEPTION,
                    "Unauthorized: Process Restricted!");
        }
        var response = welcomeService.changePassword(userSession, request);

        return responseUtil.buildResponse(
                response.getHttpStatus(),
                response.getType(),
                response.getMessage());
    }


    // CHANGE PASSWORD VIA EMAIL LINK
    // Request user with the email associate on the account
    // Respond user to check email
    @PostMapping("/change-password-email")
    public ResponseEntity<ResponseClient> changePasswordWithEmail(HttpSession session, @RequestBody EmailDTO request) throws Exception {
        // Check if user is logged in
        if (redisSession.getSession(session, SessionAttr.LOGIN_ACCESS_SESSION) == null
                || redisSession.getSession(session, SessionAttr.USER_ID_SESSION) == null) {
            return responseUtil.buildResponse(
                    HttpStatus.UNAUTHORIZED,
                    ResponseType.ACTION_UNAUTHORIZED,
                    "Action invalid: Not logged in");
        }

        // Send change-password link in email
        var response = welcomeService.changePasswordByEmail(
                (String) redisSession.getSession(session, SessionAttr.USER_ID_SESSION),
                request.getEmail());

        // Set session token
        Map<String, Object> sessionData = redisSession.convertToMap(response.getDataAccess().getObjects().getFirst());
        redisSession.setSessionHash(session,
                SessionAttr.CHANGE_PASSWORD_LINK_SESSION,                     // attr
                sessionData,                                                  // body
                response.getDataAccess().getDates().getFirst().getTime());    // expire

        return responseUtil.buildResponse(
                response.getHttpStatus(),
                response.getType(),
                response.getMessage());
    }

    // CHANGE PASSWORD LINK
    // Check if URL is valid
    @GetMapping("/request-change-password/{id}")
    public ResponseEntity<ResponseClient> changePassword(HttpSession session, @PathVariable String id) {
        // Retrieve session data
        Map<String, Object> sessionData = redisSession.getSessionHash(session, SessionAttr.CHANGE_PASSWORD_LINK_SESSION);
        LinkToken retrievedToken;

        if (!sessionData.isEmpty()) {
            retrievedToken = new ObjectMapper().convertValue(sessionData, LinkToken.class);

            // Check if retrieved token is null or doesn't match
            if (retrievedToken == null || !retrievedToken.getToken().equals(id)) {
                return responseUtil.buildResponse(
                        HttpStatus.UNAUTHORIZED,
                        ResponseType.CHANGE_PASSWORD_LINK_EXPIRED,
                        "Action invalid. Session unauthorized.");
            }
            return responseUtil.buildResponse(
                    HttpStatus.OK,
                    ResponseType.CHANGE_PASSWORD_LINK_ACCESSED,
                    "Link is valid. Change Password Domain is open for a limited time!");
        }

        return responseUtil.buildResponse(
                HttpStatus.UNAUTHORIZED,
                ResponseType.CHANGE_PASSWORD_LINK_EXPIRED,
                "Action invalid. Session unauthorized.");
    }


    // RETRIEVE CHANGE-PASSWORD REQUEST
    // Check user inputs
    // return response
    @PutMapping("/change-password/{id}")
    public ResponseEntity<ResponseClient> changePassword(HttpSession session, @PathVariable String id, @RequestBody PasswordDTO request) {
        Map<String, Object> sessionData = redisSession.getSessionHash(session, SessionAttr.CHANGE_PASSWORD_LINK_SESSION);
        LinkToken retrievedToken;

        if (!sessionData.isEmpty()) {
            retrievedToken = new ObjectMapper().convertValue(sessionData, LinkToken.class);
            // Check if retrieved token is null or doesn't match
            if (retrievedToken == null || !retrievedToken.getToken().equals(id)) {
                return responseUtil.buildResponse(
                        HttpStatus.UNAUTHORIZED,
                        ResponseType.CHANGE_PASSWORD_LINK_EXPIRED,
                        "Action invalid. Session unauthorized.");
            }

            var response = welcomeService.changePassword(retrievedToken.getId().toString(), request);

            // Remove session if password change is successful
            if (response.isSuccess()) {
                linkService.deleteVerificationLink(retrievedToken);
                redisSession.removeSession(session, SessionAttr.CHANGE_PASSWORD_LINK_SESSION);
            }

            return responseUtil.buildResponse(
                    response.getHttpStatus(),
                    response.getType(),
                    response.getMessage());
        }
        return responseUtil.buildResponse(
                HttpStatus.UNAUTHORIZED,
                ResponseType.CHANGE_PASSWORD_LINK_EXPIRED,
                "Action invalid. Session unauthorized.");
    }


    // EXIT
    @GetMapping("/exit")
    public ResponseEntity<ResponseClient> exitWelcome(HttpSession session) {
        if (redisSession.getSession(session, SessionAttr.LOGIN_ACCESS_SESSION) == null
                || redisSession.getSession(session, SessionAttr.USER_ID_SESSION) == null) {
            return responseUtil.buildResponse(
                    HttpStatus.UNAUTHORIZED,
                    ResponseType.ACTION_UNAUTHORIZED,
                    "Action invalid: Not login");
        }

        redisSession.removeSession(session, SessionAttr.LOGIN_ACCESS_SESSION);
        redisSession.removeSession(session, SessionAttr.USER_ID_SESSION);

        return responseUtil.buildResponse(
                HttpStatus.OK,
                ResponseType.EXIT_APPLICATION,
                "GoodBye!");
    }
}




