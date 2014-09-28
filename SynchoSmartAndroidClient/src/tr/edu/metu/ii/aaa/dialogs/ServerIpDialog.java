package tr.edu.metu.ii.aaa.dialogs;

import tr.edu.metu.ii.aaa.R;
import tr.edu.metu.ii.aaa.core.AnalysisApp;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

public class ServerIpDialog extends DialogFragment{

    private AnalysisApp _app;
    
    public ServerIpDialog(AnalysisApp app){
        
        _app = app;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater()
                                       .inflate(R.layout.server_ip_layout,
                                                null);
        final TextView tv1 = (TextView) view.findViewById(R.id.server_ip_1);
        final TextView tv2 = (TextView) view.findViewById(R.id.server_ip_2);
        final TextView tv3 = (TextView) view.findViewById(R.id.server_ip_3);
        final TextView tv4 = (TextView) view.findViewById(R.id.server_ip_4);
        
        String ip          = _app.getServerIp();
        String[] splitedIp = ip.split("\\.");
        tv1.setText(splitedIp[0]);
        tv2.setText(splitedIp[1]);
        tv3.setText(splitedIp[2]);
        splitedIp[3] = splitedIp[3].replace(".", "");
        tv4.setText(splitedIp[3]);
        
        dialog.setView(view);
        dialog.setPositiveButton(R.string.lbl_ok, 
                                 new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
             
                String serverIp = tv1.getText().toString().concat(".")
                                     .concat(tv2.getText().toString().concat("."))
                                     .concat(tv3.getText().toString().concat("."))
                                     .concat(tv4.getText().toString());
                _app.saveServerIp(serverIp);
                dialog.dismiss();
            }
        });
        
        dialog.setNegativeButton(R.string.lbl_cancel, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
        
                dialog.dismiss();
            }
        });
        
        return dialog.create();
        
    }
 
    
}
