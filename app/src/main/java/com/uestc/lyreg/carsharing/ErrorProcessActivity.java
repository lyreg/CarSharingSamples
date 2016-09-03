package com.uestc.lyreg.carsharing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.uestc.lyreg.carsharing.presenter.ErrorProcessMvpOps;
import com.uestc.lyreg.carsharing.presenter.ErrorProcessPresenter;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

public class ErrorProcessActivity
        extends AppCompatActivity
        implements ErrorProcessMvpOps.RequiredViewOps {

    private final String TAG = getClass().getSimpleName();

    // Responsible to maintain the Objects state
    // during changing configuration
    private final StateMaintainer mStateMaintainer = new StateMaintainer(this.getFragmentManager(), TAG);

    // Presenter operations
    private ErrorProcessMvpOps.PresenterOps mPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_process);

        setupToolbar();
        setupWindowAnimations();

        startMVPOps();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("异常流程");
    }

    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(500);

        getWindow().setEnterTransition(fade);
        getWindow().setReturnTransition(fade);
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
    private void initialize(ErrorProcessMvpOps.RequiredViewOps view)
            throws InstantiationException, IllegalAccessException {
        mPresenter = new ErrorProcessPresenter(view);
        mStateMaintainer.put(ErrorProcessMvpOps.PresenterOps.class.getSimpleName(), mPresenter);
    }

    /**
     * Recovers Presenter and informs Presenter that occurred a config change.
     * If Presenter has been lost, recreates a instance
     */
    private void reinitialize( ErrorProcessMvpOps.RequiredViewOps view)
            throws InstantiationException, IllegalAccessException {
        mPresenter = mStateMaintainer.get(ErrorProcessMvpOps.PresenterOps.class.getSimpleName());

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

    public void onButtonErrorClicked(View v) {
        int id = v.getId();
        if(id == R.id.button_error1) {
            doErrorProcessOne();
        } else if(id == R.id.button_error2) {
            doErrorProcessTwo();
        } else if(id == R.id.button_error3) {
            doErrorProcessThree();
        }
    }

    private void doErrorProcessOne() {

        try {
            KeyPair keyPair = ((BaseApplication) getApplication()).getClientKeyPair();
            mPresenter.errorProcessOne(keyPair);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(e.getMessage());
        }

    }

    private void doErrorProcessTwo() {

        try {
            X509Certificate serverCert = ((BaseApplication) getApplication()).getServerCert();
            KeyPair keyPair = ((BaseApplication) getApplication()).getClientKeyPair();
            String serialNum = ((BaseApplication) getApplication()).getClientCrtSerialNum();
            if(serialNum == null) {
                throw new Exception("Please enroll before open!");
            }
            mPresenter.errorProcessTwo(serverCert, keyPair, "001");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(e.getMessage());
        }

    }

    private void doErrorProcessThree() {

        try {
            X509Certificate serverCert = ((BaseApplication) getApplication()).getServerCert();
            KeyPair keyPair = ((BaseApplication) getApplication()).getClientKeyPair();
            String serialNum = ((BaseApplication) getApplication()).getClientCrtSerialNum();
            if(serialNum == null) {
                throw new Exception("Please enroll before open!");
            }
            mPresenter.errorProcessThree(serverCert, keyPair, "001");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(e.getMessage());
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
