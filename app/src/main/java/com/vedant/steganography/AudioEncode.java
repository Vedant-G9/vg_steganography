package com.vedant.steganography;

import static com.vedant.steganography.Wavelet.WaveletType.DaubechiesD4Wavelet;
import static com.vedant.steganography.Wavelet.WaveletType.HaarWavelet;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;


public class AudioEncode extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

//    private static final int SELECT_AUDIO = 100;
//    private ImageView audioView;
private int levelsOfDecomposition;
    private String message;
    private String filePath;
    private String newFilePath;
    private Wavelet wavelet;
    private EditText et;
    private static final String[] spinnerLevelValues = new String[]{"1", "2", "3", "4"};
    // "Daubechies D4" add this for use Daubechies transform.......v
    private static final String[] waveletTypes = new String[]{"Haar"};
    private AdapterView<?> adapterView;
    private View view;
    private long l;
    private static final int SELECT_AUDIO = 100;



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
        setContentView(R.layout.activity_audio_encode);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1001);
        }

        /// INPUT ///
        ArrayAdapter levelsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerLevelValues);
        Spinner decompLevel = (Spinner) findViewById(R.id.decompLevelSpinner);
        decompLevel.setAdapter(levelsAdapter);
        decompLevel.setOnItemSelectedListener(this);

        ArrayAdapter waveletsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, waveletTypes);
        Spinner waveletTypes = (Spinner) findViewById(R.id.waveletTypesSpinner);
        waveletTypes.setAdapter(waveletsAdapter);
        waveletTypes.setOnItemSelectedListener(this);

        et = (EditText) findViewById(R.id.textToHide);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                message = et.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Button buttonSearch = (Button) findViewById(R.id.buttonSearchFile);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(AudioEncode.this)
//                        .withFilter(Pattern.compile(".*\\.wav$"))
                        .withRequestCode(1000)
                        .withHiddenFiles(true)
                        .start();
//                AudioChooser();
            }
        });
    }

//    private void AudioChooser() {
//        Intent intent = new Intent();
//        intent.setType("audio/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Audio"), SELECT_AUDIO);
//    }




    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        this.adapterView = adapterView;
        this.view = view;
        this.l = l;

        switch (adapterView.getId()) {
            case R.id.waveletTypesSpinner:
                String stringWaveletType = String.valueOf(waveletTypes[i]);
                switch (stringWaveletType) {
                    case "Haar":
                        wavelet = createWaveletFromType(HaarWavelet);
                        break;
                    case "Daubechies D4":
                        wavelet = createWaveletFromType(DaubechiesD4Wavelet);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){

            case 1001:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Not Granted!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private boolean isMessageLongEnoughForFile() {

        boolean result = true;

        try {
            WavFile wavFile = WavFile.openWavFile(new File(filePath));
            double sampleCount = wavFile.getNumFrames();

            if (message.length() > sampleCount/Math.pow(2,levelsOfDecomposition) ) {
                result = false;
            }

        } catch (Exception e) {
            System.err.println(e);
        }

        return result;
    }

    public void embedMessage(View v) {

        if (filePath != null && message != null && isMessageLongEnoughForFile()) {

            final int index = filePath.lastIndexOf("/");
            final String absolutePath = filePath.substring(0, index + 1);

            AlertDialog.Builder fileNameAlert = new AlertDialog.Builder(this);

            fileNameAlert.setMessage("Name of the new .wav file:");

            final EditText inputFileName = new EditText(this);
            fileNameAlert.setView(inputFileName);

            fileNameAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    newFilePath = absolutePath + inputFileName.getText().toString() + ".wav";
                    new LongOperation().execute();
                    Toast.makeText(getApplicationContext(), "MESSAGE EMBEDDED SUCCESSFULLY", Toast.LENGTH_SHORT).show();
                }
            });

            fileNameAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            fileNameAlert.show();

        } else {
            if (filePath == null) {
                Toast.makeText(getApplicationContext(), "File not chosen! Please search for .wav file", Toast.LENGTH_SHORT).show();
            }else if (message == null) {
                Toast.makeText(getApplicationContext(), "No message was written to be embedded! Please write your message", Toast.LENGTH_SHORT).show();
            } else if (!isMessageLongEnoughForFile()){
                Toast.makeText(getApplicationContext(), "Message is too long to be embedded! Please shorten your message or provide longer .wav file", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class LongOperation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                ///OPEN FILE///
                WavFile wavFile = WavFile.openWavFile(new File(filePath));

                double[] completeArrayOfSamples = new double[(int) wavFile.getNumFrames()*wavFile.getNumChannels()];
                int framesRead;
                int lengthOfSignal = completeArrayOfSamples.length;

                ///READING SIGNAL///
                do {
                    framesRead = wavFile.readFrames(completeArrayOfSamples, lengthOfSignal);
                }
                while (framesRead != 0);

                StegoEngine stegoEngine = new StegoEngine();

                ///EMBEDDING///
                stegoEngine.embedStegoMessageInSignal(completeArrayOfSamples, levelsOfDecomposition, message, wavelet);

                ///SAVING FILE///
                WavFile newWavFile = WavFile.newWavFile(new File(newFilePath), wavFile.getNumChannels(), wavFile.getNumFrames(), wavFile.getValidBits(), wavFile.getSampleRate());
                newWavFile.writeFrames(completeArrayOfSamples, (int) wavFile.getNumFrames());

                wavFile.close();
                newWavFile.close();

            } catch (Exception e) {
                System.err.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }


    }
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_audio_encode);
////
////        Button choose_audio_button = findViewById(R.id.choose_audio_button);
////        audioView = findViewById(R.id.audio_view);
////
////
////        choose_audio_button.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                AudioChooser();
////            }
////        });
//
//
//
//    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

//    private void AudioChooser() {
//        Intent intent = new Intent();
//        intent.setType("audio/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent,"Select audio"), SELECT_AUDIO);
//    }
}