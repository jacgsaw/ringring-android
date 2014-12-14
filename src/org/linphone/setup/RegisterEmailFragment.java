package org.linphone.setup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.linphone.R;
import org.linphone.core.Status;
import org.linphone.core.StatusResult;
import org.linphone.core.User;
import org.linphone.core.UserResult;
import org.linphone.rest.RingringClient;
import org.springframework.web.client.RestClientException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.ImageView;


@EFragment(R.layout.setup_register_email)
public class RegisterEmailFragment extends Fragment {
    @ViewById
    EditText setupEmail;

    @ViewById
    ImageView registerButton;

    ProgressDialog progressDialog;

    @RestService
    RingringClient ringringClient;

    /*
	 * UI Event
	 * Called when clicked on the register button
	 */
    @Click
    void registerButton() {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.setup_communicating_with_server));
        progressDialog.setMessage(getString(R.string.setup_please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        if (setupEmail.getText() == null || setupEmail.length() == 0 || !isEmailValid(setupEmail.getText().toString())) {
            showAlert(getString(R.string.setup_error),
                    getString(R.string.setup_invalid_email),
                    getString(R.string.button_ok));
        }
        else {
            registerInBackground();
        }
    }

    /*
     * Async task
     * Send register request to the server
     */
    @Background
    void registerInBackground() {

        try {
            String email = setupEmail.getText().toString();

            User user = new User();
            user.setEmail(email);

            UserResult userResult = ringringClient.register(user);
            progressDialog.dismiss();

            // Go to the activate email screen if the response is success
            if(userResult.isSuccess()) {
                SetupActivity.instance().changeActivateEmailFragment(email);
            }

            // Catch email is already registered error
            else if(userResult.getStatus() == Status.EMAIL_ALREADY_REGISTERED) {
                requestNewActivationCode();
            }

            // Any other non-success message
            else {
                showAlert(getString(R.string.setup_error),
                        userResult.getStatus().toString(),
                        getString(R.string.button_ok));
            }

        // The response is invalid
        } catch (RestClientException e) {
            showAlert(getString(R.string.setup_error),
                      getString(R.string.setup_connection_error),
                      getString(R.string.button_ok));
        }
    }

    /*
     * Async task
     * Send request to send new activation code
     */
    @Background
    void renewActivationCodeInBackground() {
        try {
            String email = setupEmail.getText().toString();

            StatusResult statusResult = ringringClient.renewActivationCode(email);
            progressDialog.dismiss();

            if(statusResult.isSuccess()) {
                SetupActivity.instance().changeActivateEmailFragment(email);
            }

        } catch (RestClientException e) {
            showAlert(getString(R.string.setup_error),
                    getString(R.string.setup_connection_error),
                    getString(R.string.button_ok));
        }
    }

    @UiThread
    public void requestNewActivationCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.setup_email_is_already_registered))
                .setMessage(getString(R.string.setup_do_you_want_new_activation_code))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                        progressDialog.show();
                        renewActivationCodeInBackground();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

    private static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }

        return isValid;
    }
}
