https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.storage;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.*;
import edu.upenn.cis.cis455.crawler.utils.*;

import javax.xml.crypto.Data;
import java.io.File;
import java.util.*;

public class StorageFactory {
    //directory <-> StorageInterface
    private static Map<String, StorageInterface> map = new HashMap<>();
    private final static String USER_DB = "USER";/*User DB*/
    private final static String DOCS_DB = "DOCS";/*Document DB*/
    private final static String CATA_DB = "CATALOG";/*Catalog DB*/
    private final static String INFO_DB = "RUNINFO";/*Save other information, the next integer id of users and docs.*/
    private final static String CONTENT_SEEN_DB = "CONTENTSEEN";
    private final static String CHANNEL_DB = "CHANNEL";
    private final static String USERCHANNEL_DB = "USERCHANNEL";
    private final static String URLCHANNEL_DB = "URLCHANNEL";

    public static StorageInterface getDatabaseInstance(String directory) {
        synchronized (StorageFactory.class){
            if (map.containsKey(directory)) {
                return map.get(directory);
            }
        }
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        Environment env = new Environment(new File(directory), envConfig);

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);

        // catalog is needed for serial bindings (java serialization)
        Database catalogDb = env.openDatabase(null, CATA_DB, dbConfig);
        StoredClassCatalog catalog = new StoredClassCatalog(catalogDb);

        Database userDB = env.openDatabase(null, USER_DB, dbConfig);
        Database docsDB = env.openDatabase(null, DOCS_DB, dbConfig);
        Database infoDB = env.openDatabase(null, INFO_DB, dbConfig);
        Database contentMd5DB = env.openDatabase(null, CONTENT_SEEN_DB, dbConfig);
        Database channelDB = env.openDatabase(null, CHANNEL_DB, dbConfig);
        Database userChannelDB = env.openDatabase(null, USERCHANNEL_DB, dbConfig);
        Database urlChannelDB = env.openDatabase(null, URLCHANNEL_DB, dbConfig);

        // key and data bindings
        SerialBinding<String> infoKeyBinding = new SerialBinding<String>(catalog, String.class);
        SerialBinding<Integer> infoDataBinding = new SerialBinding<Integer>(catalog, Integer.class);

        SerialBinding<String> contentseenKeyBinding = new SerialBinding<String>(catalog, String.class);
        SerialBinding<Integer> contentseenDataBinding = new SerialBinding<Integer>(catalog, Integer.class);

        TupleBinding<Integer> intKeyBinding = TupleBinding.getPrimitiveBinding(Integer.class);
        SerialBinding<User> userDataBinding = new SerialBinding<User>(catalog, User.class);
        SerialBinding<Document> docDataBinding = new SerialBinding<Document>(catalog, Document.class);

        SerialBinding<String> stringKeyBinding = new SerialBinding<String>(catalog, String.class);
        SerialBinding<Channel> channelDataBinding = new SerialBinding<Channel>(catalog, Channel.class);
        SerialBinding<String> stringDataBinding = new SerialBinding<String>(catalog, String.class);
        SerialBinding<UserToChannel> userChannelDataBinding = new SerialBinding<UserToChannel>(catalog, UserToChannel.class);
        SerialBinding<UrlToChannel> urlChannelDataBinding = new SerialBinding<UrlToChannel>(catalog, UrlToChannel.class);

        StoredSortedMap docMap = new StoredSortedMap<Integer, Document>(docsDB, intKeyBinding, docDataBinding, true);
        StoredSortedMap userMap = new StoredSortedMap<Integer, User>(userDB, intKeyBinding, userDataBinding, true);
        StoredSortedMap infoMap = new StoredSortedMap<String, Integer>(infoDB, infoKeyBinding, infoDataBinding, true);
        StoredSortedMap contentMd5Map = new StoredSortedMap<String, Integer>(contentMd5DB, contentseenKeyBinding, contentseenDataBinding, true);
        StoredSortedMap channelMap = new StoredSortedMap<String, Channel>(channelDB, stringKeyBinding, channelDataBinding, true);
        StoredSortedMap userChannelMap = new StoredSortedMap<Integer, UserToChannel>(userChannelDB, intKeyBinding, userChannelDataBinding, true);
        StoredSortedMap urlChannelMap = new StoredSortedMap<Integer, UrlToChannel>(urlChannelDB, intKeyBinding, urlChannelDataBinding, true);

        if(userDB.count() == 0){
            infoMap.put(USER_DB + "_NEXTID", 1);
        }
        if(docsDB.count() == 0){
            infoMap.put(DOCS_DB + "_NEXTID", 1);
        }
        if(userChannelDB.count() == 0){
            infoMap.put(USERCHANNEL_DB + "_NEXTID", 1);
        }
        if(urlChannelDB.count() == 0){
            infoMap.put(URLCHANNEL_DB + "_NEXTID", 1);
        }

