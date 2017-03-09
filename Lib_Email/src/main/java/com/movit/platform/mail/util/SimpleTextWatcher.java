package com.movit.platform.mail.util;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * all methods empty - but this way we can have TextWatchers with less boilder-plate where
 * we just override the methods we want and not always al 3
 */
public class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
