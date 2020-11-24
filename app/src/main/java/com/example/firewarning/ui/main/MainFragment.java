package com.example.firewarning.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.firewarning.ItemDecorationAlbumColumns;
import com.example.firewarning.MainActivity;
import com.example.firewarning.R;
import com.example.firewarning.common.BaseBindingAdapter;
import com.example.firewarning.common.CommonActivity;
import com.example.firewarning.common.ReplaceFragment;
import com.example.firewarning.dao.AppDatabase;
import com.example.firewarning.databinding.MainFragmentBinding;
import com.example.firewarning.ui.device.DetailDeviceFragment;
import com.example.firewarning.ui.device.DetailDeviceViewModel;
import com.example.firewarning.ui.device.LightConfigPopup;
import com.example.firewarning.ui.device.model.Device;
import com.example.firewarning.ui.login.LoginViewModel;
import com.example.firewarning.utils.FireBaseCallBack;
import com.example.firewarning.utils.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.reactivex.Single;
import io.reactivex.functions.Function;

public class MainFragment extends Fragment implements BaseBindingAdapter.OnItemClickListener<Device> {
    private static final String MyPREFERENCES = "MyPrefs1";

    private MainViewModel mainViewModel;
    private LoginViewModel loginViewModel;
    private DetailDeviceViewModel detailDeviceViewModel;
    private BaseBindingAdapter<Device> adapter;
    private MainFragmentBinding mainFragmentBinding;
    private String idDevice;
    private EditText txtInputDevice;
    private Intent tempMonitoringService;
    private SharedPreferences sharedPreferences;

