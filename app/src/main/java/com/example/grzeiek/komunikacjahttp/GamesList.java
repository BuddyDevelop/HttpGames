package com.example.grzeiek.komunikacjahttp;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GamesList extends AppCompatActivity {
    private int game;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_games_list );
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        Bundle extras = getIntent().getExtras();
        game = extras.getInt( "gra" );

//      Stwórz listner do odświeżania listy w metodzie onCreate:
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshGameList();
            }
        } );

        refreshGameList();

        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
//        uruchomienie nowej gry po wcisnieciu przycisku
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
//                Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG )
//                        .setAction( "Action", null ).show();

                Intent intencja=null;
                switch (game) {
                    case R.id.inRow:
                        intencja = new Intent( getApplicationContext(), inRow.class );
                        intencja.putExtra( inRow.STATUS, inRow.NEW_GAME );
                        intencja.putExtra( inRow.MOVES, "" );
                        break;
                    default:
                        //TODO -when gamer choose TicTacToe Game
                        intencja = new Intent( getApplicationContext(), ticTac.class );
                        intencja.putExtra( ticTac.STATUS, ticTac.NEW_GAME );
                        intencja.putExtra( ticTac.MOVES, "" );
                        break;
                }
                startActivity( intencja );
            }
        } );


//        dodaj jeszcze listner obsługi kliknięcia na listę gier
        ListView list = (ListView)findViewById(R.id.listView);

        list.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar1);
                spinner.setVisibility(View.VISIBLE);  //Pokaż progressBar

                String game_id = arg0.getItemAtPosition(arg2).toString().replace("ID: ","");

                Intent intencja = new Intent(getApplicationContext(),HttpService.class);  //Utwórz intencję do obsługi serwisu HTTPService
                PendingIntent pendingResult = createPendingResult(HttpService.GAME_INFO, new Intent(),0); //wórz obiekt PendingIntent by móc odebrać komunikat

                if(game == R.id.inRow) {
                    intencja.putExtra( HttpService.URL, HttpService.LINES + game_id );  //W zależności od wybranej przez gracza gry ustaw parametr
                }
                else{
                    intencja.putExtra( HttpService.URL, HttpService.XO + game_id );
                }
                intencja.putExtra(HttpService.METHOD, HttpService.GET);
                intencja.putExtra(HttpService.RETURN, pendingResult);
                startService(intencja);
            }
        } );
    }


//    Utwórz metodę do odświeżania listy
    public void refreshGameList(){
        ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        Snackbar.make(findViewById(R.id.main_list), getString(R.string.refresh), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        //Geting Layout elements for modyfication
        ListView list = (ListView)findViewById(R.id.listView);
        TextView emptyText = (TextView)findViewById(android.R.id.empty);

        Intent intencja = new Intent(getApplicationContext(),HttpService.class);
        PendingIntent pendingResult = createPendingResult(HttpService.GAMES_LIST, new Intent(),0);

        if(game == R.id.inRow){
            intencja.putExtra(HttpService.URL, HttpService.LINES);
        }
        else {
            intencja.putExtra(HttpService.URL, HttpService.XO);
            //TODO -geting ticTacToe games list
        }

        intencja.putExtra(HttpService.METHOD, HttpService.GET);
        intencja.putExtra(HttpService.RETURN, pendingResult);
        startService(intencja);
    }



//    by móc odebrać komunikat zwrócony przez HttpServicei wyświetlić listę gier
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ( requestCode == HttpService.GAMES_LIST ) {
            ProgressBar spinner = ( ProgressBar ) findViewById( R.id.progressBar1 );
            spinner.setVisibility( View.GONE );

            SwipeRefreshLayout swipeLayout = ( SwipeRefreshLayout ) findViewById( R.id.swipe_container );
            swipeLayout.setRefreshing( false );


            try {
                JSONObject response = new JSONObject( data.getStringExtra( HttpService.RESPONSE ) );

                if ( response.getInt( "games_count" ) > 0 ) {
                    TextView no_game = ( TextView ) findViewById( R.id.empty );
                    no_game.setVisibility( View.GONE );

                    JSONArray games = new JSONArray( response.getString( "games" ) );
                    ArrayList<String> items = new ArrayList<String>();

                    for ( int i = 0; i < response.getInt( "games_count" ); i++ ) {
                        JSONObject game = games.getJSONObject( i );
                        items.add( "ID: " + game.getString( "id" ) );
                    }

                    ArrayAdapter<String> gamesAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, items );
                    ListView list = ( ListView ) findViewById( R.id.listView );
                    list.setAdapter( gamesAdapter );
                }
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
        }
        else if( requestCode == HttpService.GAME_INFO){
            ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar1);
            spinner.setVisibility(View.GONE);

            if(game==R.id.inRow) {  //Sprawdź jaka gra została wybrana
                Intent intencja = new Intent(getApplicationContext(), inRow.class);

                try {
                    JSONObject response = new JSONObject(data.getStringExtra(HttpService.RESPONSE));

                    intencja.putExtra(inRow.GAME_ID, response.getInt("id"));

                    if (response.getInt("status") == 0 && response.getInt("player1") == 2) {
                        intencja.putExtra(inRow.STATUS, inRow.YOUR_TURN);
                    }
                    else if (response.getInt("status") == 1 && response.getInt("player1") == 1) {
                        intencja.putExtra(inRow.STATUS, inRow.YOUR_TURN);
                    }
                    else if (response.getInt("status") == 2 && response.getInt("player1") == 2) {
                        intencja.putExtra(inRow.STATUS, inRow.YOUR_TURN);
                    }
                    else
                        intencja.putExtra(inRow.STATUS, inRow.WAIT);

                    intencja.putExtra(inRow.PLAYER, response.getInt("player1"));
                    intencja.putExtra(inRow.MOVES, response.getString("moves"));
                    startActivity(intencja);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            else if(game==R.id.ticTac) {
                //TODO -start chosen game for TicTacToe
                Intent intencja = new Intent(getApplicationContext(), ticTac.class);

                try {
                    JSONObject response = new JSONObject(data.getStringExtra(HttpService.RESPONSE));

                    intencja.putExtra(inRow.GAME_ID, response.getInt("id"));

                    if (response.getInt("status") == 0 && response.getInt("player1") == 2) {
                        intencja.putExtra(ticTac.STATUS, ticTac.YOUR_TURN);
                    }
                    else if (response.getInt("status") == 1 && response.getInt("player1") == 1) {
                        intencja.putExtra(ticTac.STATUS, ticTac.YOUR_TURN);
                    }
                    else if (response.getInt("status") == 2 && response.getInt("player1") == 2) {
                        intencja.putExtra(ticTac.STATUS, ticTac.YOUR_TURN);
                    }
                    else
                        intencja.putExtra(ticTac.STATUS, ticTac.WAIT);

                    intencja.putExtra(ticTac.PLAYER, response.getInt("player1"));
                    intencja.putExtra(ticTac.MOVES, response.getString("moves"));
                    startActivity(intencja);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }
}
