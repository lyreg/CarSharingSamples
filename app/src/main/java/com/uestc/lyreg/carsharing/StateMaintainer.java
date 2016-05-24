package com.uestc.lyreg.carsharing;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/5/24.
 *
 * @Author lyreg
 */
public class StateMaintainer {

    private final String TAG = getClass().getSimpleName();

    private final String mStateMaintainerTag;
    private final WeakReference<FragmentManager> mFragmentManager;
    private StateMngFragment mStateMaintainerFrag;

    /**
     * Constructor
     * @param fragmentManager       FragmentManager reference
     * @param stateMaintainerTag    the TAG used to insert the state maintainer fragment
     * @return
     * created at 2016/5/24 9:33
     */
    public StateMaintainer(FragmentManager fragmentManager, String stateMaintainerTag) {
        this.mFragmentManager = new WeakReference<FragmentManager>(fragmentManager);
        this.mStateMaintainerTag = stateMaintainerTag;
    }

    /**
     * Create the state maintainer fragment
     * @return  true: the frag was created for the first time
     *          false: recovering the object
     */
    public boolean firstTimeIn() {

        try {
            mStateMaintainerFrag =
                    (StateMngFragment) mFragmentManager.get().findFragmentByTag(mStateMaintainerTag);

            // Creating a new RetainedFragment
            if (mStateMaintainerFrag == null) {
                Log.d(TAG, "Creating a new RetainedFragment " + mStateMaintainerTag);
                mStateMaintainerFrag = new StateMngFragment();
                mFragmentManager.get().beginTransaction()
                        .add(mStateMaintainerFrag, mStateMaintainerTag).commit();

                return true;
            } else {
                Log.d(TAG, "Returns a exist retained fragment " + mStateMaintainerTag);
                return false;
            }
        } catch (NullPointerException e) {
            Log.w(TAG, "Error firstTimeIn()");
            return false;
        }
    }

    /**
     * Insert Object to be preserved during configuration change
     * @param key   Object's TAG reference
     * @param obj   Object to maintain
     */
    public void put(String key, Object obj) {
        mStateMaintainerFrag.put(key, obj);
    }

    /**
     * Recover saved obj
     * @param key   reference TAG
     * @param <T>   Class
     * @return      Obj saved
     */
    public <T> T get(String key) {
        return mStateMaintainerFrag.get(key);
    }

    /**
     * Verify the object existence
     * @param key   Obj TAG
     */
    public boolean hasKey(String key) {
        return mStateMaintainerFrag.get(key) != null;
    }

    /**
     * Insert Object to be preserved during configuration change
     * Uses the Object's class name as a TAG reference
     * Should only be used one time by type class
     * @param obj   Object to maintain
     */
    public void put(Object obj) {
        put(obj.getClass().getName(), obj);
    }

    /**
     * Save and manages objects that should be preserved
     * during configuration changes.
     */
    public static class StateMngFragment extends Fragment {
        private HashMap<String, Object> mData = new HashMap<>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Grants that the frag will be preserved
            setRetainInstance(true);
        }

        /**
         * Insert objects
         * @param key   reference TAG
         * @param obj   Object to save
         */
        public void put(String key, Object obj) {
            mData.put(key, obj);
        }

        /**
         * Insert obj using class name as TAG
         * @param object    obj to save
         */
        public void put(Object object) {
            put(object.getClass().getName(), object);
        }

        /**
         * Recover obj
         * @param key   reference TAG
         * @param <T>   Class
         * @return      Obj saved
         */
        public <T> T get(String key) {
            return (T) mData.get(key);
        }
    }
}