    public static MainFragment newInstance(String idDevice) {

        Bundle args = new Bundle();

        args.putString("idDevice", idDevice);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getBundleData();
        mainFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false);
        unit();
        checkService();
        onFabClicked();
        return mainFragmentBinding.getRoot();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.reset);
        item.setVisible(false);
    }

    private void checkService() {
        if (TempMonitoringService.isRunning != null) {
            TempMonitoringService.isRunning.observe(this, aBoolean -> {
                mainFragmentBinding.switchObserve.setChecked(aBoolean);
                if (!aBoolean) {
                    Toast.makeText(getActivity(), getString(R.string.service_offline), Toast.LENGTH_SHORT).show();
                    mainFragmentBinding.tvObserve.setText(getString(R.string.turn_off_monitoring));
                } else {
                    Toast.makeText(getActivity(), getString(R.string.service_online), Toast.LENGTH_SHORT).show();
                    mainFragmentBinding.tvObserve.setText(getString(R.string.turn_on_monitoring));
                }
            });
        } else {
            mainFragmentBinding.switchObserve.setChecked(false);
            mainFragmentBinding.tvObserve.setText(getString(R.string.turn_off_monitoring));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onFabClicked() {
        mainFragmentBinding.fab.setOnClickListener(v -> {
            addDevice();
        });
    }

    private void getBundleData() {
        idDevice = "";
        Bundle bundle = getArguments();
        if (!CommonActivity.isNullOrEmpty(bundle)) {
            if (CommonActivity.isNullOrEmpty(idDevice)) {
                idDevice = bundle.getString("idDevice");
            }
        }
    }

    @SuppressLint("NewApi")
    private void unit() {
        loginViewModel = ViewModelProviders
                .of(Objects.requireNonNull(getActivity()))
                .get(LoginViewModel.class);
        detailDeviceViewModel = ViewModelProviders
                .of(Objects.requireNonNull(getActivity()))
                .get(DetailDeviceViewModel.class);
        adapter = new BaseBindingAdapter<>(getActivity(), R.layout.item_device);
        mainViewModel =
                ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(MainViewModel.class);
        mainFragmentBinding.setLifecycleOwner(this);
        mainFragmentBinding.listDevice.setAdapter(adapter);
        int mNoOfColumns = Utility.calculateNoOfColumns(Objects.requireNonNull(getContext()), 130);
        mainFragmentBinding.listDevice
                .setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mainFragmentBinding.listDevice.addItemDecoration(new ItemDecorationAlbumColumns(
                getResources().getDimensionPixelSize(R.dimen.photos_list_spacing),
                mNoOfColumns));

        // TODO: Use the ViewModel
        // Read from the database
        tempMonitoringService = new Intent(getActivity(), TempMonitoringService.class);
        tempMonitoringService.putExtra("idDevice", idDevice);

        initSharedPreferences();
        observeAllDevice();
        onSwitchObserveChange();

        adapter.setOnItemClickListener(this);
    }

    private void onSwitchObserveChange() {
        mainFragmentBinding.tvObserve.setText(getString(R.string.turn_on_monitoring));
//        if (mainFragmentBinding.switchObserve.isChecked()) {
//            startService();
//        } else {
//            stopService();
//        }
        mainFragmentBinding.switchObserve.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainFragmentBinding.turningSwitch.setVisibility(View.VISIBLE);
            mainFragmentBinding.switchObserve.setVisibility(View.GONE);
            new Handler().postDelayed(() -> {
                mainFragmentBinding.turningSwitch.setVisibility(View.GONE);
                mainFragmentBinding.switchObserve.setVisibility(View.VISIBLE);
            }, 2000);
            boolean switchStatus = mainFragmentBinding.switchObserve.isChecked();

            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("serviceRunning", switchStatus);
            editor.apply();

            if (isChecked) {
                startService();
                mainFragmentBinding.tvObserve.setText(getString(R.string.turn_on_monitoring));
            } else {
                stopService();
                mainFragmentBinding.tvObserve.setText(getString(R.string.turn_off_monitoring));
            }
        });
    }

    private void startService() {
        tempMonitoringService = new Intent(getActivity(), TempMonitoringService.class);
        tempMonitoringService.putExtra("idDevice", idDevice);
        if (getActivity() != null) {
            Objects.requireNonNull(getActivity()).startService(tempMonitoringService);
        }
    }

    private void stopService() {
        if (!CommonActivity.isNullOrEmpty(tempMonitoringService) && getActivity() != null) {
            Objects.requireNonNull(getActivity()).stopService(tempMonitoringService);
        }
    }

    private void restartService() {
        stopService();
        if (mainFragmentBinding.switchObserve.isChecked()) {
            startService();
        }
    }

    private void initSharedPreferences() {
        sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        boolean switchStatus = sharedPreferences.getBoolean("serviceRunning", false);
        if (!CommonActivity.isNullOrEmpty(switchStatus)) {
            mainFragmentBinding.switchObserve.setChecked(switchStatus);
        } else {
            mainFragmentBinding.switchObserve.setChecked(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private void test() {
        mainViewModel.test(idDevice).map((Function<Device, Object>) this::add).subscribe(o -> adapter.setData((ArrayList<Device>) o));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void observeAllDevice() {
//        test();
        mainViewModel.getAllDevices().observe(Objects.requireNonNull(getActivity()), dataSnapshot -> {
            ArrayList<Device> devicesOfUser = new ArrayList<>();
            getData(dataSnapshot, devices -> {
                if (devices != null) {
                    for (Device device : devices) {
                        if (CommonActivity.isNullOrEmpty(idDevice)) {
                            viewEmpty();
                        } else {
                            viewEmpty();
                            if (idDevice.contains(device.getId())) {
                                saveDevice(device);
                                device.setPosition(String.valueOf(devices.indexOf(device)));
                                devicesOfUser.add(device);
                            }
                        }
                    }
                    try {
                        if (!CommonActivity.isNullOrEmpty(devicesOfUser)) {
                            viewList();
                            adapter.setData(devicesOfUser);
                        } else {
                            viewEmpty();
                        }
//                        startService();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void viewEmpty() {
        mainFragmentBinding.listDevice.setVisibility(View.GONE);
        mainFragmentBinding.tvEmpty.setVisibility(View.VISIBLE);
        mainFragmentBinding.tvObserve.setVisibility(View.GONE);
        mainFragmentBinding.switchObserve.setVisibility(View.GONE);
    }

    private void viewList() {
        mainFragmentBinding.listDevice.setVisibility(View.VISIBLE);
        mainFragmentBinding.tvEmpty.setVisibility(View.GONE);
        mainFragmentBinding.tvObserve.setVisibility(View.VISIBLE);
        mainFragmentBinding.switchObserve.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getData(DataSnapshot dataSnapshot, FireBaseCallBack<ArrayList<Device>> callBack) {
        GenericTypeIndicator<ArrayList<Device>> t = new GenericTypeIndicator<ArrayList<Device>>() {
        };
        ArrayList<Device> devices = dataSnapshot.getValue(t);
        callBack.afterDataChanged(devices);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) Objects.requireNonNull(getActivity())).enableBackBtn();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addDevice() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.enter_firebase_url_dialog, null);
        txtInputDevice = alertLayout.findViewById(R.id.txtInputDevice);
        ImageView scanBarcode = alertLayout.findViewById(R.id.scanBarcode);

        AlertDialog.Builder alert = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        alert.setTitle(R.string.app_name);
        alert.setView(alertLayout);
        alert.setCancelable(false);

        scanBarcode.setOnClickListener(v -> {
            Intent scanIntent = new IntentIntegrator(getActivity())
                    .setBeepEnabled(false)
                    .createScanIntent();
            startActivityForResult(scanIntent, 1);
        });

        alert.setNegativeButton(getString(R.string.cancel), (dialog, which)
                -> Toast.makeText(getActivity(), "Cancel clicked", Toast.LENGTH_SHORT).show());

        alert.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            if (txtInputDevice.getText() != null) {
                idDevice += txtInputDevice.getText().toString() + ",";
                loginViewModel.updateDevice(idDevice, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        observeAllDevice();
                        restartService();
                    }
                });
            } else {
                Toast.makeText(getActivity(), getString(R.string.input_device_name), Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemClick(Device item) {
        ReplaceFragment.replaceFragment(getActivity(),
                DetailDeviceFragment.newInstance(item, item.getId()),
                true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBtnEditClick(Device item, int position) {
        displayAlertDialog(item, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemLongClick(Device item) {
        deleteDevice(item);
//        showPopup();
    }

    private void showPopup() {
        LightConfigPopup lightConfigPopup = new LightConfigPopup();
        if (getFragmentManager() != null) {
            lightConfigPopup.show(getFragmentManager(), "LightConfigPopup");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void deleteDevice(Device item) {
        Objects.requireNonNull(CommonActivity.createDialog(getActivity(),
                "Bạn có muốn xóa thiết bị " + item.getId() + "?",
                getString(R.string.app_name),
                getString(R.string.delete),
                getString(R.string.cancel),
                v -> {
                    if (idDevice.contains(item.getId())) {
                        idDevice = idDevice.replaceAll(item.getId(), "");
                        loginViewModel.updateDevice(idDevice, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                observeAllDevice();
                                restartService();
                            }
                        });
                    }
                },
                null)).show();
    }

    interface DataTransfer {
        void transferIdDevice(String idDevice);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(49374, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                if (requestCode == 1) {
                    txtInputDevice.setText(result.getContents());
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void displayAlertDialog(Device device, int indexOfDevice) {
        DetailDeviceViewModel viewModel = new DetailDeviceViewModel();
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View alertLayout = inflater.inflate(R.layout.dialog_edit_device_name, null);
        final EditText edtDeviceName = alertLayout.findViewById(R.id.edtDeviceName);
        final TextView txtWarning = alertLayout.findViewById(R.id.txtWarning);
        final TextView txtDeleteDevice = alertLayout.findViewById(R.id.txtDeleteDevice);

        txtDeleteDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDevice(device);
            }
        });

        if (!CommonActivity.isNullOrEmpty(device.getName())) {
            edtDeviceName.setText(device.getName());
        } else {
            edtDeviceName.setText(device.getId());
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        alert.setTitle(getString(R.string.change_device_name));
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton(getString(R.string.cancel), (dialog, which) -> Toast.makeText(getActivity(), getString(R.string.cancel), Toast.LENGTH_SHORT).show());

        alert.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            if (!CommonActivity.isNullOrEmpty(edtDeviceName.getText().toString())) {
                device.setName(edtDeviceName.getText().toString());
                viewModel.setName(Integer.parseInt(device.getPosition()), device.getName());
                txtWarning.setVisibility(View.GONE);
            } else {
                txtWarning.setVisibility(View.VISIBLE);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<Device> add(Device device) {
        ArrayList<Device> devices = new ArrayList<>();
        Set<String> idSet = new HashSet<>();
        if (idSet.add(device.getId())) {
            devices.add(device);
        } else {
            devices.set((getIndex(idSet, device.getId())), device);
        }
        return devices;
    }

    public static int getIndex(Set<? extends Object> set, Object value) {
        int result = 0;
        for (Object entry : set) {
            if (entry.equals(value)) return result;
            result++;
        }
        return -1;
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
}
