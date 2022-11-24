https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class QueueSpout implements IRichSpout {
    static Logger logger = LogManager.getLogger(QueueSpout.class);
    SpoutOutputCollector collector;
    String executorId = UUID.randomUUID().toString();
    private int times = 0;

    @Override
    public void open(Map<String, String> config, TopologyContext topo, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void close() {

    }

    @Override
    public void nextTuple() {
        URLInfo urlInfo = Crawler.queue.poll();
        if(urlInfo == null){
            times += 1;
            try{
                Thread.sleep(100);
            } catch (Exception e){

            }
            if(times >= 300){
                logger.info("queue is empty over 30s, shutdown");
                System.exit(0);
            }
            return;
        }
        times = 0;
        //build the absolute url
        String suffix = urlInfo.getFilePath();
        if(suffix.equals("/")){
            suffix = "";
        }
        String url = urlInfo.getHostName() + ":" + urlInfo.getPortNo() + suffix;

        if(urlInfo.isSecure()){
            url = "https://" + url;
        } else {
            url = "http://" + url;
        }
        this.collector.emit(new Values<Object>(url));
        Thread.yield();
        try{
            Thread.sleep(100);
        } catch (Exception e){

        }
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("url"));
    }
}
