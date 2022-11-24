https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.xpathengine;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Implement this factory to produce your XPath engine
 * and SAX handler as necessary.  It may be called by
 * the test/grading infrastructure.
 * 
 * @author cis455
 *
 */
public class XPathEngineFactory {
	static XPathEngine instance = null;
	public static XPathEngine getXPathEngine() {
		if(instance == null){
			instance = new XPathEngineImpl();
		}
		return instance;
	}
	
	public static DefaultHandler getSAXHandler() {
		return null;
	}
}
