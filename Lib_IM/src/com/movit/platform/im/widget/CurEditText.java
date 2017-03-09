package com.movit.platform.im.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.movit.platform.im.constants.IMConstants;

/**
 * Created by Administrator on 2015/11/17.
 */
public class CurEditText extends EditText {

    public CurEditText(Context context) {
        super(context);
    }

    public CurEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CurEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:

                String curText = getText().toString();

                int startIndex = getSelectionStart();
                int endIndex = getSelectionEnd();

                    //情景1：start光标位置==end光标位置
                    if(startIndex == endIndex){
                        // 情景1-1：光标位置在@XX内
                        for (String key : IMConstants.atMembers.keySet()) {

                            if (key.contains("#"+startIndex+"#")) {

                                //光标在@XX末尾时，直接抹掉@XX；光标不在@XX末尾，暂不做处理
                                if (key.endsWith("#" + startIndex + "#")) {

                                    String memberName = IMConstants.atMembers.get(key).getEmpCname();
                                    String subStr = curText.substring(startIndex - memberName.length(),startIndex);
                                    //验证删除是否正确,防止规则被破坏的情况
                                    if(subStr.equalsIgnoreCase(memberName)){
                                        setText(curText.substring(0, startIndex - memberName.length() - 1) + curText.substring(startIndex));
                                        setSelection(getText().length());
                                    }
                                    IMConstants.atMembers.keySet().remove(IMConstants.atMembers.get(key));
                                }
                            }
                        }
                        //情景1-2：光标位置不在@XX内，这种情况无需理会
                    }else{
                      //情景2：start光标位置!=end光标位置

                        //情景2-1：start、end光标位置在同一个@XX内
                        //情景2-2：start、end光标位置在不同的@XX内
                        //情景2-3：start、end光标位置都不在@XX内
                        for (String key : IMConstants.atMembers.keySet()) {
                            if (key.contains("#" + startIndex + "#")  || key.contains("#" + endIndex + "#")) {
                                IMConstants.atMembers.keySet().remove(IMConstants.atMembers.get(key));
                            }
                        }
                    }
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }
}
