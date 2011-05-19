/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Loic Quentin                                                        *
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

import java.util.ArrayList;


public class KloTraceBlock {

    private String _file;
    private String _method;
    private String _name;
    private int _id;
    private ArrayList<KloTraceLine> _TraceLine = new ArrayList<KloTraceLine>();

    public KloTraceBlock(String file, String method, String name, int id) {
        this._file = file;
        this._method = method;
        this._name = name;
        this._id = id;
    }

    public void addTraceLine(int line, String text, char type, int refId) {
        _TraceLine.add(new KloTraceLine(line, text, type, refId));
    }

    public void addTraceLine(int line, String text, char type) {
        _TraceLine.add(new KloTraceLine(line, text, type));
    }

    @SuppressWarnings("unused")
    public String get_file() {
        return _file;
    }

    @SuppressWarnings("unused")
    public String get_method() {
        return _method;
    }

    @SuppressWarnings("unused")
    public String get_name() {
        return _name;
    }

    @SuppressWarnings("unused")
    public int get_id() {
        return _id;
    }

    @Override
    public String toString() {
        String ret = "file: " + _file + ";method: " + _method + ";name: " + _name + ";id: " + _id;

        if (_TraceLine.size() != 0) {
            ret += "\nTraceline:\n";
            for (int i = 0; i < _TraceLine.size(); i++)
                ret += _TraceLine.get(i).toString() + "\n";
        }

        return ret;
    }


}
