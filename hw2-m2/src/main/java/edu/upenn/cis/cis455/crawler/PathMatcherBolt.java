https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.utils.Channel;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.XPathEngine;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PathMatcherBolt implements IRichBolt {
    static Logger logger = LogManager.getLogger(PathMatcherBolt.class);
    OutputCollector collector;
    String executorId = UUID.randomUUID().toString();
    Fields schema = new Fields("channel");

    XPathEngine engine = XPathEngineFactory.getXPathEngine();

    @Override
    public void cleanup() {

    }

    @Override
    public void execute(Tuple input) {
        String url = input.getStringByField("url");
        OccurrenceEvent event = (OccurrenceEvent)input.getObjectByField("event");
        if(event == null){
            return;
        }
//        logger.info(event.toString());
        boolean[] matches = engine.evaluateEvent(event);
        if(matches == null){
            return;
        }
        List<Channel> channels = Crawler.db.channels();
        for(int i = 0; i < matches.length; i++){
            if(matches[i] == true){
                Channel c = channels.get(i);
                //link channel and url
                Crawler.db.linkChannelToUrl(url, c.getName());
            }
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
