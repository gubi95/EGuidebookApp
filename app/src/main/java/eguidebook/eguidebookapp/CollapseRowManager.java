package eguidebook.eguidebookapp;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CollapseRowManager {
    public static void setup(final View objView, String strHeader) {
        ((TextView)objView.findViewById(R.id.tv_expand_collapse_row_header)).setText(strHeader);
        objView.findViewById(R.id.iv_expand_collapse_row).setTag(false);
        objView.findViewById(R.id.tv_collapse_row_text_content).setVisibility(View.GONE);
        objView.findViewById(R.id.iv_expand_collapse_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View objViewImage) {
                ImageView ivExpandCollapse = (ImageView) objViewImage;
                boolean bIsExpanded = (boolean) ivExpandCollapse.getTag();
                ivExpandCollapse.setImageResource(bIsExpanded ? R.drawable.ic_add_circle_black_24dp : R.drawable.ic_remove_circle_black_24dp);
                ivExpandCollapse.setTag(!bIsExpanded);
                objView.findViewById(R.id.tv_collapse_row_text_content).setVisibility(bIsExpanded ? View.GONE : View.VISIBLE);
            }
        });
    }

    public static void setText(View objView, String strText) {
        ((TextView)objView.findViewById(R.id.tv_collapse_row_text_content)).setText(strText);
    }


}
