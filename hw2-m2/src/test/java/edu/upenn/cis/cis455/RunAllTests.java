https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.upenn.cis.cis455.crawler.CrawlerTest;
import edu.upenn.cis.cis455.crawler.DocumentFetcherBoltTest;
import edu.upenn.cis.cis455.crawler.DomParserBoltTest;
import edu.upenn.cis.cis455.crawler.LinkExtractBoltTest;
import edu.upenn.cis.cis455.storage.StorageInterfaceTest;
import edu.upenn.cis.cis455.xpathengine.XPathEngineTest;

@RunWith(Suite.class)
@SuiteClasses({ CrawlerTest.class, DocumentFetcherBoltTest.class, DomParserBoltTest.class,LinkExtractBoltTest.class,StorageInterfaceTest.class,XPathEngineTest.class })
public class RunAllTests {

}
