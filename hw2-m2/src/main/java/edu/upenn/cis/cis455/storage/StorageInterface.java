https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.storage;

import edu.upenn.cis.cis455.crawler.utils.Channel;
import edu.upenn.cis.cis455.crawler.utils.Document;

import java.util.List;
import java.util.Map;

public interface StorageInterface {

    /**
     * How many documents so far?
     */
    public int getCorpusSize();

    /**
     * Add a new document, getting its ID
     */
    public int addDocument(String url, String documentContents);

    /**
     * Retrieves a document's contents by URL
     */
    public String getDocument(String url);

    /**
     * Adds a user and returns an ID
     */
    public int addUser(String username, String password);

    /**
     * Tries to log in the user, or else throws a HaltException
     */
    public boolean getSessionForUser(String username, String password);

    /**
     * Shuts down / flushes / closes the storage system
     */
    public void close();

    /**
     * Check if content exist
     */
    public boolean existsContent(String content);

    /**
     * list all docs, limit 100
     */
    public List<String> docs();

    /**
     * save channel
     */
    public void saveChannel(String username, String channelName, String xpath);

    /**
     * List all channels name
     */
    public List<Channel> channels();

    /**
     * List documents by channel
     */
    public List<Document> bychannel(String name);

    /**
     * get channel by name
     */
    public Channel getChannel(String channelName);

    /**
     * Link channel to url
     */
    public void linkChannelToUrl(String url, String channel);
}
