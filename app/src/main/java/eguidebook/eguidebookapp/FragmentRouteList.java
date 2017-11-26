package eguidebook.eguidebookapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class FragmentRouteList extends Fragment {
    ArrayList<WebAPIManager.Route> _listRoute = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View objView = inflater.inflate(R.layout.fragment_route_list, container, false);

        ((MainActivity)getActivity()).setTopBarTitle("Trasy");
        ((MainActivity)getActivity()).showHideSearchIcon(false);
        ((MainActivity)getActivity()).showHide3DotVerticalIcon(true);

        this.loadRoutes();

        return objView;
    }

    @SuppressLint("StaticFieldLeak")
    public void loadRoutes() {
        ((MainActivity) getActivity()).showHideProgressBar(true);
        new AsyncTask<Void, Void, WebAPIManager.GetAllRoutesReply>() {
            @Override
            protected WebAPIManager.GetAllRoutesReply doInBackground(Void... voids) {
                return new WebAPIManager().getAllRoutes();
            }

            @Override
            protected void onPostExecute(WebAPIManager.GetAllRoutesReply getAllRoutesReply) {
                super.onPostExecute(getAllRoutesReply);

                if(getAllRoutesReply.isSuccess() && getAllRoutesReply.Routes != null) {
                     _listRoute = new ArrayList<>(Arrays.asList(getAllRoutesReply.Routes));
                    loadRecyclerViewRoutes(getView(), _listRoute);
                }

                ((MainActivity) getActivity()).showHideProgressBar(false);
            }
        }.execute();
    }

    public void loadRecyclerViewRoutes(View objView, ArrayList<WebAPIManager.Route> listRoute) {
        RecyclerView objRecyclerView = objView.findViewById(R.id.rv_route_list);
        objRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        objRecyclerView.setAdapter(new RouteRecyclerViewAdapter(listRoute));
    }

    private class RouteRecyclerViewAdapter extends RecyclerView.Adapter<RouteRecyclerViewAdapter.RouteViewHolder> {
        private ArrayList<WebAPIManager.Route> _listRoute = null;

        public RouteRecyclerViewAdapter(ArrayList<WebAPIManager.Route> listRoute) {
            _listRoute = listRoute != null ? new ArrayList<>(listRoute) : new ArrayList<WebAPIManager.Route>();
        }

        @Override
        public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RouteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_route_list_route_template, parent, false));
        }

        @Override
        public void onBindViewHolder(RouteViewHolder holder, int position) {
            final WebAPIManager.Route objRoute = this._listRoute.get(position);
            CollapseRowManager.setup(holder.getCollapseRow(), objRoute.Name + " (" + GoogleMapsManager.getSummedDistanceBetweenPoints(objRoute) + ")", CollapseRowManager.Type.TEXT,
                    new CollapseRowManager.IOnRowClick() {
                        @Override
                        public void doAction() {
                            Intent objIntent = new Intent(getContext(), ActivityRouteDetails.class);
                            objIntent.putExtra("Route", objRoute);
                            startActivity(objIntent);
                        }
                    }
            );
            CollapseRowManager.setText(holder.getCollapseRow(), objRoute.Description);
        }

        @Override
        public int getItemCount() {
            return _listRoute.size();
        }

        public class RouteViewHolder extends RecyclerView.ViewHolder {
            private View _objViewCollapseRow = null;

            public View getCollapseRow() {
                return this._objViewCollapseRow;
            }

            public RouteViewHolder(View itemView) {
                super(itemView);
                this._objViewCollapseRow = itemView.findViewById(R.id.collapse_row_route);
            }
        }
    }

    public static FragmentRouteList newInstance() {
        Bundle objBundle = new Bundle();
        FragmentRouteList objFragmentRouteList = new FragmentRouteList();
        objFragmentRouteList.setArguments(objBundle);
        return objFragmentRouteList;
    }
}
