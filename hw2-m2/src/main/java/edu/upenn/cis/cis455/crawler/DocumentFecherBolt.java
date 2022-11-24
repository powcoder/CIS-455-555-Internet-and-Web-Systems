https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.utils.Helper;
import edu.upenn.cis.cis455.crawler.utils.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DocumentFecherBolt implements IRichBolt {
    static Logger logger = LogManager.getLogger(DocumentFecherBolt.class);
    OutputCollector collector;
    String executorId = UUID.randomUUID().toString();

    Fields schema = new Fields("url", "text", "type");

    @Override
    public void cleanup() {

    }

    /**
     * Returns true if it's permissible to access the site right now
     * eg due to robots, etc.
     */
    public boolean isOKtoCrawl(String site, int port, boolean isSecure) {
        URLInfo urlInfo = new URLInfo(site);
        String hostAndPort = urlInfo.getHostName() + ":" + urlInfo.getPortNo();
        RobotsTxtInfo robotsTxtInfo = Crawler.robotsMap.get(hostAndPort);
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
        RobotsTxtInfo robotsTxtInfo = Crawler.robotsMap.get(hostAndPort);
        if(robotsTxtInfo != null){
            Long lasttime = Crawler.delayMap.get(hostAndPort);
            int delayForUs = robotsTxtInfo.getCrawlDelay("cis455crawler");
            if(delayForUs > 0 && lasttime != null){
                if(lasttime + delayForUs * 1000 > System.currentTimeMillis()){
                    return true;
                }
            }
        }
        Crawler.delayMap.put(hostAndPort, System.currentTimeMillis());
        return false;
    }

    /**
     * Returns true if it's permissible to fetch the content,
     * eg that it satisfies the path restrictions from robots.txt
     */
    public boolean isOKtoParse(URLInfo url) {
        String hostAndPort = url.getHostName() + ":" + url.getPortNo();
        RobotsTxtInfo robotsTxtInfo = Crawler.robotsMap.get(hostAndPort);
        if(robotsTxtInfo != null){
            List<String> urlsForUs = robotsTxtInfo.getDisallowedLinks("cis455crawler");
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
        return !Crawler.db.existsContent(content);
    }

    @Override
    public void execute(Tuple input) {
        String url = input.getStringByField("url");
        if(Helper.isEmpty(url)){
            return;
        }
        String content = "";
        //check if it need to crawl
        URLInfo urlInfo = new URLInfo(url);
        //try to find robots
        String hostAndPort = urlInfo.getHostName() + ":" + urlInfo.getPortNo();
        if(!Crawler.robotsMap.containsKey(hostAndPort)){
            String robotsUrl = hostAndPort + "/robots.txt";
            if(urlInfo.isSecure()){
                robotsUrl = "https://" + robotsUrl;
            } else {
                robotsUrl = "http://" + robotsUrl;
            }
            String robotContent = null;
            try{
                robotContent = Helper.httpGet(robotsUrl);
                RobotsTxtInfo robotsTxtInfo = Helper.parseRobot(robotContent);
                Crawler.robotsMap.put(hostAndPort, robotsTxtInfo);
            } catch (Exception ex){
                logger.error("not found robots.txt, use default");
                Crawler.robotsMap.put(hostAndPort, new RobotsTxtInfo());
            }
        }

        if(!isOKtoCrawl(url, urlInfo.getPortNo(), urlInfo.isSecure())){
            logger.error("robots deny craw");
            return;
        }
        if(!isOKtoParse(urlInfo)){
            logger.error("robots deny parse");
            return;
        }
        boolean canContinue = true;
        //test head and content-Length
        String contentType = "";
        try{
            Map<String, List<String>> headers = Helper.httpHead(url);
            for(String key : headers.keySet()){
                if(key == null){
                    continue;
                }
                if(key.toLowerCase().equals("content-length")){
                    Integer len = Integer.parseInt(headers.get(key).get(0));
                    if(len > Crawler.size * 1024 * 1024){
                        //oversize skip
                        canContinue = false;
                        break;
                    }
                } else if(key.toLowerCase().equals("content-type")){
                    contentType = headers.get(key).get(0).toLowerCase();
                    if(!contentType.contains("text/html") && !contentType.contains("/xml") && !contentType.contains("+xml")){
                        //skip other file type
                        canContinue = false;
                        break;
                    }
                }
            }
        } catch (Exception e){
            logger.error("error when head request");
        }
        if(!canContinue){
            logger.info("not html or xml");
            return;
        }
        if(deferCrawl(url)){
            //add it into the tail of the queue
            Crawler.queue.add(new URLInfo(url));
            logger.info(url + " moved to queue tail for delay value in robots.txt");
            return;
        }

        try{
            logger.info(url + " : downloading");
            content = Helper.httpGet(url);
        } catch (Exception e){
            logger.error("error when fetch " + url);
        }
        if(!Helper.isEmpty(content)){
            collector.emit(new Values<Object>(url, content, contentType));
        }
    }

    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }

    @Override
    public Fields getSchema() {
        return schema;
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }
}
