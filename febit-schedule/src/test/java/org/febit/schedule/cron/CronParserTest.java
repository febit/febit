/**
 * Copyright 2013-present febit.org (support@febit.org)
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
package org.febit.schedule.cron;

import org.febit.lang.Time;
import org.febit.lang.TimeTestUtil;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
public class CronParserTest {

    @Test
    public void test() {
        Matcher matcher;

        matcher = new CronParser("* * * * *").parse();
        matcher = new CronParser("* * *").parse();
        matcher = new CronParser("").parse();
        matcher = new CronParser("1,3,4  5,6-8  8/3,0-10/2|12,34 * 2,111,33").parse();

        matcher = new CronParser("8-10/3,5 1").parse();

        int i = 0;
    }

    @Test
    public void testMinute() {
        Matcher matcher;

        //ALL
        assertSame(Matcher.MATCH_ALL, new CronParser((String) null).parse());
        assertSame(Matcher.MATCH_ALL, new CronParser("* *").parse());
        assertSame(Matcher.MATCH_ALL, new CronParser("* * *").parse());
        assertSame(Matcher.MATCH_ALL, new CronParser("* * * * * * * * *").parse());

        //range
        matcher = new CronParser("0-100 * * * *").parse();
        assertTrue(matcher instanceof MinuteMatcher);
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 0, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 2, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 3, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 4, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 59, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 60, 1, true)));

        // div
        matcher = new CronParser("*/2 * * * *").parse();

        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 0, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 2, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 100, 1, true)));

        //range div & list
        matcher = new CronParser("54,1-4/2,55,100 * * * *").parse();

        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 0, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 2, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 3, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 4, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 55, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 59, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 60, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 100, 1, true)));

        //range div & list 2
        matcher = new CronParser("0-59/3,5,8 * * *").parse();

        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 0, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 2, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 3, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 4, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 5, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 6, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 7, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 8, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 59, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 60, 1, true)));

        //range div 2
        matcher = new CronParser("3/2 * * * *").parse();

        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 0, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 2, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 3, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 4, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 59, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 60, 1, true)));

    }

    @Test
    public void testHour() {
        Matcher matcher;

        matcher = new CronParser("3,5,8 1,2,3 * *").parse();

        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 0, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 2, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 3, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 4, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 5, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 1, 8, 1, true)));

        assertTrue(matcher.match(createTime(1, 1, 1, 1, 2, 3, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 1, 3, 3, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 4, 3, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 24, 3, 1, true)));

    }

    @Test
    public void testDay() {
        Matcher matcher;

        matcher = new CronParser("* * 2,4,*/3 *").parse();

        assertTrue(matcher.match(createTime(1, 1, 1, 0, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 1, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 2, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 3, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 4, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 1, 1, 5, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 1, 1, 6, 1, 1, 1, true)));
    }

    @Test
    public void testMouth() {

        Matcher matcher;

        matcher = new CronParser("* * * */2 * *").parse();

        assertFalse(matcher.match(createTime(1, 2014, 1, 1, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 2014, 2, 1, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 2014, 3, 1, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 2014, 4, 1, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 2014, 5, 1, 1, 1, 1, true)));
    }

    @Test
    public void testYear() {

        Matcher matcher;

        matcher = new CronParser("* * * * */2 *").parse();

        assertTrue(matcher.match(createTime(1, 2010, 1, 1, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 2011, 1, 1, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 2012, 1, 1, 1, 1, 1, true)));
        assertFalse(matcher.match(createTime(1, 2013, 1, 1, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 2014, 1, 1, 1, 1, 1, true)));
    }

    @Test
    public void testWeek() {

        Matcher matcher;

        matcher = new CronParser("* * * * * */2").parse();

        assertFalse(matcher.match(createTime(1, 2014, 1, 1, 1, 1, 1, true)));
        assertTrue(matcher.match(createTime(1, 2014, 1, 1, 1, 1, 2, true)));
        assertFalse(matcher.match(createTime(1, 2014, 1, 1, 1, 1, 3, true)));
        assertTrue(matcher.match(createTime(1, 2014, 1, 1, 1, 1, 4, true)));
        assertFalse(matcher.match(createTime(1, 2014, 1, 1, 1, 1, 5, true)));
    }

    protected static Time createTime(long millisecond, int year, int month, int day, int hour, int minute, int dayofweek, boolean leap) {
        return TimeTestUtil.createTime(millisecond, year, month, day, hour, minute, dayofweek, leap);
    }

}
