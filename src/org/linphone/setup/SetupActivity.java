package org.linphone.setup;

import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphonePreferences.AccountBuilder;
import org.linphone.R;
import org.linphone.core.LinphoneAddress.TransportType;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneProxyConfig;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class SetupActivity extends FragmentActivity implements LinphoneCoreListener {
	private static SetupActivity instance;
	private SetupFragmentsEnum currentFragment;
	private SetupFragmentsEnum firstFragment;
	private Fragment fragment;
	private LinphonePreferences mPrefs;
	private boolean accountCreated = false;
	private Handler mHandler = new Handler();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getResources().getBoolean(R.bool.isTablet) && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
		
		setContentView(R.layout.setup);
        firstFragment = SetupFragmentsEnum.REGISTER_EMAIL;

        if (findViewById(R.id.fragmentContainer) != null) {
            if (savedInstanceState == null) {
                displayRegisterEmail();

            } else {
            	currentFragment = (SetupFragmentsEnum) savedInstanceState.getSerializable("CurrentFragment");
            }
        }
        else {
            displayRegisterEmail();
        }

        mPrefs = LinphonePreferences.instance();
        instance = this;
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.addListener(this);
		}
	}
	
	@Override
	protected void onPause() {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.removeListener(this);
		}
		
		super.onPause();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("CurrentFragment", currentFragment);
		super.onSaveInstanceState(outState);
	}
	
	public static SetupActivity instance() {
		return instance;
	}
	
	private void changeFragment(Fragment newFragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
//		transaction.addToBackStack("");
		transaction.replace(R.id.fragmentContainer, newFragment);
		
		transaction.commitAllowingStateLoss();
	}

    public void changeActivateEmailFragment(String email) {
        ActivateEmailFragment fragment = new ActivateEmailFragment_();
        Bundle bundle = new Bundle();

        bundle.putString("email", email);
        fragment.setArguments(bundle);

        changeFragment(fragment);
        currentFragment = SetupFragmentsEnum.ACTIVATE_EMAIL;
    }

	private void launchEchoCancellerCalibration(boolean sendEcCalibrationResult) {
		boolean needsEchoCalibration = LinphoneManager.getLc().needsEchoCalibration();
		if (needsEchoCalibration && mPrefs.isFirstLaunch()) {
			EchoCancellerCalibrationFragment fragment = new EchoCancellerCalibrationFragment();
			fragment.enableEcCalibrationResultSending(sendEcCalibrationResult);
			changeFragment(fragment);
			currentFragment = SetupFragmentsEnum.ECHO_CANCELLER_CALIBRATION;
		} else {
			if (mPrefs.isFirstLaunch()) {
				mPrefs.setEchoCancellation(false);
			}
			success();
		}
	}

	private void logIn(String username, String password, String domain, boolean sendEcCalibrationResult) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && getCurrentFocus() != null) {
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}

        saveCreatedAccount(username, password, domain);

		if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
			launchEchoCancellerCalibration(sendEcCalibrationResult);
		}
	}
	
	public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage) {
		if (state == RegistrationState.RegistrationOk) {
			
			if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
				mHandler .post(new Runnable () {
					public void run() {
						launchEchoCancellerCalibration(true);
					}
				});
			}
		} else if (state == RegistrationState.RegistrationFailed) {
			mHandler.post(new Runnable () {
				public void run() {
					Toast.makeText(SetupActivity.this, getString(R.string.first_launch_bad_login_password), Toast.LENGTH_LONG).show();
				}
			});
		}
	}
	
	public void genericLogIn(String username, String password, String domain) {
		logIn(username, password, domain, false);
	}

    public void displayRegisterEmail() {
        fragment = new RegisterEmailFragment_();
        changeFragment(fragment);
        currentFragment = SetupFragmentsEnum.REGISTER_EMAIL;
    }

	public void saveCreatedAccount(String username, String password, String domain) {
		if (accountCreated)
			return;
		
		AccountBuilder builder = new AccountBuilder(LinphoneManager.getLc())
		.setUsername(username)
		.setDomain(domain)
		.setPassword(password)
        .setTransport(TransportType.LinphoneTransportTls);

        LinphoneManager.getLc().setMediaEncryption(LinphoneCore.MediaEncryption.ZRTP);

        String forcedProxy = getResources().getString(R.string.setup_forced_proxy);
        if (!TextUtils.isEmpty(forcedProxy)) {
            builder.setProxy(forcedProxy)
                    .setOutboundProxyEnabled(true)
                    .setAvpfRRInterval(5);
        }

		try {
			builder.saveNewAccount();
			accountCreated = true;
		} catch (LinphoneCoreException e) {
			e.printStackTrace();
		}
	}

	public void isEchoCalibrationFinished() {
		success();
	}
	
	public void success() {
		mPrefs.firstLaunchSuccessful();
		setResult(Activity.RESULT_OK);
		finish();
	}
}
