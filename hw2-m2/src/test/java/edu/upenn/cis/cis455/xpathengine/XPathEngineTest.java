https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.xpathengine;

import org.junit.Assert;
import org.junit.Test;

public class XPathEngineTest {
    @Test
    public void test(){
        XPathEngine engine = XPathEngineFactory.getXPathEngine();
        engine.setXPaths(new String[]{"/rss/channel/title[text()=\"NYT\"]"});
        boolean isvalid = engine.isValid(0);
        Assert.assertEquals(isvalid, true);
    }

    @Test
    public void test_contains(){
        XPathEngine engine = XPathEngineFactory.getXPathEngine();
        engine.setXPaths(new String[]{"/rss/channel/title[contains(text(), \"sports\")]"});
        boolean isvalid = engine.isValid(0);
        Assert.assertEquals(isvalid, true);
    }

    @Test
    public void test_evalueate(){
        XPathEngine engine = XPathEngineFactory.getXPathEngine();

        engine.setXPaths(new String[]{"/rss/title[contains(text(), \"sports\")]"});
        engine.evaluateEvent(new OccurrenceEvent(OccurrenceEvent.Type.Open, "rss", "http://test.com"));
        engine.evaluateEvent(new OccurrenceEvent(OccurrenceEvent.Type.Open, "title", "http://test.com"));
        boolean[] last = engine.evaluateEvent(new OccurrenceEvent(OccurrenceEvent.Type.Text, "sports", "http://test.com"));
        engine.evaluateEvent(new OccurrenceEvent(OccurrenceEvent.Type.Close, "title", "http://test.com"));
        engine.evaluateEvent(new OccurrenceEvent(OccurrenceEvent.Type.Close, "rss", "http://test.com"));
        for(boolean b : last){
            System.out.println(b);
        }
        Assert.assertNotNull(last);
    }
}
