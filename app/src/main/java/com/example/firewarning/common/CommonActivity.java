package com.example.firewarning.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.firewarning.R;
import com.example.firewarning.dialog.FixedHoloDatePickerDialog;
import com.example.firewarning.utils.DateTimeUtils;
import com.example.firewarning.utils.StringUtils;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@SuppressLint("NewApi")
public class CommonActivity extends Activity {

    private static final String TAG = "CommonActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static String currentPhotoPath;
    private ImageView imageView;

    public static void showDatePickerDialog(final Activity activity,
                                            final DateListtennerInterface dateCallback) {

        OnDateSetListener callback = new OnDateSetListener() {
            boolean receiverCallBack = false;

            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // Date
                if (!receiverCallBack) {
                    String mDateRange = (dayOfMonth) + "/" + (monthOfYear + 1)
                            + "/" + year;
                    dateCallback.onlistennerDate(mDateRange, dayOfMonth,
                            monthOfYear, year);
                }
                receiverCallBack = true;
            }
        };

        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog pic = new FixedHoloDatePickerDialog(activity, AlertDialog.THEME_HOLO_LIGHT, callback, mYear,
                month, day);
        pic.setTitle(activity.getString(R.string.chon_ngay));
        pic.show();
    }


    public static void showDatePickerDialog(final Context context,
                                            Date lastDate, OnDateSetListener callback) {
        if (context == null || callback == null) {
            return;
        }
        final Calendar cal = Calendar.getInstance();
        if (lastDate == null) {
            lastDate = new Date();
        }
        cal.setTime(lastDate);
        DatePickerDialog pic = new FixedHoloDatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, callback,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        pic.setTitle(context.getString(R.string.chon_ngay));
        pic.show();
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context context) {

        String deviceId = "";
        try {
            TelephonyManager telephony = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if (telephony.getDeviceId() != null) {
                deviceId = telephony.getDeviceId();
            } else {
                deviceId = Secure.getString(context.getContentResolver(),
                        Secure.ANDROID_ID);
            }
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
        }

        return deviceId;
    }

    public static Dialog createAlertDialog(Activity act, String message, String title) {
        if (!CommonActivity.isNullOrEmpty(message)) {
            if (message.contains("java.lang") || message.contains("xception")) {
                message = act.getString(R.string.errorProcess) + message.substring(0, 30);
            }
        }
        try {
            return new MaterialDialog.Builder(act)
                    .title(title)
                    .content(message)
                    .positiveText(act.getString(R.string.ok)).autoDismiss(true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    }).build();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    public static Dialog createAlertNetworkDialog(Activity act) {
        return createAlertDialog(act, act.getString(R.string.errorNetwork),
                act.getString(R.string.app_name));
    }

    public static Dialog createExceptionDialog(Activity act, Exception ex) {
        try {
            return createAlertDialog(act, act.getString(R.string.exception)
                    + " - " + ex.toString(), act.getString(R.string.app_name));
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
            return null;
        }
    }

    private static void dismissDialog(MaterialDialog dialog) {
        try {
            dialog.dismiss();
        } catch (IllegalStateException e) {
            return;
        } catch (Exception e) {
            return;
        }
    }

    private static void dismissDialog(DialogInterface dialog) {
        try {
            dialog.dismiss();
        } catch (IllegalStateException e) {
            return;
        } catch (Exception e) {
            return;
        }
    }

    private static void dismissDialog(Dialog dialog) {
        try {
            dialog.dismiss();
        } catch (IllegalStateException e) {
            return;
        } catch (Exception e) {
            return;
        }
    }

    public static Dialog createAlertDialog(Context act, String message,
                                           String title) {
        try {

            return new MaterialDialog.Builder(act)
                    .title(title)
                    .content(message)
                    .positiveText(act.getString(R.string.ok))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    }).build();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    public static Dialog createDialog(final Activity act, int messageId, int titleId,
                                      int leftButtonTextId, int rightButtonTextId,
                                      final OnClickListener leftClick, final OnClickListener rightClick) {
        try {
            return new MaterialDialog.Builder(act)
                    .title(act.getString(titleId))
                    .content(act.getString(messageId))
                    .positiveText(act.getString(rightButtonTextId))
                    .negativeText(act.getString(leftButtonTextId))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (rightClick != null) {
                                rightClick.onClick(act.getCurrentFocus());
                            }

                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (leftClick != null) {
                                leftClick.onClick(act.getCurrentFocus());
                            }

                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    }).build();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    public static Dialog createDialog(final Activity act, String messageId, int titleId,
                                      int leftButtonTextId, int rightButtonTextId,
                                      final OnClickListener leftClick, final OnClickListener rightClick) {
        try {
            return new MaterialDialog.Builder(act)
                    .title(act.getString(titleId))
                    .content(messageId)
                    .positiveText(act.getString(rightButtonTextId))
                    .negativeText(act.getString(leftButtonTextId))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (rightClick != null) {
                                rightClick.onClick(act.getCurrentFocus());
                            }

                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (leftClick != null) {
                                leftClick.onClick(act.getCurrentFocus());
                            }

                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    }).build();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    public static Dialog createCustomDialog(final Activity act, int messageId, int titleId, View view,
                                            int leftButtonTextId, int rightButtonTextId,
                                            final OnClickListener leftClick, final OnClickListener rightClick) {
        return createCustomDialog(act, act.getString(messageId), act.getString(titleId), view, leftButtonTextId, rightButtonTextId, leftClick, rightClick);
    }

    public static Dialog createCustomDialog(final Activity act, String message, String title, View view,
                                            int leftButtonTextId, int rightButtonTextId,
                                            final OnClickListener leftClick, final OnClickListener rightClick) {

        AlertDialog.Builder builder = new AlertDialog.Builder(act);

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(act.getString(rightButtonTextId), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (rightClick != null) {
                            rightClick.onClick(act.getCurrentFocus());
                        }

                        if (dialog != null) {
                            dismissDialog(dialog);
                        }
                    }
                })
                .setNegativeButton(act.getString(leftButtonTextId), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (leftClick != null) {
                            leftClick.onClick(act.getCurrentFocus());
                        }

                        if (dialog != null) {
                            dismissDialog(dialog);
                        }
                    }
                })
                .setView(view);
        return builder.create();
    }

    public static DefaultHandler parseXMLHandler(DefaultHandler handler, String original) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            //spf.setFeature(Constant.FEATURE_GENERAL, false);
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(handler);
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(original));
            xr.parse(inStream);
        } catch (SAXNotRecognizedException saxex) {
            Log.e("parseXMLHandler", saxex.getMessage());
        } catch (Exception ex) {
            Log.e("parseXMLHandler", ex.getMessage());
        }
        return handler;
    }

    /**
     * @param handler
     * @return
     * @throws Exception
     */
    public static DefaultHandler parseXMLHandler(DefaultHandler handler,
                                                 InputSource inSource) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        //spf.setFeature(Constant.FEATURE_GENERAL, false);
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        xr.setContentHandler(handler);
        xr.parse(inSource);
        return handler;
    }

    /**
     * Kiem tra ket noi mang
     *
     * @param activity
     * @return
     */
    public static boolean isNetworkConnected(Context activity) {
        boolean val = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) activity
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                return false;
            }
            NetworkInfo mobile = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobile != null && mobile.isAvailable() && mobile.isConnected()) {
                val = true;

                int networkType = mobile.getSubtype();
                Log.d("TAG", "networkType: " + networkType);
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: // api<8 : replace by
                        // 11
                        // 2G
                        // 2G
                        Toast.makeText(activity,
                                activity.getString(R.string.require_2g),
                                Toast.LENGTH_LONG).show();
                        // createAlertDialog(activity,
                        // activity.getString(R.string.require_2g),
                        // activity.getString(R.string.app_name)).show();
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // api<9 : replace by
                        // 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // api<11 : replace by
                        // 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // api<13 : replace by
                        // 15
                        // 3G
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE: // api<11 : replace by
                        // 13
                        // 4G
                        break;
                    default:
                        Toast.makeText(
                                activity,
                                activity.getString(R.string.require_netwotk_unknown),
                                Toast.LENGTH_LONG).show();
                        break;
                }
            } else {
                NetworkInfo wifi = connectivityManager
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (wifi != null && wifi.isAvailable() && wifi.isConnected()) {
                    Toast.makeText(activity,
                            activity.getString(R.string.require_wifi),
                            Toast.LENGTH_SHORT).show();
                    val = true;
                }
            }
        } catch (Exception e) {
            Log.e("Exception", e.toString(), e);
        }
        Log.d("TAG", "isNetworkConnected: " + val);
        return val;
    }

    public static Dialog createAlertDialog(Activity act, int message, int title) {
        try {

            return new MaterialDialog.Builder(act)
                    .title(act.getString(title))
                    .content(act.getString(message))
                    .positiveText(act.getString(R.string.ok))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    }).build();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    public static Dialog createAlertDialog(Activity act, String message, int title) {
        try {

            return new MaterialDialog.Builder(act)
                    .title(act.getString(title))
                    .content(message)
                    .positiveText(act.getString(R.string.ok))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    }).build();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    public static Dialog createAlertDialog(final Activity act, int message,
                                           int title, final OnClickListener onClick) {
        try {

            return new MaterialDialog.Builder(act)
                    .title(act.getString(title))
                    .content(act.getString(message))
                    .positiveText(act.getString(R.string.ok))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (onClick != null) {
                                onClick.onClick(act.getCurrentFocus());
                            }

                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    }).build();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    // SHOW KEYBOARD
    public static void showKeyBoard(View view, Context context) {
        try {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
            // TODO: handle exception
        }

    }

    public static void hideKeyboard(View view, Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    public static boolean checkGps(Context context) {

        boolean check = false;
        try {
            LocationManager locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            check = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
        }

        return check;
    }

    public static void DoNotLocation(Activity context) {
        try {
            if (checkGps(context)) {
                createAlertDialog(context,
                        context.getResources().getString(R.string.checkgps),
                        context.getResources().getString(R.string.app_name))
                        .show();
            } else {
                createAlertDialog(
                        context,
                        context.getResources().getString(
                                R.string.cannot_get_location),
                        context.getResources().getString(R.string.app_name))
                        .show();

            }
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
        }

    }

    public static String encodeStringUrl(Activity context, String text) {
        String textEncode = "";
        try {
            textEncode = Uri.encode(text, Constants.ALLOWED_URI_CHARS);
        } catch (Exception e) {
            // TODO: handle exception
            Log.d(Constants.TAG, "error", e);
        }

        return textEncode;
    }

    public static void ChangLanguage(Activity activity) {
        try {
            SharedPreferences prefs = activity.getSharedPreferences(
                    "MBCCS_SETTING_LANGUAGE", MODE_PRIVATE);
            String codeLanguage = prefs.getString("VT_LANGUAGE", "vi");
            chang(activity, codeLanguage);
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
            // TODO: handle exception
        }

    }

    private static void chang(Activity activity, String languageToLoad) {
        try {
            Configuration config = new Configuration();
            if (languageToLoad == null || "".equals(languageToLoad)) {
                config.locale = Locale.getDefault();
            } else {
                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                config.locale = locale;
            }

            activity.getResources().updateConfiguration(config,
                    activity.getResources().getDisplayMetrics());
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
            // TODO: handle exception
        }

    }

    // genera account
    public static String genPasswordAuto(String customerName) {
        try {
            if (customerName == null || "".equals(customerName.trim())) {
                // CustomerName Is Alphabet
                StringBuilder strBuffer = new StringBuilder();
                for (int j = 65; j <= (65 + 24); j++) {
                    strBuffer.append((char) j);
                }
                customerName = strBuffer.toString();
            }
            StringBuilder buffer = new StringBuilder();
            customerName = convertUnicode2Nosign(customerName);
            customerName = customerName.replaceAll("[^a-zA-Z0-9]", "");
            customerName = refineStringForPassword(customerName);
            Random random = new Random();
            customerName = customerName.concat("0123456789");
            char[] chars = customerName.toCharArray();
            for (int i = 0; i < 6; i++) {
                buffer.append(chars[random.nextInt(chars.length)]);
            }
            return buffer.toString();
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
            return "";
        }

    }

    public static String convertUnicode2Nosign(String org) {
        if (org == null || org.trim().isEmpty()) {
            return "";
        }
        // convert to VNese no sign. @haidh 2008
        char arrChar[] = org.toCharArray();
        char result[] = new char[arrChar.length];
        for (int i = 0; i < arrChar.length; i++) {
            switch (arrChar[i]) {
                case '\u00E1':
                case '\u00E0':
                case '\u1EA3':
                case '\u00E3':
                case '\u1EA1':
                case '\u0103':
                case '\u1EAF':
                case '\u1EB1':
                case '\u1EB3':
                case '\u1EB5':
                case '\u1EB7':
                case '\u00E2':
                case '\u1EA5':
                case '\u1EA7':
                case '\u1EA9':
                case '\u1EAB':
                case '\u1EAD':
                case '\u0203':
                case '\u01CE': {
                    result[i] = 'a';
                    break;
                }
                case '\u00E9':
                case '\u00E8':
                case '\u1EBB':
                case '\u1EBD':
                case '\u1EB9':
                case '\u00EA':
                case '\u1EBF':
                case '\u1EC1':
                case '\u1EC3':
                case '\u1EC5':
                case '\u1EC7':
                case '\u0207': {
                    result[i] = 'e';
                    break;
                }
                case '\u00ED':
                case '\u00EC':
                case '\u1EC9':
                case '\u0129':
                case '\u1ECB': {
                    result[i] = 'i';
                    break;
                }
                case '\u00F3':
                case '\u00F2':
                case '\u1ECF':
                case '\u00F5':
                case '\u1ECD':
                case '\u00F4':
                case '\u1ED1':
                case '\u1ED3':
                case '\u1ED5':
                case '\u1ED7':
                case '\u1ED9':
                case '\u01A1':
                case '\u1EDB':
                case '\u1EDD':
                case '\u1EDF':
                case '\u1EE1':
                case '\u1EE3':
                case '\u020F': {
                    result[i] = 'o';
                    break;
                }
                case '\u00FA':
                case '\u00F9':
                case '\u1EE7':
                case '\u0169':
                case '\u1EE5':
                case '\u01B0':
                case '\u1EE9':
                case '\u1EEB':
                case '\u1EED':
                case '\u1EEF':
                case '\u1EF1': {
                    result[i] = 'u';
                    break;
                }
                case '\u00FD':
                case '\u1EF3':
                case '\u1EF7':
                case '\u1EF9':
                case '\u1EF5': {
                    result[i] = 'y';
                    break;
                }
                case '\u0111': {
                    result[i] = 'd';
                    break;
                }
                case '\u00C1':
                case '\u00C0':
                case '\u1EA2':
                case '\u00C3':
                case '\u1EA0':
                case '\u0102':
                case '\u1EAE':
                case '\u1EB0':
                case '\u1EB2':
                case '\u1EB4':
                case '\u1EB6':
                case '\u00C2':
                case '\u1EA4':
                case '\u1EA6':
                case '\u1EA8':
                case '\u1EAA':
                case '\u1EAC':
                case '\u0202':
                case '\u01CD': {
                    result[i] = 'A';
                    break;
                }
                case '\u00C9':
                case '\u00C8':
                case '\u1EBA':
                case '\u1EBC':
                case '\u1EB8':
                case '\u00CA':
                case '\u1EBE':
                case '\u1EC0':
                case '\u1EC2':
                case '\u1EC4':
                case '\u1EC6':
                case '\u0206': {
                    result[i] = 'E';
                    break;
                }
                case '\u00CD':
                case '\u00CC':
                case '\u1EC8':
                case '\u0128':
                case '\u1ECA': {
                    result[i] = 'I';
                    break;
                }
                case '\u00D3':
                case '\u00D2':
                case '\u1ECE':
                case '\u00D5':
                case '\u1ECC':
                case '\u00D4':
                case '\u1ED0':
                case '\u1ED2':
                case '\u1ED4':
                case '\u1ED6':
                case '\u1ED8':
                case '\u01A0':
                case '\u1EDA':
                case '\u1EDC':
                case '\u1EDE':
                case '\u1EE0':
                case '\u1EE2':
                case '\u020E': {
                    result[i] = 'O';
                    break;
                }
                case '\u00DA':
                case '\u00D9':
                case '\u1EE6':
                case '\u0168':
                case '\u1EE4':
                case '\u01AF':
                case '\u1EE8':
                case '\u1EEA':
                case '\u1EEC':
                case '\u1EEE':
                case '\u1EF0': {
                    result[i] = 'U';
                    break;
                }

                case '\u00DD':
                case '\u1EF2':
                case '\u1EF6':
                case '\u1EF8':
                case '\u1EF4': {
                    result[i] = 'Y';
                    break;
                }
                case '\u0110':
                case '\u00D0':
                case '\u0089': {
                    result[i] = 'D';
                    break;
                }
                default:
                    result[i] = arrChar[i];
            }
        }
        return new String(result);
    }

    private static String refineStringForPassword(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager im = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(activity.getWindow().getDecorView()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
        }

    }

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity
                    .getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
        }

    }

    public static String getStardardIsdn(String phoneNumber) {
        String strReturn = "";
        if (phoneNumber == null) {
            return strReturn;
        }
        strReturn = phoneNumber;
        if (strReturn.startsWith("+")) {
            strReturn = strReturn.substring(1);
        }
        if (strReturn.startsWith("0")) {
            strReturn = strReturn.substring(1);
        }
        if (!strReturn.startsWith("84")) {
            strReturn = "84" + strReturn;
        }
        return strReturn;
    }

    /**
     * Kiem tra object null or empty
     *
     * @param input
     * @return
     */
    public static boolean isNullOrEmpty(Object input) {
        if (input == null) {
            return true;
        }
        if (input instanceof String) {
            return input.toString().trim().isEmpty();
        }
        if (input instanceof EditText) {
            return ((EditText) input).getText().toString().trim().isEmpty();
        }
        if (input instanceof List) {
            return ((List) input).isEmpty();
        }

        if (input instanceof HashMap) {
            return ((HashMap) input).isEmpty();
        }

        return input instanceof ArrayList && ((ArrayList) input).isEmpty();
    }

    public static boolean isNullOrEmptyArray(Object... input) {
        if (isNullOrEmpty(input)) {
            return true;
        }
        for (Object anInput : input) {
            if (isNullOrEmpty(anInput)) {
                return true;
            }
        }
        return false;
    }

    public static void unZipFileToDirectory(String zipFile, String outputFolder) {

        byte[] buffer = new byte[1024];
        try {

            // create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            // get the zip file content
            ZipInputStream zis = new ZipInputStream(
                    new FileInputStream(zipFile));

            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator
                        + fileName);

                Log.d("file unzip : ", "" + newFile.getAbsoluteFile());

                // create all non exists folders
                // else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        } catch (IOException ex) {
            Log.d(Constants.TAG, "error", ex);
        }
    }

    // thinhhq1 =====================> call phone
    public static void callphone(final Activity context, final String phone) {

        OnClickListener onclickCall = new OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View arg0) {
                String uri = "tel:" + CommonActivity.getStardardIsdnBCCSCall(phone);
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                try {
                    context.startActivity(intent);
                } catch (SecurityException ex) {
                    Log.e("SecurityException call phone", ex.getMessage());
                }
            }
        };

        CommonActivity.createDialog(context,
                context.getString(R.string.confirmspport, phone),
                context.getString(R.string.app_name),
                context.getString(R.string.cancel),
                context.getString(R.string.ok), null, onclickCall).show();

    }

    public static String checkStardardIsdn(String phoneNumber) {
        String strReturn = "";
        if (phoneNumber == null) {
            return strReturn;
        }
        strReturn = phoneNumber.trim();
        if (strReturn.startsWith("+")) {
            strReturn = strReturn.substring(1);
        }

        if (StringUtils.isDigit(phoneNumber)) {
            while (!strReturn.isEmpty() && strReturn.startsWith("0")) {
                strReturn = strReturn.substring(1);
            }
        }

        if (strReturn.length() > 10 && strReturn.startsWith("84")) {
            strReturn = strReturn.substring(2, strReturn.length());
        }
        return strReturn;
    }

    public static Boolean validateIsdn(String phoneNumber) {
        String strReturn;
        if (phoneNumber == null) {
            return false;
        }
        strReturn = phoneNumber;
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

        if (isNullOrEmpty(strReturn)) {
            return false;
        }
        if (!StringUtils.isDigit(strReturn)) {
            return false;
        }
        if (strReturn.length() >= 9 && strReturn.length() <= 13) {
            return true;
        }
        return false;
    }

    public static Boolean validateIsdn12Characters(String phoneNumber) {
        String strReturn;
        if (phoneNumber == null) {
            return false;
        }
        strReturn = phoneNumber;
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

        if (isNullOrEmpty(strReturn)) {
            return false;
        }
        if (!StringUtils.isDigit(strReturn)) {
            return false;
        }
        if (strReturn.length() >= 9 && strReturn.length() <= 12) {
            return true;
        }
        return false;
    }

    public static Boolean validateHomephone(String phoneNumber) {
        String strReturn = "";
        if (phoneNumber == null) {
            return false;
        }
        strReturn = phoneNumber;
        if (strReturn.startsWith("+")) {
            strReturn = strReturn.substring(1);
        }
        while (!strReturn.isEmpty() && strReturn.startsWith("0")) {
            strReturn = strReturn.substring(1);
        }

        if (strReturn.length() > 10 && strReturn.startsWith("84")) {
            strReturn = strReturn.substring(2, strReturn.length());
            Log.d("validateIsdn", "leng phone number: " + strReturn.length());
        }
        if (isNullOrEmpty(strReturn)) {
            return false;
        }
        if (!StringUtils.isDigit(strReturn)) {
            return false;
        }

        return strReturn.length() == 9 || strReturn.length() == 10;

    }

    public static String convertUnicode2NosignString(String org) {
        // convert to VNese no sign. @haidh 2008
        char arrChar[] = org.toCharArray();
        char result[] = new char[arrChar.length];
        for (int i = 0; i < arrChar.length; i++) {
            switch (arrChar[i]) {
                case '\u00E1':
                case '\u00E0':
                case '\u1EA3':
                case '\u00E3':
                case '\u1EA1':
                case '\u0103':
                case '\u1EAF':
                case '\u1EB1':
                case '\u1EB3':
                case '\u1EB5':
                case '\u1EB7':
                case '\u00E2':
                case '\u1EA5':
                case '\u1EA7':
                case '\u1EA9':
                case '\u1EAB':
                case '\u1EAD':
                case '\u0203':
                case '\u01CE': {
                    result[i] = 'a';
                    break;
                }
                case '\u00E9':
                case '\u00E8':
                case '\u1EBB':
                case '\u1EBD':
                case '\u1EB9':
                case '\u00EA':
                case '\u1EBF':
                case '\u1EC1':
                case '\u1EC3':
                case '\u1EC5':
                case '\u1EC7':
                case '\u0207': {
                    result[i] = 'e';
                    break;
                }
                case '\u00ED':
                case '\u00EC':
                case '\u1EC9':
                case '\u0129':
                case '\u1ECB': {
                    result[i] = 'i';
                    break;
                }
                case '\u00F3':
                case '\u00F2':
                case '\u1ECF':
                case '\u00F5':
                case '\u1ECD':
                case '\u00F4':
                case '\u1ED1':
                case '\u1ED3':
                case '\u1ED5':
                case '\u1ED7':
                case '\u1ED9':
                case '\u01A1':
                case '\u1EDB':
                case '\u1EDD':
                case '\u1EDF':
                case '\u1EE1':
                case '\u1EE3':
                case '\u020F': {
                    result[i] = 'o';
                    break;
                }
                case '\u00FA':
                case '\u00F9':
                case '\u1EE7':
                case '\u0169':
                case '\u1EE5':
                case '\u01B0':
                case '\u1EE9':
                case '\u1EEB':
                case '\u1EED':
                case '\u1EEF':
                case '\u1EF1': {
                    result[i] = 'u';
                    break;
                }
                case '\u00FD':
                case '\u1EF3':
                case '\u1EF7':
                case '\u1EF9':
                case '\u1EF5': {
                    result[i] = 'y';
                    break;
                }
                case '\u0111': {
                    result[i] = 'd';
                    break;
                }
                case '\u00C1':
                case '\u00C0':
                case '\u1EA2':
                case '\u00C3':
                case '\u1EA0':
                case '\u0102':
                case '\u1EAE':
                case '\u1EB0':
                case '\u1EB2':
                case '\u1EB4':
                case '\u1EB6':
                case '\u00C2':
                case '\u1EA4':
                case '\u1EA6':
                case '\u1EA8':
                case '\u1EAA':
                case '\u1EAC':
                case '\u0202':
                case '\u01CD': {
                    result[i] = 'A';
                    break;
                }
                case '\u00C9':
                case '\u00C8':
                case '\u1EBA':
                case '\u1EBC':
                case '\u1EB8':
                case '\u00CA':
                case '\u1EBE':
                case '\u1EC0':
                case '\u1EC2':
                case '\u1EC4':
                case '\u1EC6':
                case '\u0206': {
                    result[i] = 'E';
                    break;
                }
                case '\u00CD':
                case '\u00CC':
                case '\u1EC8':
                case '\u0128':
                case '\u1ECA': {
                    result[i] = 'I';
                    break;
                }
                case '\u00D3':
                case '\u00D2':
                case '\u1ECE':
                case '\u00D5':
                case '\u1ECC':
                case '\u00D4':
                case '\u1ED0':
                case '\u1ED2':
                case '\u1ED4':
                case '\u1ED6':
                case '\u1ED8':
                case '\u01A0':
                case '\u1EDA':
                case '\u1EDC':
                case '\u1EDE':
                case '\u1EE0':
                case '\u1EE2':
                case '\u020E': {
                    result[i] = 'O';
                    break;
                }
                case '\u00DA':
                case '\u00D9':
                case '\u1EE6':
                case '\u0168':
                case '\u1EE4':
                case '\u01AF':
                case '\u1EE8':
                case '\u1EEA':
                case '\u1EEC':
                case '\u1EEE':
                case '\u1EF0': {
                    result[i] = 'U';
                    break;
                }

                case '\u00DD':
                case '\u1EF2':
                case '\u1EF6':
                case '\u1EF8':
                case '\u1EF4': {
                    result[i] = 'Y';
                    break;
                }
                case '\u0110':
                case '\u00D0':
                case '\u0089': {
                    result[i] = 'D';
                    break;
                }
                default:
                    result[i] = arrChar[i];
            }
        }
        return new String(result);
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newMaxLength) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        long newArea = (long) newMaxLength * newMaxLength;
        long oldArea = (long) width * height;
        if (newArea < oldArea) {
            float scale = (float) Math.sqrt((double) newArea / oldArea);
            // float scaleWidth = ((float) newWidth) / width;
            // float scaleHeight = ((float) newHeight) / height;

            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scale, scale);
            // RECREATE THE NEW BITMAP
            return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        } else {
            return bm;
        }
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }


    private static class MemoryAsyncTask extends
            AsyncTask<String, String, Long> {
        private final Activity activity;

        public MemoryAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Long doInBackground(String... params) {
            ActivityManager activityManager = (ActivityManager) activity
                    .getSystemService(ACTIVITY_SERVICE);
            MemoryInfo memoryInfo = new MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);

            long memory = (memoryInfo.availMem + memoryInfo.threshold) / 1048576;
            if (memoryInfo.lowMemory)
                memory = -1L;
            return memory;
        }

        @Override
        protected void onPostExecute(Long memory) {
            if (memory < Constants.lowMemory) {
                String message = String.format(
                        activity.getString(R.string.lowMemory), "" + memory, ""
                                + Constants.lowMemory);
                createAlertDialog(activity, message, "Low Memory").show();
            } else {
                String message = String.format(
                        activity.getString(R.string.okMemory), "" + memory);
                //Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static JSONObject getMemory(Activity activity) {
        JSONObject deviceInfo = null;

        MemoryAsyncTask asyn = new MemoryAsyncTask(activity);
        asyn.execute();

        // ActivityManager activityManager = (ActivityManager)
        // activity.getSystemService(ACTIVITY_SERVICE);
        // MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        // activityManager.getMemoryInfo(memoryInfo);
        //
        // Log.i(Constant.TAG, " memoryInfo.availMem " + memoryInfo.availMem +
        // "\n");
        // Log.i(Constant.TAG, " memoryInfo.lowMemory " + memoryInfo.lowMemory +
        // "\n");
        // Log.i(Constant.TAG, " memoryInfo.threshold " + memoryInfo.threshold +
        // "\n");
        // Log.i(Constant.TAG, " memoryInfo.totalMem " + memoryInfo.totalMem +
        // "\n");
        //
        // deviceInfo = new JSONObject();
        // deviceInfo.put("availMem", memoryInfo.availMem);
        // deviceInfo.put("lowMemory", memoryInfo.lowMemory);
        // deviceInfo.put("threshold", memoryInfo.threshold);
        // deviceInfo.put("totalMem", memoryInfo.totalMem);
        //
        // long memory = (memoryInfo.availMem + memoryInfo.threshold) / 1048576;
        //
        // if(memoryInfo.lowMemory || memory < Constant.lowMemory) {
        // String message =
        // String.format(activity.getString(R.string.lowMemory), "" + memory, ""
        // + Constant.lowMemory);
        // createAlertDialog(activity, message, "Low Memory").show();
        // } else {
        // String message = String.format(activity.getString(R.string.okMemory),
        // "" + memory);
        // Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        // }
        // Log.i(Constant.TAG, "Build.BRAND " + Build.BRAND);
        // Log.i(Constant.TAG, "Build.CPU_ABI " + Build.CPU_ABI);
        // Log.i(Constant.TAG, "Build.CPU_ABI2 " + Build.CPU_ABI2);
        // Log.i(Constant.TAG, "Build.DEVICE " + Build.DEVICE);
        // Log.i(Constant.TAG, "Build.MANUFACTURER " + Build.MANUFACTURER);
        // Log.i(Constant.TAG, "Build.MODEL " + Build.MODEL);
        // Log.i(Constant.TAG, "Build.PRODUCT " + Build.PRODUCT);
        // Log.i(Constant.TAG, "Build.TIME " + Build.TIME);
        // Log.i(Constant.TAG, "Build.VERSION.RELEASE " +
        // Build.VERSION.RELEASE);
        // Log.i(Constant.TAG, "Build.VERSION.SDK_INT " +
        // Build.VERSION.SDK_INT);
        //
        // deviceInfo.put("BRAND", Build.BRAND);
        // deviceInfo.put("CPU_ABI", Build.CPU_ABI);
        // deviceInfo.put("CPU_ABI2", Build.CPU_ABI2);
        // deviceInfo.put("DEVICE", Build.DEVICE);
        // deviceInfo.put("MANUFACTURER", Build.MANUFACTURER);
        // deviceInfo.put("MODEL", Build.MODEL);
        // deviceInfo.put("PRODUCT", Build.PRODUCT);
        // deviceInfo.put("TIME", Build.TIME);
        // deviceInfo.put("RELEASE", Build.VERSION.RELEASE);
        // deviceInfo.put("SDK_INT", Build.VERSION.SDK_INT);

        // List<RunningAppProcessInfo> runningAppProcesses =
        // activityManager.getRunningAppProcesses();
        // Map<Integer, String> pidMap = new TreeMap<Integer, String>();
        // for (RunningAppProcessInfo runningAppProcessInfo :
        // runningAppProcesses) {
        // pidMap.put(runningAppProcessInfo.pid,
        // runningAppProcessInfo.processName);
        // }
        // Collection<Integer> keys = pidMap.keySet();
        //
        // for (int key : keys) {
        // int pids[] = new int[1];
        // pids[0] = key;
        // Debug.MemoryInfo[] memoryInfoArray =
        // activityManager.getProcessMemoryInfo(pids);
        // for (android.os.Debug.MemoryInfo pidMemoryInfo : memoryInfoArray) {
        // Log.i(Constant.TAG, String.format("** MEMINFO in pid %d [%s] **\n",
        // pids[0], pidMap.get(pids[0])));
        // Log.i(Constant.TAG, " pidMemoryInfo.getTotalPrivateDirty(): " +
        // pidMemoryInfo.getTotalPrivateDirty() + "\n");
        // Log.i(Constant.TAG, " pidMemoryInfo.getTotalPss(): " +
        // pidMemoryInfo.getTotalPss() + "\n");
        // Log.i(Constant.TAG, " pidMemoryInfo.getTotalSharedDirty(): " +
        // pidMemoryInfo.getTotalSharedDirty() + "\n");
        // }
        // }
        return deviceInfo;
    }

    public static void toast(Activity activity, String mesage) {
        Toast.makeText(activity, mesage, Toast.LENGTH_LONG).show();
    }

    public static void toast(Activity activity, int stringId) {
        Toast.makeText(activity, activity.getString(stringId),
                Toast.LENGTH_LONG).show();
    }

    public static void toastShort(Activity activity, int stringId) {
        Toast.makeText(activity, activity.getString(stringId),
                Toast.LENGTH_SHORT).show();
    }

    public static String checkVTmapCode(String temp) {
        String[] result = temp.split("_");
        if (result.length == 4) {
            return result[3];
        }
        if (result.length == 3) {
            return result[2];
        }
        if (result.length == 2) {
            return result[1];
        }
        return null;

    }

    public static Object cloneObject(Object objSource) {
        Object objDest = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(objSource);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            try {
                objDest = new ObjectInputStream(bais).readObject();
            } catch (ClassNotFoundException e) {
                Log.d(Constants.TAG, "error", e);
            }
        } catch (IOException e) {
            Log.d(Constants.TAG, "error", e);
        }
        return objDest;
    }

    private static boolean isInternetReachableMap(String ip, int port) {
        boolean isReachable = false;
        try {
            SocketAddress sockaddr = new InetSocketAddress(ip, port);
            Socket sock = new Socket();
            sock.connect(sockaddr, 60000);
            sock.close();
            isReachable = true;
        } catch (Exception e) {
            Log.d(Constants.TAG, "error", e);
        }
        Log.d("TAG", "isInternetReachable: " + isReachable);
        return isReachable;
    }

    public static boolean isConnectedToServer(String url, int timeout) {
        try {

            // InetAddress.getByName(Constant.IP_MAP_NEW).isReachable(Constant.TIME_OUT_PING);

            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return true;
        } catch (Exception e) {
            // Handle your exceptions
            Log.d(Constants.TAG, "error", e);
            return false;
        }
    }


    public static String convertCharacter1(String input) {
        String inpNormal = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String reStr = pattern.matcher(inpNormal).replaceAll("");
        reStr = reStr.replace("Ä‘", "d");
        reStr = reStr.replace("Ä?", "D");
        return reStr;
    }

    // format string them
    public static String formatIsdn1(String t) {
        String s = t.substring(t.length() - 3, t.length());
        int length = t.length() - 3;
        for (int i = 0; i < length; i++) {
            s = "*" + s;
        }
        return s;
    }

    // format string them
    public static String formatIsdn(String t) {
        String s = t.substring(0, t.length() - 3);

        for (int i = t.length() - 3; i < t.length(); i++) {
            s = s + "*";
        }
        return s;
    }

    /**
     * Ham lay mo ta loi tu Exception
     *
     * @param context
     * @param ex
     * @return
     */
    public static String getDescription(Context context, Exception ex) {
        if (ex instanceof UnknownHostException) {
            return context.getString(R.string.unknown_host_exception);
        } else if (ex instanceof ConnectTimeoutException) {
            // return context.getString(R.string.connect_timeout_exception);
            return context.getString(R.string.txt_transaction_timeout);
        } else if (ex instanceof SocketTimeoutException) {
            // return context.getString(R.string.socket_timeout_exception);
            return context.getString(R.string.txt_transaction_timeout);
//			return context.getString(R.string.http_host_connect_exception);
        } else if (ex instanceof SocketException) {
            // return context.getString(R.string.socket_close_exception);
            return context.getString(R.string.txt_transaction_timeout);
        } else if (ex instanceof IllegalStateException) {
            return context.getString(R.string.illegal_state_exception);
        } else if (ex instanceof IOException) {
            // return context.getString(R.string.socket_io_exception);
            return context.getString(R.string.txt_transaction_timeout);
        } else {
            return context.getString(R.string.exception) + " - " + ex.toString();
        }
    }

    public static void showDatePickerDialog(Context ctx, final EditText edt) {
        final Calendar cal = Calendar.getInstance();
        OnDateSetListener callback = new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                cal.set(year, monthOfYear, dayOfMonth);
                edt.setText(DateTimeUtils.convertDateTimeToString(
                        cal.getTime(), "dd/MM/yyyy"));
            }
        };

        Date date = DateTimeUtils.convertStringToTime(edt.getText().toString(),
                "dd/MM/yyyy");
        if (date != null) {
            cal.setTime(date);
        }

        DatePickerDialog pic = new FixedHoloDatePickerDialog(ctx, AlertDialog.THEME_HOLO_LIGHT, callback,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        pic.setTitle(ctx.getString(R.string.chon_ngay));
        pic.show();
    }

    public static void showDatePickerDialogV2(Context ctx, final EditText edt) {
        Calendar cal = Calendar.getInstance();
        Date date = DateTimeUtils.convertStringToTime(edt.getText().toString(),
                "dd/MM/yyyy");
        if (date != null) {
            cal.setTime(date);
            if (cal.get(Calendar.YEAR) < 1900)
                cal = Calendar.getInstance();
        }
        final Calendar finalCal = cal;
        OnDateSetListener callback = new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                finalCal.set(year, monthOfYear, dayOfMonth);
                edt.setText(DateTimeUtils.convertDateTimeToString(
                        finalCal.getTime(), "dd/MM/yyyy"));
            }
        };
        DatePickerDialog pic = new FixedHoloDatePickerDialog(ctx, AlertDialog.THEME_HOLO_LIGHT, callback,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        pic.setTitle(ctx.getString(R.string.chon_ngay));
        pic.show();
    }

    public static void showDatePickerDialog(Context ctx, final TextView edt) {
        final Calendar cal = Calendar.getInstance();
        OnDateSetListener callback = new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                cal.set(year, monthOfYear, dayOfMonth);
                edt.setText(DateTimeUtils.convertDateTimeToString(
                        cal.getTime(), "dd/MM/yyyy"));
            }
        };

        if (CommonActivity.isNullOrEmpty(edt.getText().toString())) {
            edt.setText(DateTimeUtils.convertDateTimeToString(new Date(),
                    "dd/MM/yyyy"));
        }

        cal.setTime(DateTimeUtils.convertStringToTime(edt.getText().toString(),
                "dd/MM/yyyy"));
        DatePickerDialog pic = new FixedHoloDatePickerDialog(ctx, AlertDialog.THEME_HOLO_LIGHT, callback,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        pic.setTitle(ctx.getString(R.string.chon_ngay));
        pic.show();
    }

    public static void showDatePickerDialog(Context ctx, final TextView edt, long timeStampMax) {
        final Calendar cal = Calendar.getInstance();
        OnDateSetListener callback = new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                cal.set(year, monthOfYear, dayOfMonth);
                edt.setText(DateTimeUtils.convertDateTimeToString(
                        cal.getTime(), "dd/MM/yyyy"));
            }
        };

        if (CommonActivity.isNullOrEmpty(edt.getText().toString())) {
            edt.setText(DateTimeUtils.convertDateTimeToString(new Date(),
                    "dd/MM/yyyy"));
        }

        cal.setTime(DateTimeUtils.convertStringToTime(edt.getText().toString(),
                "dd/MM/yyyy"));
        DatePickerDialog pic = new FixedHoloDatePickerDialog(ctx, AlertDialog.THEME_HOLO_LIGHT, callback,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        pic.setTitle(ctx.getString(R.string.chon_ngay));
        pic.getDatePicker().setMaxDate(timeStampMax);
        pic.show();
    }

    public static void showDatePickerDialog(Context ctx, final TextView edt, long timeStampMax, long timeStampMin) {
        final Calendar cal = Calendar.getInstance();
        OnDateSetListener callback = new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                cal.set(year, monthOfYear, dayOfMonth);
                edt.setText(DateTimeUtils.convertDateTimeToString(
                        cal.getTime(), "dd/MM/yyyy"));
            }
        };

        if (CommonActivity.isNullOrEmpty(edt.getText().toString())) {
            edt.setText(DateTimeUtils.convertDateTimeToString(new Date(),
                    "dd/MM/yyyy"));
        }

        cal.setTime(DateTimeUtils.convertStringToTime(edt.getText().toString(),
                "dd/MM/yyyy"));
        DatePickerDialog pic = new FixedHoloDatePickerDialog(ctx, AlertDialog.THEME_HOLO_LIGHT, callback,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        pic.setTitle(ctx.getString(R.string.chon_ngay));
        pic.getDatePicker().setMaxDate(timeStampMax);
        pic.getDatePicker().setMinDate(timeStampMin);
        pic.show();
    }

    public static void showDatePickerDialogNotDay(Context ctx, final TextView edt, final String patternDate) {
        final Calendar cal = Calendar.getInstance();
        OnDateSetListener callback = new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                cal.set(year, monthOfYear, dayOfMonth);
                edt.setText(DateTimeUtils.convertDateTimeToString(
                        cal.getTime(), patternDate));
            }
        };

        if (CommonActivity.isNullOrEmpty(edt.getText().toString())) {
            edt.setText(DateTimeUtils.convertDateTimeToString(new Date(),
                    patternDate));
        }

        cal.setTime(DateTimeUtils.convertStringToTime(edt.getText().toString(),
                patternDate));
        DatePickerDialog pic = new FixedHoloDatePickerDialog(ctx, AlertDialog.THEME_HOLO_LIGHT, callback,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        pic.setTitle(ctx.getString(R.string.chon_ngay));
        pic.getDatePicker()
                .findViewById(ctx.getResources().getIdentifier("day", "id", "android"))
                .setVisibility(View.GONE);
        pic.show();
    }

    // lay so nam
    public static int getYear(Date d1, Date d2) {
        double diff = d2.getTime() - d1.getTime();
        double d = 1000 * 60 * 60 * 24 * 365;
        return (int) Math.round(diff / d);
    }

    public static int getDiffDays(Date d1, Date d2) {
        double diff = d1.getTime() - d2.getTime();
        double d = 1000 * 60 * 60 * 24;
        return (int) Math.round(diff / d);
    }

    public static String getStardardIsdnBCCS(String phoneNumber) {
        String strReturn = "";
        if (phoneNumber == null) {
            return strReturn;
        }
        strReturn = phoneNumber;
        if (strReturn.startsWith("+")) {
            strReturn = strReturn.substring(1);
        }
        if (strReturn.startsWith("0")) {
            strReturn = strReturn.substring(1);
        }
        if (strReturn.length() > 10 && strReturn.startsWith("84")) {
            strReturn = strReturn.substring(2, strReturn.length());
        }
        return strReturn;
    }

    public static String getStardardIsdnBCCSCall(String phoneNumber) {
        String strReturn = "";
        if (phoneNumber == null) {
            return strReturn;
        }
        strReturn = phoneNumber;
        if (strReturn.startsWith("+")) {
            strReturn = strReturn.replaceFirst("\\+", "");
        }
        if (strReturn.length() > 10 && strReturn.startsWith("84")) {
            strReturn = strReturn.replaceFirst("84", "0");
        }
        if (!strReturn.startsWith("0") && !strReturn.equals(Constants.phoneNumber)) {
            strReturn = "0" + strReturn;
        }
        return strReturn;
    }

    public static int InsertAPN(String name, String apn, Context context) {

        // Set the URIs and variables
        int id = -1;
        boolean existing = false;
        final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
        final Uri PREFERRED_APN_URI = Uri
                .parse("content://telephony/carriers/preferapn");

        // Check if the specified APN is already in the APN table, if so skip
        // the insertion
        Cursor parser = context.getContentResolver().query(APN_TABLE_URI, null,
                null, null, null);
        parser.moveToLast();
        while (!parser.isBeforeFirst()) {
            int index = parser.getColumnIndex("name");
            String n = parser.getString(index);
            if (n.equals(name)) {
                existing = true;
                Toast.makeText(context, "APN s-intranet already configured.",
                        Toast.LENGTH_SHORT).show();
                break;
            }
            parser.moveToPrevious();
        }

        // if the entry doesn't already exist, insert it into the APN table
        if (!existing) {

            // Initialize the Content Resolver and Content Provider
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();

            // Capture all the existing field values excluding name
            Cursor apu = context.getContentResolver().query(PREFERRED_APN_URI,
                    null, null, null, null);
            apu.moveToFirst();

            // Assign them to the ContentValue object
            values.put("name", name); // the method parameter
            values.put("apn", apn);

            // Actual insertion into table
            Cursor c = null;
            try {
                Uri newRow = resolver.insert(APN_TABLE_URI, values);

                if (newRow != null) {
                    c = resolver.query(newRow, null, null, null, null);
                    int idindex = c.getColumnIndex("_id");
                    c.moveToFirst();
                    id = c.getShort(idindex);
                }
            } catch (SQLException ex) {
                Log.d(Constants.TAG, "error", ex);
            }
            if (c != null)
                c.close();
        }

        return id;
    }

    // Takes the ID of the new record generated in InsertAPN and sets that
    // particular record the default preferred APN configuration
    public static boolean setPreferredAPN(String name, Context context) {
        final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
        final Uri PREFERRED_APN_URI = Uri
                .parse("content://telephony/carriers/preferapn");

        Cursor parser = context.getContentResolver().query(APN_TABLE_URI, null,
                null, null, null);
        parser.moveToLast();
        int id = -1;
        while (!parser.isBeforeFirst()) {
            int index = parser.getColumnIndex("name");
            String n = parser.getString(index);
            if (n.equals(name)) {
                int idindex = parser.getColumnIndex("_id");
                parser.moveToFirst();
                id = parser.getShort(idindex);
                break;
            }
            parser.moveToPrevious();
        }

        // If the id is -1, that means the record was found in the APN table
        // before insertion, thus, no action required
        if (id == -1) {
            return false;
        }

        Uri.parse("content://telephony/carriers");

        boolean res = false;
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put("apn_id", id);
        try {
            resolver.update(PREFERRED_APN_URI, values, null, null);
            Cursor c = resolver.query(PREFERRED_APN_URI, new String[]{"name",
                    "apn"}, "_id=" + id, null, null);
            if (c != null) {
                res = true;
                c.close();
            }
        } catch (SQLException e) {
            Log.d(Constants.TAG, "error", e);
        }
        return res;
    }

    public static String getNormalText(String text) {

        if (CommonActivity.isNullOrEmpty(text)) {
            return "";
        }

        return Normalizer.normalize(text.trim(), Normalizer.Form.NFC);
    }

    public static boolean askPermission() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static String formatJsonString(String text) {

        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            switch (letter) {
                case '{':
                case '[':
                    json.append("\n").append(indentString).append(letter).append("\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n").append(indentString).append(letter);
                    break;
                case ',':
                    json.append(letter).append("\n").append(indentString);
                    break;

                default:
                    json.append(letter);
                    break;
            }
        }

        return json.toString();
    }

    public static Dialog createDialog(final Activity act, String message,
                                      String title, String leftButtonText, String rightButtonText,
                                      final OnClickListener leftClick, final OnClickListener rightClick) {
        try {
            return new MaterialDialog.Builder(act)
                    .title(title)
                    .content(message)
                    .positiveText(rightButtonText)
                    .negativeText(leftButtonText)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (rightClick != null) {
                                rightClick.onClick(act.getCurrentFocus());
                            }

                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (leftClick != null) {
                                leftClick.onClick(act.getCurrentFocus());
                            }

                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    }).build();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static Bitmap decodeFile(File f, int WIDTH, int HIGHT) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            final int REQUIRED_WIDTH = WIDTH;
            final int REQUIRED_HIGHT = HIGHT;
            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_WIDTH && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static String replaceNull(String str) {
        if (CommonActivity.isNullOrEmpty(str)) {
            return "";
        }
        return str.replace("null", "");
    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);

