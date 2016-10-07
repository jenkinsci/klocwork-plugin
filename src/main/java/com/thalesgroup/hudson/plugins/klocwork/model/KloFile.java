/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Loic Quentin                                                        *
 *
 *		                                                                       *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *                                                                              *
 *******************************************************************************/
package com.thalesgroup.hudson.plugins.klocwork.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;


public class KloFile implements Serializable {

    private static final long serialVersionUID = -5141540487527114137L;
    private ArrayList<KloTraceBlock> traceBlock = new ArrayList<KloTraceBlock>();
    private Integer key;

    /**
     * AM : Adding a hashmap to store the different tags of the XML file and their values.
     * Key : tags name / Value : tags text
     * In the beginning, Object class was chosen as type of the values of the map, but an Exception is thrown and the result of the Hudson build is not saved if it succeeds because Object doesn't implement Serializable.
     * If you want to change the type of the values, change it into something implementing Serializable.
     */
    //private HashMap<String, Object> elements = new HashMap<String, Object>();
    private HashMap<String, String> elements = new HashMap<String, String>();

    public void addTraceBlock(String file, String method, String name, int id) {

        traceBlock.add(new KloTraceBlock(file, method, name, id));
    }

    public void addTraceLine(int id, int line, String text, char type) {

        traceBlock.get(id).addTraceLine(line, text, type);
    }

    public void addTraceLine(int id, int line, String text, char type, int refId) {

        traceBlock.get(id).addTraceLine(line, text, type, refId);
    }

    /**
     * @return the serialVersionUID
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /**
     * @return the key
     */
    public Integer getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(Integer key) {
        this.key = key;
    }

//	@Override
//	public String toString(){
//		String ret = "  \nanchor "  + getAnchor() +  " \ncategory " 
//		+ getCategory() +  " \ncode "  + getCode()
//		+  " \ncodeNum "  + getCodeNum() +  " \ncolumn " 
//		+ getColnum() +  " \ndateFixed "  + getDateFix()
//		+  " \ndateOriginated "  + getDateOrigin() +  " \ndisplayAs " 
//		+ getDisplayAs() +  " \nfile "  + getFile()
//		+  " \nfileShortName "  + getFileName() +  " \nfileUeid " 
//		+ getFileUeid() +  " \nfilter "  + getFilter()
//		+  " \nlastComment "  + getLastComment() +  " \nline " 
//		+ getLine() +  " \nmsg "  + getMessage() +  " \nmethod " 
//		+ getMethod() +  " \nid "  + getProblemId()
//		+  " \nseverity "  + getSeverity() +  " \nstate " 
//		+ getState() +  " \ncitingStatus "  + getStatus() +  " \n " ; 
//
//		if(traceBlock.size() != 0)
//		{
//			for(int i=0;i<traceBlock.size();i++){
//				ret += traceBlock.get(i).toString();
//			}
//		}	
//		
//		return ret;
//	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> e : elements.entrySet()) {
            sb.append(e.getKey()).append(" : ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    public void store(String key, String value) {
        elements.put(key, value);
    }

    public String get(String key) {
        return elements.get(key);
    }

    public Set<Entry<String, String>> entrySet() {
        return elements.entrySet();
    }

    public int size() {
        return elements.size();
    }

}
