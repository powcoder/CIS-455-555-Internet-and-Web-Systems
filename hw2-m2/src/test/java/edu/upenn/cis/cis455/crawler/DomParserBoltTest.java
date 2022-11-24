https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

public class DomParserBoltTest {
    @Test
    public void test(){
        OutputCollector collector = mock(OutputCollector.class);
        DomParserBolt bolt = new DomParserBolt();
        bolt.prepare(null, null, collector);
        List<Object> list = new ArrayList<>();
        list.add("http://a.com");
        list.add("<html><head></head><body>123</body></html>");
        bolt.execute(new Tuple(new Fields("url", "text"), list));

        //3 tag * 2 + 1 text = 7
        verify(collector, atLeast(7)).emit(anyList());
    }

    @Test
    public void testXml(){
        OutputCollector collector = mock(OutputCollector.class);
        DomParserBolt bolt = new DomParserBolt();
        bolt.prepare(null, null, collector);
        List<Object> list = new ArrayList<>();
        list.add("http://a.com/b.xml");
        list.add("<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>" +
                "<rss><channel><title>12345</title><link></link></channel></rss>");
        bolt.execute(new Tuple(new Fields("url", "text"), list));

        verify(collector, atLeast(9)).emit(anyList());
    }
}
