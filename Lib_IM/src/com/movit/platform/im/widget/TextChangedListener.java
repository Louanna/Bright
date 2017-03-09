package com.movit.platform.im.widget;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by Administrator on 2015/11/17.
 */
public class TextChangedListener implements TextWatcher {

    private static final String KEYWORD_AT = "@";
    private static final String KEYWORD_SPACE = " ";

    private  String lastStr = "";
    private static int repalceChatLength = 0;
    private CurKeyClickedListener _curKeyClickedListener;

    public TextChangedListener(CurKeyClickedListener _curKeyClickedListener) {
        this._curKeyClickedListener = _curKeyClickedListener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        repalceChatLength = before;
    }

    @Override
    public void afterTextChanged(Editable s) {
        String curText = s.toString();
        _curKeyClickedListener.onClickedListener(curText);

        //1、第一个字符为@
        //2、空格+@
        //3、0!=repalceChatLength：表示当前为退格键
//        if (KEYWORD_AT.equals(curText) || curText.endsWith(KEYWORD_SPACE + KEYWORD_AT)) {
        if (KEYWORD_AT.equals(curText.trim())) {
            lastStr = curText;
            _curKeyClickedListener.onATClickedListener();
        }
    }

    public interface CurKeyClickedListener {
        public void onATClickedListener();
        public void onClickedListener(String curText);
    }

}