//		19/07/2002
//		20/07/2017

        int diff = b.get(b.YEAR) - a.get(a.YEAR); // 15
        if (a.get(a.MONTH) < b.get(b.MONTH) ||
                (a.get(a.MONTH) == b.get(b.MONTH) && a.get(a.DATE) < b.get(b.DATE))) {
            diff++;
        }
        return diff;
    }

    public static int getDiffYearsMain(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(b.YEAR) - a.get(a.YEAR);

        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static void showConfirmValidate(Activity activity, int id) {
        Dialog dialog = CommonActivity.createAlertDialog(activity, activity.getResources().getString(id), activity.getResources().getString(R.string.app_name));
        dialog.show();
    }

    public static void showConfirmValidate(Activity activity, String message) {
        Dialog dialog = CommonActivity.createAlertDialog(activity, message, activity.getResources().getString(R.string.app_name));
        dialog.show();
    }

    public static Dialog createAlertDialogInfo(Activity act, String message, String title, String ok) {
        try {
            return new MaterialDialog.Builder(act)
                    .title(title)
                    .content(message)
                    .positiveText(ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            if (dialog != null && dialog.isShowing()) {
                                dismissDialog(dialog);
                            }
                        }
                    }).build();
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return null;
        }
    }

    public static void openFileFromLink(String link, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.replace(" ", "%20")));
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        Intent intentChooser = Intent.createChooser(intent, "Open File");
        try {
            activity.startActivity(intentChooser);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveFileBase64(String content, String directory, String fileName) {
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        File dirFile = new File(directory);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        try {
            fileOutputStream = new FileOutputStream(directory + File.separator + fileName);
            byte[] fileByte = Base64.decode(content, Base64.DEFAULT);
            inputStream = new ByteArrayInputStream(fileByte);
            byte[] buffer = new byte[1024];
            int leng;
            while ((leng = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, leng);
            }
            fileOutputStream.close();
            inputStream.close();
            Log.d("saveFileBase64", directory + File.separator + fileName);
        } catch (Exception e) {
            Log.e("saveFileBase64", e.getMessage());
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getBase64String(Bitmap bitmap, String ext) {

        if (CommonActivity.isNullOrEmpty(bitmap)) {
            return null;
        }

        Bitmap bitmapImage = CommonActivity.getResizedBitmap(
                bitmap, Constants.SIZE_IMAGE_SCALE);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (Constants.IMG_EXT_PNG.equals(ext)) {
                bitmapImage.compress(Bitmap.CompressFormat.PNG,
                        Constants.DEFAULT_JPEG_QUALITY, baos);
            } else {
                bitmapImage.compress(Bitmap.CompressFormat.PNG,
                        Constants.DEFAULT_JPEG_QUALITY, baos);
            }
            byte[] imageBytes = baos.toByteArray();
            String base64String = Base64.encodeToString(
                    imageBytes, Activity.TRIM_MEMORY_BACKGROUND);
            baos.close();
            return base64String;
        } catch (IOException ex) {
            Log.e("Error", "getBase64String", ex);
            return "";
        }
    }

    public static File getFileInDir(String directoryPath, String fileName) {
        File dirFile = new File(directoryPath);
        if (!dirFile.exists()) {
            return null;
        }
        File[] filenames = dirFile.listFiles();
        String name;
        for (File tmpf : filenames) {
            name = tmpf.getName().substring(0, tmpf.getName().length() - 3);
            name = name.split("_")[0];
            if (name.equals(fileName)) {
                return tmpf;
            }
        }
        return null;
    }

    public static void deleteAllFileInDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            return;
        }
        File[] filenames = dirFile.listFiles();
        if (!CommonActivity.isNullOrEmptyArray((Object[]) filenames)) {
            for (File tmpf : filenames) {
                tmpf.delete();
            }
        }
    }

    public static String SaveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Shopic Snaps");

        if (!myDir.exists())
            myDir.mkdirs();

        // Random generator = new Random();
        // int n = 10000;
        // n = generator.nextInt(n);
        String fname = "Image_" + System.currentTimeMillis() + ".jpg";
        File file = new File(myDir, fname);

        if (file.exists())
            file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root + "/App Snaps/" + fname;
    }

    public static List<File> getAllFileInDir(String directoryPath) {
        List<File> result = new ArrayList<>();
        File dirFile = new File(directoryPath);
        if (!dirFile.exists()) {
            return null;
        }
        File[] filenames = dirFile.listFiles();
        String name;
        for (File tmpf : filenames) {
            result.add(tmpf);
        }
        return result;
    }

    public static String getFileExtension(String filePath) {
        try {
            return filePath.substring(filePath.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return Constants.IMG_EXT_JPG;
        }
    }

    public static boolean validateInputPin(Long channelTypeId, Long type) {
        if (channelTypeId == null) {
            return true;
        }

        //kenh nhan vien khac da dich vu khong bat buoc nhap ma OTP
        if (channelTypeId.equals(14L) && (type == null || !type.equals(9L))) {
            return false;
        }

        return true;
    }

    public static boolean areThereMockPermissionApps(Context context) {

        int count = 0;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i]
                                .equals("android.permission.ACCESS_MOCK_LOCATION")
                                && !applicationInfo.packageName.equals(context.getPackageName())) {
                            count++;
                        }
                    }
                } else {
                    return false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("E", "Got exception " + e.getMessage());
                return false;
            }
        }

        if (count > 0)
            return true;
        return false;

        // mo check
