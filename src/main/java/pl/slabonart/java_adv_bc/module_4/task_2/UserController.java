package pl.slabonart.java_adv_bc.module_4.task_2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class UserController {

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASS = System.getenv("DB_PASS");

    private static final String CONTENT_TYPE_PLAIN_TEXT = "text/plain;charset=UTF-8";
    private static final String USER_SUCCESSFULLY_CREATED = "User successfully created!";
    private static final String INTERNAL_SERVER_ERROR = "Internal server error";
    private static final String USERNAME_IS_TOO_SHORT = "Username is too short!";
    private static final String USERNAME_AND_PASSWORD_ARE_REQUIRED = "Username and password are required";
    private static final String USER_ID = "userId";
    private static final String USER_ID_IS_REQUIRED = "User ID is required";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final String SELECT_QUERY = "SELECT name FROM Users WHERE id = ?";

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        String userId = request.getParameter(USER_ID);

        if (userId == null || userId.isEmpty()) {
            sendErrorResponse(response, USER_ID_IS_REQUIRED, 400);
            return;
        }

        List<String> users = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(SELECT_QUERY)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(rs.getString("name"));
                }
            }

            response.setContentType(CONTENT_TYPE_JSON);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(users.toString());

        } catch (SQLException | IOException e) {
            logger.error("exception while calling database: {}", e.getMessage());
            sendErrorResponse(response, INTERNAL_SERVER_ERROR, 500);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse response) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            sendErrorResponse(response, USERNAME_AND_PASSWORD_ARE_REQUIRED, 400);
            return;
        }

        if (username.length() < 5) {
            sendErrorResponse(response, USERNAME_IS_TOO_SHORT, 400);
            return;
        }

        response.setContentType(CONTENT_TYPE_PLAIN_TEXT);
        try {
            response.getWriter().write(USER_SUCCESSFULLY_CREATED);
        } catch (IOException e) {
            logger.error("Error while preparing response message: {}", e.getMessage());
            sendErrorResponse(response, INTERNAL_SERVER_ERROR, 500);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) {
        response.setContentType(CONTENT_TYPE_PLAIN_TEXT);
        response.setStatus(statusCode);
        try {
            response.getWriter().write(message);
        } catch (IOException e) {
            logger.error("Error while preparing response message: {}", e.getMessage());
        }
    }
}

