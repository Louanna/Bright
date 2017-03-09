package com.movit.platform.framework.utils;

import java.io.UnsupportedEncodingException;

/**
 * @author Jerry Sun
 */
public class Base64Utils {

    public static final char AMPERSAND = '&';

    public static final char APOSTROPHE = '\'';

    public static final char AT = '@';

    public static final char BACK_SLASH = '\\';

    public static final char CLOSE_BRACKET = ']';

    public static final char CLOSE_CURLY_BRACE = '}';

    public static final char CLOSE_PARENTHESIS = ')';

    public static final char COLON = ':';

    public static final char COMMA = ',';

    public static final char DASH = '-';

    public static final char EQUAL = '=';

    public static final char GREATER_THAN = '>';

    public static final char FORWARD_SLASH = '/';

    public static final char LESS_THAN = '<';

    public static final char LOWER_CASE_A = 'a';

    public static final char LOWER_CASE_B = 'b';

    public static final char LOWER_CASE_C = 'c';

    public static final char LOWER_CASE_D = 'd';

    public static final char LOWER_CASE_E = 'e';

    public static final char LOWER_CASE_F = 'f';

    public static final char LOWER_CASE_G = 'g';

    public static final char LOWER_CASE_H = 'h';

    public static final char LOWER_CASE_I = 'i';

    public static final char LOWER_CASE_J = 'j';

    public static final char LOWER_CASE_K = 'k';

    public static final char LOWER_CASE_L = 'l';

    public static final char LOWER_CASE_M = 'm';

    public static final char LOWER_CASE_N = 'n';

    public static final char LOWER_CASE_O = 'o';

    public static final char LOWER_CASE_P = 'p';

    public static final char LOWER_CASE_Q = 'q';

    public static final char LOWER_CASE_R = 'r';

    public static final char LOWER_CASE_S = 's';

    public static final char LOWER_CASE_T = 't';

    public static final char LOWER_CASE_U = 'u';

    public static final char LOWER_CASE_V = 'v';

    public static final char LOWER_CASE_W = 'w';

    public static final char LOWER_CASE_X = 'x';

    public static final char LOWER_CASE_Y = 'y';

    public static final char LOWER_CASE_Z = 'z';

    public static final char MINUS = '-';

    public static final char NEW_LINE = '\n';

    public static final char NUMBER_0 = '0';

    public static final char NUMBER_1 = '1';

    public static final char NUMBER_2 = '2';

    public static final char NUMBER_3 = '3';

    public static final char NUMBER_4 = '4';

    public static final char NUMBER_5 = '5';

    public static final char NUMBER_6 = '6';

    public static final char NUMBER_7 = '7';

    public static final char NUMBER_8 = '8';

    public static final char NUMBER_9 = '9';

    public static final char OPEN_BRACKET = '[';

    public static final char OPEN_CURLY_BRACE = '{';

    public static final char OPEN_PARENTHESIS = '(';

    public static final char PERCENT = '%';

    public static final char PERIOD = '.';

    public static final char PIPE = '|';

    public static final char PLUS = '+';

    public static final char POUND = '#';

    public static final char QUESTION = '?';

    public static final char QUOTE = '\"';

    public static final char RETURN = '\r';

    public static final char SEMICOLON = ';';

    public static final char SLASH = FORWARD_SLASH;

    public static final char SPACE = ' ';

    public static final char STAR = '*';

    public static final char TAB = '\t';

    public static final char TILDE = '~';

    public static final char UNDERLINE = '_';

    public static final char UPPER_CASE_A = 'A';

    public static final char UPPER_CASE_B = 'B';

    public static final char UPPER_CASE_C = 'C';

    public static final char UPPER_CASE_D = 'D';

    public static final char UPPER_CASE_E = 'E';

    public static final char UPPER_CASE_F = 'F';

    public static final char UPPER_CASE_G = 'G';

    public static final char UPPER_CASE_H = 'H';

    public static final char UPPER_CASE_I = 'I';

    public static final char UPPER_CASE_J = 'J';

    public static final char UPPER_CASE_K = 'K';

    public static final char UPPER_CASE_L = 'L';

    public static final char UPPER_CASE_M = 'M';

    public static final char UPPER_CASE_N = 'N';

    public static final char UPPER_CASE_O = 'O';

    public static final char UPPER_CASE_P = 'P';

    public static final char UPPER_CASE_Q = 'Q';

    public static final char UPPER_CASE_R = 'R';

