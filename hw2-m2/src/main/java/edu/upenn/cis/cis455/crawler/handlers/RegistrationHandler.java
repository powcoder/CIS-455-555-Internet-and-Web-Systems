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
import edu.upenn.cis.cis455.storage.StorageInterface;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RegistrationHandler implements Route {
    StorageInterface db;
    public RegistrationHandler(StorageInterface db) {
        this.db = db;
    }
    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(Helper.isEmpty(request.body())){
            return Helper.htmlpage("Bad Request");
        }
        Map<String, String> map = Helper.parseFormData(request.body());
        String username = map.get("username");
        String password = map.get("password");

        if(Helper.isEmpty(username) || Helper.isEmpty(password)){
        	response.status(406);
            return Helper.htmlpage("the username or password is empty");
        }
        int result = db.addUser(username, password);
        String content = "";
        if(result > 0){
            content = "<h3>Success!</h3><p>" +
                    "<a href=\"index.html\">main page</a></p>" +
                    "<p><a href=\"login-form.html\">login page</a></p>";
        } else {
        	response.status(409);
            content = "the username already exists";
        }
        return Helper.htmlpage(content);
    }
}