//      return false;

    }

    public static boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
//      Logs.i("DATA", "ALLOW_MOCK_LOCATION : "+Settings.Secure.getString(context.getContentResolver(),
//                                    Settings.Secure.ALLOW_MOCK_LOCATION));
        if (Secure.getString(context.getContentResolver(),
                Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;

        // mo check
//      return false;


    }

    //	public static String getversionclient(Context context) {
//		String versionclient = "";
//		try {
//			PackageInfo packageInfo = context.getPackageManager()
//					.getPackageInfo(context.getPackageName(), 0);
//			versionclient = packageInfo.versionName;
//			Log.d("versionclienttttttttttttttttttt", versionclient);
//			CacheData.getInstanse().setVersionclient(versionclient);
//		} catch (PackageManager.NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return versionclient;
//	}

    public static void saveFile(String content, String directory, String fileName) {
        FileOutputStream f = null;
        InputStream in = null;
        try {


            f = new FileOutputStream(directory + File.separator + "/" + fileName);

            byte[] fileByte = Base64.decode(content, Base64.DEFAULT);
            in = new ByteArrayInputStream(fileByte);

            byte[] buffer = new byte[1024];
            int len1;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
            in.close();
            Log.i("download File", directory + File.separator + "/" + fileName);
        } catch (Exception e) {
            Log.e("download file", e.getMessage());
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setListViewHeight(ExpandableListView listView,
                                         int group) {
        ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    /***
     * truong hop khong the fill ducc IMG ra ImageView dung ham //loadImageLargeByUri
     * */
    public static void loadImageLarge(final ImageView iv_show_last_image, final int with, final int height, final String path) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                return decodeSampledBitmapFromResource(path, with, height);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                iv_show_last_image.setImageBitmap(bitmap);
            }
        }.execute();
    }

    //truong hop khong the fill ducc IMG ra ImageView se dung nay
    public static void loadImageLargeByUri(final Activity activity, final ImageView iv_show_last_image, final int with, final int height, final Uri uri) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                return decodeSampledBitmapFromResource(activity, uri, with, height);
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                super.onPostExecute(bitmap);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmapBackup = bitmap;
                        if (bitmapBackup == null) {
                            bitmapBackup = BitmapFactory.decodeFile(new File(uri.toString()).getAbsolutePath());
                            if (bitmapBackup != null) {
                                bitmapBackup = Bitmap.createScaledBitmap(bitmapBackup, with, height, true);
                            }
                        }
                        iv_show_last_image.setImageBitmap(bitmapBackup);
                    }
                });
            }
        }.execute();
    }

    public static Bitmap decodeSampledBitmapFromResource(Activity activity, Uri uri, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        String path = uri.getPath();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap == null)
            bitmap = getBitmapFromPathWithStream(uri, activity, options);
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = bitmap;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;
        }
        return rotatedBitmap;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap != null)
            bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = bitmap;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;
        }
        return rotatedBitmap;
    }

    public static Bitmap getBitmapFromPathWithStream(Uri uri, Activity activity, BitmapFactory.Options bmOptions) {
        bmOptions.inJustDecodeBounds = true;
        ContentResolver cr = activity.getContentResolver();
        Bitmap bitmap = null;
        try {
            InputStream input = cr.openInputStream(uri);
            BitmapFactory.decodeStream(input, null, bmOptions);
            if (input != null) {
                input.close();
            }
            InputStream input1 = cr.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input1);
            if (input1 != null) {
                input1.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static String fomatAndReplaceString(String text, int size) {
        String textFormat = text;
        if (size > 4) {
            textFormat = text.substring(0, 2) + "/" + text.substring(2, 4) + "/" + text.substring(4);
        } else if (size > 2) {
            textFormat = text.substring(0, 2) + "/" + text.substring(2);
        }
        return textFormat;
    }

    //dd/MM/yyyy
    public static void writeTextDateFormat(final EditText editText, @NonNull final Calendar maxTime) {
        editText.addTextChangedListener(new TextWatcher() {
            boolean isChangeText = false;
            String oldText = "";

            String processDay(@NonNull String day, @Nullable String month) {
                int dayInt = Integer.parseInt(day);
                int monthInt = 0;
                if (month != null && !month.isEmpty())
                    monthInt = Integer.parseInt(month);
                int maxDay = monthInt > 0 ? getMaxDayByMonth(monthInt) : 31;

                dayInt = dayInt <= 0 ? 1 : dayInt;
                dayInt = maxDay > dayInt ? dayInt : maxDay;
                return dayInt < 10 ? "0" + dayInt : dayInt + "";
            }

            String processMonth(@NonNull String month) {
                int monthInt = Integer.parseInt(month);
                monthInt = monthInt > 12 ? 12 : monthInt;
                monthInt = monthInt <= 0 ? 1 : monthInt;
                return monthInt < 10 ? "0" + monthInt : monthInt + "";
            }

            String processYear(@NonNull String year) {
                int yearInt = Integer.parseInt(year);
                yearInt = yearInt > maxTime.get(Calendar.YEAR) ? maxTime.get(Calendar.YEAR) : yearInt;
//                yearInt = yearInt < 1900 ? 1900 : yearInt;
                return yearInt + "";
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(".") || s.toString().contains("-")) {
                    String sReplace = s.toString().replace("-", "")
                            .replace(".", "");
                    editText.setText(sReplace);
                    editText.setSelection(sReplace.length());
                    return;
                }

                if (oldText.length() > s.toString().length()) {
                    oldText = s.toString();
                    return;
                }

                if (!isChangeText) {
                    StringBuilder changeText = new StringBuilder();
                    String[] lstString = s.toString().trim().split("/");
                    if (lstString.length == 0) {
                        changeText.append("");
                    } else {
                        if (lstString[0].length() >= 2) {
                            String month = lstString.length > 1 ? lstString[1] : null;
                            String day = processDay(lstString[0].substring(0, 2), month);
                            changeText.append(day).append("/").append(lstString[0].substring(2, lstString[0].length()));
                        } else {
                            if (s.toString().length() == 2 && s.toString().substring(1).equals("/"))
                                changeText.append(processMonth(lstString[0])).append("/");
                            else
                                changeText.append(lstString[0]);
                        }
                        if (lstString.length >= 2) {
                            if (lstString[1].length() >= 2) {
                                String month = processMonth(lstString[1]);
                                changeText.append(month).append("/").append(lstString[1].substring(2, lstString[1].length()));
                            } else {
                                if (s.toString().length() == 5 && s.toString().substring(4).equals("/"))
                                    changeText.append(processMonth(lstString[1])).append("/");
                                else
                                    changeText.append(lstString[1]);
                            }
                        }
                        if (lstString.length == 3) {
                            if (lstString[2].length() >= 4) {
                                String year = processYear(lstString[2]);
                                changeText.append(year);
                            } else {
                                changeText.append(lstString[2]);
                            }
                        }
                        if (lstString.length > 3) {
                            changeText = new StringBuilder(s.toString().substring(0, s.toString().length() - 1));
                        }
                    }
                    isChangeText = true;
                    editText.setText(changeText.toString());
                    editText.setSelection(changeText.length());
                    oldText = changeText.toString();
                } else
                    isChangeText = false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });
    }

    //kiennk code
    public static void showDatePickerDialogs(final Activity activity, long time, String check,
                                             final DateListtennerInterface dateCallback) {

        OnDateSetListener callback = new OnDateSetListener() {
            boolean receiverCallBack = false;

            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // Date
                if (!receiverCallBack) {
                    String mDateRange = (dayOfMonth) + "/" + (monthOfYear + 1)
                            + "/" + year;
                    dateCallback.onlistennerDate(mDateRange, dayOfMonth,
                            monthOfYear, year);
                }
                receiverCallBack = true;
            }
        };

        Calendar calendar = Calendar.getInstance();

        int mYear = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog pic = new FixedHoloDatePickerDialog(activity, AlertDialog.THEME_HOLO_LIGHT, callback, mYear,
                month, day);
        pic.setTitle(activity.getString(R.string.chon_ngay));
        pic.getDatePicker().setMaxDate(time);

//
        pic.show();
    }

    public static final boolean clearAllEditTextFocuses(Activity act) {
        View v = act.getCurrentFocus();
        if (v instanceof EditText) {
            final FocusedEditTextItems list = new FocusedEditTextItems();
            list.addAndClearFocus((EditText) v);

            //Focus von allen EditTexten entfernen
            boolean repeat = true;
            do {
                v = act.getCurrentFocus();
                if (v instanceof EditText) {
                    if (list.containsView(v))
                        repeat = false;
                    else list.addAndClearFocus((EditText) v);
                } else repeat = false;
            } while (repeat);

            final boolean result = !(v instanceof EditText);
            //Focus wieder setzen
            list.reset();
            return result;
        } else return false;
    }

    private final static class FocusedEditTextItem {

        private final boolean focusable;

        private final boolean focusableInTouchMode;

        @NonNull
        private final EditText editText;

        private FocusedEditTextItem(final @NonNull EditText v) {
            editText = v;
            focusable = v.isFocusable();
            focusableInTouchMode = v.isFocusableInTouchMode();
        }

        private final void clearFocus() {
            if (focusable)
                editText.setFocusable(false);
            if (focusableInTouchMode)
                editText.setFocusableInTouchMode(false);
            editText.clearFocus();
        }

        private final void reset() {
            if (focusable)
                editText.setFocusable(true);
            if (focusableInTouchMode)
                editText.setFocusableInTouchMode(true);
        }

    }

    private final static class FocusedEditTextItems extends ArrayList<FocusedEditTextItem> {

        private final void addAndClearFocus(final @NonNull EditText v) {
            final FocusedEditTextItem item = new FocusedEditTextItem(v);
            add(item);
            item.clearFocus();
        }

        private final boolean containsView(final @NonNull View v) {
            boolean result = false;
            for (FocusedEditTextItem item : this) {
                if (item.editText == v) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        private final void reset() {
            for (FocusedEditTextItem item : this)
                item.reset();
        }

    }

    public static int getMaxDayByMonth(int month) {
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            return 31;
        } else if (month == 2) {
            return 28;
        }
        return 30;
    }

    public static Bitmap decodeSampledBitmapFromResource(byte[] data, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //options.inPurgeable = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static Bitmap resovleRotateBitmap(Activity act, Uri uri) {
        ExifInterface ei = null;
        Bitmap bitmap = null;
        try {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(act.getContentResolver(), uri);
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    bitmap = getBitmapFromPathWithStream(uri, act, options);
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
                    }
                } catch (Exception exx) {
                    exx.printStackTrace();
                }
            }
            ei = new ExifInterface(uri.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        if (bitmap == null)
            return null;
        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
