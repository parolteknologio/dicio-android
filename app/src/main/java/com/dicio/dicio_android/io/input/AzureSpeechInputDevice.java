package com.dicio.dicio_android.io.input;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.dicio.dicio_android.ApiKeys;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import com.dicio.dicio_android.R;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.Future;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public class AzureSpeechInputDevice extends InputDevice {
    private static final String azureApiKey = ApiKeys.azure;
    private static final String azureServiceRegion = "westus";

    private MenuItem voiceInputItem;
    private Drawable microphoneOn, microphoneOff;

    private Disposable speechRecognitionDisposable;

    public AzureSpeechInputDevice(Activity context) {
        this.microphoneOn = context.getDrawable(R.drawable.ic_mic_white);
        this.microphoneOff = context.getDrawable(R.drawable.ic_mic_none_white);

        ActivityCompat.requestPermissions(context, new String[]{RECORD_AUDIO, INTERNET}, 5);
    }

    public void setVoiceInputItem(MenuItem voiceInputItem) {
        this.voiceInputItem = voiceInputItem;

        voiceInputItem.setIcon(microphoneOff);
        voiceInputItem.setOnMenuItemClickListener(item -> {
            startListening();
            return false;
        });
    }

    @Override
    public void startListening() {
        voiceInputItem.setIcon(microphoneOn);
        if (speechRecognitionDisposable != null) speechRecognitionDisposable.dispose();

        //noinspection CodeBlock2Expr
        speechRecognitionDisposable = Completable.fromCallable(() -> {
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(azureApiKey, azureServiceRegion);
            SpeechRecognizer speechRecognizer = new SpeechRecognizer(speechConfig);
            Future<SpeechRecognitionResult> task = speechRecognizer.recognizeOnceAsync();

            SpeechRecognitionResult speechRecognitionResult = task.get();
            switch (speechRecognitionResult.getReason()) {
                case RecognizedSpeech: case NoMatch:
                    notifyInputReceived(speechRecognitionResult.getText());
                    break;
                case Canceled:
                    throw new SocketException();
                default:
                    throw new IOException("Unable to recognize speech.");
            }

            return null;
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> {
                    voiceInputItem.setIcon(microphoneOff);
                }, throwable -> {
                    voiceInputItem.setIcon(microphoneOff);
                    notifyError(throwable);
                });
    }
}