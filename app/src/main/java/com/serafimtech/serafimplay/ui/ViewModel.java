package com.serafimtech.serafimplay.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;

public class ViewModel extends androidx.lifecycle.ViewModel {
    private MutableLiveData<String> deviceconnect;
    private MutableLiveData<Integer> gametag;
    private MutableLiveData<String> gamepage;
    private MutableLiveData<String> navigationpage;
    private MutableLiveData<Boolean> updaterecycler;
    private MutableLiveData<Boolean> getupdategamepage;
    private MutableLiveData<String> option;
    private MutableLiveData<String> getr1recycleradaptertype;
    private MutableLiveData<HashMap<String,String>> r1gamepage;

    public LiveData<String> getdeviceconnect() {
        if (deviceconnect == null) {
            deviceconnect = new MutableLiveData<>();
        }
        return deviceconnect;
    }

    public LiveData<Integer> getgametag() {
        if (gametag == null) {
            gametag = new MutableLiveData<>();
        }
        return gametag;
    }

    public LiveData<String> getgamepage() {
        if (gamepage == null) {
            gamepage = new MutableLiveData<>();
        }
        return gamepage;
    }

    public LiveData<String> getnavigationpage() {
        if (navigationpage == null) {
            navigationpage = new MutableLiveData<>();
        }
        return navigationpage;
    }

    public LiveData<Boolean> getupdaterecycler() {
        if (updaterecycler == null) {
            updaterecycler = new MutableLiveData<>();
        }
        return updaterecycler;
    }

    public LiveData<String> getoption() {
        if (option == null) {
            option = new MutableLiveData<>();
        }
        return option;
    }

    public LiveData<HashMap<String,String>> r1gamepage() {
        if (r1gamepage == null) {
            r1gamepage = new MutableLiveData<>();
        }
        return r1gamepage;
    }


}