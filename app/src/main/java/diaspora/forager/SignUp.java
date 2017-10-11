package diaspora.forager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private long mLastClickTime = 0;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private EditText email;
    private EditText password;
    private EditText retypePassword;

    private Button signUp;
    private Button clear;

    private void setComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        retypePassword = (EditText) findViewById(R.id.retypePassword);

        signUp = (Button) findViewById(R.id.signUp);
        clear = (Button) findViewById(R.id.clear);
    }

    private void setOnClickListeners() {
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Does not let the user click the sign in button multiple times
                if (SystemClock.elapsedRealtime() - mLastClickTime < 3000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                final String emailStr = email.getText().toString();
                final String passwordStr = password.getText().toString();
                String retypePasswordStr = retypePassword.getText().toString();

                if (!emailStr.isEmpty() && !passwordStr.isEmpty() && !retypePasswordStr.isEmpty()) {
                    if (passwordStr.equals(retypePasswordStr)) {
                        if (passwordStr.length() >= 6) {
                            new AlertDialog.Builder(SignUp.this).setTitle("Terms & Conditions")
                                    // TODO add this in the strings and access with R.strings.termsAndConditions
                                    .setMessage("These are the terms and conditions")
                                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // DO NOTHING
                                        }
                                    })
                                    .setPositiveButton("I Accept", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            createUserWithEmailAndPassword(emailStr, passwordStr);
                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(SignUp.this).setTitle("Password Too Short")
                                    .setMessage("The password should be a minimum of six characters")
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // DO NOTHING
                                        }
                                    })
                                    .show();
                        }
                    } else {
                        new AlertDialog.Builder(SignUp.this).setTitle("Passwords Do Not Match")
                                .setMessage("The 2 passwords that you have entered do not match")
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // DO NOTHING
                                    }
                                })
                                .show();
                    }
                } else {
                    new AlertDialog.Builder(SignUp.this).setTitle("Empty Fields")
                            .setMessage("Please Do Not Leave Any Fields Empty")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // DO NOTHING
                                }
                            })
                            .show();
                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email.setText(null);
                password.setText(null);
                retypePassword.setText(null);
            }
        });
    }

    private void createUserWithEmailAndPassword(String emailStr, String passwordStr) {
        firebaseAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            writeNewUser(FirebaseAuth.getInstance().getCurrentUser());
                            Toast.makeText(SignUp.this, "Account Successfully Created", Toast.LENGTH_LONG).show();
                            // TODO get the nickname;
                            Intent intent = new Intent(SignUp.this, MainMenu.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            new AlertDialog.Builder(SignUp.this).setTitle("Account Taken")
                                    .setMessage("The following account already exists, please enter another email address")
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // DO NOTHING
                                        }
                                    })
                                    .show();
                            FirebaseCrash.log("User attempting to create an account that already exists");
                        }
                    }
                });
    }

    public void writeNewUser(FirebaseUser firebaseUser) {
        // TODO handle errors
        // TODO if google sign in, check whether user is a new one or not
        String userId = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        User user = new User(email, 0, 0);
        databaseReference.child("users").child(userId).setValue(user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setComponents();
        setOnClickListeners();
    }

}
