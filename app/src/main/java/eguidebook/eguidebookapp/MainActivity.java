package eguidebook.eguidebookapp;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
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

        listExpandableItem.add(objExpandableItemCategories);
    }

    private void enableExpandableList() {
        final List<MenuExpandListAdapter.ExpandableItem> listExpandableItem = new ArrayList<>();

        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.left_drawer);

        bindMenu(listExpandableItem);
        MenuExpandListAdapter listAdapter = new MenuExpandListAdapter(this, listExpandableItem);
        expListView.setAdapter(listAdapter);

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) { }
        });

        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) { }
        });

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                // spot by category
                if(groupPosition == 0) {
                    String strSpotCategoryID = listExpandableItem.get(0).getChildren().get(childPosition).getCustomID();
                    FragmentManager objFragmentManager = getSupportFragmentManager();
                    FragmentTransaction objFragmentTransaction = objFragmentManager.beginTransaction();
                    objFragmentTransaction.replace(R.id.main_content, FragmentSpotsByCategory.newInstance(strSpotCategoryID));
                    objFragmentTransaction.commit();
                }

                closeDrawer();
                return false;
            }
        });
    }
}