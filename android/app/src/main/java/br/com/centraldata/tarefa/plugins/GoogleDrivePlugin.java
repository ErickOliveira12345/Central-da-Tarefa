
package br.com.centraldata.tarefa.plugins;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import br.com.centraldata.tarefa.GoogleDriveAuthActivity;

@CapacitorPlugin(name = "GoogleDrive")
public class GoogleDrivePlugin extends Plugin {

    @PluginMethod
    public void authorize(PluginCall call) {
        Intent intent = new Intent(
            getContext(),
            GoogleDriveAuthActivity.class
        );

        startActivityForResult(
            call,
            intent,
            "authorizationResult"
        );
    }

    @ActivityCallback
    private void authorizationResult(
        PluginCall call,
        ActivityResult result
    ) {
        if (call == null) {
            return;
        }

        if (result.getResultCode() != Activity.RESULT_OK) {
            Intent data = result.getData();

            String error = data != null
                ? data.getStringExtra("error")
                : "Autorização cancelada.";

            call.reject(
                error != null ? error : "Autorização cancelada."
            );
            return;
        }

        Intent data = result.getData();

        if (data == null) {
            call.reject("O Google não retornou os dados da autorização.");
            return;
        }

        String accessToken = data.getStringExtra("accessToken");

        if (accessToken == null || accessToken.isEmpty()) {
            call.reject("O Google não retornou um token de acesso.");
            return;
        }

        JSObject response = new JSObject();
        response.put("accessToken", accessToken);

        call.resolve(response);
    }
}
