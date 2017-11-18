package eguidebook.eguidebookapp;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FragmentSpotsByCategory _objFragmentSpotsByCategory = null;
    private FragmentRouteList _objFragmentRouteList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        enableExpandableList();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager objSearchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView objSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        objSearchView.setSearchableInfo(objSearchManager.getSearchableInfo(getComponentName()));
        objSearchView.setQueryHint("Wpisza nazwę miejsca...");

        objSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String strSearchQuery) {
                objSearchView.clearFocus();

                if(_objFragmentSpotsByCategory != null && _objFragmentSpotsByCategory.isVisible()) {
                    _objFragmentSpotsByCategory.filterSpots(strSearchQuery);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s == null || s.equals("")) {
                    if(_objFragmentSpotsByCategory != null && _objFragmentSpotsByCategory.isVisible()) {
                        _objFragmentSpotsByCategory.filterSpots("");
                    }
                }
                return false;
            }
        });

//        SearchView objSettingsMore = (SearchView) menu.findItem(R.id.settings_more).getActionView();
//        objSettingsMore.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(_objFragmentRouteList != null && _objFragmentRouteList.isVisible()) {
//
//                }
//            }
//        });

        return true;
    }

    public void setTopBarTitle(String strTitle) {
        try {
            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle(strTitle);
            }
        }
        catch (Exception ex) { }
    }

    public void showHideSearchIcon(boolean bShow) {
        try {
            findViewById(R.id.search).setVisibility(bShow ? View.VISIBLE : View.GONE);
        }
        catch (Exception ex) { }
    }

    public void showHide3DotVerticalIcon(boolean bShow) {
        try {
            findViewById(R.id.settings_more).setVisibility(bShow ? View.VISIBLE : View.GONE);
        }
        catch (Exception ex) { }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        this.closeDrawer();
        return true;
    }

    public void showHideProgressBar(boolean bShow) {
        findViewById(R.id.progressBar).setVisibility(bShow ? View.VISIBLE : View.GONE);
    }

    public void closeDrawer() {
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
    }

    private void bindMenu(List<MenuExpandListAdapter.ExpandableItem> listExpandableItem) {
        List<MenuExpandListAdapter.ExpandableItemChild> listExpandableItemChildCategories = new ArrayList<>();
        for(WebAPIManager.SpotCategory objSpotCategory : EGuidebookApplication.mSpotCategories) {
            listExpandableItemChildCategories.add(new MenuExpandListAdapter.ExpandableItemChild(objSpotCategory.SpotCategoryId, objSpotCategory.Name, objSpotCategory.IconPath));
        }

        MenuExpandListAdapter.ExpandableItem objExpandableItemCategories = new MenuExpandListAdapter.ExpandableItem("Miejsca", true, listExpandableItemChildCategories);
        MenuExpandListAdapter.ExpandableItem objExpandableItemAddSpot = new MenuExpandListAdapter.ExpandableItem("Zaproponuj miejsce", true, new ArrayList<MenuExpandListAdapter.ExpandableItemChild>());
        MenuExpandListAdapter.ExpandableItem objExpandableItemRoutes = new MenuExpandListAdapter.ExpandableItem("Trasy", false, new ArrayList<MenuExpandListAdapter.ExpandableItemChild>());
        MenuExpandListAdapter.ExpandableItem objExpandableItemLogout = new MenuExpandListAdapter.ExpandableItem("Wyloguj", false, new ArrayList<MenuExpandListAdapter.ExpandableItemChild>());

        listExpandableItem.add(objExpandableItemCategories);
        listExpandableItem.add(objExpandableItemAddSpot);
        listExpandableItem.add(objExpandableItemRoutes);
        listExpandableItem.add(objExpandableItemLogout);
    }

    private void enableExpandableList() {
        final List<MenuExpandListAdapter.ExpandableItem> listExpandableItem = new ArrayList<>();

        ExpandableListView objExpandableListView = (ExpandableListView) findViewById(R.id.left_drawer);

        this.bindMenu(listExpandableItem);
        MenuExpandListAdapter objMenuExpandListAdapter = new MenuExpandListAdapter(this, listExpandableItem, objExpandableListView);

        objExpandableListView.setAdapter(objMenuExpandListAdapter);

        objExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // create spot
                if(groupPosition == 1) {
                    FragmentManager objFragmentManager = getSupportFragmentManager();
                    FragmentTransaction objFragmentTransaction = objFragmentManager.beginTransaction();
                    FragmentCreateSpot objFragmentCreateSpot = FragmentCreateSpot.newInstance();
                    objFragmentTransaction.replace(R.id.main_content, objFragmentCreateSpot);
                    objFragmentTransaction.commit();
                    closeDrawer();
                }
                // routes
                else if(groupPosition == 2) {
                    FragmentManager objFragmentManager = getSupportFragmentManager();
                    FragmentTransaction objFragmentTransaction = objFragmentManager.beginTransaction();
                    _objFragmentRouteList = FragmentRouteList.newInstance();
                    objFragmentTransaction.replace(R.id.main_content, _objFragmentRouteList);
                    objFragmentTransaction.commit();
                    closeDrawer();
                }
                // logout
                else if(groupPosition == 3) {
                    EGuidebookApplication.logout(new EGuidebookApplication.ILogoutSuccessCallback() {
                        @Override
                        public void doAction() {
                            Intent objIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            objIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(objIntent);
                        }
                    });
                }

                return false;
            }
        });

        objExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) { }
        });

        objExpandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) { }
        });

        objExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                // spot by category
                if(groupPosition == 0) {
                    String strSpotCategoryID = listExpandableItem.get(0).getChildren().get(childPosition).getCustomID();
                    FragmentManager objFragmentManager = getSupportFragmentManager();
                    FragmentTransaction objFragmentTransaction = objFragmentManager.beginTransaction();
                    _objFragmentSpotsByCategory = FragmentSpotsByCategory.newInstance(strSpotCategoryID);
                    objFragmentTransaction.replace(R.id.main_content, _objFragmentSpotsByCategory);
                    objFragmentTransaction.commit();
                }

                closeDrawer();
                return false;
            }
        });


        objExpandableListView.setGroupIndicator(null);
    }
}