package org.linphone.setup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.ImageView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.linphone.R;
import org.linphone.core.Status;
import org.linphone.core.StatusResult;
import org.linphone.core.User;
import org.linphone.rest.RingringClient;
import org.springframework.web.client.RestClientException;

@EFragment(R.layout.setup_activate_email)
public class ActivateEmailFragment extends Fragment {

    @FragmentArg
    String email;

    @ViewById
    EditText setupActivationCode;

    @ViewById
    ImageView activateButton;

    ProgressDialog progressDialog;

    @RestService
    RingringClient ringringClient;


    /*
     * UI Event
     * Called when clicked on the activate button
     */
    @Click
    void activateButton() {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.setup_communicating_with_server));
        progressDialog.setMessage(getString(R.string.setup_please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        if (setupActivationCode.getText() == null || setupActivationCode.length() == 0) {
            showAlert(getString(R.string.setup_error),
                    getString(R.string.setup_enter_the_activation_code),
                    getString(R.string.button_ok));
        }
        else {
            activateInBackground();
        }
    }

    /*
     * Async task
     * Send register request to the server
     */
    @Background
    void activateInBackground() {

        try {
            String activationCode = setupActivationCode.getText().toString();

            User user = new User();
            user.setEmail(email);
            user.setActivationCode(activationCode);

            StatusResult statusResult = ringringClient.activate(user, email);
            progressDialog.dismiss();

            // Registration success. Go to the main screen
            // Skip user already message. In this case we still need to go to the main screen
            if(statusResult.isSuccess() || statusResult.getStatus() == Status.USER_ALREADY_ACTIVATED ) {
                login(user);
            }

            // The activation code is not correct
            else if(statusResult.getStatus() == Status.INVALID_ACTIVATION_CODE) {
                requestRepeatActivationCode();
            }

            // Any other non-success message
            else {
                showAlert(getString(R.string.setup_error),
                        statusResult.getStatus().toString(),
                        getString(R.string.button_ok));
            }

            // The response is invalid
        } catch (RestClientException e) {
            showAlert(getString(R.string.setup_error),
                    getString(R.string.setup_connection_error),
                    getString(R.string.button_ok));
        }
    }

    @UiThread
    void requestRepeatActivationCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.setup_invalid_activation_code))
                .setMessage(getString(R.string.setup_do_you_want_to_try_again))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setupActivationCode.setText("");
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        SetupActivity.instance().displayRegisterEmail();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /*
     * Registration success. Go to the main screen
     */
    @UiThread
    public void login(User user) {
        String sipUser = user.getEmail().replace("@", "_AT_");

        SetupActivity.instance().genericLogIn(sipUser, user.getActivationCode(), "sip.ringring.io");
    }

    /*
     * Helper function
     * show a generic alert window
     */
    @UiThread
    public void showAlert(String title, String message, String button) {

        // close progress dialog
        progressDialog.dismiss();

        // show alert
        new AlertDialog
                .Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(button, null)
                .show();
    }
}
