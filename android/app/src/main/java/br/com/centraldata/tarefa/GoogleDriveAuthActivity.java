
package br.com.centraldata.tarefa;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.Scope;

import java.util.Collections;

public class GoogleDriveAuthActivity extends ComponentActivity {

    private final ActivityResultLauncher<IntentSenderRequest> authorizationLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            this::handleAuthorizationResult
        );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestDriveAuthorization();
    }

    private void requestDriveAuthorization() {
        Scope driveScope = new Scope(
            "https://www.googleapis.com/auth/drive.file"
        );

        AuthorizationRequest request =
            AuthorizationRequest.builder()
                .setRequestedScopes(Collections.singletonList(driveScope))
                .build();

        Identity.getAuthorizationClient(this)
            .authorize(request)
            .addOnSuccessListener(this, result -> {
                if (result.hasResolution()) {
                    IntentSenderRequest senderRequest =
                        new IntentSenderRequest.Builder(
                            result.getPendingIntent().getIntentSender()
                        ).build();

                    authorizationLauncher.launch(senderRequest);
                } else {
                    finishSuccessfully(result);
                }
            })
            .addOnFailureListener(this, error -> {
                finishWithError(error.getMessage());
            });
    }

    private void handleAuthorizationResult(ActivityResult activityResult) {
        if (activityResult.getResultCode() != RESULT_OK) {
            finishWithError("Autorização cancelada.");
            return;
        }

        try {
            AuthorizationResult result =
                Identity.getAuthorizationClient(this)
                    .getAuthorizationResultFromIntent(activityResult.getData());

            finishSuccessfully(result);

        } catch (Exception error) {
            finishWithError(error.getMessage());
        }
    }

    private void finishSuccessfully(AuthorizationResult result) {
        String accessToken = result.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            finishWithError("O Google não retornou um token de acesso.");
            return;
        }

        Intent data = new Intent();
        data.putExtra("accessToken", accessToken);

        setResult(RESULT_OK, data);
        finish();
    }

    private void finishWithError(String message) {
        Intent data = new Intent();
        data.putExtra("error", message);

        setResult(RESULT_CANCELED, data);
        finish();
    }
}
