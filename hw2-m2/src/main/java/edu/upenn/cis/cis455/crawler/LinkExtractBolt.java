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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkExtractBolt implements IRichBolt {
    static Logger logger = LogManager.getLogger(LinkExtractBolt.class);
    OutputCollector collector;
    String executorId = UUID.randomUUID().toString();

    Fields schema = new Fields("links");

    @Override
    public void cleanup() {

    }

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

    @Override
    public void execute(Tuple input) {
        String content = input.getStringByField("text");
        String url = input.getStringByField("url");
        String contentType = input.getStringByField("type");
        if(Helper.isEmpty(url) || Helper.isEmpty(content)){
            return;
        }
        try{
            //save document
            if(Crawler.db.existsContent(content)){
                logger.info(url + " had been indexed already");
            } else {
                //add document
                Crawler.db.addDocument(url, content);
                Crawler.indexedCount += 1;
                logger.info(url + " indexed and added to db");
            }

            if(!contentType.toLowerCase().contains("text/html")){
                return;
            }
            //extract links
            List<String> links = extractLinks(content);
            logger.info(url + " extracted " + links.size() + " links");
            URLInfo parentUrlInfo = new URLInfo(url);
            String parentDir = extractParentDir(parentUrlInfo);
            for(String uri : links){
                if(Helper.isEmpty(uri)){
                    continue;
                }
                //build and add the absolute url
                if(!uri.startsWith("http")){
                    //relative path
                    String suffix = uri;
                    if(uri.equals("/")){
                        suffix = "";
                    } else if(!uri.startsWith("/")){
                        suffix = "/" + suffix;
                    }
                    if(parentUrlInfo.isSecure()){
                        uri = "https://" + parentDir + suffix;
                    } else {
                        uri = "http://" + parentDir + suffix;
                    }

                }
                URLInfo uriObj = new URLInfo(uri);
                if(!uri.equals(url)){
                    Crawler.queue.add(uriObj);
                }

            }
            if(links == null || links.size() == 0){
                return;
            }
            collector.emit(new Values<Object>(links));
        } catch (Exception e){
            logger.error(e.getMessage(), e);
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
