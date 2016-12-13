package com.eris.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eris.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    /*
     * Constants
     */
    final int RC_SIGN_IN = 0000;

    /*
     * Private members
     */
    private GoogleApiClient googleApiClient;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        // Setup listeners for UI elements.
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        // Create shared preferences
        sharedPreferences = getSharedPreferences(getResources()
                .getString(R.string.sharedpreferences_curr_user_account_info), Context.MODE_PRIVATE);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View view) {

        // Determine which button was pressed.
        switch ( view.getId() ) {

            // Google Sign-In button.
            case R.id.sign_in_button:
                signIn();
                break;

            // Unrecognized UI element.
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * Helper method to manage sign-in actions.
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Helper method that handles all actions pertaining to the result of a sign-in action.
     * @param result
     */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("LoginActivity", "handleSignInResult:" + result.isSuccess());

        // Signed in.
        if (result.isSuccess()) {

            // Obtain access to the user's account.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(this, "Welcome " + acct.getDisplayName(), Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getResources()
                    .getString(R.string.sharedpreferences_entry_userID), acct.getEmail());
            editor.commit();

            // Go to the Main activity.
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // Signed out.
        else {
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    }
}
