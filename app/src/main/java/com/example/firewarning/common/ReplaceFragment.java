package com.example.firewarning.common;

import android.app.Activity;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.firewarning.MainActivity;
import com.example.firewarning.R;

public class ReplaceFragment {
    private static final String TAG = ReplaceFragment.class.getSimpleName();

    public static void replaceFragment(Activity activity, Fragment fragment, boolean isAnimation) {
        if (activity != null && !activity.isFinishing()) {
//            if (activity instanceof MainActivity) {
//                ((MainActivity) activity).enableViews(true);
//            }

            String backStateName = fragment.getClass().getName();

            FragmentManager manager = ((AppCompatActivity) activity).getSupportFragmentManager();
            boolean fragmentPopped = false;
            try {
                fragmentPopped = manager.popBackStackImmediate(backStateName, 0);
            } catch (IllegalStateException ignored) {
                // There's no way to avoid getting this if saveInstanceState has already been called.
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!fragmentPopped) { // fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();

                if (isAnimation) {
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                }

                ft.replace(R.id.frame_container, fragment, backStateName);

                ft.addToBackStack(backStateName);
                ft.commitAllowingStateLoss();
            }
        }
    }

    public static void replaceFragmentByTag(Activity activity,
                                            Fragment fragment,
                                            boolean isAnimation,
                                            String TAG) {
        if (activity != null && !activity.isFinishing()) {
//            if (activity instanceof MainActivity) {
//                ((MainActivity) activity).enableViews(true);
//            }

            String backStateName = fragment.getClass().getName();

            FragmentManager manager = ((AppCompatActivity) activity).getSupportFragmentManager();
            boolean fragmentPopped = false;
            try {
                fragmentPopped = manager.popBackStackImmediate(backStateName, 0);
            } catch (IllegalStateException ignored) {
                // There's no way to avoid getting this if saveInstanceState has already been called.
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!fragmentPopped) { // fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();

                if (isAnimation) {
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                }

                ft.replace(R.id.frame_container, fragment, backStateName);

                ft.addToBackStack(backStateName);
                ft.commitAllowingStateLoss();
            }
        }
    }

    public static void addFragment(Activity activity, Fragment fragment, boolean isAnimation) {
        if (activity != null && !activity.isFinishing()) {
            String backStateName = fragment.getClass().getName();

            FragmentManager manager = ((AppCompatActivity) activity).getSupportFragmentManager();
            boolean fragmentPopped = false;
            try {
                fragmentPopped = manager.popBackStackImmediate(backStateName, 0);
            } catch (IllegalStateException ignored) {
                // There's no way to avoid getting this if saveInstanceState has already been called.
            } catch (Exception e) {
                e.printStackTrace();

            }

            if (!fragmentPopped) { // fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();

                ft.add(R.id.frame_container, fragment, backStateName);

                ft.addToBackStack(backStateName);
                ft.commitAllowingStateLoss();
            }
        }
    }

    public static Fragment getActiveFragment(Activity activity) {
        Fragment fragment = null;

        FragmentManager manager = ((AppCompatActivity) activity).getSupportFragmentManager();
        if (manager != null && manager.getBackStackEntryCount() > 0) {
            String fragmentTagName = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1).getName();
            fragment = manager.findFragmentByTag(fragmentTagName);
            if (fragment != null) {
                Log.d(Constants.TAG, "getActiveFragment fragmentTagName: " + fragmentTagName + " isVisible: " + fragment.isVisible());
            } else {
                Log.d(Constants.TAG, "getActiveFragment fragmentTagName: " + fragmentTagName + " Fragment NULL");
            }
        } else {
            Log.d(Constants.TAG, "getFragmentManager NULL");
        }
        return fragment;
    }

    public static void replaceFragmentSlidingMenu(Activity activity, Fragment fragment, boolean isAnimation) {
        if (activity != null && !activity.isFinishing()) {
            if (activity instanceof MainActivity) {
//                ((MainActivity) activity).enableViews(true);
            }

            String backStateName = fragment.getClass().getName();

            FragmentManager manager = ((AppCompatActivity) activity).getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped) { // fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.frame_container, fragment);

                ft.addToBackStack(backStateName);
                ft.commitAllowingStateLoss();
            }
        }
    }

//    public static void getActionBar(Activity activity, String title, String content, OnClickListener listener) {
//        ActionBar mActionBar = ((AppCompatActivity) activity).getSupportActionBar();
//        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        mActionBar.setCustomView(R.layout.layout_actionbar_channel);
//        Button btnHome = mActionBar.getCustomView().findViewById(
//                R.id.btnHome);
//        btnHome.setVisibility(View.VISIBLE);
//        btnHome.setOnClickListener(listener);
//        LinearLayout relaBackHome = mActionBar.getCustomView()
//                .findViewById(R.id.relaBackHome);
//        relaBackHome.setOnClickListener(listener);
//        TextView txtNameActionBar = mActionBar.getCustomView()
//                .findViewById(R.id.txtNameActionbar);
//        txtNameActionBar.setText(title);
//        TextView txtAddressActionBar = mActionBar.getCustomView()
//                .findViewById(R.id.txtAddressActionbar);
//        if (content == null) {
//            txtAddressActionBar.setVisibility(View.GONE);
//        } else {
//            txtAddressActionBar.setVisibility(View.VISIBLE);
//            txtAddressActionBar.setText(content);
//        }
//    }

    public static void replaceFragmentToHome(Activity activity, boolean isAnimation) {


    }

    public static void replaceFragmentFromMain(final Activity activity, final Fragment fragment, boolean isAnimation) {
        if (activity != null && !activity.isFinishing()) {
            if (activity instanceof MainActivity) {
//                ((MainActivity) activity).enableViews(true);
            }

            String backStateName = fragment.getClass().getName();

            FragmentManager manager = ((AppCompatActivity) activity).getSupportFragmentManager();
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped) { // fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.frame_container, fragment, backStateName);
                ft.addToBackStack(backStateName);
                ft.commitAllowingStateLoss();
            }
        }
    }

    public static void replaceFragmentWithBackStack(final Activity activity, final Fragment fragment, boolean addToBackStack) {
        if (activity != null && !activity.isFinishing()) {
            String backStateName = fragment.getClass().getName();

            FragmentManager manager = ((AppCompatActivity) activity).getSupportFragmentManager();
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped) {
                FragmentTransaction ft = manager.beginTransaction();

                if (addToBackStack) {
                    ft.replace(R.id.frame_container, fragment, backStateName);
                    ft.addToBackStack(backStateName);
                } else {
                    ft.add(R.id.frame_container, fragment);
                }
                ft.commitAllowingStateLoss();
            }
        }
    }
}
