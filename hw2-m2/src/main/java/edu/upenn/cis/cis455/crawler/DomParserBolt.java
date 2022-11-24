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
import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeVisitor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static edu.upenn.cis.cis455.xpathengine.OccurrenceEvent.Type.*;

public class DomParserBolt implements IRichBolt {
    static Logger logger = LogManager.getLogger(DomParserBolt.class);
    OutputCollector collector;
    String executorId = UUID.randomUUID().toString();
    Fields schema = new Fields("url", "event");

    @Override
    public void cleanup() {

    }

    @Override
    public void execute(Tuple input) {
        String content = input.getStringByField("text");
        String url = input.getStringByField("url");
        if(Helper.isEmpty(url) || Helper.isEmpty(content)){
            return;
        }
        Document doc = Jsoup.parse(content, "", Parser.xmlParser());
        final boolean isXml = url.toLowerCase().endsWith(".xml");
        //traverse
        doc.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int i) {
                if(node.nodeName().startsWith("#document") || node.nodeName().startsWith("#comment") || node.nodeName().startsWith("#declaration")){
                    return;
                }
                if(node instanceof TextNode){
                    collector.emit(new Values<Object>(url, new OccurrenceEvent(Text, ((TextNode) node).text(), url)));
                    return;
                }
                collector.emit(new Values<Object>(url, new OccurrenceEvent(Open, node.nodeName(), url)));
            }

            @Override
            public void tail(Node node, int i) {
                if(node.nodeName().startsWith("#")){
                    return;
                }
                collector.emit(new Values<Object>(url, new OccurrenceEvent(Close, node.nodeName(), url)));
            }
        });
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
