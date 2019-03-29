package com.example.grzeiek.komunikacjahttp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Grześiek on 28.03.2019.
 */

public class ticTacBoard extends BaseAdapter {
    private Context context;
    private int player;
    private int[][] board = new int[3][3];


    public ticTacBoard(Context cont, String moves) {
        context = cont;

        int mvs = 0;
//        Toast.makeText( context, "move: " + moves, Toast.LENGTH_LONG ).show();
        for ( String move : moves.split( "(?!^)" ) ) {
            if ( move != "" )
                this.move( Integer.parseInt( move ), mvs++ % 2 );
        }
//      Ustal którym graczem jest aktywny użytkownik
        player = mvs % 2;
    }


//    Utwórz prywatną metodę symulującą ruch w grze move(int, int)a.
// Dla wybranej kolumny sprawdź, który wiersz jest wolny (wartość 0)  b.Na wolnym elemencie wstaw wartość gracza (1 lub 2)
    private boolean move( int pos, int player) {
        int row = getRowByPosition( pos ),
            col = getColByPosition( pos );

        try {
            board[ row ][ col ] = player + 1;
//            Toast.makeText( context, "wcisnales guzik na pozycji: " + row + " i w kolumnie nr: " + col, Toast.LENGTH_SHORT ).show();
        } catch ( Exception ex ) {
            return false;
        }
        return true;

    }


//    Utwórz publiczną metodę wykonania ruchu przez aktualnego gracza o nazwie add
    public ticTacBoard add(int pos) {
        //If change `player++%2` to `player` there is no switching between players
        if ( this.move(  pos, player++%2 ) )
            return this;
        return null;
    }



    //    Przeciąż wymagane metody w adapterze:
    @Override
    public int getCount() {
        return 3*3;
    }

    @Override
    public Object getItem(int position){
        return position%3;
    }

    @Override
    public long getItemId(int position){
        return position%3;
    }


    //    Przeciąż wymaganą metodę public View getView odpowiadającą za rysowanie pojedynczej komórki
    @Override
    public View getView( int position, View convertView, ViewGroup parent) {
        ImageView iv = new ImageView( context );

//        int col = position % 3;
//        int row = position / 3;

        int row = getRowByPosition( position );
        int col = getColByPosition( position );

        switch ( board[ row ][ col ] ) {
            case 0:
                iv.setImageResource( R.drawable.square );
                break;
            case 1:
                iv.setImageResource( R.drawable.cross );
                break;
            case 2:
                iv.setImageResource( R.drawable.player2 );
                break;
        }
//        iv.setLayoutParams( new Constraints.LayoutParams(  ) );
        iv.setLayoutParams( new LinearLayout.LayoutParams( 120, 120 ) );
        return iv;
    }


    public int checkWin() {
        int inRow = 0;
        for ( int row = 0; row < 2; row++, inRow = 0 )
            for ( int col = 0; col < 2; col++ )
                if ( board[ row ][ col ] == board[ row ][ col + 1 ] ) {
                    inRow++;
                    if ( inRow == 2 && board[ row ][ col ] != 0 )
                        return board[ row ][ col ];
                }
                else
                    inRow = 0;

        //check cols

        for ( int col = 0; col < 2; col++, inRow = 0 )
            for ( int row = 0; row < 2; row++ )
                if ( board[ row ][ col ] == board[ row + 1 ][ col ] ) {
                    inRow++;
                    if ( inRow == 2 && board[ row ][ col ] != 0 )
                        return board[ row ][ col ];
                }
                else
                    inRow = 0;

        //Chceck rising horizontal
        for ( int posx = 0; posx < 2; posx++ )
            for ( int posy = 0; posy < 2; posy++ ) {
                inRow = 0;
                for ( int x = posx, y = posy; x > 0 && y < 2; x--, y++ )
                    if ( board[ x ][ y ] == board[ x - 1 ][ y + 1 ] ) {
                        inRow++;
                        if ( inRow == 2 && board[ x ][ y ] != 0 )
                            return board[ x ][ y ];
                    }
                    else
                        inRow = 0;
            }

        //Chceck falling horizontal
        for ( int posx = 0; posx < 2; posx++ )
            for ( int posy = 0; posy < 2; posy++ ) {
                inRow = 0;
                for ( int x = posx, y = posy; x < 2 && y < 2; x++, y++ )
                    if ( board[ x ][ y ] == board[ x + 1 ][ y + 1 ] ) {
                        inRow++;
                        if ( inRow == 2 && board[ x ][ y ] != 0 )
                            return board[ x ][ y ];
                    }
                    else
                        inRow = 0;
            }
        return 0;
    }

    private int getRowByPosition( int position ){
        int row = 0;
        switch ( position ){
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                row = 1;
                break;
            case 4:
                row = 1;
                break;
            case 5:
                row = 1;
                break;
            case 6:
                row = 2;
                break;
            case 7:
                row = 2;
                break;
            case 8:
                row = 2;
                break;
            default:
                Toast.makeText( context, "Don't know where to put element",Toast.LENGTH_SHORT ).show();
                break;
        }

        return row;
    }

    private int getColByPosition( int position ){
        int col = 0;
        switch ( position ){
            case 0:
                break;
            case 1:
                col = 1;
                break;
            case 2:
                col = 2;
                break;
            case 3:
                break;
            case 4:
                col = 1;
                break;
            case 5:
                col = 2;
                break;
            case 6:
                break;
            case 7:
                col = 1;
                break;
            case 8:
                col = 2;
                break;
            default:
                Toast.makeText( context, "Don't know where to put element",Toast.LENGTH_SHORT ).show();
                break;
        }
        return col;
    }
}
