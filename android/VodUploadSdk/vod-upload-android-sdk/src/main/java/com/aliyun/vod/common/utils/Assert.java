/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.common.utils;

public class Assert {

    public static <T>
    T fail(Object detailMessage) {
        throw new AssertionError(detailMessage);
    }

    public static <T>
    T fail() {
        throw new AssertionError("failure");
    }

    public static void assertNotNull(Object value) {
        if (value == null) {
            throw new AssertionError("unexpected null");
        }
    }

    public static void assertNull(Object actual) {
        assertEquals(null, actual);
    }

    public static void assertTrue(Object actual) {
        assertEquals(true, actual);
    }

    public static void assertFalse(Object actual) {
        assertEquals(false, actual);
    }

    public static void assertEquals(int expected, int actual) {
        assertEquals((Integer) expected, (Integer) actual);
    }

    public static void assertEquals(Object expected, Object actual) {
        if (expected == actual) {
            return;
        }

        if (expected != null && actual != null && expected.equals(actual)) {
            return;
        }

        throw new AssertionError("expected " + expected + ", got " + actual);
    }

    public static void assertNotEquals(Object expected, Object actual) {
        if (expected.equals(actual)) {
            throw new AssertionError("unexpected equality: " + actual);
        }
    }

    public static void assertSame(Object expected, Object actual) {
        if (expected == actual) {
            return;
        }
        throw new AssertionError("expected " + expected + ", got " + actual);
    }

    public static void assertNotSame(Object expected, Object actual) {
        if (expected != actual) {
            return;
        }
        throw new AssertionError("unexpected " + actual);
    }

    public static void assertGreaterThan(int actual, int value) {
        if (actual > value) {
            return;
        }
        throw new AssertionError("unexpected " + actual + " <= " + value);
    }

    public static void assertLessOrEqual(int actual, int value) {
        if (actual <= value) {
            return;
        }
        throw new AssertionError("unexpected " + actual + " > " + value);
    }
}
