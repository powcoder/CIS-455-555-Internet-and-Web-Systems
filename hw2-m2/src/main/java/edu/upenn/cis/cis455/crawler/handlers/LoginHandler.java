https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.crawler.utils.Helper;
import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;

import java.util.Map;

public class LoginHandler implements Route {
    StorageInterface db;

    public LoginHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        if(Helper.isEmpty(req.body())){
            return Helper.htmlpage("Bad Request");
        }
        Map<String, String> map = Helper.parseFormData(req.body());
        String user = map.get("username");
        String pass = map.get("password");

        System.err.println("Login request for " + user + " and " + pass);
        if (db.getSessionForUser(user, pass)) {
            Session session = req.session();
            if(!Helper.isSessionValid(session)){
                System.err.println("session timeout");
                resp.redirect("/login-form.html");
            }
            System.err.println("Logged in!");
            session.attribute("user", user);
            session.attribute("password", pass);
            resp.redirect("/index.html");
        } else {
            System.err.println("Invalid credentials");
            return "Invalid credentials";
        }

        return "";
    }
}
