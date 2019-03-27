package com.example.grzeiek.komunikacjahttp;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpService extends IntentService  {
    public static final int GAMES_LIST = 1;
    public static final int IN_ROW = 2;
    public static final int REFRESH = 3;
    public static final int GAME_INFO = 4;
    public static final String URL = "URL";
    public static final String METHOD = "Method";
    public static final String PARAMS = "Params";
    public static final String RETURN = "Return";
    public static final String RESPONSE = "Response";
    public static final String LINES = "http://games.antons.pl/lines/";
    public static final String XO = "http://games.antons.pl/xo/";
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;


//    Utwórz konstruktor, który wywoła konstruktor rodzicielski (super) z jakąś wartością
    public HttpService() {
        super("HTTP calls handler");
    }

    @Override
    protected void onHandleIntent( @Nullable Intent intent ) {
        String urlstr = intent.getStringExtra(HttpService.URL);
        URL url = null;
        try {
            url = new URL(urlstr);
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection ) url.openConnection();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        switch (intent.getIntExtra(HttpService.METHOD,1)) {
            case HttpService.POST:
                try {
                    conn.setRequestMethod( "POST" );
                } catch ( ProtocolException e ) {
                    e.printStackTrace();
                }
                break;
            case HttpService.PUT:
                try {
                    conn.setRequestMethod( "PUT" );
                } catch ( ProtocolException e ) {
                    e.printStackTrace();
                }
                break;
            default:
                try {
                    conn.setRequestMethod( "GET" );
                } catch ( ProtocolException e ) {
                    e.printStackTrace();
                }
        }

        Config conf = new Config(getApplicationContext());
        conn.setRequestProperty("PKEY", conf.getPublic().replace("\n",""));
        conn.setRequestProperty("SIGN",conf.sign(urlstr).replace("\n",""));

        String params = intent.getStringExtra(HttpService.PARAMS);
        if(params!=null) {
            conn.setDoOutput( true );
            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter( conn.getOutputStream() );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
            try {
                writer.write( params );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
            try {
                writer.flush();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
            try {
                writer.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }

        try {
            conn.connect();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        int responseCode = 0;
        try {
            responseCode = conn.getResponseCode();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        String response = "";
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                response += line;
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        conn.disconnect();

        Intent returns = new Intent();
        returns.putExtra(HttpService.RESPONSE, response);
        PendingIntent reply = intent.getParcelableExtra(HttpService.RETURN);
        try {
            reply.send(this, responseCode, returns);
        } catch ( PendingIntent.CanceledException e ) {
            e.printStackTrace();
        }
    }
}
