package com.movitech.EOP.module.workbench.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.module.workbench.adapter.FragmentPageAdapter;
import com.movitech.EOP.module.workbench.fragment.ProcessDoneFragment;
import com.movitech.EOP.module.workbench.fragment.ProcessToDoFragment;
import com.movitech.EOP.view.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by air on 16/10/19.
 * 流程审批
 */

public class ProcessActivity extends EOPBaseActivity {

    public static final int PAGE_COUNT = 2;

    private UnderlinePageIndicator indicator;
    private ViewPager vpFragment;
    private List<TextView> textViews = null;

    private TextView tvNotDo;
    private TextView tvDone;
    private ImageView topImgRight;

    int currentItem = 0;
    String systemCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        findViews();
        initData();
    }

    private void findViews(){
        systemCode = getIntent().getStringExtra("systemCode");

        TextView title = (TextView) findViewById(R.id.common_top_title);
        title.setText(getIntent().getStringExtra("topTitle"));
        ImageView back = (ImageView) findViewById(R.id.common_top_img_left);
        topImgRight = (ImageView) findViewById(R.id.common_top_img_right);
        topImgRight.setImageResource(R.drawable.nf_ico_search);
        topImgRight.setVisibility(View.GONE);
        topImgRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProcessActivity.this,SearchProcessActivity.class);
                intent.putExtra("systemCode",systemCode);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        indicator = (UnderlinePageIndicator) findViewById(R.id.process_indicator);
        vpFragment = (ViewPager) findViewById(R.id.vp_fragment);

        tvNotDo = (TextView) findViewById(R.id.tv_not_do);
        tvNotDo.setText(String.format(getString(R.string.not_do),"0"));
        tvDone = (TextView) findViewById(R.id.tv_done);
        tvDone.setText(String.format(getString(R.string.done),"0"));
    }

    private void initData(){
        textViews = new ArrayList<>();
        textViews.add(tvNotDo);
        textViews.add(tvDone);


        Bundle bundle = new Bundle();
        bundle.putString("systemCode", systemCode);

        ArrayList<Fragment> pages = new ArrayList<>();
        ProcessToDoFragment processToDoFragment = new ProcessToDoFragment();
        processToDoFragment.setArguments(bundle);
        ProcessDoneFragment processDoneFragment = new ProcessDoneFragment();
        processDoneFragment.setArguments(bundle);
        pages.add(processToDoFragment);
        pages.add(processDoneFragment);

        FragmentPageAdapter adapter = new FragmentPageAdapter(getSupportFragmentManager());
        adapter.addAll(pages);
        vpFragment.setOffscreenPageLimit(PAGE_COUNT-1);
        vpFragment.setAdapter(adapter);
        vpFragment.setOnTouchListener(null);
        vpFragment.setCurrentItem(currentItem);
        initTextColor(currentItem);
        indicator.setViewPager(vpFragment);
        indicator.setOnPageChangeListener(new VPOnPageChangeListener());

        processToDoFragment.setProcessNumListener(new ProcessToDoFragment.ProcessNumListener() {
            @Override
            public void getTodoNum(String todo, String done) {
                tvNotDo.setText(String.format(getString(R.string.not_do),todo));
                tvDone.setText(String.format(getString(R.string.done),done));
            }
        });

        processDoneFragment.setProcessNumListener(new ProcessDoneFragment.ProcessNumListener() {
            @Override
            public void getTodoNum(String todo, String done) {
                tvNotDo.setText(String.format(getString(R.string.not_do),todo));
                tvDone.setText(String.format(getString(R.string.done),done));
            }
        });
    }

    void initTextColor(int pos) {
        for (int i = 0; i < textViews.size(); ++i){
            textViews.get(i).setTextColor(
                    getResources().getColor(
                            i == pos ? R.color.red
                                    : R.color.black_0));
        }

        if(pos == 0){
            Drawable clipboard_on = getResources().getDrawable(R.drawable.clipboard_text_on);
            clipboard_on.setBounds(0, 0, clipboard_on.getMinimumWidth(), clipboard_on.getMinimumHeight());
            textViews.get(0).setCompoundDrawables(clipboard_on,null,null,null);

            Drawable ico_yiban = getResources().getDrawable(R.drawable.ico_yiban);
            ico_yiban.setBounds(0, 0, ico_yiban.getMinimumWidth(), ico_yiban.getMinimumHeight());
            textViews.get(1).setCompoundDrawables(ico_yiban,null,null,null);

            topImgRight.setVisibility(View.GONE);

        }else {
            Drawable clipboard = getResources().getDrawable(R.drawable.clipboard_text);
            clipboard.setBounds(0, 0, clipboard.getMinimumWidth(), clipboard.getMinimumHeight());
            textViews.get(0).setCompoundDrawables(clipboard,null,null,null);

            Drawable ico_yiban_on = getResources().getDrawable(R.drawable.ico_yiban_on);
            ico_yiban_on.setBounds(0, 0, ico_yiban_on.getMinimumWidth(), ico_yiban_on.getMinimumHeight());
            textViews.get(1).setCompoundDrawables(ico_yiban_on,null,null,null);

            topImgRight.setVisibility(View.VISIBLE);
        }
    }

    class VPOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int pos) {
            currentItem = pos;
            vpFragment.setCurrentItem(currentItem);
            initTextColor(currentItem);
        }
    }

    public void onTipClick(View view){

        switch (view.getId()) {
            case R.id.tv_not_do:
                currentItem = 0;
                break;
            case R.id.tv_done:
                currentItem = 1;
                break;
            default:
                break;
        }

        vpFragment.setCurrentItem(currentItem);
        initTextColor(currentItem);
    }
}