    public static final char UPPER_CASE_S = 'S';

    public static final char UPPER_CASE_T = 'T';

    public static final char UPPER_CASE_U = 'U';

    public static final char UPPER_CASE_V = 'V';

    public static final char UPPER_CASE_W = 'W';

    public static final char UPPER_CASE_X = 'X';

    public static final char UPPER_CASE_Y = 'Y';

    public static final char UPPER_CASE_Z = 'Z';

    protected static char getChar(int sixbit) {
        if (sixbit >= 0 && sixbit <= 25) {
            return (char) (65 + sixbit);
        }

        if (sixbit >= 26 && sixbit <= 51) {
            return (char) (97 + (sixbit - 26));
        }

        if (sixbit >= 52 && sixbit <= 61) {
            return (char) (48 + (sixbit - 52));
        }

        if (sixbit == 62) {
            return PLUS;
        }

        return sixbit != 63 ? QUESTION : SLASH;
    }

    protected static int getValue(char c) {
        if ((c >= UPPER_CASE_A) && (c <= UPPER_CASE_Z)) {
            return c - 65;
        }

        if ((c >= LOWER_CASE_A) && (c <= LOWER_CASE_Z)) {
            return (c - 97) + 26;
        }

        if (c >= NUMBER_0 && c <= NUMBER_9) {
            return (c - 48) + 52;
        }

        if (c == PLUS) {
            return 62;
        }

        if (c == SLASH) {
            return 63;
        }

        return c != EQUAL ? -1 : 0;
    }

    public static String encode(byte raw[]) {
        return encode(raw, 0, raw.length);
    }

    public static String encode(byte raw[], int offset, int length) {
        int lastIndex = Math.min(raw.length, offset + length);

        StringBuilder sb = new StringBuilder(((lastIndex - offset) / 3 + 1) * 4);

        for (int i = offset; i < lastIndex; i += 3) {
            sb.append(encodeBlock(raw, i, lastIndex));
        }

        return sb.toString();
    }

    protected static char[] encodeBlock(byte raw[], int offset, int lastIndex) {
        int block = 0;
        int slack = lastIndex - offset - 1;
        int end = slack < 2 ? slack : 2;

        for (int i = 0; i <= end; i++) {
            byte b = raw[offset + i];

            int neuter = b >= 0 ? ((int) (b)) : b + 256;
            block += neuter << 8 * (2 - i);
        }

        char base64[] = new char[4];

        for (int i = 0; i < 4; i++) {
            int sixbit = block >>> 6 * (3 - i) & 0x3f;
            base64[i] = getChar(sixbit);
        }

        if (slack < 1) {
            base64[2] = EQUAL;
        }

        if (slack < 2) {
            base64[3] = EQUAL;
        }

        return base64;
    }

    public static boolean isNull(String s) {
        if (s == null) {
            return true;
        }

        int counter = 0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == SPACE) {
                continue;
            } else if (counter > 3) {
                return false;
            }

            if (counter == 0) {
                if (c != LOWER_CASE_N) {
                    return false;
                }
            } else if (counter == 1) {
                if (c != LOWER_CASE_U) {
                    return false;
                }
            } else if ((counter == 2) || (counter == 3)) {
                if (c != LOWER_CASE_L) {
                    return false;
                }
            }

            counter++;
        }

        if ((counter == 0) || (counter == 4)) {
            return true;
        }

        return false;
    }

    public static byte[] decode(String base64) {
        if (isNull(base64)) {
            return new byte[0];
        }

        int pad = 0;

        for (int i = base64.length() - 1; base64.charAt(i) == EQUAL; i--) {

            pad++;
        }

        int length = (base64.length() * 6) / 8 - pad;
        byte raw[] = new byte[length];
        int rawindex = 0;

        for (int i = 0; i < base64.length(); i += 4) {
            int block = (getValue(base64.charAt(i)) << 18) + (getValue(base64.charAt(i + 1)) << 12)
                    + (getValue(base64.charAt(i + 2)) << 6) + getValue(base64.charAt(i + 3));

            for (int j = 0; j < 3 && rawindex + j < raw.length; j++) {
                raw[rawindex + j] = (byte) (block >> 8 * (2 - j) & 0xff);
            }

            rawindex += 3;
        }

        return raw;
    }

    public static String getBase64(String str) {
        byte[] b = null;
        String s = null;
        try {
            b = str.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (b != null) {
            s = encode(b);
        }
        return s;
    }

}