package tr.edu.metu.ii.aaa.gui;

import tr.edu.metu.ii.aaa.R;
import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.gson.GsonQuestionList;
import tr.edu.metu.ii.aaa.gson.Option;
import tr.edu.metu.ii.aaa.gson.Question;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


public class QuestionnaireAdapter extends ArrayAdapter<Question>{
    
    private GsonQuestionList _data;

    public QuestionnaireAdapter(Context context,
                                GsonQuestionList data) {

        super(context,
              R.layout.question_layout);
        _data = data;
    }
    
    // ******************************************************************************* //
    // ************************ OVERRIDEN METHODS RELATED TO DATA ******************** //
    // ******************************************************************************* //
    @Override
    public long getItemId(int position) {

        return _data.get(position).getId();
    }
 
    @Override
    public int getPosition(Question item) {

        return _data.indexOf(item);
    }

    @Override
    public void clear() {

        if(_data != null)
            _data.clear();
        super.clear();
    }


    @Override
    public Question getItem(int position) {

        return _data.get(position);
    }

    @Override
    public void remove(Question object) {

        super.remove(object);
        _data.remove(object);
    }

    @Override
    public int getCount() {

        return _data.size();
    }
    
    @Override
    public View getView(final int position, View row, ViewGroup parent) {

        final Question item     = getItem(position);
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        String question = getContext().getText(R.string.lbl_question).toString();
        question = question.concat(" " + Integer.toString(position + 1) + " : ");
        question = question.concat(item.getValue());
        
        if(item.getOptions().size() <= 0){
            // if there is no option we display just a EditText for the answer
            row    = inflater.inflate(R.layout.question_layout, 
                                      parent, 
                                      false);
            final QuestionHolder holder = new QuestionHolder();
            holder._answerEt   = (EditText) row.findViewById(R.id.answer_et);
            holder._answerEt.addTextChangedListener(new TextWatcher() {
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setAnswer(holder._answerEt.getText().toString());
                }
                
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
            holder._questionTv = (TextView) row.findViewById(R.id.question_tv);
            holder._questionTv.setText(question);
            row.setTag(holder);
        }else {
            
            final OptionalQuestionHolder optionalHolder = new OptionalQuestionHolder();
            row            = inflater.inflate(R.layout.optional_question_layout, 
                                              parent, 
                                              false);
            optionalHolder._questionTv = (TextView) row.findViewById(R.id.optional_question_tv);
            optionalHolder._optionsRg  = (RadioGroup) row.findViewById(R.id.option_rg);
            for(Option o : item.getOptions()){
                RadioButton rb = new RadioButton(this.getContext());
                rb.setText(o.getText());
                optionalHolder._optionsRg.addView(rb);

                optionalHolder._optionsRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        
                        item.setSelectedOption(group.indexOfChild(group.findViewById(checkedId)));
                    }
                });
            }
            optionalHolder._questionTv.setText(question);
            row.setTag(optionalHolder);
        }

        return row;
    }
    
    public String getGsonAnswers(){
        
        return ((AnalysisApp)((Activity)getContext()).getApplication())
                                       .getQuestionnaireGson()
                                       .toJson(_data);
    }
    
    public boolean areAllQuestionsAnswered(){
        
        for(Question q : _data){
            if(q.getOptions().size() <= 0){
                if(q.getAnswer().equals(""))
                    return false;
            } else {
                if(q.getSelectedOption() < 0)
                    return false;
            }
        }
        
        return true;
    }
    
    private class OptionalQuestionHolder{
        
        TextView   _questionTv;
        RadioGroup _optionsRg;
    }
    
    private class QuestionHolder {
        
        TextView _questionTv;
        EditText _answerEt;
    }
}
