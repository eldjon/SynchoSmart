package tr.edu.metu.ii.aaa.dialogs;

import tr.edu.metu.ii.aaa.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

@SuppressLint("ValidFragment")
public class InfoDialog extends DialogFragment {
  
    private ListView _statusLv;
    private ArrayAdapter<String> _listAdapter;

    public InfoDialog(){

        super();
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater()
                                 .inflate(R.layout.dialog_info_layout,
                                  null);
        _statusLv    = (ListView) view.findViewById(R.id.status_lv);
        _listAdapter = new ArrayAdapter<String>(getActivity(), 
                                                android
                                                .R
                                                .layout.simple_dropdown_item_1line);
        
        _statusLv.setAdapter(_listAdapter);
        dialog.setView(view);
        
        dialog.setPositiveButton(R.string.lbl_ok, 
                                 new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
             
                dialog.dismiss();
            }
        });
        
        return dialog.create();
    }
    
    public ListView getStatusList(){
        
        return _statusLv;
    }
    
    public void addStatus(String status){
        
        _listAdapter.add(status);
        _listAdapter.notifyDataSetChanged();
    }
}
