package com.example.vanessa.groupsms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;

import org.w3c.dom.DocumentFragment;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "hr.rma.textscanner.MESSAGE";
    private FragmentAdapter mFragmentAdapter;
    private ViewPager mViewPager;
    public static ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_tab);
        // Find the view pager that will allow the user to swipe between fragments
        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        //spusti tipkovnicu
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Retrieving data, please wait. ");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void setupViewPager(ViewPager viewPager){
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainActivityTab(), "Groups");
        adapter.addFragment(new Templates(), "Templates");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
