package com.example.firewarning.ui.device.model;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.firewarning.common.CommonActivity;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
@Entity
public class Device implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int autoGeneID;

    @SerializedName("id")
    @Expose
    @NonNull
    private String id;
    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("NCL")
    @PropertyName("NCL")
    @Expose
    private String nCL;
    @SerializedName("NCLT")
    @PropertyName("NCLT")
    @Expose
    private String nCLT;
    @SerializedName("ND")
    @PropertyName("ND")
    @Expose
    private String nD;
    @SerializedName("NDU")
    @PropertyName("NDU")
    @Expose
    private String nDU;
    @SerializedName("NG")
    @PropertyName("NG")
    @Expose
    private String nG;
    @SerializedName("NO")
    @PropertyName("NO")
    @ColumnInfo(name = "no")
    @Expose
    private String nO;
    @PropertyName("total")
    private String total;
    @PropertyName("highTemp")
    private String highTemp;
    @PropertyName("time")
    @ColumnInfo(name = "time")
    private String time;

    private byte[] bitmapImage;

    @PropertyName("des")
    @Expose
    private String des;

    private int index;
    private String temp;
    private String position;
    private boolean active;

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int visibleOn() {
        return active ? View.VISIBLE : View.GONE;
    }

    public int visibleOff() {
        return active ? View.GONE : View.VISIBLE;
    }

    public Device(@NonNull String id, String nO, String time) {
        this.id = id;
        this.nO = nO;
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getAutoGeneID() {
        return autoGeneID;
    }

    public void setAutoGeneID(int autoGeneID) {
        this.autoGeneID = autoGeneID;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public byte[] getBitmapImage() {
        return bitmapImage;
    }

    public void setBitmapImage(byte[] bitmapImage) {
        this.bitmapImage = bitmapImage;
    }

    public void loadPicture(String picture) {
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(String highTemp) {
        this.highTemp = highTemp;
    }

    public String getTime() {
        if (time == null) {
            Date date = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat
                    = new SimpleDateFormat("dd-MM-yyyy   HH:mm:ss");
            time = dateFormat.format(date);
        }
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNCL() {
        return nCL;
    }

    public void setNCL(String nCL) {
        this.nCL = nCL;
    }

    public String getNCLT() {
        return nCLT;
    }

    public void setNCLT(String nCLT) {
        this.nCLT = nCLT;
    }

    public String getND() {
        return nD;
    }

    public void setND(String nD) {
        this.nD = nD;
    }

    public String getNDU() {
        return nDU;
    }

    public void setNDU(String nDU) {
        this.nDU = nDU;
    }

    public String getNG() {
        String nGDisplay = "";
        if ("HHA000002".equals(id)) {
            if (nG != null) {
                try {
                    nGDisplay = String.valueOf((Integer.parseInt(nG) - 2730) / 10);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } else {
            nGDisplay = nG;
        }
        return nGDisplay;
    }

    public void setNG(String nG) {
        this.nG = nG;
    }

    public String getNO() {
        String tempDisplay = "";
        if ("HHA000001".equals(id)) {
            if (nO != null) {
                try {
                    tempDisplay = String.valueOf(Double.parseDouble(nO) - 3);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } else if ("HHA000002".equals(id)) {
            if (nO != null) {
                try {
                    tempDisplay = String.valueOf((Double.parseDouble(nO) - 2730) / 10);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } else {
            tempDisplay = nO;
        }
        Log.d(id, "NO: " + tempDisplay);
        return tempDisplay;
    }

    public String getTemp() {
        String tempDisplay = "";
        if ("HHA000001".equals(id)) {
            if (nO != null) {
                try {
                    tempDisplay = String.valueOf(Double.parseDouble(nO) - 3);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } else if ("HHA000002".equals(id)) {
            if (nO != null) {
                try {
                    tempDisplay = String.valueOf((Double.parseDouble(nO) - 2730) / 10);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } else {
            tempDisplay = nO;
        }
        return tempDisplay + " " + (char) 0x00B0 + "C";
    }

    public void setNO(String nO) {
        this.nO = nO;
    }

    public Device() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAverageND() {
        int sum = 0;
        if (nD == null) return "";
        String[] arrayND = nD.split("&");
        for (String c : arrayND) {
            sum += Integer.parseInt(c);
        }
        return String.valueOf(sum / arrayND.length);
    }

    @NonNull
    @Override
    public String toString() {
        if (CommonActivity.isNullOrEmpty(name)) {
            return id;
        } else {
            return name;
        }
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        if (!CommonActivity.isNullOrEmpty(nCL)) {
            result.put("NCL", nCL);
        }
        if (!CommonActivity.isNullOrEmpty(nCLT)) {
            result.put("NCLT", nCLT);
        }
        if (!CommonActivity.isNullOrEmpty(nD)) {
            result.put("ND", nD);
        }
        if (!CommonActivity.isNullOrEmpty(nDU)) {
            result.put("NDU", nDU);
        }
        if (!CommonActivity.isNullOrEmpty(nG)) {
            result.put("NG", nG);
        }
        if (!CommonActivity.isNullOrEmpty(nO)) {
            result.put("NO", nO);
        }
        if (!CommonActivity.isNullOrEmpty(id)) {
            result.put("id", id);
        }
        if (!CommonActivity.isNullOrEmpty(user)) {
            result.put("user", user);
        }
        if (!CommonActivity.isNullOrEmpty(highTemp)) {
            result.put("highTemp", highTemp);
        }
        if (!CommonActivity.isNullOrEmpty(total)) {
            result.put("total", total);
        }
        return result;
    }
}
