package com.mdp.mdpcontroller;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ControlRobotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlRobotFragment extends Fragment implements BluetoothClient.BluetoothCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView    logWindowTextArea;
    private LinearLayout controllerLayout;
    private BluetoothClient bluetoothClient;

    Spinner spinner;
    private ImageButton arrowUpBtn, arrowLeftBtn, arrowRightBtn, arrowDownBtn;

    public ControlRobotFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ControlRobotFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ControlRobotFragment newInstance(String param1, String param2) {
        ControlRobotFragment fragment = new ControlRobotFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if (MainActivity.bluetoothClient != null) {
            bluetoothClient = MainActivity.bluetoothClient;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control_robot, container, false);
        logWindowTextArea = (TextView) view.findViewById(R.id.editTextTextMultiLine);  // Assume you have a TextView in your XML with this id
        logWindowTextArea.setMovementMethod(new ScrollingMovementMethod());
        controllerLayout = view.findViewById(R.id.manualControlLayout);
        arrowUpBtn = (ImageButton) view.findViewById(R.id.upArrow);
        arrowLeftBtn = (ImageButton) view.findViewById(R.id.leftArrow);
        arrowRightBtn = (ImageButton) view.findViewById(R.id.rightArrow);
        arrowDownBtn = (ImageButton) view.findViewById(R.id.downArrow);
        spinner = view.findViewById(R.id.controlDropDownList);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
//                LinearLayout controllerLayout = view.findViewById(R.id.manualControlLayout);
              //  Toast.makeText(parent.getContext(), selectedOption, Toast.LENGTH_SHORT).show();
                if (selectedOption.equals("Manual")){

                    controllerLayout.setVisibility(View.VISIBLE);
                }
                else {
                    controllerLayout.setVisibility(View.GONE);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });


        arrowUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something here when the button is clicked
                Log.d("ControlRobotFragment", "Up arrow button clicked");
                bluetoothClient.sendData("mf");
            }
        });

        arrowLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something here when the button is clicked
                Log.d("ControlRobotFragment", "Left arrow button clicked");
                bluetoothClient.sendData("ml");
            }
        });

        arrowRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something here when the button is clicked
                Log.d("ControlRobotFragment", "Right arrow button clicked");
                bluetoothClient.sendData("mr");
            }
        });

        arrowDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something here when the button is clicked
                Log.d("ControlRobotFragment", "Down arrow button clicked");
                bluetoothClient.sendData("md");
            }
        });
    }

    @Override
    public void onBluetoothDataReceived(String data) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logWindowTextArea.append(data + "\n");  // Append the received data to the existing data in the TextView.
                }
            });
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d("onStart", "At controlrobotfragment");
        if (MainActivity.bluetoothClient != null) {
            bluetoothClient = MainActivity.bluetoothClient;
            bluetoothClient.registerCallback(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bluetoothClient != null) {
            bluetoothClient.unregisterCallback();
        }
    }


}