package com.example.vanessa.groupsms;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;

import org.w3c.dom.DocumentFragment;

import java.io.File;

public class MainActivityTab extends AppCompatActivity {
    final int REQUEST_TAKE_PHOTO = 1;
    final int PICK_IMAGE_REQUEST = 2;
    //final int RESULT_OK = 0;
    public static boolean storage_flag;
    public static final int EXTERNAL_MEMORY = 2;
    TextView myText;
    Uri photoURI = null;
    String imageFileName;
    File photoFile = null;
    public static final String EXTRA_MESSAGE = "hr.rma.textscanner.MESSAGE";
    private FragmentAdapter mFragmentAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_tab);
        // ImageButton camButton = findViewById(R.id.camera_button);
        //     ImageButton galleryButton = findViewById(R.id.gallery_button);
        // Find the view pager that will allow the user to swipe between fragments
        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        //spusti tipkovnicu
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    private void setupViewPager(ViewPager viewPager){
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new Templates(), "Groups");
        adapter.addFragment(new MainActivity(), "Templates");
        viewPager.setAdapter(adapter);
    }
}
