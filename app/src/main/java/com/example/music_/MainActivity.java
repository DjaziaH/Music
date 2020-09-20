package com.example.music_;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    /* ***************************************************************************************************** */
    /* ******************************     Declaring variables :   ****************************************** */
    /* ***************************************************************************************************** */

    ImageView play,stop,pause ;

    EditText link;

    Button download ;

    // Cpt to count the number of files downloded
    int cpt = 0;

    // The media player
    private MediaPlayer mPlayer;

    //Integer for resuming music
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing variables :

        link = findViewById(R.id.link);

        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.stop);

        play.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);

        download = findViewById(R.id.download);


        //Adding listeners to buttons :

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseMusic();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMusic();
            }
        });


        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!link.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,link.getText().toString(),Toast.LENGTH_SHORT).show();
                    Uri uri = Uri.parse(link.getText().toString());
                    new DownloadFile().execute(uri);
                }else{
                    Toast.makeText(MainActivity.this,"Empty Link ",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    /* ***************************************************************************************************** */
    /* ********************************       Play Pause Stop     ****************************************** */
    /* ***************************************************************************************************** */
    protected void playMusic(){
        //TODO hardcoded uri

        // music + cpt is the name of the file

        Uri myUri1 = Uri.parse("file:///sdcard/"+"music"+cpt);
        mPlayer  = new MediaPlayer();

        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(getApplicationContext(), myUri1);
            mPlayer.prepare();
            mPlayer.seekTo(position);
            mPlayer.start();

            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);

        } catch (IOException e) {
         }
    }

    protected void stopMusic(){

        mPlayer.stop();
        mPlayer.release();
        position = 0;

        play.setVisibility(View.VISIBLE);
        pause.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);

    }

    protected void pauseMusic(){

        mPlayer.pause();
        position= mPlayer.getCurrentPosition();

        play.setVisibility(View.VISIBLE);
        pause.setVisibility(View.GONE);
        stop.setVisibility(View.VISIBLE);
    }


    /* ***************************************************************************************************** */
    /* ********************************           AsyncTask       ****************************************** */
    /* ***************************************************************************************************** */

    class DownloadFile extends AsyncTask<Uri, Integer, Integer> {

        @Override
        protected Integer doInBackground(Uri... uris) {
                DownloadData(uris[0]);
            return 0;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {

        }
        @Override
        protected void onPostExecute(Integer s) {
            play.setVisibility(View.VISIBLE);
            stop.setVisibility(View.GONE);
            pause.setVisibility(View.GONE);
            link.setText("");
        }
        private void DownloadData (Uri uri) {

            DownloadManager downloadmanager = (DownloadManager)
                    getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("My music");
            request.setDescription("Downloading");

            //Cpt is used to count the nulber if songs downloaded ;
            cpt++;
            request.setDestinationInExternalPublicDir("",
                    "music"+cpt);

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            final long downloadId=downloadmanager.enqueue(request);

            final ConditionVariable mCondition = new ConditionVariable(false);
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (downloadId == reference) {
                        mCondition.open(); }
                }
            };
            getApplicationContext().registerReceiver(receiver, filter);
            mCondition.block();

        }
            }
}
