package eguidebook.eguidebookapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MenuExpandListAdapter extends BaseExpandableListAdapter {

    private ExpandableListView _objExpandableListView = null;

    public static class ExpandableItem {
        private long _lId;
        private String _strName;
        private boolean _bIsExpandable;
        private List<ExpandableItemChild> _listExpandableItemChild;

        public ExpandableItem(String strName, boolean bIsExpandable, List<ExpandableItemChild> listExpandableItemChild) {
            this._strName = strName;
            this._bIsExpandable = bIsExpandable;
            this._listExpandableItemChild = listExpandableItemChild;
        }

        public long getId() {
            return this._lId;
        }

        public String getName() {
            return this._strName;
        }

        public boolean getIsExpandable() {
            return this._bIsExpandable;
        }

        public List<ExpandableItemChild> getChildren() {
            return this._listExpandableItemChild;
        }
    }

    public static class ExpandableItemChild {
        private long _lId;
        private String _strName;
        private String _strBitmapURL;
        private String _strCustomID;

        public ExpandableItemChild(String strCustomID, String strName, String objBitmapURL) {
            this._strCustomID = strCustomID;
            this._strName = strName;
            this._strBitmapURL = objBitmapURL;
        }

        public long getId() {
            return this._lId;
        }

        public String getCustomID() {
            return this._strCustomID;
        }

        public String getName() {
            return this._strName;
        }

        public String getBitmapURL() {
            return this._strBitmapURL;
        }
    }

    private Context _objContext;
    private List<ExpandableItem> _listExpandableItem;

    public MenuExpandListAdapter(Context objContext, List<ExpandableItem> listExpandableItem, ExpandableListView objExpandableListView) {
        this._objExpandableListView = objExpandableListView;
        this._objContext = objContext;
        this._listExpandableItem = listExpandableItem;
    }

    @Override
    public Object getChild(int nGroupPosition, int nChildPosititon) {
        return this._listExpandableItem.get(nGroupPosition).getChildren().get(nChildPosititon);
    }

    @Override
    public long getChildId(int nGroupPosition, int nChildPosition) {
        return _listExpandableItem.get(nGroupPosition).getChildren().get(nChildPosition).getId();
    }

    @Override
    public View getChildView(int nGroupPosition, final int nChildPosition, boolean bIsLastChild, View objConvertView, ViewGroup objParentview) {
        ExpandableItemChild objExpandableItemChild = ((ExpandableItemChild) getChild(nGroupPosition, nChildPosition));

        if (objConvertView == null) {
            LayoutInflater objLayoutInflater = (LayoutInflater) this._objContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            objConvertView = objLayoutInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild = objConvertView.findViewById(R.id.lblListItem);
        txtListChild.setText(objExpandableItemChild.getName());

        ImageView ivItemSubMenu = objConvertView.findViewById(R.id.iv_item_sub_menu);
        Picasso.with(_objContext).load(WebAPIManager.getBaseURL() + objExpandableItemChild.getBitmapURL()).into(ivItemSubMenu);

        return objConvertView;
    }

    @Override
    public int getChildrenCount(int nGroupPosition) {
        return this._listExpandableItem.get(nGroupPosition).getChildren().size();
    }

    @Override
    public Object getGroup(int nGroupPosition) {
        return this._listExpandableItem.get(nGroupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listExpandableItem.size();
    }

    @Override
    public long getGroupId(int nGroupPosition) {
        return this._listExpandableItem.get(nGroupPosition).getId();
    }

    @Override
    public View getGroupView(int nGroupPosition, boolean bIsExpanded, View objConvertView, ViewGroup objParentView) {
        String strName = ((ExpandableItem) getGroup(nGroupPosition)).getName();
        if (objConvertView == null) {
            LayoutInflater objLayoutInflater = (LayoutInflater) this._objContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            objConvertView = objLayoutInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = objConvertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(strName);
        return objConvertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }}