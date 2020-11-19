package com.example.firewarning.utils;

import androidx.arch.core.util.Function;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.List;

public class Deserializer <T>implements Function<DataSnapshot, List<T>> {

    @Override
    public List<T> apply(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<ArrayList<T>> t = new GenericTypeIndicator<ArrayList<T>>() {};
        return dataSnapshot.getValue(t);
    }
}
