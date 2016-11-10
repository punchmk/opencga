/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.opencga.core.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeUtils {

    private static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
    private static final String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";
    
    public static String getTime() {
        return getTime(new Date());
    }

    public static String getTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMddHHmmss);
        return sdf.format(date);
    }

    public static String getTimeMillis() {
        return getTimeMillis(new Date());
    }

    public static String getTimeMillis(Date date) {
        SimpleDateFormat sdfMillis = new SimpleDateFormat(yyyyMMddHHmmssSSS);
        return sdfMillis.format(date);
    }

    public static Date add24HtoDate(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.setTimeInMillis(date.getTime());// sumamos 24h a la fecha del login
        cal.add(Calendar.DATE, 1);
        return new Date(cal.getTimeInMillis());
    }

    public static Date toDate(String dateStr) {
        Date date = null;
        try {
            if (dateStr.length() == yyyyMMddHHmmss.length()) {
                SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMddHHmmss);
                date = sdf.parse(dateStr);
            } else {
                SimpleDateFormat sdfMillis = new SimpleDateFormat(yyyyMMddHHmmssSSS);
                date = sdfMillis.parse(dateStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
