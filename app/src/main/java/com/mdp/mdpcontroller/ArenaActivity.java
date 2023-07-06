package com.mdp.mdpcontroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ArenaActivity extends AppCompatActivity {
    BluetoothClient bluetoothClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arena);
        if (!MainActivity.bluetoothClient.isInterrupted()){
            bluetoothClient = MainActivity.bluetoothClient;
        }



//        Button configureWifi = (Button) findViewById(R.id.configureWifi);
//        configureWifi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragmentContainerView, WifiFragment.class, null)
//                        .setReorderingAllowed(true)
//                        .commit();
//            }
//        });


//        Button configureArena = (Button) findViewById(R.id.configureArena);
//        configureArena.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragmentContainerView, ArenaFragment.class, null)
//                        .setReorderingAllowed(true)
//                        .commit();
//            }
//        });

        Button changeRightPane = (Button) findViewById(R.id.setupComplete);
        changeRightPane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set upper right pane visibility to be gone
                LinearLayout upperRightPane = findViewById(R.id.upperRightPane);
                upperRightPane.setVisibility(View.GONE);

                //Replace belowRightPane with Control Robot Fragment
                ControlRobotFragment controlRobot = new ControlRobotFragment();


                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.belowRightPane, controlRobot)
                        .commit();
            }
        });
    }
}