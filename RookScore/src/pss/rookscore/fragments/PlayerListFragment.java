
package pss.rookscore.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pss.rookscore.R;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PlayerListFragment extends Fragment {

    /**
     * PlayerListFragment will display a list of players and allow selection of
     * one - Parent activity must implement PlayerSelectionListener - Array of
     * names for select expected to be provided
     */

    public static interface PlayerSelectionListener {
        public void playerSelected(List<String> playerNames);

        public void playerRemoved(List<String> playerNames);
    }

    private static final String PLAYER_LIST = PlayerListFragment.class.getName() + ".PlayerList";

    protected static final float MINIMUM_FLING_FACTOR = 200;

    private ArrayAdapter<String> mListAdapter;
    private List<String> mPlayerList = new ArrayList<String>();

    private GestureDetector mGestureDetector;

    private ListView mPlayerListView;

    private boolean mUseMultiSelect = true;

    private PlayerListMultiChoiceModeListener mMultiselectlistener;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.player_list_fragment, container, false);

        mListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if(mPlayerListView.isItemChecked(position)){
                    v.setBackgroundColor(getActivity().getResources().getColor(R.color.rook_cyan));
                } else {
                   v.setBackgroundColor(Color.TRANSPARENT);
                }
                return v;
            };
        };

        mPlayerListView = (ListView) v.findViewById(R.id.playerList);
        mPlayerListView.setAdapter(mListAdapter);
        

        mPlayerListView
                .setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        ((PlayerSelectionListener) getActivity()).playerSelected(Collections.singletonList(mListAdapter.getItem(arg2)));
                    }
                });

        
        if(mUseMultiSelect){
            mPlayerListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mMultiselectlistener = new PlayerListMultiChoiceModeListener(getActivity(), mPlayerListView);
            mPlayerListView.setMultiChoiceModeListener(mMultiselectlistener);
        }
        
        

        
        mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                
                //A few conditions to check
                // - Was fling restricted to a single list item's boundaries?
                //- Was flight from left to righy?
               
                if(mMultiselectlistener != null && mMultiselectlistener.isIgnoreFling()){
                        return true;
                }
                
                
                
                if(e1.getX() < e2.getX() && e2.getX() - e1.getX() > MINIMUM_FLING_FACTOR){
                    
                    System.out.println(e2.getX() - e1.getX());
                    
                    for(int i = mPlayerListView.getFirstVisiblePosition(); i <= mPlayerListView.getLastVisiblePosition(); i++){
                        View v = mPlayerListView.getChildAt(i);
                        
                        
                        float topLeftY = v.getY();
                        float bottomRightY = v.getY() + v.getHeight();
                        
                        if(e1.getY() >= topLeftY){
                            
                            if(e2.getY() <= bottomRightY){
                                //ok, we've found the list item that the fling was on. Translate to a model item and remove it
                                String itemAtPosition = (String)mPlayerListView.getItemAtPosition(i);
                                ((PlayerSelectionListener) getActivity()).playerRemoved(Collections.singletonList(itemAtPosition));
                                
               
                                
                                return true;
                            } 
                        } else {
                            //the fling crossed across two list items. Ignore. We don't need to look for any more list items
                            break;

                        }
                    }
                }
                
                
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        mPlayerListView.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
                
            }
        });
        
        
        return v;
    }
    
    

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(PLAYER_LIST, new ArrayList<String>(mPlayerList));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPlayerList = savedInstanceState.getStringArrayList(PLAYER_LIST);
        }
    }

    public void setPlayerList(List<String> playerList) {
        mPlayerList = playerList;
        if (mListAdapter != null) {
            populateList();
        }
    }

    private void populateList() {
        mListAdapter.clear();
        mListAdapter.addAll(mPlayerList);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if(!(getActivity() instanceof PlayerSelectionListener)){
            throw new IllegalArgumentException("Parent activity must implement " + PlayerSelectionListener.class.getName());
        }

        
        populateList();
    }

    public void removePlayer(final String playerName) {
        int position = mListAdapter.getPosition(playerName);
        View v = mPlayerListView.getChildAt(position);

        Animation listItemSlideAnimation = AnimationUtils.loadAnimation(getActivity(), R.animator.list_item_slide_anim);
        v.startAnimation(listItemSlideAnimation);
        listItemSlideAnimation.setDuration(500);
        listItemSlideAnimation.setInterpolator(new AccelerateInterpolator());
        listItemSlideAnimation.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            
            @Override
            public void onAnimationEnd(Animation arg0) {
                mListAdapter.remove(playerName);
                mPlayerListView.clearChoices();
            }
        });
        
    }



    public void addPlayer(String newPlayer) {
        mListAdapter.add(newPlayer);
    }
    
    protected void setUseMultiSelect(boolean b) {
        mUseMultiSelect = b;
    }





}
