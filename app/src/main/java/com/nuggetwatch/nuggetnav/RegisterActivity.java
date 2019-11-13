package com.nuggetwatch.nuggetnav;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    public static final String MY_PREFS_NAME = "Prefs";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email = findViewById(R.id.emailInput);
                EditText name = findViewById(R.id.nameInput);
                boolean checks = true;

                if (name.getText().toString().matches("")) {
                    TextInputLayout nameLayout = findViewById(R.id.nameLayout);
                    nameLayout.setErrorEnabled(true);
                    nameLayout.setError("Name can't be blank");
                    checks = false;
                }

                if (!isValidEmail(email.getText())) {
                    TextInputLayout emailLayout = findViewById(R.id.emailLayout);
                    emailLayout.setErrorEnabled(true);
                    emailLayout.setError("Please use a valid email address");
                    checks = false;
                }

                if (checks) {
                    prefs.edit().putString("name", name.getText().toString()).apply();
                    prefs.edit().putString("email", email.getText().toString()).apply();

                    Intent intent = new Intent(RegisterActivity.this, ReviewActivity.class);
                    intent.putExtra("name", getIntent().getStringExtra("name"));
                    intent.putExtra("nicename", getIntent().getStringExtra("nicename"));
                    finish();
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
