https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upenn.cis.cis455.crawler.utils.Channel;
import edu.upenn.cis.cis455.crawler.utils.Helper;
import edu.upenn.cis.cis455.crawler.utils.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.xpathengine.XPathEngine;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.tuple.Fields;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Crawler implements CrawlMaster {
    Logger logger = LogManager.getLogger(Crawler.class);
    static final int NUM_WORKERS = 10;
    private String startUrl;
    public static StorageInterface db;
    public static int size;
    private int count;
    ExecutorService threadPool = Executors.newCachedThreadPool();
    /*host:port <-> */
    public static HashMap<String, RobotsTxtInfo> robotsMap = new HashMap<>();
    public static HashMap<String, Long> delayMap = new HashMap<>();
    public static int indexedCount = 0;
    //stack for urls
    public static Queue<URLInfo> queue = new LinkedBlockingDeque<>();
    private int workingThreads = 0;
    boolean done = false;

    public LocalCluster cluster = new LocalCluster();

    public Crawler(String startUrl, StorageInterface db, int size, int count) {
        // TODO: initialize
        this.startUrl = startUrl;
        Crawler.db = db;
        Crawler.size = size;
        this.count = count;
    }

    public HashMap<String, RobotsTxtInfo> getRobotsMap() {
        return robotsMap;
    }

    public void setRobotsMap(HashMap<String, RobotsTxtInfo> robotsMap) {
        Crawler.robotsMap = robotsMap;
    }

    public HashMap<String, Long> getDelayMap() {
        return delayMap;
    }

    public void setDelayMap(HashMap<String, Long> delayMap) {
        Crawler.delayMap = delayMap;
    }

    ///// TODO: you'll need to flesh all of this out.  You'll need to build a thread
    // pool of CrawlerWorkers etc. and to implement the functions below which are
    // stubs to compile

    /**
     * Exctract links from html content
     * @param content
     * @return
     */
    public List<String> extractLinks(String content){
        String regex ="(?i)<a.*?/a>";
        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(content);
        List<String> list = new ArrayList<>();
        while(mt.find()) {
            String alink = mt.group();
            Matcher myurl=Pattern.compile("(?i)href=.*?>").matcher(alink);
            if(myurl.find())
            {
                String url = myurl.group().replaceAll("(?i)href=|>","");
                url = url.substring(1);
                int idx = url.indexOf("\"");
                if(idx < 0){
                    continue;
                }
                url = url.substring(0, idx);
                if(!Helper.isEmpty(url) && !url.startsWith("#") && !url.toLowerCase().startsWith("javascript:")){
                    list.add(url);
                }
            }
        }
        //find <link>

        return list;
    }

    public String extractParentDir(URLInfo parentUrlInfo){
        String parentDir = parentUrlInfo.getHostName() + ":" + parentUrlInfo.getPortNo();
        String path = parentUrlInfo.getFilePath();
        if(Helper.isEmpty(path) || path.equals("/")){
            return parentDir;
        }

        if(path.endsWith("/")){
            return parentDir + path.substring(0, path.length() - 1);
        }
        int idxLast =path.lastIndexOf("/");
        if(idxLast >= 0 && idxLast < parentUrlInfo.getFilePath().length() - 1){
            parentDir = parentDir + path.substring(0, idxLast);
        } else {
            parentDir = parentDir + path;
        }
        return parentDir;
    }

    private boolean queueContains(URLInfo input){
        for(URLInfo info : queue){
            if(info.toString().equals(input.toString())){
                return true;
            }
        }
        return false;
    }

    /**
     * Main thread
     */
    public void start() {
        queue.add(new URLInfo(startUrl));
        logger.info("start crawler");
        QueueSpout queueSpout = new QueueSpout();
        DocumentFecherBolt documentFecherBolt = new DocumentFecherBolt();
        DomParserBolt domParserBolt = new DomParserBolt();
        LinkExtractBolt linkExtractBolt = new LinkExtractBolt();
        PathMatcherBolt pathMatcherBolt = new PathMatcherBolt();
        XPathEngine engine = XPathEngineFactory.getXPathEngine();
        List<Channel> channels = db.channels();
        if(channels != null){
            List<String> xpaths = new ArrayList<>();
            String[] arr = new String[xpaths.size()];
            for(Channel c : channels){
                xpaths.add(c.getXpath());
            }
            engine.setXPaths(xpaths.toArray(arr));
        }
        /**
         * QueueSpout->DocumentFecherBolt->DomParserBolt->PathMatcherBolt
         *           ->DocumentFecherBolt->linkExtractBolt
         */

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(QueueSpout.class.getSimpleName(), queueSpout, 1);
        builder.setBolt(DocumentFecherBolt.class.getSimpleName(), documentFecherBolt, 1).shuffleGrouping(QueueSpout.class.getSimpleName());
        builder.setBolt(LinkExtractBolt.class.getSimpleName(), linkExtractBolt, 1).shuffleGrouping(DocumentFecherBolt.class.getSimpleName());

        builder.setBolt(DomParserBolt.class.getSimpleName(), domParserBolt, 1).shuffleGrouping(DocumentFecherBolt.class.getSimpleName());
        builder.setBolt(PathMatcherBolt.class.getSimpleName(), pathMatcherBolt, 1).fieldsGrouping(DomParserBolt.class.getSimpleName(), new Fields("url"));

        Topology topo = builder.createTopology();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String str = mapper.writeValueAsString(topo);

            logger.info("The StormLite topology is:\n" + str);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        Config config = new Config();
        cluster.submitTopology("crawler", config, builder.createTopology());
        while(!isDone()){
            try {
                Thread.sleep(30);
            } catch (Exception e){
            }
        }
        cluster.shutdown();
    }
    
    /**
     * Returns true if it's permissible to access the site right now
     * eg due to robots, etc.
     */
    public boolean isOKtoCrawl(String site, int port, boolean isSecure) {
        URLInfo urlInfo = new URLInfo(site);
        String hostAndPort = urlInfo.getHostName() + ":" + urlInfo.getPortNo();
        RobotsTxtInfo robotsTxtInfo = robotsMap.get(hostAndPort);
        if(robotsTxtInfo != null){
            List<String> urls = robotsTxtInfo.getDisallowedLinks("*");
            List<String> urlsForUs = robotsTxtInfo.getDisallowedLinks("cis455crawler");
            List<String> alloweds = robotsTxtInfo.getAllowedLinks("*");
            List<String> allowdForUs = robotsTxtInfo.getAllowedLinks("cis455crawler");
            //prevent null pointer
            if(urls == null){
                urls = new ArrayList<>();
            }
            if(urlsForUs == null){
                urlsForUs = new ArrayList<>();
            }
            if(alloweds == null){
                alloweds = new ArrayList<>();
            }
            if(allowdForUs == null){
                allowdForUs = new ArrayList<>();
            }

            if((urlsForUs.contains("/")) && allowdForUs.size() == 0){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the crawl delay says we should wait
     */
    public boolean deferCrawl(String site) {
        URLInfo urlInfo = new URLInfo(site);
        String hostAndPort = urlInfo.getHostName() + ":" + urlInfo.getPortNo();
        RobotsTxtInfo robotsTxtInfo = robotsMap.get(hostAndPort);
        if(robotsTxtInfo != null){
            int delayForAll = robotsTxtInfo.getCrawlDelay("*");
            Long lasttime = delayMap.get(hostAndPort);
            if(delayForAll > 0 && lasttime != null){
                if(lasttime + delayForAll * 1000 > System.currentTimeMillis()){
                    return true;
                }
            }
            int delayForUs = robotsTxtInfo.getCrawlDelay("cis455crawler");
            if(delayForUs > 0 && lasttime != null){
                if(lasttime + delayForUs * 1000 > System.currentTimeMillis()){
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns true if it's permissible to fetch the content,
     * eg that it satisfies the path restrictions from robots.txt
     */
    public boolean isOKtoParse(URLInfo url) {
        String hostAndPort = url.getHostName() + ":" + url.getPortNo();
        RobotsTxtInfo robotsTxtInfo = robotsMap.get(hostAndPort);
        if(robotsTxtInfo != null){
            List<String> urls = robotsTxtInfo.getDisallowedLinks("*");
            List<String> urlsForUs = robotsTxtInfo.getDisallowedLinks("cis455crawler");
//            if(urls != null){
//                for(String prefix : urls){
//                    if(url.getFilePath() != null && url.getFilePath().startsWith(prefix)){
//                        return false;
//                    }
//                }
//            }
            if(urlsForUs != null){
                for(String prefix : urlsForUs){
                    if(url.getFilePath() != null && url.getFilePath().startsWith(prefix) || (url.getFilePath() + "/").equals(prefix)){
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Returns true if the document content looks worthy of indexing,
     * eg that it doesn't have a known signature
     */
    public boolean isIndexable(String content) {
        return !db.existsContent(content);
    }
    
    /**
     * We've indexed another document
     */
    public void incCount() {
        synchronized (this){
            Crawler.indexedCount ++;
            logger.info("indexed " + Crawler.indexedCount + " in this process ");
        }
    }
    
    /**
     * Workers can poll this to see if they should exit, ie the
     * crawl is done
     */
    public boolean isDone() {
        if(Crawler.indexedCount >= this.count || done == true){
            return true;
        }
        return false;
    }
    
    /**
     * Workers should notify when they are processing an URL
     */
    public void setWorking(boolean working) {
        if(working){
            synchronized (this){
                this.workingThreads += 1;
            }
        }
    }
    
    /**
     * Workers should call this when they exit, so the master
     * knows when it can shut down
     */
    public void notifyThreadExited() {
        synchronized (this){
            this.workingThreads -= 1;
        }
    }
    
    /**
     * Main program:  init database, start crawler, wait
     * for it to notify that it is done, then close.
     */
    public static void main(String[] args) {
        if (args.length < 3 || args.length > 5) {
            System.out.println("Usage: Crawler {start URL} {database environment path} {max doc size in MB} {number of files to index}");
            System.exit(1);
        }

        System.out.println("Crawler starting");
        String startUrl = args[0];
        String envPath = args[1];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;

        if (!Files.exists(Paths.get(envPath))) {
            try {
                Files.createDirectory(Paths.get(envPath));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        StorageInterface db = StorageFactory.getDatabaseInstance(envPath);

        Crawler crawler = new Crawler(startUrl, db, size, count);

        System.out.println("Starting crawl of " + count + " documents, starting at " + startUrl);
        crawler.start();

        while (!crawler.isDone())
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        // TODO: final shutdown
        crawler.cluster.killTopology("crawler");
        crawler.cluster.shutdown();
        System.out.println("Done crawling!");
        System.exit(0);
    }

}
