package com.artificialsolutions.tiesdktestapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.artificialsolutions.tiesdk.TieApiService;
import com.artificialsolutions.tiesdk.model.TieCloseSessionResponse;
import com.artificialsolutions.tiesdk.model.TieResponse;
import com.google.gson.Gson;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private EditText inputTextField;
    private EditText parameter1TextField;
    private EditText parameter1ValueTextField;
    private EditText parameter2TextField;
    private EditText parameter2ValueTextField;

    private TextView inputText;
    private TextView outputText;
    private TextView rawResponseText;

    private final String baseUrl = "fill in base url before use";
    private final String endPoint = "fill in endpoint url before use";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        rawResponseText = findViewById(R.id.rawResponseText);
        inputTextField = findViewById(R.id.inputTextField);
        parameter1TextField = findViewById(R.id.param1TextField);
        parameter1ValueTextField = findViewById(R.id.param1ValueTextField);
        parameter2TextField = findViewById(R.id.param2TextField);
        parameter2ValueTextField = findViewById(R.id.param2ValueTextField);

        TieApiService.getSharedInstance().setup(this.getApplicationContext(), baseUrl, endPoint);

        final Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String text = inputTextField.getText().toString();
                String param1 = parameter1TextField.getText().toString();
                String param1Value = parameter1ValueTextField.getText().toString();
                String param2 = parameter2TextField.getText().toString();
                String param2Value = parameter2ValueTextField.getText().toString();

                HashMap<String, String> parameters = new HashMap<>();
                if (param1 != null && !param1.isEmpty()) parameters.put(param1, param1Value);
                if (param2 != null && !param2.isEmpty()) parameters.put(param2, param2Value);

                sendInput(text, parameters);
            }
        });

        final Button closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closeSession();
            }
        });
    }

    private void sendInput(String text, HashMap<String, String> parameters) {
        TieApiService.getSharedInstance().sendInput(text, parameters)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableSingleObserver<TieResponse>() {
                @Override
                public void onSuccess(TieResponse result) {
                    Gson gson = new Gson();
                    String json = gson.toJson(result);
                    rawResponseText.setText(json);

                    inputText.setText(result.getInput().getText());
                    outputText.setText(result.getOutput().getText());
                }

                @Override
                public void onError(Throwable e) {
                    inputText.setText("");
                    outputText.setText(e.getMessage());
                    rawResponseText.setText("");
                }
            });
    }

    private void closeSession() {
        TieApiService.getSharedInstance().close()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<TieCloseSessionResponse>() {
                    @Override
                    public void onSuccess(TieCloseSessionResponse result) {
                        Gson gson = new Gson();
                        String json = gson.toJson(result);
                        rawResponseText.setText(json);

                        inputText.setText(R.string.close_session_feedback);
                        outputText.setText(result.getResponse().getMessage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        inputText.setText("");
                        outputText.setText(e.getMessage());
                        rawResponseText.setText("");
                    }
                });
    }
}
