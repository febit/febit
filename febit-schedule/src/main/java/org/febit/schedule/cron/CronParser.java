/**
 * Copyright 2013 febit.org (support@febit.org)
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Cron Parser
 *
 * @author zqq90
 */
public class CronParser {

    private final char[] buffer;

    CronParser(final char[] cron) {
        this.buffer = cron != null ? cron : new char[0];
    }

    CronParser(final String cron) {
        this.buffer = cron != null ? cron.toCharArray() : new char[0];
    }

    /**
     * Parse cron string.
     *
     * @param cron
     * @return Matcher
     * @throws InvalidCronException
     */
    public static Matcher parse(String cron) throws InvalidCronException {
        return new CronParser(cron).parse();
    }

    /**
     * Parse cron char array.
     *
     * @param cron
     * @return Matcher
     * @throws InvalidCronException
     */
    public static Matcher parse(final char[] cron) throws InvalidCronException {
        return new CronParser(cron).parse();
    }

    /**
     * Parse cron string.
     *
     * @return
     * @throws InvalidCronException
     */
    public Matcher parse() throws InvalidCronException {
        final int buff_len = buffer.length;
        final List<Matcher> matchers = new ArrayList<>();
        int start = 0;
        int next;
        Matcher matcher;
        while (start < buff_len) {
            next = nextIndexOf('|', start, buff_len);
            matcher = parseSingleMatcher(start, next);
            if (matcher != null
                    && matcher != Matcher.MATCH_ALL) {
                matchers.add(matcher);
            } else {
                //NOTE: if one matcher is null, it means match all.
                return Matcher.MATCH_ALL;
            }
            start = next + 1;
        }
        //export
        switch (matchers.size()) {
            case 0:
                return Matcher.MATCH_ALL;
            case 1:
                return matchers.get(0);
            case 2:
                return new OrMatcher(matchers.get(0), matchers.get(1));
            default:
                return new OrMatcherGroup(matchers.toArray(new Matcher[matchers.size()]));
        }
    }

    private int skipRepeatChar(char c, int offset, final int to) {
        while (offset < to) {
            if (buffer[offset] == c) {
                offset++;
            } else {
                return offset;
            }
        }
        return to;
    }

    private Matcher parseSingleMatcher(final int offset, final int to) {
        if (offset >= to) {
            throw createInvalidCronException("Invalid cron-expression", offset);
        }
        final List<Matcher> matchers = new ArrayList<>();

        int start = skipRepeatChar(' ', offset, to);
        if (start >= to) {
            throw createInvalidCronException("Invalid cron-expression", offset);
        }
        int next;
        Atom atom;

        int step = 0;

        while (true) {
            next = nextIndexOf(' ', start, to);
            final List<AtomProto> atomProtos = parseAtoms(start, next);
            switch (step) {
                case 0: //minute
                    atom = warpToOrAtom(atomProtos, 0, 59);
                    if (atom != null) {
                        matchers.add(new MinuteMatcher(atom));
                    }
                    break;
                case 1: //hour
                    atom = warpToOrAtom(atomProtos, 0, 23);
                    if (atom != null) {
                        matchers.add(new HourMatcher(atom));
                    }
                    break;
                case 2: //Day
                    atom = warpToOrAtom(atomProtos, 1, 31);
                    if (atom != null) {
                        matchers.add(new DayOfMonthMatcher(atom));
                    }
                    break;
                case 3: //month
                    atom = warpToOrAtom(atomProtos, 1, 12);
                    if (atom != null) {
                        matchers.add(new MonthMatcher(atom));
                    }
                    break;
                case 4: //year
                    atom = warpToOrAtom(atomProtos, 1, 99999);
                    if (atom != null) {
                        matchers.add(new YearMatcher(atom));
                    }
                    break;
                case 5: //dayofweek
                    atom = warpToOrAtom(atomProtos, 1, 7);
                    if (atom != null) {
                        matchers.add(new DayOfWeekMatcher(atom));
                    }
                    break;
                default:
                    return wrapToSingleAndMatcher(matchers);
            }
            step++;

            start = skipRepeatChar(' ', next, to);
            if (start == to) {
                return wrapToSingleAndMatcher(matchers);
            }
        }
    }

    private Matcher wrapToSingleAndMatcher(final List<Matcher> matchers) {
        //XXX: sort by performance
        switch (matchers.size()) {
            case 0:
                return null;
            case 1:
                return matchers.get(0);
            case 2:
                return new AndMatcher(matchers.get(0), matchers.get(1));
            default:
                return new AndMatcherGroup(matchers.toArray(new Matcher[matchers.size()]));
        }
    }

