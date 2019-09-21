
package com.example.face;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.content.Context;
import android.text.Editable;

import java.util.logging.Handler;


public class MainActivity4 extends AppCompatActivity implements TextWatcher, CompoundButton.OnCheckedChangeListener{
    private EditText Username, Pass;
    private CheckBox rememberme;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button btn1;
    private static final String PREF_NAME = "prefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASS = "password";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        Username = (EditText)findViewById(R.id.username);
        Pass = (EditText)findViewById(R.id.password);
        rememberme = (CheckBox)findViewById(R.id.rembemerme);
        btn1 = findViewById(R.id.btn);

//        check remember me
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(sharedPreferences.getBoolean(KEY_REMEMBER, false))
            rememberme.setChecked(true);
        else
            rememberme.setChecked(false);

        Username.setText(sharedPreferences.getString(KEY_USERNAME,""));
        Pass.setText(sharedPreferences.getString(KEY_PASS,""));
        Username.addTextChangedListener(this);
        Pass.addTextChangedListener(this);
        rememberme.setOnCheckedChangeListener(this);

//        button log in
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name="a";
                String pass = "1";
                if (Username.getText().toString().equals(name) && Pass.getText().toString().equals(pass))
                {
                    openAct2();
                }

            }
        });

    }


    public void openAct2()
    {
        Intent intent = new Intent(this,MainActivity3.class);
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Remember();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Remember();
    }

    private void Remember(){
        if(rememberme.isChecked()){
            editor.putString(KEY_USERNAME, Username.getText().toString().trim());
            editor.putString(KEY_PASS, Pass.getText().toString().trim());
            editor.putBoolean(KEY_REMEMBER, true);
            editor.apply();
        }else{
            editor.putBoolean(KEY_REMEMBER, false);
            editor.remove(KEY_PASS);//editor.putString(KEY_PASS,"");
            editor.remove(KEY_USERNAME);//editor.putString(KEY_USERNAME, "");
            editor.apply();
        }
    }

}