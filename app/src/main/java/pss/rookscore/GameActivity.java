
package pss.rookscore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import pss.rookscore.NFCLifecycleCallbacks.RookScoreNFCBroadcaster;
import pss.rookscore.events.GameOverEvent;
import pss.rookscore.events.GameStateChangedEvent;
import pss.rookscore.events.SpectatorsChangedEvent;
import pss.rookscore.fragments.ScoresheetFragment;
import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.Player;
import pss.rookscore.ruleset.RoundStateModel;

public class GameActivity extends Activity implements RookScoreNFCBroadcaster {

    private static final String GAME_STATE_MODEL_KEY = GameActivity.class.getName() + ".GameStateModel";

    public static final String PLAYER_LIST_KEY = GameActivity.class.getName() + ".PlayerList";

    private static final int PLAY_ROUND_REQUEST = 1;

    private GameStateModel mGameModel = new GameStateModel();

    private EventBus mEventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);


        Player players[] = (Player[]) getIntent().getSerializableExtra(PLAYER_LIST_KEY);
        mGameModel.getPlayers().clear();
        Collections.addAll(mGameModel.getPlayers(), players);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    // events
    @Subscribe
    public void handleSpectatorsChanged(SpectatorsChangedEvent e) {
        // push out a game state change so that all spectators are guaranteed to
        // be up to date
        mEventBus.post(new GameStateChangedEvent(mGameModel));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start the listener service when starting a game. No effect if called
        // 2x
        Intent bluetoothServiceIntent = new Intent(this, BluetoothBroadcastService.class);
        startService(bluetoothServiceIntent);

        mEventBus = ((RookScoreApplication) getApplication()).getEventBus();
        mEventBus.register(this);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.game_activity_end_game_dialog_title)
                .setMessage(R.string.game_activity_end_game_dialog_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        endGame();
                        GameActivity.super.onBackPressed();
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    protected void endGame() {
        mEventBus.post(new GameOverEvent(mGameModel));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        scoresheetFragment.setGameStateModel(mGameModel);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionBarNewRound) {
            startNewRound();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void startNewRound() {
        Runnable newRoundRunnable = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(GameActivity.this, PlayRoundActivity.class);
                i.putExtra(PlayRoundActivity.GAME_STATE_MODEL, mGameModel);
                startActivityForResult(i, PLAY_ROUND_REQUEST);
                overridePendingTransition(0, 0);

            }
        };

        // but before all that... let's have some fun.
        ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        scoresheetFragment.runExitAnimation(newRoundRunnable);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLAY_ROUND_REQUEST) {
            if (resultCode == RESULT_OK) {
                // build up a new round from the intend
                RoundStateModel rsm = (RoundStateModel) data.getSerializableExtra(PlayRoundActivity.ROUND_STATE_MODEL);
                mGameModel.getRounds().add(rsm.getRoundResult());

                mEventBus.post(new GameStateChangedEvent(mGameModel));

                // onResume() will be called since we're just about to show view
                // -
                // that will cause the view to be updated with the latest model
            }
            
            
            final ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
            scoresheetFragment.getView().getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                
                @Override
                public boolean onPreDraw() {
                    scoresheetFragment.getView().getViewTreeObserver().removeOnPreDrawListener(this);        
                    scoresheetFragment.runEnterAnimation();
                    return true;
                }
            });
            


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(GAME_STATE_MODEL_KEY, mGameModel);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Serializable gameState = savedInstanceState.getSerializable(GAME_STATE_MODEL_KEY);
        if (gameState != null) {
            mGameModel = (GameStateModel) gameState;
        }
    }

    public void removeRound(int position) {
        mGameModel.getRounds().remove(position);

        // TODO: Should be handled by event push
        ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        scoresheetFragment.setGameStateModel(mGameModel);

        mEventBus.post(new GameStateChangedEvent(mGameModel));
    }

}