        StorageInterface si = new StorageInterface() {
            @Override
            public int getCorpusSize() {
                return (int)docsDB.count();
            }

            @Override
            public int addDocument(String url, String documentContents) {
                if(Helper.isEmpty(url) || Helper.isEmpty(documentContents)){
                    return 0;
                }
                synchronized (docMap){
                    String md5key = Helper.md5(documentContents);
                    if(contentMd5Map.containsKey(md5key)){
                        return -1;
                    }
                    Integer nextId = (Integer)infoMap.get(DOCS_DB + "_NEXTID");
                    docMap.put(nextId, new Document(nextId, url, documentContents, System.currentTimeMillis()));
                    int storedId = nextId;
                    contentMd5Map.put(md5key, storedId);
                    nextId += 1;
                    infoMap.put(DOCS_DB + "_NEXTID", nextId);
                    return storedId;
                }
            }

            @Override
            public String getDocument(String url) {
                if(Helper.isEmpty(url)){
                    return null;
                }
                Iterator<Map.Entry<Integer, Document>> iter = docMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Integer, Document> entry = iter.next();
                    if(url.equals(entry.getValue().getUrl())){
                        return entry.getValue().getContent();
                    }
                }
                return null;
            }

            @Override
            public int addUser(String username, String password) {
                if(Helper.isEmpty(username) || Helper.isEmpty(password)){
                    return 0;
                }
                synchronized (userMap){
                    //check the username
                    Iterator<Map.Entry<Integer, User>> iter = userMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<Integer, User> entry = iter.next();
                        if(username.equals(entry.getValue().getUsername())){
                            //the user exists
                            return -1;
                        }
                    }
                    Integer nextId = (Integer)infoMap.get(USER_DB + "_NEXTID");
                    userMap.put(nextId, new User(nextId, username, Helper.sha256(password)));
                    int storedId = nextId;
                    nextId += 1;
                    infoMap.put(USER_DB + "_NEXTID", nextId);
                    return storedId;
                }
            }

            @Override
            public boolean getSessionForUser(String username, String password) {
                if(Helper.isEmpty(username) || Helper.isEmpty(password)){
                    return false;
                }
                Iterator<Map.Entry<Integer, User>> iter = userMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Integer, User> entry = iter.next();
                    String encodedPassword = Helper.sha256(password);
                    if(username.equals(entry.getValue().getUsername())
                            && encodedPassword.equals(entry.getValue().getPassword())){
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void close() {
                docsDB.close();
                userDB.close();
                env.close();
            }

            @Override
            public boolean existsContent(String content) {
                String md5Hash = Helper.md5(content);
                return contentMd5Map.containsKey(md5Hash);
            }

            @Override
            public List<String> docs() {
                Iterator<Map.Entry<Integer, Document>> iter = docMap.entrySet().iterator();
                List<String> list = new ArrayList<>();
                while (iter.hasNext()) {
                    Map.Entry<Integer, Document> entry = iter.next();
                    list.add(entry.getValue().getUrl());
                }
                return list;
            }

            @Override
            public void saveChannel(String username, String channelName, String xpath) {
                channelMap.put(channelName, new Channel(channelName, xpath, username, System.currentTimeMillis()));
            }

            @Override
            public List<Channel> channels() {
                Iterator<Map.Entry<String, Channel>> iter = channelMap.entrySet().iterator();
                List<Channel> list = new ArrayList<>();
                while (iter.hasNext()) {
                    Map.Entry<String, Channel> entry = iter.next();
                    list.add(entry.getValue());
                }
                return list;
            }

            @Override
            public List<Document> bychannel(String name) {
                Iterator<Map.Entry<Integer, UrlToChannel>> iter = urlChannelMap.entrySet().iterator();
                List<Document> list = new ArrayList<>();
                while (iter.hasNext()) {
                    Map.Entry<Integer, UrlToChannel> entry = iter.next();
                    UrlToChannel urlToChannel = entry.getValue();
                    if(urlToChannel == null){
                        continue;
                    }
                    if(!urlToChannel.getChannelName().equals(name)){
                        continue;
                    }
                    Iterator<Map.Entry<Integer, Document>> iterForDocument = docMap.entrySet().iterator();
                    while (iterForDocument.hasNext()) {
                        Map.Entry<Integer, Document> entryForDocument = iterForDocument.next();
                        if(urlToChannel.getUrl().equals(entryForDocument.getValue().getUrl())){
                            list.add(entryForDocument.getValue());
                            break;
                        }
                    }
                }
                return list;
            }

            @Override
            public Channel getChannel(String channelName) {
                if(Helper.isEmpty(channelName)){
                    return null;
                }
                return (Channel) channelMap.get(channelName);
            }

            @Override
            public void linkChannelToUrl(String url, String channel) {
                if(Helper.isEmpty(url) || Helper.isEmpty(channel)){
                    return;
                }
                List<Document> docs = bychannel(channel);
                for(Document d : docs){
                    if(url.equals(d.getUrl())){
                        return;
                    }
                }
                synchronized (urlChannelMap){
                    Integer nextId = (Integer)infoMap.get(URLCHANNEL_DB + "_NEXTID");
                    urlChannelMap.put(nextId, new UrlToChannel(url, channel));
                    nextId += 1;
                    infoMap.put(URLCHANNEL_DB + "_NEXTID", nextId);
                }
            }
        };
        map.put(directory, si);
        return si;
    }
}
