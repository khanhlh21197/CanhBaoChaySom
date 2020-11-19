package com.example.firewarning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;

public class TestFragment extends Fragment {
    TextView result;
    private String scanResult = "";

    public void setResult(String scanResult){
        this.scanResult = scanResult;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.testfragment, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        Button scan = view.findViewById(R.id.scan);
        result = view.findViewById(R.id.result);

        scan.setOnClickListener(v -> {
            IntentIntegrator.forSupportFragment(TestFragment.this)
                    .setBeepEnabled(false)
                    .initiateScan();
        });
    }
}
