package com.livelife.playwarecourse;

import android.graphics.Color;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.MotoConnection;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by cameronlee on 19/03/2018.
 */

public class HitTheColor extends Game {
    ArrayList<Integer> colors = new ArrayList<>();
    int color = 1;
    MotoConnection connection = MotoConnection.getInstance();

    @Override
    public void onGameStart() {
        int amount = connection.connectedTiles.size();
        colors = new ArrayList<>();
        switch (amount){
            case 1:
                connection.setTileColor(AntData.LED_COLOR_RED, connection.connectedTiles.get(0));
                colors.add(AntData.LED_COLOR_RED);
                break;
            case 2:
                connection.setTileColor(AntData.LED_COLOR_RED, connection.connectedTiles.get(0));
                connection.setTileColor(AntData.LED_COLOR_BLUE, connection.connectedTiles.get(1));
                colors.add(AntData.LED_COLOR_RED);
                colors.add(AntData.LED_COLOR_BLUE);
                break;
            case 3:
                connection.setTileColor(AntData.LED_COLOR_RED, connection.connectedTiles.get(0));
                connection.setTileColor(AntData.LED_COLOR_BLUE, connection.connectedTiles.get(1));
                connection.setTileColor(AntData.LED_COLOR_GREEN, connection.connectedTiles.get(2));
                colors.add(AntData.LED_COLOR_RED);
                colors.add(AntData.LED_COLOR_BLUE);
                colors.add(AntData.LED_COLOR_GREEN);
                break;
            case 4:
                connection.setTileColor(AntData.LED_COLOR_RED, connection.connectedTiles.get(0));
                connection.setTileColor(AntData.LED_COLOR_BLUE, connection.connectedTiles.get(1));
                connection.setTileColor(AntData.LED_COLOR_GREEN, connection.connectedTiles.get(2));
                connection.setTileColor(AntData.LED_COLOR_INDIGO, connection.connectedTiles.get(3));
                colors.add(AntData.LED_COLOR_RED);
                colors.add(AntData.LED_COLOR_BLUE);
                colors.add(AntData.LED_COLOR_GREEN);
                colors.add(AntData.LED_COLOR_INDIGO);
                break;
            default:
                break;
        }
        super.onGameStart();
    }

    @Override
    public void onGameUpdate(byte[] message) {
        super.onGameUpdate(message);
        int command = AntData.getCommand(message);
        int received = AntData.getColorFromPress(message);
        if (received == color && command == AntData.EVENT_PRESS) {
            randomColor();
            incrementScore(1);
        }else if(received != color && command == AntData.EVENT_PRESS){
            randomColor();
            incrementScore((-1));
        }
    }

    @Override
    public void onGameEnd() {
        super.onGameEnd();
        connection.setAllTilesToInit();
    }

    public String getCommand (){
        switch (color) {
            case 1:
                return "Red";
            case 2:
                return "Blue";
            case 3:
                return "Green";
            case 4:
                return "Indigo";
            default:
                return "error";
        }
    }

    public void randomColor(){
        Collections.shuffle(colors);
        color = colors.get(0);
    }
}
