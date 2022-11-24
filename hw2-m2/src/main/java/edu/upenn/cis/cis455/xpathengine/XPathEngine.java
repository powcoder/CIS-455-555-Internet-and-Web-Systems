https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.xpathengine;

public interface XPathEngine {


	/**
	 * Sets the XPath expression(s) that are to be evaluated.
	 * @param expressions
	 */
	void setXPaths(String[] expressions);

	/**
	 * Returns true if the i.th XPath expression given to the last setXPaths() call
	 * was valid, and false otherwise. If setXPaths() has not yet been called, the
	 * return value is undefined. 
	 * @param i
	 * @return
	 */
	boolean isValid(int i);
	
	/**
	 * Event driven pattern match.
	 * 
	 * Takes an event at a time as input
	 *
	 * @param event notification about something parsed, from a
	 * given document
	 * 
 	 * @return bit vector of matches to XPaths
	 */
	boolean[] evaluateEvent(OccurrenceEvent event);

}
