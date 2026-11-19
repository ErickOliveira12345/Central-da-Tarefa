
package br.com.centraldata.tarefa;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;
import br.com.centraldata.tarefa.plugins.GoogleDrivePlugin;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(GoogleDrivePlugin.class);
        super.onCreate(savedInstanceState);
    }
}
