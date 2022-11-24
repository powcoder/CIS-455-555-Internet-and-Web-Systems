https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.utils.RobotsTxtInfo;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.mockito.Mockito.mock;


public class DocumentFetcherBoltTest {
    @Test
    public void test_delay(){
        StorageInterface db = mock(StorageInterface.class);
        Crawler crawler = new Crawler("http://www.test.com", db, 0, 0);
        HashMap<String, RobotsTxtInfo> robotsMap = new HashMap<>();
        RobotsTxtInfo info = new RobotsTxtInfo();
        info.addCrawlDelay("cis455crawler", 100);
        robotsMap.put("www.test.com:80", info);
        crawler.setRobotsMap(robotsMap);

        OutputCollector collector = mock(OutputCollector.class);
        DocumentFecherBolt bolt = new DocumentFecherBolt();
        bolt.prepare(null, null, collector);
        //two requests
        boolean b1 = bolt.deferCrawl("http://www.test.com");
        boolean b2 = bolt.deferCrawl("http://www.test.com");
        Assert.assertEquals(b1, false);
        Assert.assertEquals(b2, true);
    }
}
