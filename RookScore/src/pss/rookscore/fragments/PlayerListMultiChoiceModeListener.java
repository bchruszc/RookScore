package pss.rookscore.fragments;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.R;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;
import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListAdapter;
import android.widget.ListView;

class PlayerListMultiChoiceModeListener implements MultiChoiceModeListener {

    private ListView mListView;
    private ListAdapter mAdapter;
    private Activity mActivity;

    public PlayerListMultiChoiceModeListener(Activity parentActivity, ListView lv) {
        mActivity = parentActivity;
        mListView = lv;
        mAdapter = mListView.getAdapter();
    }
    
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(pss.rookscore.R.menu.player_list_multi_select_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<String> selectedPlayers = new ArrayList<String>();
        for(int i = 0; i < mAdapter.getCount(); i++){
            if(mListView.isItemChecked(i)){
                selectedPlayers.add((String)mAdapter.getItem(i));
            }
        }
        
        if(item.getItemId() == R.id.addMultiplePlayers){
            ((PlayerSelectionListener) mActivity).playerSelected(selectedPlayers);
            mode.finish();
            return true;
        } else if(item.getItemId() == R.id.deleteMultiplePlayers){
            ((PlayerSelectionListener) mActivity).playerRemoved(selectedPlayers);
            mode.finish();
            return true;
        }
        
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        mListView.invalidateViews();
    }

}