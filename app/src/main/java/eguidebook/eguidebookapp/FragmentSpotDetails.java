package eguidebook.eguidebookapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class FragmentSpotDetails extends Fragment {
    private WebAPIManager.Spot _objSpot = null;
    private int nSelectedUserGrade = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View objView = inflater.inflate(R.layout.fragment_spot_details, container, false);

        _objSpot = getArguments().getParcelable("Spot");

        ImageView ivSpotDetailsImage1 = objView.findViewById(R.id.iv_spot_details_image1);
        ImageView ivSpotDetailsImage2 = objView.findViewById(R.id.iv_spot_details_image2);
        ImageView ivSpotDetailsImage3 = objView.findViewById(R.id.iv_spot_details_image3);
        ImageView ivSpotDetailsImage4 = objView.findViewById(R.id.iv_spot_details_image4);
        ImageView ivSpotDetailsImage5 = objView.findViewById(R.id.iv_spot_details_image5);

        Context objContext = getContext();

        this.setSpotImage(objContext, ivSpotDetailsImage1, _objSpot.Image1Path);
        this.setSpotImage(objContext, ivSpotDetailsImage2, _objSpot.Image2Path);
        this.setSpotImage(objContext, ivSpotDetailsImage3, _objSpot.Image3Path);
        this.setSpotImage(objContext, ivSpotDetailsImage4, _objSpot.Image4Path);
        this.setSpotImage(objContext, ivSpotDetailsImage5, _objSpot.Image5Path);

        this.setSpotGrade(objView, _objSpot.AverageGrade);

        ((TextView) objView.findViewById(R.id.tv_spot_name)).setText(_objSpot.Name);

        CollapseRowManager.setup(objView.findViewById(R.id.row_description), "Opis", CollapseRowManager.Type.TEXT, null);
        CollapseRowManager.setText(objView.findViewById(R.id.row_description), _objSpot.Description);

        if(_objSpot.IsOpeningHoursDefined) {
            CollapseRowManager.setup(objView.findViewById(R.id.row_opening_hours), "Godziny otwarcia", CollapseRowManager.Type.DOUBLE_TEXT, null);
            CollapseRowManager.setText(objView.findViewById(R.id.row_opening_hours), this.buildOpeningHoursString(_objSpot)[0], this.buildOpeningHoursString(_objSpot)[1]);
        }
        else {
            objView.findViewById(R.id.row_opening_hours).setVisibility(View.GONE);
        }

        this.setCreateGradeOverlay(objView);

        ((MainActivity)getActivity()).setTopBarTitle(_objSpot.Name);
        ((MainActivity)getActivity()).showHideSearchIcon(false);
        ((MainActivity)getActivity()).showHide3DotVerticalIcon(false);

        return objView;
    }

    private void setSpotImage(Context objContext, ImageView objImageView, String strURL) {
        if(PLHelpers.stringIsNullOrEmpty(strURL)) {
            objImageView.setVisibility(View.GONE);
            objImageView.setImageBitmap(null);
        }
        else {
            Picasso.with(objContext).load(WebAPIManager.getBaseURL() + strURL).into(objImageView);
            objImageView.setVisibility(View.VISIBLE);
        }
    }

    private void setSpotGrade(View objView, int nGrade) {
        ImageView[] arrIvStars = new ImageView[]
            {
                    objView.findViewById(R.id.iv_spot_grade_star1),
                    objView.findViewById(R.id.iv_spot_grade_star2),
                    objView.findViewById(R.id.iv_spot_grade_star3),
                    objView.findViewById(R.id.iv_spot_grade_star4),
                    objView.findViewById(R.id.iv_spot_grade_star5)
            };

        arrIvStars[0].setImageResource(R.drawable.star_yellow);
        for(int i = 1; i < arrIvStars.length; i++) {
            arrIvStars[i].setImageResource(i + 1 <= nGrade ? R.drawable.star_yellow : R.drawable.star_empty);
        }
    }

    private void setCreateGradeOverlay(final View objView) {
        objView.findViewById(R.id.btn_create_grade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ImageView[] arrUserSpotGradeStars = new ImageView[]
                {
                    objView.findViewById(R.id.iv_spot_user_grade_star1),
                    objView.findViewById(R.id.iv_spot_user_grade_star2),
                    objView.findViewById(R.id.iv_spot_user_grade_star3),
                    objView.findViewById(R.id.iv_spot_user_grade_star4),
                    objView.findViewById(R.id.iv_spot_user_grade_star5)
                };

                for(int i = 0; i < _objSpot.UserGrade; i++) {
                    arrUserSpotGradeStars[i].setImageResource(_objSpot.UserGrade >= (i + 1) ? R.drawable.star_yellow : R.drawable.star_empty);
                }

                objView.findViewById(R.id.rl_create_spot_grade).setVisibility(View.VISIBLE);
                nSelectedUserGrade = -1;
            }
        });

        objView.findViewById(R.id.btn_send_grade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nSelectedUserGrade == -1) {
                    objView.findViewById(R.id.rl_create_spot_grade).setVisibility(View.GONE);
                    return;
                }

                ((MainActivity)getActivity()).showHideProgressBar(true);
                new AsyncTask<Void, Void, WebAPIManager.WebAPIReply>() {
                    @Override
                    protected WebAPIManager.WebAPIReply doInBackground(Void... voids) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return new WebAPIManager().createSpotGrade(nSelectedUserGrade, _objSpot.SpotID);
                    }

                    @Override
                    protected void onPostExecute(WebAPIManager.WebAPIReply objWebAPIReply) {
                        if(objWebAPIReply != null && objWebAPIReply.isSuccess()) {
                            _objSpot.UserGrade = nSelectedUserGrade;
                        }
                        ((MainActivity)getActivity()).showHideProgressBar(false);
                        objView.findViewById(R.id.rl_create_spot_grade).setVisibility(View.GONE);
                    }
                }.execute();
            }
        });

        objView.findViewById(R.id.btn_cancel_grade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objView.findViewById(R.id.rl_create_spot_grade).setVisibility(View.GONE);
            }
        });

        final ImageView[] arrUserSpotGradeStars = new ImageView[]
        {
            objView.findViewById(R.id.iv_spot_user_grade_star1),
            objView.findViewById(R.id.iv_spot_user_grade_star2),
            objView.findViewById(R.id.iv_spot_user_grade_star3),
            objView.findViewById(R.id.iv_spot_user_grade_star4),
            objView.findViewById(R.id.iv_spot_user_grade_star5)
        };

        for(int i = 0; i < arrUserSpotGradeStars.length; i++) {
            final int finalI = i;
            arrUserSpotGradeStars[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nSelectedUserGrade = finalI + 1;
                    for(int j = 0; j <= finalI; j++) {
                        arrUserSpotGradeStars[j].setImageResource(R.drawable.star_yellow);
                    }
                    for(int j = finalI + 1; j < arrUserSpotGradeStars.length; j++) {
                        arrUserSpotGradeStars[j].setImageResource(R.drawable.star_empty);
                    }
                }
            });
        }
    }

    private String[] buildOpeningHoursString(WebAPIManager.Spot objSpot) {
        StringBuilder objStringBuilderText1 = new StringBuilder();
        objStringBuilderText1.append(objSpot.OpeningHours_MondayFrom + "  -  " + objSpot.OpeningHours_MondayTo + '\n');
        objStringBuilderText1.append(objSpot.OpeningHours_TuesdayFrom + "  -  " + objSpot.OpeningHours_TuesdayTo + '\n');
        objStringBuilderText1.append(objSpot.OpeningHours_WednesdayFrom + "  -  " + objSpot.OpeningHours_WednesdayTo + '\n');
        objStringBuilderText1.append(objSpot.OpeningHours_ThursdayFrom + "  -  " + objSpot.OpeningHours_ThursdayTo + '\n');
        objStringBuilderText1.append(objSpot.OpeningHours_FridayFrom + "  -  " + objSpot.OpeningHours_FridayTo + '\n');
        objStringBuilderText1.append(objSpot.OpeningHours_SaturdayFrom + "  -  " + objSpot.OpeningHours_SaturdayTo + '\n');
        objStringBuilderText1.append(objSpot.OpeningHours_SundayFrom + "  -  " + objSpot.OpeningHours_SundayTo + '\n');

        StringBuilder objStringBuilderText2 = new StringBuilder();
        objStringBuilderText2.append("Poniedziałek:" + '\n');
        objStringBuilderText2.append("Wtorek:" + '\n');
        objStringBuilderText2.append("Środa:" + '\n');
        objStringBuilderText2.append("Czwartek:" + '\n');
        objStringBuilderText2.append("Piątek:" + '\n');
        objStringBuilderText2.append("Sobota:" + '\n');
        objStringBuilderText2.append("Niedziela:" + '\n');

        return new String[] { objStringBuilderText2.toString(), objStringBuilderText1.toString() };
    }

    public static FragmentSpotDetails newInstance(WebAPIManager.Spot objSpot) {
        Bundle objBundle = new Bundle();
        objBundle.putParcelable("Spot", objSpot);
        FragmentSpotDetails objFragmentSpotDetails = new FragmentSpotDetails ();
        objFragmentSpotDetails.setArguments(objBundle);
        return objFragmentSpotDetails;
    }
}