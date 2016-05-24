package com.uestc.lyreg.carsharing;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.uestc.lyreg.carsharing.presenter.EnrollMvpOps;
import com.uestc.lyreg.carsharing.presenter.EnrollPresenter;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

public class EnrollActivity extends AppCompatActivity
        implements EnrollMvpOps.RequiredViewOps {

    private final String TAG = getClass().getSimpleName();

    private TextInputLayout mEditUsername;
    private TextInputLayout mEditPassword;

    // Responsible to maintain the Objects state
    // during changing configuration
    private final StateMaintainer mStateMaintainer = new StateMaintainer(this.getFragmentManager(), TAG);

    // Presenter operations
    private EnrollMvpOps.PresenterOps mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);

        setupWindowAnimations();
        setupToolBar();
        setupView();

        startMVPOps();
    }

    private void setupWindowAnimations() {
        Slide slide = new Slide(Gravity.RIGHT);
        slide.setDuration(500);
        getWindow().setEnterTransition(slide);
//        getWindow().setReturnTransition(slide);

        Fade fade = new Fade();
        fade.setDuration(500);
        getWindow().setReturnTransition(fade);
    }

    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Registration");
    }

    private void setupView() {
        mEditUsername = (TextInputLayout) findViewById(R.id.username_wrapper);
        mEditPassword = (TextInputLayout) findViewById(R.id.password_wrapper);
    }

    /**
     * Initialize and restart the Presenter.
     * This method should be called after onCreate
     */
    public void startMVPOps() {
        try {
            if(mStateMaintainer.firstTimeIn()) {
                Log.d(TAG, "onCreate() called for the first time");
                initialize(this);
            } else {
                Log.d(TAG, "onCreate() called more than once");
                reinitialize(this);
            }
        } catch ( InstantiationException | IllegalAccessException e ) {
            Log.d(TAG, "onCreate() " + e );
            throw new RuntimeException( e );
        }

    }
    /**
     * Initialize relevant MVP Objects.
     * Creates a Presenter instance, saves the presenter in {@link StateMaintainer}
     */
    private void initialize(EnrollMvpOps.RequiredViewOps view)
            throws InstantiationException, IllegalAccessException {
        mPresenter = new EnrollPresenter(view);
        mStateMaintainer.put(EnrollMvpOps.PresenterOps.class.getSimpleName(), mPresenter);
    }
    /**
     * Recovers Presenter and informs Presenter that occurred a config change.
     * If Presenter has been lost, recreates a instance
     */
    private void reinitialize( EnrollMvpOps.RequiredViewOps view)
            throws InstantiationException, IllegalAccessException {
        mPresenter = mStateMaintainer.get(EnrollMvpOps.PresenterOps.class.getSimpleName());

        if(mPresenter == null) {
            Log.w(TAG, "recreating Presenter");
            initialize(view);
        } else {
            mPresenter.onConfigurationChanged(view);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestory(false);
    }

    public void onButtonSubmetClicked(View v) {
        if(v.getId() == R.id.login_submit_button) {

            String username = mEditUsername.getEditText().getText().toString();
            String password = mEditPassword.getEditText().getText().toString();

            Log.e(TAG, "username => " + username);
            Log.e(TAG, "password => " + password);

            if (!validateUsername(username)) {
                mEditUsername.setError("Not a valid username!");
                Log.e(TAG, "Not a valid username!");
            } else if (!validatePassword(password)) {
                mEditPassword.setError("Not a valid password!");
                Log.e(TAG, "Not a valid password!");
            } else {
                mEditUsername.setErrorEnabled(false);
                mEditUsername.setError(null);
                mEditPassword.setErrorEnabled(false);
                mEditPassword.setError(null);

                doEnroll(username, password);
            }
        }
    }

    private boolean validateUsername(String username) {
        return username.length() > 0;
    }
    private boolean validatePassword(String password) {
        return password.length() > 0;
    }

    private void doEnroll(String username, String password) {

        try {
            X509Certificate serverCert = ((BaseApplication) getApplication()).getServerCert();
            KeyPair keyPair = ((BaseApplication) getApplication()).getClientKeyPair();
            mPresenter.enroll(serverCert, keyPair, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showAlert(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
