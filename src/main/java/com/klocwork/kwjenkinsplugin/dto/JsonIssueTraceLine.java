/*
 * *****************************************************************************
 * Copyright (c) 2020 Rogue Wave Software, Inc., a Perforce company
 * Author : Klocwork
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * *****************************************************************************
 */

package com.klocwork.kwjenkinsplugin.dto;

public class JsonIssueTraceLine {
    private Integer line;
    public Integer column;
    private String text;
    private TraceLineType type;
    private Integer refId;

    public Integer getLine() {
        return line;
    }

    public void setLine(final Integer line) {
        this.line = line;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(final Integer column) {
        this.column = column;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public TraceLineType getType() {
        return type;
    }

    public void setType(final TraceLineType type) {
        this.type = type;
    }

    public Integer getRefId() {
        return refId;
    }

    public void setRefId(final Integer refId) {
        this.refId = refId;
    }

    public enum TraceLineType {
        NULL('N'),
        CONDITION('C'),
        EVENT('E');

        private final char type;

        private TraceLineType(char type) {
            this.type = type;
        }

        public char getType() {
            return this.type;
        }

        public static TraceLineType parseType(Character type) {
            switch(type) {
                case 'C':
                    return CONDITION;
                case 'E':
                    return EVENT;
                default:
                    return NULL;
            }
        }

        public static TraceLineType parseType(String type) {
            return type != null ? parseType(type.charAt(0)) : NULL;
        }

        public static String toString(TraceLineType type) {
            return Character.toString(type.type);
        }

        public String toString() {
            return toString(this);
        }
    }
}
