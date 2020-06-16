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

package com.klocwork.kwjenkinsplugin.definitions;

import java.util.HashMap;
import java.util.Map;

public enum Status {
    ANALYZE(1, "Analyze"),
    IGNORE(2, "Ignore"),
    NOT_A_PROBLEM(3, "Not a Problem"),
    FIX(4, "Fix"),
    FIX_IN_NEXT_RELEASE(5, "Fix in Next Release"),
    FIX_IN_LATER_RELEASE(6, "Fix in Later Release"),
    DEFER(7, "Defer"),
    FILTER(8, "Filter");

    private static final Map<String, Status> NAME_VALUES = new HashMap<String, Status>();

    static {
        for (final Status status : Status.values()) {
            NAME_VALUES.put(status.name, status);
        }
    }

    private static final Map<Integer, Status> ID_VALUES = new HashMap<Integer, Status>();

    static {
        for (final Status status : Status.values()) {
            ID_VALUES.put(status.id, status);
        }
    }

    private final int id;
    private final String name;

    Status(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public static Status getValue(final String value) {
        if (value == null) {
            return null;
        }
        final Status result = NAME_VALUES.get(value);
        if (result == null) {
            throw new IllegalArgumentException("There is no status for value " + value); //NON-NLS
        }
        return result;
    }

    public static Status getValue(final int value) {
        final Status result = ID_VALUES.get(value);
        if (result == null) {
            throw new IllegalArgumentException("There is no status for id " + value); //NON-NLS
        }
        return result;
    }
}
