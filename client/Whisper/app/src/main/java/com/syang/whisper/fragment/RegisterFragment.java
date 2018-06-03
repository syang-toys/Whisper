package com.syang.whisper.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class RegisterFragment extends Fragment {

    private TextView mLoginLink;
    private EditText email;
    private EditText password;
    private EditText mConfirmPassword;
    private Button submit;

    public RegisterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        submit = (Button)view.findViewById(R.id.submit);
        email = (EditText) view.findViewById(R.id.email);
        password = (EditText) view.findViewById(R.id.password);
        mConfirmPassword = (EditText)view.findViewById(R.id.confirm_password);
        mLoginLink = (TextView)view.findViewById(R.id.login);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFieldsFilled()) {
                    ((LoginActivity)getActivity()).onRegister(email.getText().toString(), password.getText().toString(), mConfirmPassword.getText().toString());
                    clear();
                } else {
                    Toast.makeText(getContext(), "Some information miss 😰", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)getActivity()).startLoginFragment();
            }
        });

        return view;
    }

    private boolean isFieldsFilled() {
        return !(email.getText().toString().isEmpty() || password.getText().toString().isEmpty() || mConfirmPassword.getText().toString().isEmpty());
    }

    private void clear(){
        this.email.setText("");
        this.password.setText("");
        this.mConfirmPassword.setText("");
    }
}