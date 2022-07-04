
/*
 * *****************************************************************************
 * Copyright (c) 2022 Rogue Wave Software, Inc., a Perforce company
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

package com.klocwork.kwjenkinsplugin.util;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class reads versions of the following forms: X.X, X.X.X and X.X.X.X
 * Versions are stored as 3 integers.
 * If the third integer is missing (ex: 1.2), the third integer is assumed to be 0. (ex: 1.2.0)
 * If there are more than 3 integers (ex: 1.2.3.4), only the first three are retained. (ex: 1.2.3)
 * Output (toString()) returns 3 integers. (ex: 1.2.3)
 *
 */
public class KlocworkVersion {
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d*)\\.(\\d*)(\\.(\\d*))?(\\.(\\d*))*");
    public static final String UNKNOWN_VERSION_STR = "Unknown";
    public static final KlocworkVersion UNKNOWN = new KlocworkVersion(UNKNOWN_VERSION_STR);
    private static final int[] EMPTY_ARRAY = {0, 0, 0};
    private static final Map<String, KlocworkVersion> cache = new HashMap<>();
    static {
        cache.put(UNKNOWN.toString(), UNKNOWN);
    }

    public static final KlocworkVersion LICENSE_PROVIDER_INTRODUCED    = create("2022.2");

    private int[] version = new int[3];

    private KlocworkVersion(final String name) {
        if (!UNKNOWN_VERSION_STR.equalsIgnoreCase(name)) {
            final String[] versions = name.split("\\.");
            for (int i = 0; i < versions.length && i < version.length; i++) {
                version[i] = Integer.parseInt(versions[i]);
            }
        }
    }

    private KlocworkVersion(final int major, final int minor, final int release) {
        version = new int[]{major, minor, release};
    }

    public int getMajor() {
        return version[0];
    }

    public int getMinor() {
        return version[1];
    }

    public int getRelease() {
        return version[2];
    }

    public boolean lessThan(final KlocworkVersion other) {
        if (other.equals(this)) {
            return false;
        }

        for (int i = 0; i < version.length; i++) {
            if (version[i] < other.version[i]) return true;
            if (version[i] > other.version[i]) return false;
        }

        return false;
    }

    public boolean lessThanOrEqual(final KlocworkVersion other) {
        return equals(other) || lessThan(other);
    }

    public boolean greaterThan(final KlocworkVersion other) {
        return !lessThanOrEqual(other);
    }

    public boolean greaterThanOrEqual(final KlocworkVersion other) {
        return !lessThan(other);
    }

    public String toString() {
        if (Arrays.equals(version, EMPTY_ARRAY)) {
            return UNKNOWN_VERSION_STR;
        }
        return version[0] + "." + version[1] + "." + version[2];
    }

    public String toStringMajMin() {
        if (Arrays.equals(version, EMPTY_ARRAY)) {
            return UNKNOWN_VERSION_STR;
        }
        return version[0] + "." + version[1];
    }

    public static KlocworkVersion create(final String version) {
        if (version == null) {
            return UNKNOWN;
        }
        final Matcher matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.find()) {
            return UNKNOWN;
        }
        final int maj = Integer.parseInt(matcher.group(1));
        final int min = Integer.parseInt(matcher.group(2));
        final int rel = matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4));
        return create(maj, min, rel);
    }

    public static KlocworkVersion create(final int major, final int minor, final int release) {
        synchronized (cache) {
            KlocworkVersion version = cache.get(major + "." + minor + "." + release);
            if (version == null) {
                version = new KlocworkVersion(major, minor, release);
                cache.put(version.toString(), version);
            }
            return version;
        }
    }

    public static KlocworkVersion create(final int major, final int minor) {
        return create(major, minor, 0);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final KlocworkVersion version1 = (KlocworkVersion) o;

        return Arrays.equals(version, version1.version);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(version);
    }

    public boolean equalsMajorMinor(KlocworkVersion v2) {
        if (v2 == null) {
            return false;
        }
        return version[0] == v2.version[0] && version[1] == v2.version[1];
    }

    public boolean equalsMajor(KlocworkVersion v2) {
        if (v2 == null) {
            return false;
        }
        return version[0] == v2.version[0];
    }

}
