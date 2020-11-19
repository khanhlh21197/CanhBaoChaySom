package com.example.firewarning.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import com.example.firewarning.R;
import com.example.firewarning.common.CommonActivity;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class StringUtils {

    public static boolean containsIgnoreCase(String str, String searchStr)     {
        if(str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }

    private static final String EMAIL_REG = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static Map<String, String> characterMap = new HashMap<String, String>();


    static {
        characterMap
                .put("[ \\t\\xA0\\u1680\\u180e\\u2000-\\u200a\\u202f\\u205f\\u3000]",
                        " ");
        characterMap.put("\\s+", " ");
        characterMap.put("[\\u01F9\\u0144\\u00F1\\u1E47]", "n");
        characterMap.put("[\\u0300\\u0301\\u0309\\u0303\\u0323\\u001c\\u001d]", "");
    }

    public static String formatTemperature(String input) {
        return "Nhiệt độ là: " + input + "độ C";
    }

    public static String formatMoney(double input) {
        if (CommonActivity.isNullOrEmpty(input)) {
            return "";
        }

        try {
            DecimalFormat formatter = new DecimalFormat("#,###,###,##0");
            String result = formatter.format(input);
            return result.replaceAll(",", ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input + "";
    }

    public static String formatMoney(String input) {
        if (CommonActivity.isNullOrEmpty(input)) {
            return "";
        }

        try {
            Double value = Double.parseDouble(input);
            DecimalFormat formatter = new DecimalFormat("#,###,###,##0");
            String result = formatter.format(value);
            return result.replaceAll(",", ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    public static String formatMoney2(String input) {
        if (CommonActivity.isNullOrEmpty(input)) {
            return "";
        }
        try {
            Long n1 = Long.valueOf(input.split("\\.")[0]);
            Double n2 = null;
            if (input.contains(".")) {
                n2 = Double.valueOf("0." + input.split("\\.")[1]);
            }
            DecimalFormat f1 = new DecimalFormat("#,###,###,###");
            DecimalFormat f2 = new DecimalFormat("#,###,###.####");
            if (n2 != null && Double.compare(n2, 0.0001) > 0) {
                return f2.format(n1) + "." + f2.format(n2).split("\\.")[1];
            } else {
                return f1.format(n1);
            }
        } catch (Exception e) {
            Log.i("FormatException", e.getMessage());
        }
        return input;
    }

    public static String formatNumber(Double input) {
        if (CommonActivity.isNullOrEmpty(input)) {
            return "";
        }

        String numberAsString = "";

        try {
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.###", new DecimalFormatSymbols(Locale.US));
            numberAsString = decimalFormat.format(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return numberAsString;
    }

    public static String formatMoneyFromObject(Object input) {
        try {

            Double value = Double.parseDouble(input.toString());
            DecimalFormat formatter = new DecimalFormat("#,###,###,##0");
            String result = formatter.format(value);
            return result.replaceAll(",", ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static String formatAbsMoney(String input) {
        try {
            Double value = Double.parseDouble(input);
            if (value < 0) {
                value = -value;
            }
            DecimalFormat formatter = new DecimalFormat("#,###,###,##0");
            String result = formatter.format(value);
            return result.replaceAll(",", ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    public static String unFormatMoney(String input) {
        try {
            Log.d("unFormatMoney", " input " + input);

            input = input.replaceAll("\\.", "");

            Log.d("unFormatMoney", " output " + input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    private static byte[] compress(byte[] bytesToCompress) {
        Deflater deflater = new Deflater();
        deflater.setInput(bytesToCompress);
        deflater.finish();
        byte[] bytesCompressed = new byte[Short.MAX_VALUE];
        int numberOfBytesAfterCompression = deflater.deflate(bytesCompressed);
        byte[] returnValues = new byte[numberOfBytesAfterCompression];
        System.arraycopy(bytesCompressed, 0, returnValues, 0,
                numberOfBytesAfterCompression);
        return returnValues;
    }

    public static byte[] compress(String stringToCompress) {
        byte[] returnValues = null;

        try {

            returnValues = compress(stringToCompress.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        return returnValues;
    }

    public static boolean isNumberic(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String standardizeNumber(String number, int length) {
        try {
            while (number.length() < length) {
                number = "0" + number;
            }
        } catch (Exception ignored) {
        }
        return number;
    }

    public static boolean checkSpecialCharacter(String text) {
        if (CommonActivity.isNullOrEmpty(text)) {
            System.out.println("Incorrect format of string");
            return false;
        }
        Pattern p = Pattern.compile("[^A-Za-z0-9]");
        Matcher m = p.matcher(text);

        return m.find();
    }

    public static boolean CheckCharSpecical(String s) {
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
        String specialChars = "@*!@#$%^&<>'\"";
        for (int i = 0; i < s.length(); i++) {
            if (specialChars.contains(String.valueOf(s.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    public static boolean CheckCharSpecicalNumber(String s) {
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
        String specialChars = "0123456789";
        for (int i = 0; i < s.length(); i++) {
            if (specialChars.contains(String.valueOf(s.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    public static boolean CheckCharSpecical(String s, String specialChars) {
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
//        String specialChars = "@*!@#$%^&<>'\"";
        for (int i = 0; i < s.length(); i++) {
            if (specialChars.contains(String.valueOf(s.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    public static boolean CheckCharSpecical_1(String s) {
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
        String specialChars = "<>#$%^&*!'\"";
        for (int i = 0; i < s.length(); i++) {
            if (specialChars.contains(String.valueOf(s.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    public static String checkEscapeText(String t) {
        String ret = null;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            switch (c) {
                case '<':
                    ret = "<";
                    break;
                case '>':
                    ret = "<";
                    break;
            }
        }
        return ret;
    }

    public static String xmlEscapeText(String t) {
        if (CommonActivity.isNullOrEmpty(t)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // t = t.replace(">", "");
        // t = t.replace("<", "");
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    if (c > 0x7e) {
                        sb.append("&#").append((int) c).append(";");
                    } else
                        sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String xmlEscapeTextIdNoMNP(String t) {
        if (CommonActivity.isNullOrEmpty(t)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // t = t.replace(">", "");
        // t = t.replace("<", "");
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&#60;");
                    break;
                case '>':
                    sb.append("&#62;");
                    break;
                case '\"':
                    sb.append("&#34;");
                    break;
                case '&':
                    sb.append("&#38;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    if (c > 0x7e) {
                        sb.append("&#").append((int) c).append(";");
                    } else
                        sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String getDateFromDateTime(String originalDate) {
        String[] dateToken = null;
        if (originalDate == null || originalDate.length() < 9) {
            return "";
        } else if (originalDate.length() >= 10) {
            dateToken = originalDate.substring(0, 10).split("-");
            return dateToken[2] + "/" + dateToken[1] + "/" + dateToken[0];
        }
        return "";
    }

    public static String convertDate(String originalDate) {

        try {

            if (originalDate.length() >= 10) {

                String[] dateToken = originalDate.substring(0, 10).split("-");

                if (dateToken.length >= 3)

                    return dateToken[2] + "/" + dateToken[1] + "/"
                            + dateToken[0];

            }

        } catch (Exception ignored) {

        }

        return originalDate;

    }

    public static String convertDateToString(String originalDate) {

        String[] dateToken = null;
        try {

            if (originalDate.length() >= 9) {
                dateToken = originalDate.substring(0, 9).split("/");
            }
            if (originalDate.length() >= 10) {
                dateToken = originalDate.substring(0, 10).split("/");
            }

            if (dateToken != null) {
                if (dateToken.length >= 3) {

                    return dateToken[2] + "-" + dateToken[1] + "-"
                            + dateToken[0];
                }
            }

        } catch (Exception ignored) {

        }

        return null;

    }

    public static String convertDateTimeToTimeDate(String input) {
        try {
            String valueArray[] = input.split(" ");
            if (valueArray.length > 1) {
                return valueArray[1] + " " + valueArray[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    public static String convertArrayToString(ArrayList<String> lstSelectedSerialForOmniSale) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (int i = 0; i < lstSelectedSerialForOmniSale.size(); i++) {
                if (i != lstSelectedSerialForOmniSale.size() - 1) {
                    stringBuilder.append(lstSelectedSerialForOmniSale.get(i)).append(",");
                } else {
                    stringBuilder.append(lstSelectedSerialForOmniSale.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static ArrayList<String> convertStringToArray(String s) {
        ArrayList<String> arr = new ArrayList<>();
        String member = "";
        try {
            String[] parts = s.split(",");
            for (int i = 0; i < parts.length; i++) {
                member = parts[i].trim();
                arr.add(member);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    public static int ordinalIndexOf(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos + 1);
        return pos;
    }

    private static String buildRequestId(String wscode) {
        Random r = new Random();
        int i = r.nextInt(900000000) + 100000000;

        return wscode +
                ";" +
                System.currentTimeMillis() +
                ";" +
                i;
    }

    private static String getPakageInfo(Context context) {
        String pakage = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context
                    .getApplicationContext().getPackageName(), 0);
            pakage = info.packageName + "_" + info.versionCode + "_"
                    + info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pakage;
    }

    public static String encryptIt(String value, Context context) {
        try {
            String cryptoPass = "mBCCS123456a@";
            if (context != null) {
                cryptoPass += getPakageInfo(context);
            }

            DESKeySpec keySpec = new DESKeySpec(cryptoPass.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] clearText = value.getBytes("UTF8");
            // Cipher is not thread safe
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // Log.d(Constant.TAG, "Encrypted: " + value + " -> " +
            // encrypedValue);
            return Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String decryptIt(String value, Context context) {
        try {
            String cryptoPass = "mBCCS123456a@";
            if (context != null) {
                cryptoPass += getPakageInfo(context);
            }

            DESKeySpec keySpec = new DESKeySpec(cryptoPass.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] encrypedPwdBytes = Base64.decode(value, Base64.DEFAULT);
            // cipher is not thread safe
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));

            // Log.d(Constant.TAG, "Decrypted: " + value + " -> " +
            // decrypedValue);
            return new String(decrypedValueBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @SuppressLint("NewApi")
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        input = replaceSpecialCharacter(input);
        return Html.escapeHtml(input);
    }

    public static Boolean validateEmail(String input) {
        return input != null && input.matches(EMAIL_REG);

    }

    public static String formatMsisdn(String mobile) {
        if (CommonActivity.isNullOrEmpty(mobile)) {
            return "";
        }

        if (mobile.startsWith("0")) {
            mobile = "84" + mobile.substring(1);
        } else if (!mobile.startsWith("84")) {
            mobile = "84" + mobile;
        }
        return mobile;
    }

//    public static String unescapeXMLChars(String s) {
//        return s.replaceAll("&amp;",  "&")
//                .replaceAll("&apos;", "'")
//                .replaceAll("&quot;", "\"")
//                .replaceAll("&lt;",   "<")
//                .replaceAll("&gt;",   ">");
//    }
//
//    public static String escapeXMLChars(String s) {
//        return s.replaceAll("&",  "&amp;")
//                .replaceAll("'",  "&apos;")
//                .replaceAll("\"", "&quot;")
//                .replaceAll("<",  "&lt;")
//                .replaceAll(">",  "&gt;");
//    }

    public static String formatIsdn(String mobile) {
        return formatIsdnNew(mobile);
    }

    public static String formatIsdnNew(String strReturn) {
        if (strReturn == null) {
            return "";
        }
        if (strReturn.startsWith("+")) {
            strReturn = strReturn.substring(1);
        }

        if (strReturn.length() > 10 && strReturn.startsWith("84")) {
            strReturn = strReturn.substring(2, strReturn.length());
            Log.d("validateIsdn", "leng phone number: " + strReturn.length());
        }

        while (!strReturn.isEmpty() && strReturn.startsWith("0")) {
            strReturn = strReturn.substring(1);
        }

        if (CommonActivity.isNullOrEmpty(strReturn)) {
            return "";
        }
        if (!StringUtils.isDigit(strReturn)) {
            return "";
        }
        if (strReturn.length() >= 9 && strReturn.length() <= 13) {
            return strReturn;
        }
        return "";
    }

    public static String replaceSpecialCharacter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        input = Normalizer.normalize(input.trim(), Normalizer.Form.NFC);
        for (Entry<String, String> entry : characterMap.entrySet()) {
            input = input.replaceAll(entry.getKey(), entry.getValue());
        }
        return input;
    }

    public static boolean isViettelMobile(String mobile) {
        mobile = formatMsisdn(mobile);

        String REGEX_VIETTEL_MOBILE = "84\\d{9}";
        Pattern pattern = Pattern.compile(REGEX_VIETTEL_MOBILE);
        Matcher matcher = pattern.matcher(mobile);
        return matcher.matches();
    }

    public static String removeTonal(String input) {
        String nfdNormalizedString = Normalizer.normalize(input,
                Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static String unescapeXML(final String xml) {
        Pattern xmlEntityRegex = Pattern.compile("&(#?)([^;]+);");
        // Unfortunately, Matcher requires a StringBuffer instead of a
        // StringBuilder
        StringBuffer unescapedOutput = new StringBuffer(xml.length());

        Matcher m = xmlEntityRegex.matcher(xml);
        Map<String, String> builtinEntities = null;
        String entity;
        String hashmark;
        String ent;
        int code;
        while (m.find()) {
            ent = m.group(2);
            hashmark = m.group(1);
            if ((hashmark != null) && (hashmark.length() > 0)) {
                code = Integer.parseInt(ent);
                entity = Character.toString((char) code);
            } else {
                // must be a non-numerical entity
                if (builtinEntities == null) {
                    builtinEntities = buildBuiltinXMLEntityMap();
                }
                entity = builtinEntities.get(ent);
                if (entity == null) {
                    // not a known entity - ignore it
                    entity = "&" + ent + ';';
                }
            }
            m.appendReplacement(unescapedOutput, entity);
        }
        m.appendTail(unescapedOutput);

        return unescapedOutput.toString();
    }

    private static Map<String, String> buildBuiltinXMLEntityMap() {
        Map<String, String> entities = new HashMap<String, String>(10);
        entities.put("lt", "<");
        entities.put("gt", ">");
        entities.put("amp", "&");
        entities.put("apos", "'");
        entities.put("quot", "\"");
        return entities;
    }

    /**
     * validateString
     *
     * @param context
     * @param data
     * @param msgEmpty
     * @param msgSpecial
     * @return
     */
    public static boolean validateString(Context context, String data, String msgEmpty, String msgSpecial) {
        if (!CommonActivity.isNullOrEmpty(msgEmpty)) {
            if (CommonActivity.isNullOrEmpty(data)) {
                CommonActivity
                        .createAlertDialog(
                                context,
                                msgEmpty,
                                context.getString(R.string.app_name))
                        .show();
                return false;
            }
        }

        if (!CommonActivity.isNullOrEmpty(msgSpecial)) {
            if (CheckCharSpecical(data)) {
                CommonActivity
                        .createAlertDialog(
                                context,
                                msgSpecial,
                                context.getString(R.string.app_name))
                        .show();
                return false;
            }
        }

        return true;
    }

    /**
     * validateString
     *
     * @param context
     * @param data
     * @param msgEmpty
     * @param msgSpecial
     * @return
     */
    public static boolean validateString(Context context, String data, Integer msgEmpty, Integer msgSpecial) {
        if (msgEmpty != null) {
            if (CommonActivity.isNullOrEmpty(data)) {
                CommonActivity
                        .createAlertDialog(
                                context,
                                context.getString(msgEmpty.intValue()),
                                context.getString(R.string.app_name))
                        .show();
                return false;
            }
        }

        if (msgSpecial != null) {
            if (CheckCharSpecical(data)) {
                CommonActivity
                        .createAlertDialog(
                                context,
                                context.getString(msgSpecial.intValue()),
                                context.getString(R.string.app_name))
                        .show();
                return false;
            }
        }

        return true;
    }

    public static String getTextDefault(EditText edt) {
        String text = edt.getText().toString();
        if (text == null || text.equals("")) {
            return "";
        } else {
            String replaceable = String.format("[%s,.\\s]", NumberFormat
                    .getCurrencyInstance().getCurrency().getSymbol());
            String textDefault = text.replaceAll(replaceable, "");
            textDefault = textDefault.replaceAll("-", "");
            return textDefault;
        }
    }

    public static String getTextDefault(EditText edt, String partern) {
        String text = edt.getText().toString();
        if (text == null || text.equals("")) {
            return "";
        } else {
            String replaceable = String.format(partern, NumberFormat
                    .getCurrencyInstance().getCurrency().getSymbol());
            String textDefault = text.replaceAll(replaceable, "");
            textDefault = textDefault.replaceAll("-", "");
            return textDefault;
        }
    }

    public static Boolean isDigit(String strInput) {
        if (CommonActivity.isNullOrEmpty(strInput)) {
            return false;
        }
        String regex = "[0-9]+";
        return strInput.matches(regex);
    }

    public static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").replaceAll("đ", "d");
        return s;
    }

    private byte[] decompress(byte[] bytesToDecompress) {
        byte[] returnValues = null;
        Inflater inflater = new Inflater();

        int numberOfBytesToDecompress = bytesToDecompress.length;

        inflater.setInput(bytesToDecompress, 0, numberOfBytesToDecompress);

        List<Byte> bytesDecompressedSoFar = new ArrayList<>();

        try {
            while (!inflater.needsInput()) {
                byte[] bytesDecompressedBuffer = new byte[numberOfBytesToDecompress];

                int numberOfBytesDecompressedThisTime = inflater
                        .inflate(bytesDecompressedBuffer);

                for (int b = 0; b < numberOfBytesDecompressedThisTime; b++) {
                    bytesDecompressedSoFar.add(bytesDecompressedBuffer[b]);
                }
            }

            returnValues = new byte[bytesDecompressedSoFar.size()];
            for (int b = 0; b < returnValues.length; b++) {
                returnValues[b] = (bytesDecompressedSoFar.get(b));
            }

        } catch (DataFormatException dfe) {
            dfe.printStackTrace();
        }

        inflater.end();

        return returnValues;
    }

    public String decompressToString(byte[] bytesToDecompress) {
        byte[] bytesDecompressed = this.decompress(bytesToDecompress);

        String returnValue = null;

        try {
            returnValue = new String(bytesDecompressed, 0,
                    bytesDecompressed.length, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        return returnValue;
    }

    public static String convertDateToStringV1(String originalDate) {

        String dateString = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (originalDate.length() >= 10) {
                dateString = originalDate.substring(0, 10);
                Date date = simpleDateFormat1.parse(dateString);
                return simpleDateFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String xmlEscapeTextByTag(String text, String... tags) {
        for (String tag : tags) {
            text = xmlEscapeTextByTag(text, tag);
        }
        return text;
    }

    public static String xmlEscapeTextByTag(String text, String tag) {
        try {
            String tagStart = "<" + tag + ">";
            String tagEnd = "</" + tag + ">";

            if (text.contains(tagStart)) {
                StringBuilder builder = new StringBuilder();
                String[] newStr = text.split("<" + tag + ">");
                for (String str : newStr) {
                    if (str.contains(tagEnd)) {
                        String s1 = str.split(tagEnd)[0];
                        String s2 = str.split(tagEnd).length == 1 ? "" : str.split(tagEnd)[1];
                        builder.append(tagStart);
                        builder.append(escapeHtml(StringUtils.unescapeXML(s1)));
                        builder.append(tagEnd);
                        builder.append(s2);
                    } else {
                        builder.append(str);
                    }
                }
                return builder.toString();
            } else {
                return text;
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean hasDigitOnly(String str) {
        return str.matches("[0-9]+");
    }

    public static String validateIdNo(String type, String str) {
        return validateIdNo(type, str, "");
    }


    public static String validateIdNo(String type, String str, String className) {
        String msg = "";
        str = str.trim();
        int length = CommonActivity.isNullOrEmpty(str) ? 0 : str.length();
        if (str.trim().contains(" ")) {
            msg = "Số giấy tờ không được chứa khoảng trắng";
        } else if (CheckCharSpecical(str)) {
            msg = "Số giấy tờ không được chứa ký tự đặc biệt";
        } else if (!"DKCM".equals(className) &&
                ((type.equals("ID") || "01".equals(type)) && (length != 9 && length != 12 || !hasDigitOnly(str)))) {
            msg = "Chứng minh thư phải có 9 hoặc 12 kí tự số";
        } else if ("DKCM".equals(className) &&
                ((type.equals("ID") || "01".equals(type)) && (!hasVietCharacter(str)))) {
            msg = "Chứng minh thư không hợp lệ";
            return msg;
        } else if ("DKCM".equals(className) &&
                ((type.equals("ID") || "01".equals(type)) && (length < 6 || length > 12 || !hasDigitOrLetter(str)))) {
            msg = "Chứng minh thư phải có từ 6 đến 12 kí tự số hoặc chữ";
            return msg;
        } else if (type.equals("TIN") && (length < 2 || length > 20)) {
            msg = "Mã số thuế phải có từ 2 đến 20 kí tự";
        } else if (type.equals("MID") && (length < 6 || length > 12)) {
            msg = "Chứng minh thư QĐ/CA phải có từ 6 đến 12 kí tự";
        } else if ((type.equals("PASS") || "02".equals(type)) && (length < 2 || length > 20)) {
            msg = "Hộ chiếu phải có từ 2 đến 20 kí tự";
        } else if (type.equals("POP") && (length < 2 || length > 10)) {
            msg = "Sổ hộ khẩu phải có từ 2 đến 10 kí tự";
        } else if ((type.equals("BUS") || "04".equals(type)) && length > 20) {
            msg = "GPKD phải có nhỏ hơn 20 kí tự";
        } else if (type.equals("DL") && (length < 3 || length > 20)) {
            msg = "Bằng lái xe phải có từ 3 đến 20 kí tự";
        } else if ((type.equals("IDC") || "03".equals(type)) && (length > 15 || !hasDigitOnly(str))) {
            msg = "Thẻ căn cước phải có nhỏ hơn 16 kí tự số";
        } else if (type.equals("OTH") && (length < 9 || length > 15)) {
            msg = "Giấy tờ khác phải có từ 9 đến 15 kí tự";
        } else if (type.equals("IDNG") && (length < 9 || length > 15)) {
            msg = "CMT ngoại giao phải có từ 9 đến 15 kí tự";
        }
        return msg;
    }

    public static boolean hasDigitOrLetter(String str) {
        return str.toLowerCase().matches("[a-zA-Z0-9]+");
    }

    public static boolean hasVietCharacter(String str) {
        return str.toLowerCase().matches("[^àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]+");
    }

    public static String removeAccent(String s) {
        try {
            String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}
