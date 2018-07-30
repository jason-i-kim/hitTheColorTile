package com.livelife.playwarecourse;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnAntEventListener {

    private static String tag = MainActivity.class.getSimpleName();
    MotoConnection connection;

    Spinner spinner;
    Button connectButton, pairingButton, updFirmwareButton, setColorButton;
    LinearLayout actionsLayout;
    TextView tilesConnectedLabel;
    Button sendCommandButton;
    Spinner colorSpinner, tileSpinner;

    Boolean isConnected = false;
    Boolean pairing = false;
    Boolean updating = false;

    HitTheColor game = new HitTheColor();
    Button gameButton;
    TextView scoreLabel, timeLabel, gameNameLabel, gameDescriptionLabel;
    boolean playing;

    public static final int LED_COLOR_OFF = 0;
    public static final int LED_COLOR_RED = 1;
    public static final int LED_COLOR_BLUE = 2;
    public static final int LED_COLOR_GREEN = 3;
    public static final int LED_COLOR_INDIGO = 4;
    public static final int LED_COLOR_ORANGE = 5;
    public static final int LED_COLOR_WHITE = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        connection = MotoConnection.getInstance();

        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (spinner.getSelectedItemPosition() == 0) return;

                if (!isConnected) {
                    connection.registerListener(MainActivity.this);
                    connection.startMotoConnection(MainActivity.this, spinner.getSelectedItemPosition());
                    connectButton.setEnabled(false);
                    spinner.setEnabled(false);
                } else {
                    connection.unregisterListener(MainActivity.this);
                    connection.stopMotoConnection();
                    connectButton.setText("CONNECT");
                    enableActions(false);
                    connectButton.setEnabled(true);
                    spinner.setEnabled(true);
                    tilesConnectedLabel.setText("Waiting for ANT...");
                }

                isConnected = !isConnected;
            }
        });

        spinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.channels));
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    connectButton.setEnabled(true);
                } else {
                    connectButton.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        actionsLayout = (LinearLayout) findViewById(R.id.actionsLayout);
        enableActions(false);


        pairingButton = (Button) findViewById(R.id.pairingButton);
        pairingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pairing) {
                    connection.pairTilesStart();
                    pairingButton.setText("STOP PAIRING TILES");
                    updateActions(pairingButton.getId());
                } else {
                    connection.pairTilesStop();
                    pairingButton.setText("START PAIRING TILES");
                    enableActions(true);
                }

                pairing = !pairing;
            }
        });

        updFirmwareButton = (Button) findViewById(R.id.updFirmwareButton);
        updFirmwareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!updating) {
                    connection.updateFirmwareStart();
                    updFirmwareButton.setText("STOP UPDATE FIRMWARE");
                    updateActions(updFirmwareButton.getId());
                } else {
                    connection.updateFirmwareStop();
                    updFirmwareButton.setText("START UPDATE FIRMWARE");
                    enableActions(true);
                }

                updating = !updating;
            }
        });

        tilesConnectedLabel = (TextView) findViewById(R.id.tilesConnectedLabel);

        setColorButton = (Button) findViewById(R.id.setColorButton);
        setColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random r = new Random();
                connection.setAllTilesColor(r.nextInt(6) + 1);
            }
        });

        colorSpinner = (Spinner) findViewById(R.id.colorSpinner);
        tileSpinner = (Spinner) findViewById(R.id.tileSpinner);
        sendCommandButton = (Button) findViewById(R.id.sendCommandButton);
        sendCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected) {
                    int color = colorSpinner.getSelectedItemPosition();
                    // spinner items are 0 based so add +1 at the end
                    int tile = tileSpinner.getSelectedItemPosition() + 1;
                    connection.setTileColor(color, tile);
                } else {
                    Toast.makeText(MainActivity.this, "You're not connected to any channel", Toast.LENGTH_SHORT).show();
                }
            }
        });


        game.setName("Step on the correct color tile!");
        game.setDescription("Press the Colors in order in a given amount of time.");
        scoreLabel = (TextView) findViewById(R.id.scoreLabel);
        timeLabel = (TextView) findViewById(R.id.timeLabel);
        gameNameLabel = (TextView) findViewById(R.id.gameNameLabel);
        gameDescriptionLabel = (TextView) findViewById(R.id.gameDescriptionLabel);
        gameNameLabel.setText(game.getName());
        gameDescriptionLabel.setText(game.getDescription());
        final int gameDuration = 30 * 1000; // expressed in milliseconds
        game.setDuration(gameDuration);
        timeLabel.setText("" + gameDuration / 1000 + ""); // converted in seconds

        game.setOnGameEventListener(new Game.OnGameEventListener() {
            @Override
            public void onGameTimerEvent(int i) {
                timeLabel.setText("" + i + "");
                if (i == 0) {
                    playing = false;
                    gameButton.setText("START GAME");
                    timeLabel.setText("" + gameDuration / 1000 + ""); // converted in seconds
                }
            }

            @Override
            public void onGameScoreEvent(int i) {
 /*
 This is called in the background so
 updating the user interface requires
 your code to run on the UI thread
 */
                final int score = i;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scoreLabel.setText(" " + score + " ");
                        gameDescriptionLabel.setText(game.getCommand());
                    }
                });
            }
        });

        gameButton = (Button) findViewById(R.id.gameButton);
        gameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playing) {
                    game.startGame();
                    gameButton.setText("STOP GAME");
                    gameDescriptionLabel.setText(game.getCommand());
                    scoreLabel.setText("0");
                } else {
                    game.stopGame();
                    gameDescriptionLabel.setText(game.getDescription());
                    gameButton.setText("START GAME");
                }
                playing = !playing;
            }
        });

    }


    public void enableActions(boolean enabled) {
        for (int i = 0; i < actionsLayout.getChildCount(); i++) {
            View child = actionsLayout.getChildAt(i);
            child.setEnabled(enabled);
        }
        connectButton.setEnabled(enabled);
    }

    public void updateActions(int buttonId) {
        for (int i = 0; i < actionsLayout.getChildCount(); i++) {
            View child = actionsLayout.getChildAt(i);
            if (child.getId() != buttonId) {
                child.setEnabled(false);
            }
        }
        connectButton.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        connection.unregisterListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        connection.registerListener(this);
        if (spinner.getSelectedItemPosition() != 0 && !isConnected) {
            connection.startMotoConnection(this, spinner.getSelectedItemPosition());
        }

    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {

        int tileId = AntData.getId(bytes);
        int command = AntData.getCommand(bytes);

        switch (command) {
            case AntData.EVENT_PRESS:
                Log.v(tag, "Press on tile: " + tileId);
            default:
                Log.v(tag, "cmd: " + command + " tile: " + tileId);
                break;
        }
        // Add the message event to the game
        game.addEvent(bytes);
    }

    @Override
    public void onAntServiceConnected() {

        Log.v(tag, "Ant Service connected...");
        enableActions(true);
        connectButton.setText("DISCONNECT");
        connection.setAllTilesToInit();

    }

    @Override
    public void onNumbersOfTilesConnected(int i) {

        tilesConnectedLabel.setText(i + " MOTO found");

    }

}
