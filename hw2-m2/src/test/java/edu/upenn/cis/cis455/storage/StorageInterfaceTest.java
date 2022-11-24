https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.storage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StorageInterfaceTest {
    StorageInterface db = null;
    String envPath = "testdb";
    @Before
    public void createFile(){

        if (!Files.exists(Paths.get(envPath))) {
            try {
                Files.createDirectory(Paths.get(envPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.db = StorageFactory.getDatabaseInstance(envPath);
    }
    @After
    public void clearFile(){
        File dir = new File(envPath);
        if(!dir.exists()){
            return;
        }
        if(dir.isDirectory()){
            File[] files = dir.listFiles();
            for(File f : files){
                if(f.isFile()){
                    f.delete();
                }
            }
        }
        dir.delete();
    }

    @Test
    public void testUserFunction(){
        db.addUser("admin", "1234");
        boolean result = db.getSessionForUser("admin", "1234");

        Assert.assertEquals(true, result);
    }

    @Test
    public void testDocumentFunction(){
        db.addDocument("http://www.test.com", "hello,world");
        String content = db.getDocument("http://www.test.com");

        Assert.assertEquals("hello,world", content);
    }

    @Test
    public void testShaContent(){
        db.addDocument("http://www.test.com", "hello,world");
        boolean result = db.existsContent("hello,world");

        Assert.assertEquals(true, result);
    }
}
