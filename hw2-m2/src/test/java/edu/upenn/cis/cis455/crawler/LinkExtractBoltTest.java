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
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LinkExtractBoltTest {
    @Test
    public void test_extract(){
        LinkExtractBolt bolt = new LinkExtractBolt();
        String content = "<HTML><HEAD><TITLE>CSE455/CIS555 HW2 Sample Data</TITLE></HEAD>" +
                "<BODY><H2 ALIGN=center>CSE455/CIS555 HW2 Sample Data</H2>" +
                "<P>This page contains some sample data for your second homework assignment. The HTML pages " +
                "do not contain external links, so you shouldn't have to worry about your crawler &ldquo;" +
                "escaping&rdquo; to the outside web. The XML files do, however, contain links to external URLs," +
                " so you'll need to make sure your crawler does not follow links in XML documents.</P>" +
                "<H3>RSS Feeds</H3><UL><LI><A HREF=\"nytimes/\">The New York Times</A></LI><LI>" +
                "<A HREF=\"bbc/\">BBC News</A></LI><LI><A HREF=\"cnn/\">CNN</A></LI><LI>" +
                "<A HREF=\"international/\">News in foreign languages</A></LI></UL><H3>" +
                "Other XML data</H3><UL><LI><A HREF=\"misc/weather.xml\">Weather data</A></LI><LI>" +
                "<A HREF=\"misc/eurofxref-daily.xml\">Current Euro exchange rate data</A></LI><LI>" +
                "<A HREF=\"misc/eurofxref-hist.xml\">Historical Euro exchange rate data</A></LI></UL>" +
                "<H3>Marie's XML data</H3><UL><LI><A HREF=\"marie/\">More data</A></LI><LI><A HREF=\"marie/private\">Private</A>" +
                "</LI></UL><h3>Duplicates (for the content-seen test)</h3><ul><li><a href=\"misc/moreweather.xml\">More weather data</a> " +
                "(an exact duplicateof the <a HREF=\"misc/weather.xml\">weather data</a> from above)<li>" +
                "<a href=\"cnn/cnn_world.rss.xml\">CNN World News</a> (a duplicate linkthat also appears on the " +
                "<a href=\"cnn/index.html\">CNN</a> page)</ul></BODY></HTML>";
        List<String> list = bolt.extractLinks(content);

        Assert.assertEquals(13, list.size());
    }

    @Test
    public void test_extractParentDir(){
        LinkExtractBolt bolt = new LinkExtractBolt();
        String urlParent1 = bolt.extractParentDir(new URLInfo("http://www.test.com/test/index.html"));
        Assert.assertEquals(urlParent1, "www.test.com:80/test");
        String urlParent2 = bolt.extractParentDir(new URLInfo("http://www.test.com/test/"));
        Assert.assertEquals(urlParent2, "www.test.com:80/test");
    }
}
