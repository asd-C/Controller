package com.example.cc.controller.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.cc.controller.R;
import com.example.cc.controller.service.tools.SMSHandler;

public class PlaygroundActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout messages_ll;
    private LayoutInflater inflater;
    private EditText input_et;
    private Button enter_btn;
    private ScrollView scrollView;

    private SMSHandler smsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playground);

        smsHandler = new SMSHandler(this, true);

        inflater = this.getLayoutInflater();
        messages_ll = (LinearLayout) findViewById(R.id.messages_ll);
        input_et = (EditText) findViewById(R.id.input_et);
        enter_btn = (Button) findViewById(R.id.enter_btn);
        enter_btn.setOnClickListener(this);
        scrollView = (ScrollView) findViewById(R.id.scrollview);

    }

    private String handleUserMessage(String message) {
        return smsHandler.handleSMS_play(message);
    }

    private void putControllerMessage(String msg) {
        if (msg == null || msg.isEmpty()) return;

        TextView textView = (TextView) inflater.inflate(R.layout.message_controller, messages_ll, false);
        textView.setText(msg);
        messages_ll.addView(textView);
    }

    private String getUserMessage() {
        String tmp = input_et.getText().toString();
        input_et.setText("");
        return tmp;
    }

    private void putUserMessage(String msg) {
        if (msg == null || msg.isEmpty()) return;

        TextView textView = (TextView) inflater.inflate(R.layout.message_user, messages_ll, false);
        textView.setText(msg);
        messages_ll.addView(textView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enter_btn:
                String message = getUserMessage();
                putUserMessage(message);
                putControllerMessage(handleUserMessage(message));
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
                break;

            default:
                break;
        }
    }
}
