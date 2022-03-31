package com.vedant.steganography;

import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.util.regex.Pattern;

public class AudioDecode extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int SELECT_AUDIO = 100;
    private ImageView audioView;
    private int levelsOfDecomposition;
    private String extractedMessage;
    private String filePath;
    private Wavelet wavelet;
    private static final String[] spinnerLevelValues = new String[]{"1", "2", "3", "4"};
    // "Daubechies D4" add this for use Daubechies transform.......v
    private static final String[] waveletTypes = new String[]{"Haar"};

    public static Wavelet createWaveletFromType(Wavelet.WaveletType type) {
        Wavelet wavelet;

        switch (type) {
            case HaarWavelet:
                wavelet = new HaarWavelet();
                break;

//            case DaubechiesD4Wavelet:
//                wavelet = new DaubechiesD4Wavelet();
//                break;
            default:
                wavelet = new Wavelet();
                break;
        }

        return wavelet;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_decode);


        /// INPUT ///
        ArrayAdapter levelsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerLevelValues);
        Spinner decompLevel = (Spinner) findViewById(R.id.decompLevelSpinner);
        decompLevel.setAdapter(levelsAdapter);
        decompLevel.setOnItemSelectedListener(this);

        ArrayAdapter waveletsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, waveletTypes);
        Spinner waveletTypes = (Spinner) findViewById(R.id.waveletTypesSpinner);
        waveletTypes.setAdapter(waveletsAdapter);
        waveletTypes.setOnItemSelectedListener(this);

        Button buttonSearch = (Button) findViewById(R.id.buttonSearchFile);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(AudioDecode.this)
                        .withFilter(Pattern.compile(".*\\.wav$"))
                        .withRequestCode(1000)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });
    }

    public void extractMessage(View v) {

        if (filePath == null) {
            Toast.makeText(getApplicationContext(), "File not chosen! Please search for .wav file", Toast.LENGTH_SHORT).show();
        } else {
            new LongOperation().execute();
        }

    }

    private class LongOperation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                ///OPENING FILE WITH EMBEDDED MESSAGE///
                WavFile embeddedWavFile = WavFile.openWavFile(new File(filePath));

                double[] completeArrayOfSamples = new double[(int) embeddedWavFile.getNumFrames()*embeddedWavFile.getNumChannels()];
                int framesRead;
                int lengthOfSignal = completeArrayOfSamples.length;

                ///READING SIGNAL///
                do {
                    framesRead = embeddedWavFile.readFrames(completeArrayOfSamples, lengthOfSignal);
                }
                while (framesRead != 0);

                ///DECOMPOSING SIGNAL AND EXTRACTING THE MESSAGE///
                StegoEngine stegoEngine = new StegoEngine();
                extractedMessage = stegoEngine.extractStegoMessageFromSignal(completeArrayOfSamples, levelsOfDecomposition, wavelet);

                embeddedWavFile.close();

            } catch (Exception e) {
                System.err.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent i = new Intent(AudioDecode.this, ShowExtractedMessage.class);
            i.putExtra("msg",extractedMessage);
            startActivity(i);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        switch (adapterView.getId()) {
            case R.id.waveletTypesSpinner:
                String stringWaveletType = String.valueOf(waveletTypes[i]);
                switch (stringWaveletType) {
                    case "Haar":
                        wavelet = createWaveletFromType(Wavelet.WaveletType.HaarWavelet);
                        break;
                    case "Daubechies D4":
                        wavelet = createWaveletFromType(Wavelet.WaveletType.DaubechiesD4Wavelet);
                        break;
                    default:
                        wavelet = null;
                        break;
                }
                break;
            case R.id.decompLevelSpinner:
                levelsOfDecomposition = Integer.valueOf(spinnerLevelValues[i]);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
        }
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_audio_decode);

//        Button choose_audio_button = findViewById(R.id.choose_audio_button);
//        audioView = findViewById(R.id.audio_view);
//
//
//        choose_audio_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AudioChooser();
//            }
//        });
//    }
    private void AudioChooser() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select audio"), SELECT_AUDIO);
    }
}