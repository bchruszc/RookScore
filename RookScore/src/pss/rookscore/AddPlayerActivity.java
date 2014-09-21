
package pss.rookscore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pss.rookscore.fragments.PlayerListFragment;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

public class AddPlayerActivity extends Activity implements PlayerSelectionListener {

    /* package */static final String ADD_PLAYER_RESULT_KEY =  AddPlayerActivity.class.getName() + ".AddPlayerResult";
    public static final String PLAYER_NAMES_KEY = AddPlayerActivity.class.getName() + ".PlayerNames";
    private static final String STORED_PLAYER_NAMES_PREFS_KEY = AddPlayerActivity.class.getName()+ ".StoredPlayerNames";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_player_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_player, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addNewPlayer) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(R.string.add_player_new_player_dialog_title);
            
            alert.setMessage(R.string.add_player_new_player_dialog_message);

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            alert.setView(input);

            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String name = input.getText().toString();
                    addPlayer(name);
                    playerSelected(Collections.singletonList(name));
                }
            });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            
            
            AlertDialog alertDialog = alert.show();
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    protected void addPlayer(String name) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Set<String> stringSet = prefs.getStringSet(STORED_PLAYER_NAMES_PREFS_KEY, new HashSet<String>());
        stringSet.add(name);
        Editor prefEditor = prefs.edit();
        prefEditor.clear();
        prefEditor.putStringSet(STORED_PLAYER_NAMES_PREFS_KEY, stringSet);
        prefEditor.commit();
        loadPlayerList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlayerList();
    }

    private void loadPlayerList() {
        PlayerListFragment plf = (PlayerListFragment) getFragmentManager().findFragmentById(
                R.id.playerListFragment);

        // load playerlist from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> stringSet = prefs.getStringSet(STORED_PLAYER_NAMES_PREFS_KEY, new HashSet<String>());
        List<String> orderedPlayerList = new ArrayList<String>(stringSet);
        Collections.sort(orderedPlayerList);

        plf.setPlayerList(orderedPlayerList);
    }

    @Override
    public void playerSelected(List<String> playerNames) {
        Intent i = new Intent();
        i.putStringArrayListExtra(ADD_PLAYER_RESULT_KEY, new ArrayList<String>(playerNames));
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void playerRemoved(final List<String> playerNames) {
        
        
        String[] playerNamesArray = (String[]) playerNames.toArray(new String[playerNames.size()]);
        
        // prompt to delete
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getResources().getQuantityString(R.plurals.add_player_remove_multi_player_dialog_title, playerNames.size(), playerNames.size(), playerNamesArray))
                .setMessage(getResources().getQuantityString(R.plurals.add_player_remove_multi_player_dialog_message, playerNames.size(), playerNames.size(), playerNamesArray))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        Set<String> stringSet = prefs.getStringSet(STORED_PLAYER_NAMES_PREFS_KEY, new HashSet<String>());
                        
                        for (String playerName : playerNames) {
                            stringSet.remove(playerName);    
                        }
                        
                        Editor prefEditor = prefs.edit();
                        prefEditor.clear();
                        prefEditor.putStringSet(STORED_PLAYER_NAMES_PREFS_KEY, stringSet);
                        prefEditor.commit();
                        loadPlayerList();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
        

        
    }
}