    private List<AtomProto> parseAtoms(final int offset, final int to) {
        if (buffer[to - 1] == ',') {
            throw createInvalidCronException("Invalid chat ','", to);
        }
        final List<AtomProto> atoms = new ArrayList<>();
        int start = offset;
        int next;
        AtomProto atom;
        while (start < to) {
            next = nextIndexOf(',', start, to);
            atom = parseSingleAtom(start, next);
            if (atom != null) {
                atoms.add(atom);
            }
            start = next + 1;
        }
        return atoms;
    }

    private Atom warpToOrAtom(List<AtomProto> atomProtos, final int min, final int max) {

        final int protoSize = atomProtos.size();
        if (protoSize == 1) {
            return atomProtos.get(0);
        } else if (protoSize == 0) {
            return null;
        } else {
            final List<Atom> atoms = new ArrayList<>(protoSize);
            final IntSet list = new IntSet();
            AtomProto atomProto;

            for (Iterator<AtomProto> it = atomProtos.iterator(); it.hasNext();) {
                atomProto = it.next();
                if (atomProto.maxNumber(min, max) <= 6) {
                    atomProto.render(list, min, max);
                } else {
                    atoms.add(atomProto);
                }
            }
            switch (list.size()) {
                case 0:
                    break;
                case 1:
                    atoms.add(0, new ValueAtom(list.get(0)));
                    break;
                case 2:
                    atoms.add(0, new OrValueAtom(list.get(0), list.get(1)));
                    break;
                case 3:
                    atoms.add(0, new OrThreeValueAtom(list.get(0), list.get(1), list.get(2)));
                    break;
                default:
                    atoms.add(0, new ArrayAtom(list.toSortedArray()));
            }

            //export
            switch (atoms.size()) {
                case 0:
                    return null;
                case 1:
                    return atoms.get(0);
                case 2:
                    return new OrAtom(atoms.get(0), atoms.get(1));
                default:
                    return new OrAtomGroup(atoms.toArray(new Atom[atoms.size()]));
            }
        }
    }

    private AtomProto parseSingleAtom(final int offset, final int to) {
        if (offset >= to) {
            throw createInvalidCronException("Invalid cron-expression", offset);
        }

        int rangeChar = nextIndexOf('-', offset, to);
        if (rangeChar != to) {
            int divChar = nextIndexOf('/', rangeChar + 1, to);
            if (divChar != to) {
                return new RangeDivAtom(parseNumber(offset, rangeChar),
                        parseNumber(rangeChar + 1, divChar),
                        parseNumber(divChar + 1, to));
            } else {
                return new RangeAtom(parseNumber(offset, rangeChar),
                        parseNumber(rangeChar + 1, to));
            }
        } else if (buffer[offset] == '*') {
            final int offset_1;
            if ((offset_1 = offset + 1) == to) {
                return null; //TRUE_ATOM;
            } else if (buffer[offset_1] == '/') {
                return new DivAtom(parseNumber(offset_1 + 1, to));
            } else {
                throw createInvalidCronException("Invalid char '" + buffer[offset_1] + '\'', offset_1);
            }
        } else {
            int divChar = nextIndexOf('/', offset + 1, to);
            if (divChar != to) {
                return new RangeDivAtom(parseNumber(offset, divChar),
                        Integer.MAX_VALUE,
                        parseNumber(divChar + 1, to));
            } else {
                return new ValueAtom(parseNumber(offset, to));
            }
        }
    }

    private int parseNumber(int offset, final int to) throws InvalidCronException {
        if (offset >= to) {
            throw createInvalidCronException("Need a number", offset);
        }
        int value = 0;
        char c;
        while (offset < to) {
            c = buffer[offset++];
            if (c >= '0' && c <= '9') {
                value = value * 10 + ((int) c - (int) '0');
            } else {
                throw createInvalidCronException("Invalid numberic char '" + c + '\'', offset);
            }
        }
        return value;
    }

    private InvalidCronException createInvalidCronException(String message, int offset) {
        return new InvalidCronException(new StringBuilder(message)
                .append(", at ").append(offset)
                .append(" of cron '").append(buffer).append("'.")
                .toString());
    }

    private int nextIndexOf(char c, int offset, int to) {
        final char[] buf = buffer;
        for (; offset < to; offset++) {
            if (buf[offset] == c) {
                return offset;
            }
        }
        return to;
    }
}
