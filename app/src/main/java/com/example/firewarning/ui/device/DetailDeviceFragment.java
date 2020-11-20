package com.example.firewarning.ui.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.firewarning.R;
import com.example.firewarning.common.CommonActivity;
import com.example.firewarning.dao.AppDatabase;
import com.example.firewarning.databinding.DetailDeviceFragmentBinding;
import com.example.firewarning.serializer.ObjectSerializer;
import com.example.firewarning.ui.device.model.Device;
import com.example.firewarning.warning.WarningService;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Single;

public class DetailDeviceFragment extends Fragment
        implements View.OnClickListener,
        OnChartValueSelectedListener {
    private static final String SHARED_PREFS_HISTORY = "SHARED_PREFS_HISTORY";
    private static final String KEY_HISTORY = "HISTORY";
    private Intent warningService;
    private AppDatabase mDb;
    private CombinedChart mChart;
    private XAxis xAxis;
    private List<String> timeLabel = new ArrayList<>();
    private ArrayList<Float> temperature = new ArrayList<>();

    private DetailDeviceFragmentBinding mBinding;
    private DetailDeviceViewModel viewModel;
    private Device device;
    private String idDevice = "";
    private boolean flashingText = false;
    private HistoryAdapter historyAdapter;
    private int highTemp = 0;
    private long timeL;
    private ArrayList<Device> history = new ArrayList<>();
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    LinearLayoutManager linearLayoutManager;
    private Dialog progressDialog;
    private boolean update;

    public static DetailDeviceFragment newInstance(Device device,
                                                   String idDevice) {

        Bundle args = new Bundle();
        args.putSerializable("Device", device);
        args.putString("idDevice", idDevice);
        DetailDeviceFragment fragment = new DetailDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getBundleData();
        mBinding =
                DataBindingUtil.inflate(inflater,
                        R.layout.detail_device_fragment,
                        container,
                        false);
        initChart();
        unit();
        initAdapter();
        editDeviceName();
        editImage();
        return mBinding.getRoot();
    }

    private void initChart() {
        mChart = mBinding.combineChart;
        mChart.getDescription().setEnabled(false);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);
        mChart.setOnChartValueSelectedListener(this);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);

        timeLabel = new ArrayList<>();

        xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (timeLabel == null) return "";
                return timeLabel.get((int) value % timeLabel.size());
            }
        });
    }

    private static DataSet dataChart(ArrayList<Float> temperature) {
        if (CommonActivity.isNullOrEmpty(temperature)) return null;

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < temperature.size(); index++) {
            entries.add(new Entry(index, temperature.get(index)));
        }

        LineDataSet set = new LineDataSet(entries, "Lịch sử đo nhiệt độ");
        set.setColor(Color.GREEN);
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.GREEN);
        set.setCircleRadius(5f);
        set.setFillColor(Color.GREEN);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.GREEN);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        d.addDataSet(set);

        return set;
    }

    private void clearChart() {
        timeLabel.clear();
        temperature.clear();
        mChart.invalidate();
        mChart.clear();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void editDeviceName() {
        mBinding.imgEdit.setOnClickListener(v -> {
            displayAlertDialog();
        });
    }

    private void editImage() {
        if (!CommonActivity.isNullOrEmpty(device.getPicture())) {
            byte[] decodedString = Base64.decode(device.getPicture(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            mBinding.imageView.setVisibility(View.VISIBLE);
            mBinding.lnPickImage.setVisibility(View.GONE);
            mBinding.tvChoose.setVisibility(View.VISIBLE);
            mBinding.imageView.setImageBitmap(decodedByte);
        } else {
            mBinding.tvChoose.setVisibility(View.GONE);
            mBinding.imageView.setVisibility(View.GONE);
            mBinding.lnPickImage.setVisibility(View.VISIBLE);
        }

        mBinding.imgEditImage.setOnClickListener(v -> {
            selectImage(getActivity());
        });
    }

    private void initAdapter() {
        linearLayoutManager
                = new LinearLayoutManager(getActivity());
        mBinding.listHistory.setLayoutManager(linearLayoutManager);
        historyAdapter = new HistoryAdapter(getActivity(), history);
        mBinding.listHistory.setAdapter(historyAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getBundleData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            device = (Device) bundle.getSerializable("Device");
            idDevice = bundle.getString("idDevice");
        }
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void unit() {
        mDb = AppDatabase.getDatabase(getContext());
        getDB();
        initSpinner();
        initProgress();
        warningService = new Intent(getActivity(), WarningService.class);
        mBinding.btnWarning.setAnimation(createFlashingAnimation());
        mBinding.imgWarning.setAnimation(createFlashingAnimation());
        mBinding.btnThreshold.setOnClickListener(this);
        mBinding.btnOffset.setOnClickListener(this);
        mBinding.btnLoopingTime.setOnClickListener(this);
        mBinding.deleteHistory.setOnClickListener(this);
        mBinding.reset.setOnClickListener(this);

        viewModel = ViewModelProviders
                .of(Objects.requireNonNull(getActivity()))
                .get(DetailDeviceViewModel.class);

        viewModel.observerDevice(device.getIndex()).subscribe(device -> {
            if (update) {
                mBinding.setDetailDevice(device);
                update = false;
            }
            startHandler(device.getNCL(), device);
        });
    }

    private void initProgress() {
        progressDialog = new Dialog(requireActivity());

        Window window = progressDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        progressDialog.
                setContentView(R.layout.progress_dialog);
        progressDialog.
                setCancelable(true);
        progressDialog.
                setCanceledOnTouchOutside(false);
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter
                = ArrayAdapter.createFromResource(Objects.requireNonNull(getActivity()),
                R.array.history_display,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mBinding.spnHistoryDisplay.setAdapter(adapter);
        mBinding.spnHistoryDisplay.setSelection(0);

        mBinding.spnHistoryDisplay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mBinding.combineChart.setVisibility(View.VISIBLE);
                    mBinding.listHistory.setVisibility(View.GONE);
                } else {
                    mBinding.combineChart.setVisibility(View.GONE);
                    mBinding.listHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveDevice(Device device) {
        Single.create(emitter -> {
            Device d = new Device(device.getId(), device.getNO(), device.getTime());
            AppDatabase.getDatabase(getActivity()).deviceDAO().insertDevice(d);
        }).subscribe((o, throwable) -> {
            if (throwable == null) {
                Log.d("saveDevice", o.toString());
            } else {
                Log.d("Error", throwable.toString());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startHandler(String time, Device device) {
        try {
            timeL = Long.parseLong(time) * 1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(() -> {
            mBinding.setDetailDevice(device);
            @SuppressLint("SimpleDateFormat") String timeStamp
                    = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss").format(Calendar.getInstance().getTime());
            device.setTime(timeStamp);
            saveDevice(device);

            @SuppressLint("SimpleDateFormat") String currentTime
                    = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            timeLabel.add(currentTime);
            float temp = 0;
            try {
                temp = Float.parseFloat(device.getNO());
                temperature.add(temp);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            updateChart();
            try {
                compareTemp(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, timeL);
    }

    private void updateChart() {
        CombinedData data = new CombinedData();
        LineData lineDatas = new LineData();
        lineDatas.addDataSet((ILineDataSet) dataChart(temperature));

        data.setData(lineDatas);

        xAxis.setAxisMaximum(data.getXMax() + 0.25f);

        mChart.setData(data);
        mChart.invalidate();
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void compareTemp(Device device1) {
        if (CommonActivity.isNullOrEmpty(device1.getNO()) || CommonActivity.isNullOrEmpty(device1.getNG()))
            return;
        try {
            history.add(0, device1);
            historyAdapter.notifyItemInserted(0);
            mBinding.listHistory.smoothScrollToPosition(0);
            if (Double.parseDouble(device1.getNO()) > Double.parseDouble(device1.getNG())) {
                startWarning(device1.getNG());
                mBinding.btnWarning.setOnClickListener(v -> {
                    cancelWarning();
                });
                Log.d("history", String.valueOf(history.size()));
                Log.d("highTemp", String.valueOf(highTemp));
            } else {
                cancelWarning();
                mBinding.txtHumanTemp.clearAnimation();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void startWarning(String ng) {
        playWarningSound();
        if (!flashingText) {
            mBinding.txtHumanTemp.setAnimation(createFlashingAnimation());
            flashingText = true;
        }
        mBinding.btnWarning.setVisibility(View.VISIBLE);
        mBinding.imgWarning.setVisibility(View.VISIBLE);
        mBinding.btnWarning.setAnimation(createFlashingAnimation());
//        showNoti();
    }

    private void playWarningSound() {
        WarningService.isRunning.observe(this, aBoolean -> {
            if (!aBoolean) {
                if (CommonActivity.isNullOrEmpty(getActivity())) return;
                Objects.requireNonNull(getActivity()).startService(warningService);
            }
        });
    }

    private void cancelWarning() {
        mBinding.btnWarning.setVisibility(View.GONE);
        mBinding.imgWarning.setVisibility(View.GONE);
        mBinding.btnWarning.clearAnimation();
        mBinding.txtHumanTemp.clearAnimation();
        flashingText = false;
        if (getActivity() != null) {
            Objects.requireNonNull(getActivity()).stopService(warningService);
        }
    }

    public static Animation createFlashingAnimation() {
        final Animation flashingAnimation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        flashingAnimation.setDuration(500); // duration - half a second
        flashingAnimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        flashingAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        flashingAnimation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        return flashingAnimation;
    }

    @Override
    public void onResume() {
        super.onResume();
        update = true;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void saveHistory(ArrayList<Device> history) {
        // save the task list to preference
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHARED_PREFS_HISTORY, Context.MODE_PRIVATE);
        editor = prefs.edit();
        try {
            editor.clear();
            editor.putString(KEY_HISTORY, ObjectSerializer.serialize(history));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void displayAlertDialog() {
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View alertLayout = inflater.inflate(R.layout.dialog_edit_device_name, null);
        final EditText edtDeviceName = alertLayout.findViewById(R.id.edtDeviceName);
        final TextView txtWarning = alertLayout.findViewById(R.id.txtWarning);
        if (!CommonActivity.isNullOrEmpty(device.getName())) {
            edtDeviceName.setText(device.getName());
        } else {
            edtDeviceName.setText(device.getId());
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        alert.setTitle("Đổi tên thiết bị");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("Hủy", (dialog, which) -> Toast.makeText(getActivity(), "Hủy", Toast.LENGTH_SHORT).show());

        alert.setPositiveButton("Đồng ý", (dialog, which) -> {
            if (!CommonActivity.isNullOrEmpty(edtDeviceName.getText().toString())) {
                device.setName(edtDeviceName.getText().toString());
                viewModel.setName(device.getIndex(), device.getName());
                txtWarning.setVisibility(View.GONE);
            } else {
                txtWarning.setVisibility(View.VISIBLE);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void selectImage(Context context) {
        final CharSequence[] options = {"Chụp ảnh", "Chọn từ thư viện", "Hủy"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chọn ảnh");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Chụp ảnh")) {
                    if ((ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 111);
                    } else {
                        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 0);
                    }
                } else if (options[which].equals("Chọn từ thư viện")) {
//                    Intent intent = new Intent(getActivity(), GallerySample.class);
//                    startActivityForResult(intent, 111);
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

//                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(pickPhoto, 1);

                } else if (options[which].equals("Hủy")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private void getDB() {
        ArrayList<Device> devices = (ArrayList<Device>) mDb.deviceDAO().getAllDevice(idDevice);
        history = new ArrayList<>(devices);
        for (Device device : devices) {
            timeLabel.add(device.getTime());
            try {
                temperature.add(Float.parseFloat(device.getNO()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        Observable.just(devices).subscribe(device -> {
            Log.d("getDB", "getDB: " + devices.size());
        });
        updateChart();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reset:
                CommonActivity.createDialog(requireActivity(),
                        R.string.reset_device,
                        R.string.app_name,
                        R.string.cancel,
                        R.string.ok,
                        null,
                        v1 -> {
                            progressDialog.show();
                            viewModel.reset(device.getIndex(), "1").subscribe(s -> {
                                if (s.equals("Success")) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Reset thành công!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            progressDialog.dismiss();
                        }).show();
                break;
            case R.id.deleteHistory:
                Objects.requireNonNull(CommonActivity.createDialog(getActivity(),
                        R.string.delete_history,
                        R.string.app_name,
                        R.string.ok,
                        R.string.cancel,
                        v1 -> {
                            AppDatabase.getDatabase(getActivity()).deviceDAO().deleteHistory(idDevice);
                            clearChart();
                            history.clear();
                            historyAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(), "Xóa thành công !", Toast.LENGTH_LONG).show();
                        },
                        null)).show();
                break;
            case R.id.btnThreshold:
                configureThreshold();
                break;
            case R.id.btnOffset:
                configureOffset();
                break;
            case R.id.btnLoopingTime:
                configureLoopingTime();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private void configureLoopingTime() {
        createPopUp(Objects.requireNonNull(getActivity()),
                getString(R.string.looping_time),
                ParamType.LOOPINGTIME,
                "Thời gian");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private void configureOffset() {
        createPopUp(Objects.requireNonNull(getActivity()),
                getString(R.string.offset),
                ParamType.OFFSET,
                "Offset");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private void configureThreshold() {
        createPopUp(Objects.requireNonNull(getActivity()),
                getString(R.string.threshold),
                ParamType.THRESHOLD,
                "Ngưỡng");
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Toast.makeText(getActivity(), "Value: "
                + e.getY()
                + ", index: "
                + h.getX()
                + ", DataSet index: "
                + h.getDataSetIndex(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    public void createPopUp(Activity activity,
                            String title,
                            ParamType type,
                            String paramName) {
        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams") View alertLayout = inflater.inflate(R.layout.dialog_with_edittext, null);
        final EditText editText = alertLayout.findViewById(R.id.editText);
        final TextView tvParamName = alertLayout.findViewById(R.id.tvParamName);

        tvParamName.setText(paramName);

        androidx.appcompat.app.AlertDialog.Builder alert =
                new androidx.appcompat.app.AlertDialog.Builder(activity);
        alert.setTitle(title);
        alert.setView(alertLayout);
        alert.setCancelable(false);

        alert.setNegativeButton(activity.getString(R.string.cancel), (dialog1, which) -> {
            Toast.makeText(getActivity(), getString(R.string.cancel), Toast.LENGTH_SHORT).show();
        });
        alert.setPositiveButton(activity.getString(R.string.ok), (dialog12, which) -> {
            switch (type) {
                case OFFSET:
                    String offSet = editText.getText().toString().trim();
                    if (!CommonActivity.isNullOrEmpty(offSet)) {
                        viewModel.setOffset(device.getIndex(), offSet).subscribe(s -> {
                            if (s.equals("Success")) {
                                editText.setText("");
                                Toast.makeText(getActivity(), "Cài đặt offset thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    break;
                case THRESHOLD:
                    String ng = editText.getText().toString().trim();
                    if (!CommonActivity.isNullOrEmpty(ng)) {
                        if ("HHA000002".equals(device.getId())) {
                            try {
                                int ngDouble = Integer.parseInt(ng) * 10 + 2730;
                                ng = String.valueOf(ngDouble);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                        viewModel.setNG(device.getIndex(), ng).subscribe(s -> {
                            if (s.equals("Success")) {
                                editText.setText("");
                                Toast.makeText(getActivity(), "Cài đặt ngưỡng thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        CommonActivity.showConfirmValidate(getActivity(), "Vui lòng nhập giá trị ngưỡng");
                    }
                    break;
                case LOOPINGTIME:
                    String loopingTime = editText.getText().toString().trim();
                    if (!CommonActivity.isNullOrEmpty(loopingTime)) {
                        viewModel.setLoopingTime(device.getIndex(), loopingTime).subscribe(s -> {
                            if (s.equals("Success")) {
                                editText.setText("");
                                Toast.makeText(getActivity(), "Cài đặt thời gian thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    break;
            }
        });

        androidx.appcompat.app.AlertDialog dialog = alert.create();
        dialog.show();
    }

    enum ParamType {
        OFFSET(1),
        THRESHOLD(2),
        LOOPINGTIME(3);

        private int value;

        ParamType(int i) {
            i = value;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        try {
                            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();
                            double kb = byteArray.length / 1024;
                            if (kb > 1000) {
                                Toast.makeText(getActivity(), "Kích thước ảnh quá lớn, vui lòng chọn lại", Toast.LENGTH_SHORT).show();
                            } else {
                                mBinding.imageView.setImageBitmap(selectedImage);
                                Toast.makeText(getActivity(), "Kích thước ảnh: " + byteArray.length / 1024, Toast.LENGTH_SHORT).show();
                                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                viewModel.setPicture(device.getIndex(), encoded).subscribe();
                            }
                        } catch (RuntimeException e) {
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    break;
                case 1:
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        try {
                            final Uri imageUri = data.getData();
                            final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();

                            double kb = byteArray.length / 1024;
                            if (kb > 1000) {
                                Toast.makeText(getActivity(), "Kích thước ảnh quá lớn, vui lòng chọn lại", Toast.LENGTH_SHORT).show();
                            } else {
                                mBinding.imageView.setImageBitmap(selectedImage);
                                Toast.makeText(getActivity(), "Kích thước ảnh: " + byteArray.length / 1024, Toast.LENGTH_SHORT).show();
                                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                viewModel.setPicture(device.getIndex(), encoded).subscribe();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case 111:
                    if (resultCode == Activity.RESULT_OK) {
                        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 0);
                    }
                    break;
            }
        }
    }
}
