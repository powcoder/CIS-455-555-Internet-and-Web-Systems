https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.xpathengine;

/**
 This class encapsulates the tokens we care about parsing in XML (or HTML)
 */
public class OccurrenceEvent {
	public enum Type {Open, Close, Text};
	
	Type type;
	String value;
	String url;
	
	public OccurrenceEvent(Type t, String value, String url) {
		this.type = t;
		this.value = value;
		this.url = url;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String toString() {
		if (type == Type.Open) 
			return "<" + value + ">";
		else if (type == Type.Close)
			return "</" + value + ">";
		else
			return value;
	}
}
