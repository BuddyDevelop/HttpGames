package com.example.grzeiek.komunikacjahttp;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Grześiek on 28.03.2019.
 */

public class ticTac extends AppCompatActivity {
    public static final String STATUS = "Status";
    public static final String MOVES = "Moves";
    public static final String GAME_ID = "Game_id";
    public static final String PLAYER = "Player";
    public static final int NEW_GAME = 0;
    public static final int YOUR_TURN = 1;
    public static final int WAIT = 2;
    public static final int ERROR = 3;
    public static final int CONNECTION = 4;
    public static final int NETWORK_ERROR = 5;
    public static final int WIN = 6;
    public static final int LOSE = 7;

    private int status;
    private int game_id;
    private String moves;
    private int player;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_tic_tac );

//        zainicjalizuj zmienne prywatne i uruchom komunikat początkowy
        status = getIntent().getIntExtra(ticTac.STATUS, ticTac.NEW_GAME);
        game_id=getIntent().getIntExtra(ticTac.GAME_ID, ticTac.NEW_GAME);
        player=getIntent().getIntExtra(ticTac.PLAYER, 1);
        hints(status);



//        Utwórz i przypisz adapter inRowBoard do gridView
        GridView gv=( GridView )findViewById(R.id.gridView2);

        moves = getIntent().getStringExtra(ticTac.MOVES);

        gv.setAdapter(new ticTacBoard(this,moves));


