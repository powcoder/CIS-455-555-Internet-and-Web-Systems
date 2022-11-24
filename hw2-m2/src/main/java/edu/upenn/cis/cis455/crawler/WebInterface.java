https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.crawler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;
import edu.upenn.cis.cis455.crawler.handlers.LoginFilter;
import edu.upenn.cis.cis455.crawler.handlers.RegistrationHandler;
import edu.upenn.cis.cis455.crawler.utils.Channel;
import edu.upenn.cis.cis455.crawler.utils.Document;
import edu.upenn.cis.cis455.crawler.utils.Helper;
import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.crawler.handlers.LoginHandler;
import edu.upenn.cis.cis455.xpathengine.XPathEngine;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;

public class WebInterface {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Syntax: WebInterface {path} {root}");
            System.exit(1);
        }

        if (!Files.exists(Paths.get(args[0]))) {
            try {
                Files.createDirectory(Paths.get(args[0]));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        port(45555);
        StorageInterface database = StorageFactory.getDatabaseInstance(args[0]);

        LoginFilter testIfLoggedIn = new LoginFilter(database);

        if (args.length == 2) {
            staticFiles.externalLocation(args[1]);
            staticFileLocation(args[1]);
        }
        XPathEngine engine = XPathEngineFactory.getXPathEngine();
        List<Channel> channels = database.channels();
        if(channels != null){
            List<String> xpaths = new ArrayList<>();
            String[] arr = new String[xpaths.size()];
            for(Channel c : channels){
                xpaths.add(c.getXpath());
            }
            engine.setXPaths(xpaths.toArray(arr));
        }
        before("/*", "GET", testIfLoggedIn);
        before("/*", "POST", testIfLoggedIn);
        // TODO:  add /register, /logout, /index.html, /, /lookup
        post("/register", new RegistrationHandler(database));
        post("/login", new LoginHandler(database));
        post("/logout", (req, res)->{
            req.session().invalidate();
            res.redirect("/login-form.html");
            return "";
        });
        get("/index.html", (req, res)->{
            return "Welcome " + req.session().attribute("user");
        });
        get("/show", (req, res) ->{
            String channel = req.queryParams("channel");
            if(Helper.isEmpty(channel)){
                res.status(404);
                return "Not Found";
            }
            Channel c = database.getChannel(channel);
            if(c == null){
                res.status(404);
                return "Not Found";
            }
            List<Document> list = database.bychannel(channel);
            String content = "";
            content += "<div class=\"channelheader\">";
            content += "Channel name: " + channel + ",";
            content += "Created by: " + c.getCreator() ;
            for(Document doc : list){
                content += "<div>";
                content += "Crawled on: " + Helper.formatDate(doc.getTime()) + ",";
                content += "Location: " + doc.getUrl();
                content += "<div class=\"document\">";
                content += doc.getContent();
                content += "</div>";
                content += "</div>";
            }
            content += "</div>";
            return Helper.htmlpage(content);
        });
        get("/", (req, res)->{
            String homePageStr = "<ol>";
            List<Channel> list = database.channels();
            for(Channel c : list){
                homePageStr += "<li><a href=\"/show?channel=" + c.getName() + "\">" + c.getName() + "</a></li>";
            }
            homePageStr += "</ol>";
            return Helper.htmlpage(homePageStr);
        });
        get("/lookup", (req, res)->{
            String url = req.queryParams("url");
            if(Helper.isEmpty(url)){
                res.status(404);
                return "";
            }
            if(!url.startsWith("http")){
                url = "http://" + url;
            }
            URLInfo urlInfo = new URLInfo(url);
            String suffix = urlInfo.getFilePath();
            if(!suffix.startsWith("/")){
                suffix = "/" + suffix;
            }
            if(suffix.length() == 1){
                suffix = "";
            }
            String absoluteUrl = urlInfo.getHostName() + ":" + urlInfo.getPortNo() + suffix;
            if(urlInfo.isSecure()){
                absoluteUrl = "https://" + absoluteUrl;
            } else {
                absoluteUrl = "http://" + absoluteUrl;
            }
            String content = database.getDocument(absoluteUrl);

            if(Helper.isEmpty(content)){
                res.status(404);
                return "Not Found";
            }
            //Inject a base url to keep the links working
            content = content.replace("</head>", "<base href=\"" + absoluteUrl +  "\" /></head>");
            return content;
        });
        get("/create/:name", (req, res) ->{
            String xpath = req.queryParams("xpath");
            String name = req.params("name");
            String username = req.session().attribute("user");
            if(Helper.isEmpty(xpath) || Helper.isEmpty(name)){
                res.status(400);
                return "Params error";
            }
            database.saveChannel(username, name, xpath);
            return "OK";
        });
        //just for test
        get("/docs", (req, res)->{
            List<String> list = database.docs();
            String content = "";
            int i = 1;
            for(String str : list){
                content += "<li><a href=\"/lookup?url=" + str + "\">" + (i++) + " . " + str + "</a></li>";
            }
            return Helper.htmlpage(content);
        });
        awaitInitialization();
        System.out.println("Server started.");
    }
}
