/*
 * Ventplan
 * ventplan, ventplan
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com/
 * Copyright (C) 2011-2013 art of coding UG, http://www.art-of-coding.eu/
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 19.03.13 17:23
 */
package com.ventplan.desktop

import java.text.SimpleDateFormat

/**
 * Calculations around dates, especially 'today'.
 */
class Today {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT

    static {
        SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)
        SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getDefault())
    }

    /**
     * Private constructor: you should not create an instance of this class. Use its static methods.
     */
    private Today() {
    }

    public static String getIsoDateAsString() {
        return SIMPLE_DATE_FORMAT.format(new Date())
    }

    public static boolean isAfter(String iso1, String iso2) {
        Date _iso1 = SIMPLE_DATE_FORMAT.parse(iso1)
        Date _iso2 = SIMPLE_DATE_FORMAT.parse(iso2)
        return _iso1.after(_iso2)
    }

    public static boolean isBefore(String iso1, String iso2) {
        Date _iso1 = SIMPLE_DATE_FORMAT.parse(iso1)
        Date _iso2 = SIMPLE_DATE_FORMAT.parse(iso2)
        return _iso1.before(_iso2)
    }

    public static boolean isEqual(String iso1, String iso2) {
        Date _iso1 = SIMPLE_DATE_FORMAT.parse(iso1)
        Date _iso2 = SIMPLE_DATE_FORMAT.parse(iso2)
        return _iso1.equals(_iso2)
    }

    /**
     * Is a date in a range between 'begin' and 'end'?
     * @param begin Begin of date range.
     * @param date The date to verify wether it's in the range or not.
     * @param end End of date range.
     * @return true when 'date' is between 'begin' or 'end'. 
     */
    public static boolean isInDateRange(String begin, String date, String end) {
        boolean dateAfterBegin = isEqual(date, begin) || isAfter(date, begin)
        boolean dateBeforeEnd = isEqual(date, end) || isBefore(date, end)
        boolean b = dateAfterBegin && dateBeforeEnd
        b
    }

    /**
     * Is 'today' in a range between 'begin' and 'end'?
     * @param begin Begin of date range.
     * @param end End of date range.
     * @return true when 'date' is between 'begin' or 'end'. 
     */
    public static boolean isTodayInDateRange(String begin, String end) {
        return isInDateRange(begin, getIsoDateAsString(), end)
    }

    /**
     * Is a date in a range between 'begin' and 'end' or at least 'after begin'?
     * @param begin Begin of date range.
     * @param date The date to verify wether it's in the range or not.
     * @param end End of date range.
     * @return true when 'date' is between 'begin' or 'end'. 
     */
    public static boolean isInDateRangeOrAtLeastAfterBegin(String begin, String date, String end) {
        boolean dateAfterBegin = isEqual(date, begin) || isAfter(date, begin)
        boolean dateBeforeEnd = false
        try { dateBeforeEnd = isEqual(date, end) || isBefore(date, end) } catch(e) {}
        boolean b = false
        if (dateAfterBegin && dateBeforeEnd) {
            b = true
        } else if (dateAfterBegin) {
            b = true
        } else {
            b = false
        }
        b
    }

    /**
     * Is 'today' in a range between 'begin' and 'end' or at least 'after begin'?
     * @param begin Begin of date range.
     * @param end End of date range.
     * @return true when 'date' is between 'begin' or 'end'. 
     */
    public static boolean isTodayInDateRangeOrAtLeastAfterBegin(String begin, String end) {
        return isInDateRangeOrAtLeastAfterBegin(begin, getIsoDateAsString(), end)
    }

}