//        listner do obsługi przyciśnięcia elementu z gridView
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 ) {
                if ( status != ticTac.WAIT ) { //Sprawdź, czy użytkownik może wykonać ruch
                    status = ticTac.WAIT;

                    hints( ticTac.CONNECTION );
                    GridView gv = ( GridView ) findViewById( R.id.gridView2 );
                    ticTacBoard game = ( ticTacBoard ) gv.getAdapter(); //Pobierz adapter inRowBoard z gridView (czyli aktywną grę)

//                    Toast.makeText(getApplicationContext(), "wcisnieto " + arg2 + " i " + arg3, Toast.LENGTH_LONG).show();
                    if ( game.add( arg2 ) != null )  //Spróbuj wykonać ruch
                        gv.setAdapter( game );  //Jeżeli ruch się wykona ponownie ustaw adapter do gridView
                    else
                        hints( ticTac.ERROR );

                    Intent intencja = new Intent( getApplicationContext(), HttpService.class ); //Stwórz intencję dla usługi HTTPService
                    PendingIntent pendingResult = createPendingResult( HttpService.IN_ROW, new Intent(), 0 ); //Utwórz PendingIntent jako intencję do odbioru wyniku działania usługi

                    if ( game_id == ticTac.NEW_GAME ) {
                        intencja.putExtra( HttpService.URL, HttpService.XO );  //Jeżeli użytkownik tworzy nową grę, ustaw w intencji Extras „URL” jako „LINES”
                        intencja.putExtra( HttpService.METHOD, HttpService.POST ); //„METHOD” jako „POST”
                    } else {
                        intencja.putExtra( HttpService.URL, HttpService.XO + game_id );  //Jeżeli gracz wykonuje ruch w istniejącej grze ustaw w intencji Extras „URL” jako „LINES”+game_id
                        intencja.putExtra( HttpService.METHOD, HttpService.PUT );                //„METHOD” jako „PUT”
                    }

                    intencja.putExtra( HttpService.PARAMS, "moves=" + moves + arg2 );  //Dodaj do intencji Extras „PARAMS” nową historię ruchów
                    intencja.putExtra(HttpService.RETURN, pendingResult);
                    startService( intencja );   //Uruchom usługę
                }
            }
        } );
    }


    //    do obsługi danych powracających z usługi) utwórz dwa stany –jeżeli requestCode==HTTPService.IN_ROW
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == HttpService.IN_ROW ) {
            try {
                JSONObject response = new JSONObject( data.getStringExtra( HttpService.RESPONSE ) );
                if (resultCode == 200) { //Sprawdź, czy nie wystąpił błąd
                    if(game_id==0)
                        game_id = response.getInt("game_id");  //Jeżeli wszystko OK pobierz game_id z obiektu JSON


                    GridView gv = (GridView) findViewById(R.id.gridView2);
                    ticTacBoard game = (ticTacBoard ) gv.getAdapter();

                    int game_status = game.checkWin();  //Sprawdź, status gry

                    if (game_status==0)
                        hints(ticTac.WAIT);
                    else{
                        if(game_status==player)
                            hints(ticTac.WIN);
                        else
                            hints(ticTac.LOSE);
                    }
                }
                else {
                    if ( resultCode == 500 )
                        hints( ticTac.NETWORK_ERROR );
                    else
                        hints( ticTac.ERROR );

                    Log.d("DEBUG", response.getString("http_status"));
                }

                //set refresh after 5 sec
                try {
                    Thread.sleep( 3000 );
                }catch ( Exception ex ){
                    Log.d("ticTac sleep err", ex.toString());
                }
                refresh(null);
            } catch ( JSONException e ) {
                hints(inRow.ERROR);
                e.printStackTrace();
            }
        }
        else if ( requestCode == HttpService.REFRESH ) {
            try {
                //utwórz obiect JSONObject którzy sparsuje dane z intencji
                Toast.makeText( getApplicationContext(), "Odswiezanie", Toast.LENGTH_SHORT ).show();
                JSONObject response = new JSONObject( data.getStringExtra( HttpService.RESPONSE ) );

                GridView gv = (GridView) findViewById(R.id.gridView2);
//          Pobierz z obiektu JSON pole „moves” (ruchy) i przypisz do zmiennej prywatnej
                moves = response.getString("moves");

                ticTacBoard game = new ticTacBoard(this,moves);

                gv.setAdapter(game);

//       Sprawdź, czy jest ruch gracza („status” odpowiedzi jest taki sam jak nr gracza).Jeżeli tak, sprawdź czy gracz wygrał, czy przegrał, jeżeli gra ciągle się toczy ustawstatus gry na inRow.YOUR_TURN i wyświetl odpowiedni kounikat
                if(response.getInt("status")==player){
                    if(game.checkWin()==player) {
                        hints(ticTac.WIN);
                    }
                    else if(game.checkWin()!=0){
                        hints(ticTac.LOSE);
                    }
                    else {
                        status = ticTac.YOUR_TURN;
                        hints(status);
                    }
                }
                else {
                    try {
                        Thread.sleep( 3000 ); //Jeżeli nie jest ruch gracza odczekaj 5s i odśwież grę
                    }catch ( Exception ex ){
                        Log.d("inRow sleep err", ex.toString());
                    }
                    refresh( null );
                }


            } catch ( JSONException e ) {
                e.printStackTrace();
            }
        }
    }


    //    Utwórz metodę tworzenia menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.game_menu, menu );
        return true;
    }



    //    funkcja wywoływaną przyciskiem z menu „refresh”, która będzie odświeżać grę
    public void refresh(MenuItem item) {
//       Utwórz intencję do uruchomienia usługi HTTPService
        Intent intencja = new Intent( getApplicationContext(), HttpService.class );

//      Utwórz obiekt PendingIntent jako intencję do odbioru komunikatów z serwisu
        PendingIntent pendingResult = createPendingResult( HttpService.REFRESH, new Intent(), 0 );

//        Dodaj parametry jako Extras
        intencja.putExtra( HttpService.URL, HttpService.XO + game_id );
        //Set data -method of request
        intencja.putExtra(HttpService.METHOD, HttpService.GET);
        //Set data -intent for result
        intencja.putExtra(HttpService.RETURN, pendingResult);
        //Start unBound Service in another Thread
        startService(intencja);
    }


    //  Utwórz pomocniczą metodę do wyświetlania komunikatów „hints”
    private void hints(int status) {
        TextView hint = ( TextView ) findViewById( R.id.ticTac );

        switch ( status ) {
            case inRow.YOUR_TURN:
                hint.setText( getString( R.string.your_turn ) );
                break;
            case inRow.WAIT:
                hint.setText( getString( R.string.wait ) );
                break;
            case inRow.ERROR:
                hint.setText( getString( R.string.error ) );
                break;
            case inRow.CONNECTION:
                hint.setText( getString( R.string.connection ) );
                break;
            case inRow.NETWORK_ERROR:
                hint.setText( getString( R.string.network_error ) );
                break;
            case inRow.WIN:
                hint.setText( getString( R.string.win ) );
                break;
            case inRow.LOSE:
                hint.setText( getString( R.string.lose ) );
                break;
            default:
                hint.setText( getString( R.string.new_game ) );
                break;
        }
    }
}
