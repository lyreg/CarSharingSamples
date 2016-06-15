package com.uestc.lyreg.carsharing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import com.uestc.lyreg.carsharing.presenter.OpenMvpOps;
import com.uestc.lyreg.carsharing.presenter.OpenPresenter;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

public class OpenActivity extends AppCompatActivity
        implements OpenMvpOps.RequiredViewOps{

    private final String TAG = getClass().getSimpleName();

    // Responsible to maintain the Objects state
    // during changing configuration
    private final StateMaintainer mStateMaintainer = new StateMaintainer(this.getFragmentManager(), TAG);

    // Presenter operations
    private OpenMvpOps.PresenterOps mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        setupToolbar();
        setupWindowAnimations();

        startMVPOps();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("OpenDoor");
    }

    private void setupWindowAnimations() {
        Explode explode = new Explode();
        explode.setInterpolator(new BounceInterpolator());
        explode.setDuration(500);

        getWindow().setEnterTransition(explode);
        getWindow().setReturnTransition(explode);
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
    private void initialize(OpenMvpOps.RequiredViewOps view)
            throws InstantiationException, IllegalAccessException {
        mPresenter = new OpenPresenter(view);
        mStateMaintainer.put(OpenMvpOps.PresenterOps.class.getSimpleName(), mPresenter);
    }
    /**
     * Recovers Presenter and informs Presenter that occurred a config change.
     * If Presenter has been lost, recreates a instance
     */
    private void reinitialize( OpenMvpOps.RequiredViewOps view)
            throws InstantiationException, IllegalAccessException {
        mPresenter = mStateMaintainer.get(OpenMvpOps.PresenterOps.class.getSimpleName());

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

    public void onButtonOpenClicked(View v) {
        if(v.getId() == R.id.button_open) {
            doOpen();
        }
    }

    private void doOpen() {

        try {
            X509Certificate serverCert = ((BaseApplication) getApplication()).getServerCert();
            KeyPair keyPair = ((BaseApplication) getApplication()).getClientKeyPair();
            String serialNum = ((BaseApplication) getApplication()).getClientCrtSerialNum();
            if(serialNum == null) {
                throw new Exception("Please enroll before open!");
            }
            mPresenter.open(serverCert, keyPair, serialNum, "001");
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
