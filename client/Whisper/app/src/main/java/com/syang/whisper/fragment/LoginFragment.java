package com.syang.whisper.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.syang.whisper.R;
import com.syang.whisper.activity.LoginActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {

    private TextView mRegisterLink;
    private EditText email;
    private EditText password;
    private Button submit;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        submit = (Button)view.findViewById(R.id.submit);
        email = (EditText) view.findViewById(R.id.email);
        password = (EditText) view.findViewById(R.id.password);
        mRegisterLink = (TextView)view.findViewById(R.id.signup);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFieldsFilled()) {
                    ((LoginActivity)getActivity()).onLogin(email.getText().toString(), password.getText().toString());
                } else {
                    Toast.makeText(getContext(), "Some information miss 😰", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)getActivity()).startRegisterFragment();
            }
        });

        return view;
    }

    private boolean isFieldsFilled() {
        return !(email.getText().toString().isEmpty() || password.getText().toString().isEmpty());
    }
}
