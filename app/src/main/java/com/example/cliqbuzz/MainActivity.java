package com.example.cliqbuzz;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private int CliqBuzz;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    public MainActivity(int cliqBuzz, FirebaseUser currentUser) {
        CliqBuzz = cliqBuzz;
        this.currentUser = currentUser;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        Toolbar mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        

      

        setSupportActionBar();
        Objects.requireNonNull(getSupportActionBar()).setTitle(CliqBuzz);

        ViewPager myViewPager = findViewById(R.id.main_tabs_pager);
        TabsAccessorAdapter myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
       myViewPager.setAdapter(myTabsAccessorAdapter);

        TabLayout myTabLayout = findViewById(R.id.main_tabs);
       myTabLayout.setupWithViewPager(myViewPager);
    }

    private void setSupportActionBar(Toolbar mToolbar) {
    }

    private void setSupportActionBar() {
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }


}