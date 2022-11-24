https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.xpathengine;


import edu.upenn.cis.cis455.crawler.utils.Helper;
import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.XPathEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XPathEngineImpl implements XPathEngine {
    String[] expressions;
    HashMap<String, String> map = new HashMap();
    enum Token{
        XPATH, STEP, SLASH, NODENAME, TEST
    }
    @Override
    public void setXPaths(String[] expressions) {
        this.expressions = expressions;
    }

    private boolean isLetter(char c){
        if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')){
            return true;
        }
        return false;
    }

    private boolean isNum(char c){
        if(c >= '0' && c <= '9'){
            return true;
        }
        return false;
    }

    @Override
    public boolean isValid(int i) {
        if(expressions == null){
            return false;
        }
        String expression = expressions[i];
        int k = 0;
        Token lastToken = Token.XPATH;
        while(k < expression.length()){
            if(expression.charAt(k) == ' '){
                k ++;
                continue;
            }
            char c = expression.charAt(k);
            if(lastToken == Token.XPATH){
                if(c != '/'){
                    return false;
                }
                k++;
                lastToken = Token.SLASH;
            } else if(lastToken == Token.SLASH){
                if(c == '/'){
                    return false;
                }
                if(isNum(c)){
                    //node name cannot start with number
                    return false;
                }
                if(isLetter(c)){
                    String nodeName = "";

                    //read whole node name
                    while(k < expression.length() && (isNum(expression.charAt(k)) || isLetter(expression.charAt(k)))){
                        nodeName += expression.charAt(k);
                        k++;
                    }
                    lastToken = Token.NODENAME;
                    continue;
                }
                return false;
            } else if(lastToken == Token.NODENAME){
                if(c != '[' && c != '/'){
                    return false;
                }
                if(c == '['){
                    String test = "";
                    k++;
                    int quota = 0;
                    while(k < expression.length() && expression.charAt(k) != ']'){
                        if(expression.charAt(k) == ' ' && quota != 1){
                            k++;
                            continue;
                        }
                        if(quota == 2 && expression.charAt(k) != ')'){
                            return false;
                        }
                        test += expression.charAt(k);
                        if(quota == 0 && expression.charAt(k) == '"'){
                            quota = 1;
                        } else if(quota == 1 && expression.charAt(k) == '"'){
                            quota = 2;
                        }
                        k++;
                    }
                    if(k >= expression.length() && expression.charAt(k) != ']'){
                        return false;
                    }

                    if(test.startsWith("text()=\"")){
                        if(!test.endsWith("\"")){
                            return false;
                        }
                    } else if(test.startsWith("contains(text(),\"")){
                        if(!test.endsWith("\")")){
                            return false;
                        }
                    } else {
                        return false;
                    }
                    k++;
                    lastToken = Token.TEST;
                    continue;
                } else if(c == '/'){
                    k++;
                    lastToken = Token.SLASH;
                    continue;
                }
            } else if(lastToken == Token.TEST){
                if(c != '/'){
                    return false;
                }
                k++;
                continue;
            }
        }
        return true;
    }

    @Override
    public boolean[] evaluateEvent(OccurrenceEvent event) {
        if(event == null){
            return null;
        }
        if(expressions == null){
            return null;
        }
        String url = event.getUrl();
        String value = event.getValue();
        OccurrenceEvent.Type type = event.getType();

        if(type == OccurrenceEvent.Type.Open){
            if(!map.containsKey(url)){
                map.put(url, "/" + value);
            } else {
                map.put(url, map.get(url) + "/" + value);
            }
        } else if(type == OccurrenceEvent.Type.Close){
            String parentPath = map.get(url);
            parentPath = parentPath.substring(0, parentPath.length() - ("/" + value).length());
            map.put(url, parentPath);
            return null;
        }
        boolean[] arr = new boolean[expressions.length];
        for(int i = 0; i < expressions.length; i++){
            if(isValid(i)){
                String exp = expressions[i];
                String[] segs = exp.trim().split("/");
                String current = map.get(url);//parent path
                if(Helper.isEmpty(current)){
                    return null;
                }
                String[] currentSegs = current.trim().split("/");
                int k = 0;
                List<Integer> testExpIdx = new ArrayList<>();
                boolean result = true;
                for(k = 0; k < currentSegs.length; k++){
                    if(k >= segs.length){
                        result = false;
                        break;
                    }
                    if(!currentSegs[k].equals(segs[k]) && !segs[k].contains("text()")){
                        result = false;
                        break;
                    } else if(segs[k].contains("text()")){
                        testExpIdx.add(k);
                    }
                }
                if(k >= currentSegs.length && k < segs.length){
                    arr[i] = false;
                    break;
                }
                //check all the test exps
                for(Integer j : testExpIdx){
                    if(type == OccurrenceEvent.Type.Text && j < segs.length){
                        if(!segs[j].startsWith(currentSegs[j])){
                            result = false;
                            break;
                        }

                        if(segs[j].substring(0, currentSegs[j].length()).startsWith("[text()")){
                            int idx = segs[j].indexOf("\"");
                            int idxBack = segs[j].lastIndexOf("\"");
                            String compareTo = segs[j].substring(idx + 1, idxBack);
                            if(!value.equals(compareTo)){
                                result = false;
                                break;
                            }
                        } else {
                            int idx = segs[j].indexOf("\"");
                            int idxBack = segs[j].lastIndexOf("\"");
                            String compareTo = segs[j].substring(idx + 1, idxBack);
                            if(!value.startsWith(compareTo)){
                                result = false;
                                break;
                            }
                        }
                    } else if(j >= segs.length){
                        result = false;
                        break;
                    } else if(type != OccurrenceEvent.Type.Text){
                        result = false;
                        break;
                    }
                }
                arr[i] = result;
            }
        }
        return arr;
    }
}
