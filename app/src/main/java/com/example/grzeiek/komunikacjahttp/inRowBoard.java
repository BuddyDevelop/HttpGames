package com.example.grzeiek.komunikacjahttp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;



public class inRowBoard extends BaseAdapter{
    private Context context;
    private int player;
    private int[][] board = new int[6][7];


//    Utwórz konstruktor, który na podstawie Stringu z historią ruchów wygeneruje planszę do gry
//    A. Rozbicie stringu z historią na poszczególne ruchy  B. Wykonanie kolejnych ruchów, kolejnych graczy
    public inRowBoard(Context cont, String moves) {
        context = cont;
        int mvs = 0;
        for ( String move : moves.split( "(?!^)" ) ) {
            if ( move != "" ) this.move( Integer.parseInt( move ), mvs++ % 2 );
        }
//        Ustal którym graczem jest aktywny użytkownik
        player = mvs % 2;
    }


//    Utwórz prywatną metodę symulującą ruch w grze move(int, int)a.
// Dla wybranej kolumny sprawdź, który wiersz jest wolny (wartość 0)  b.Na wolnym elemencie wstaw wartość gracza (1 lub 2)
    private boolean move(int col, int player) {
        int row = 0;
        try {
            while ( board[ row ][ col ] != 0 ) row++;
            board[ row ][ col ] = player + 1;
        } catch ( Exception ex ) {
            return false;
        }
        return true;
    }


//    Utwórz publiczną metodę wykonania ruchu przez aktualnego gracza o nazwie add
    public inRowBoard add(long col) {
        if ( this.move( ( int ) col, player ) )
            return this;
        return null;
    }

//    Przeciąż wymagane metody w adapterze:
    @Override
    public int getCount() {
        return 6*7;}

    @Override
    public Object getItem(int position){
        return position%7;}

    @Override
    public long getItemId(int position){
        return position%7;}


//    Przeciąż wymaganą metodę public View getView odpowiadającą za rysowanie pojedynczej komórki
    @Override
    public View getView( int position, View convertView, ViewGroup parent) {
        ImageView iv = new ImageView( context );
        int col = position % 7;
        int row = 5 - position / 7;
        switch ( board[ row ][ col ] ) {
            case 0:
                iv.setImageResource( R.drawable.circle );
                break;
            case 1:
                iv.setImageResource( R.drawable.player1 );
                break;
            case 2:
                iv.setImageResource( R.drawable.player2 );
                break;
        }
        iv.setLayoutParams( new LinearLayout.LayoutParams( 120, 120 ) );
        return iv;
    }


//    Stwórz metodę checkWin(), która sprawdzi, czy na planszy są obok siebie 4 takie same wartości
//W poszczególnych wierszach, kolumnach, Horyzontalnie wznosząco, Horyzontalnie opadajaco
//Interesują nas tylko pola użytkowników, Uważaj by nie wyjść poza zakres tablicy board,Metoda powinna zwracać który gracz wygrał
    public int checkWin() {
        int inRow = 0;
        for ( int row = 0; row < 6; row++, inRow = 0 )
            for ( int col = 0; col < 6; col++ )
                if ( board[ row ][ col ] == board[ row ][ col + 1 ] ) {
                    inRow++;
                    if ( inRow == 3 && board[ row ][ col ] != 0 )
                        return board[ row ][ col ];
                }
        else
            inRow = 0;
        for ( int col = 0; col < 7; col++, inRow = 0 )
            for ( int row = 0; row < 5; row++ )
                if ( board[ row ][ col ] == board[ row + 1 ][ col ] ) {
                    inRow++;
                    if ( inRow == 3 && board[ row ][ col ] != 0 )
                        return board[ row ][ col ];
                }
        else inRow = 0;
        for ( int posx = 3; posx < 6; posx++ )
            for ( int posy = 0; posy < 4; posy++ ) {
                inRow = 0;
                for ( int x = posx, y = posy; x > 0 && y < 6; x--, y++ )
                    if ( board[ x ][ y ] == board[ x - 1 ][ y + 1 ] ) {
                        inRow++;
                        if ( inRow == 3 && board[ x ][ y ] != 0 )
                            return board[ x ][ y ];
                    }
                else
                    inRow = 0;
            }
        for ( int posx = 0; posx < 3; posx++ )
            for ( int posy = 0; posy < 4; posy++ ) {
                inRow = 0;
                for ( int x = posx, y = posy; x < 5 && y < 6; x++, y++ )
                    if ( board[ x ][ y ] == board[ x + 1 ][ y + 1 ] ) {
                        inRow++;
                        if ( inRow == 3 && board[ x ][ y ] != 0 )
                            return board[ x ][ y ];
                    }
                else
                    inRow = 0;
            }
        return 0;
    }
}
