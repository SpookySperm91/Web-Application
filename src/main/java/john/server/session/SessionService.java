package john.server.session;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    // Method to store a session attribute
    public void setSession(HttpSession session, String attributeName, Object attributeValue) {
        session.setAttribute(attributeName, attributeValue);
    }

    // Method to retrieve a session attribute
    public Object getSession(HttpSession session, String attributeName) {
        return session.getAttribute(attributeName);
    }

    // Method to remove a session attribute
    public void removeSession(HttpSession session, String attributeName) {
        session.removeAttribute(attributeName);
    }

    // Method to invalidate a session
    public void invalidate(HttpSession session) {
        session.invalidate();
    }
}

