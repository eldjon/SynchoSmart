package tr.edu.metu.ii.aaa.gui;

import java.util.ArrayList;
import java.util.List;

import tr.edu.metu.ii.aaa.R;
import tr.edu.metu.ii.aaa.core.Constants;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


public class ProbeListAdapter extends ArrayAdapter<String>{
    
    private String[]  _data             = null;
    private boolean[] _checkBoxesStatus = null;

    public ProbeListAdapter(Context context,
                              int textViewResourceId) {
        
        super(context, textViewResourceId);
        init();
    }
    
    /**
     * Initialize the basic list of sensors. For each sensor display an 
     * item on the list view. By default all sensors are selected.
     */
    protected void init(){
        
//        _data    = new String[9];
//        _data[0] = Constants.PROBE_GYRO;
//        _data[1] = Constants.PROBE_GRAVITY;
//        _data[2] = Constants.PROBE_ACC;
//        _data[3] = Constants.PROBE_LINEAR_ACC;
//        _data[4] = Constants.PROBE_ORIENTATION;
//        _data[5] = Constants.PROBE_PROXIMITY;
//        _data[6] = Constants.PROBE_MAG_FIELD;
//        _data[7] = Constants.PROBE_PRESSURE;
//        _data[8] = Constants.PROBE_SOUND_LEVEL;
        
        _data    = new String[6];
        _data[0] = Constants.PROBE_GYRO;
        _data[1] = Constants.PROBE_ACC;
        _data[2] = Constants.PROBE_LINEAR_ACC;
        _data[3] = Constants.PROBE_ORIENTATION;
        _data[4] = Constants.PROBE_MAG_FIELD;
        _data[5] = Constants.PROBE_SOUND_LEVEL;
        

        _checkBoxesStatus = new boolean[_data.length];
        for(int i = 0; i < _checkBoxesStatus.length; i++)
            _checkBoxesStatus[i] = true;
    }
    
    @Override
    public String getItem(int position) {

        return _data[position];
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public int getCount(){
        
        return _data.length;
    }
    
    @Override
    public int getPosition(String item) {

        for(int i = 0; i < _data.length; i++)
            if(_data[i].equals(item))
                return i;
        
        return -1;
    }
    
    public List<String> getSelectedProbes(){
        
        List<String> result = new ArrayList<String>();
        
        for(int i = 0; i < _checkBoxesStatus.length; i++)
            if(_checkBoxesStatus[i])
                result.add(_data[i]);
        
        return result;
    }
    
    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        Holder holder = null;
        
        view = ((Activity)getContext()).getLayoutInflater()
                                       .inflate(R
                                                .layout
                                                .probe_layout,
                                                parent,
                                                false);
        holder                  = new Holder();
        holder._nameTv          = (TextView) view.findViewById(R.id.poi_category_tv);
        holder._selectedCheckBx = (CheckBox) view.findViewById(R.id.poi_checkbx);
        
        holder._selectedCheckBx.setChecked(_checkBoxesStatus[position]);
        view.setTag(holder);
        
        CheckBoxOnClickListener l = new CheckBoxOnClickListener(holder, 
                                                                position);
        holder._nameTv.setOnClickListener(l);
        holder._selectedCheckBx.setOnClickListener(l);
        holder._nameTv.setText(_data[position]);
        return view;
    }

    private class Holder{
        
        TextView _nameTv;
        CheckBox _selectedCheckBx;
    }

    private class CheckBoxOnClickListener implements OnClickListener{

        Holder _holder;
        int _position;
        
        public CheckBoxOnClickListener(Holder holder, 
                                       int position){
            
            _holder   = holder;
            _position = position;
        }
        
        @Override
        public void onClick(View v) {

            if(!_checkBoxesStatus[_position]){
                
                _checkBoxesStatus[_position] = true;
                _holder._selectedCheckBx.setChecked(true);
            }
            else {
                
                _checkBoxesStatus[_position] = false;
                _holder._selectedCheckBx.setChecked(false);
            }
        }
    }
    
}

