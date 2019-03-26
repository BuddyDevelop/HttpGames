package com.example.grzeiek.komunikacjahttp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HttpService extends Service {
    public HttpService() {
    }

    @Override
    public IBinder onBind( Intent intent ) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException( "Not yet implemented" );
    }
}
