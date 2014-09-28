package tr.edu.metu.ii.aaa.dialogs;

import tr.edu.metu.ii.aaa.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class ForceStopDialog extends DialogFragment {
    
    public ForceStopDialog(){
        super();
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(R.string.msg_app_stop);
        
        dialog.setPositiveButton(R.string.lbl_ok, 
                                 new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
             
                dialog.dismiss();
            }
        });
        
        return dialog.create();
    }
}
