package my.edu.utar.assignment_2_v2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class InsightsFragment extends Fragment {

    private TextView tabMood, tabSleep, tabAcademic;
    private View indicatorMood, indicatorSleep, indicatorAcademic;
    private View containerMood, containerSleep, containerAcademic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

        tabMood = view.findViewById(R.id.tv_tab_mood);
        tabSleep = view.findViewById(R.id.tv_tab_sleep);
        tabAcademic = view.findViewById(R.id.tv_tab_academic);
        
        containerMood = view.findViewById(R.id.tab_mood);
        containerSleep = view.findViewById(R.id.tab_sleep);
        containerAcademic = view.findViewById(R.id.tab_academic);
        
        indicatorMood = view.findViewById(R.id.indicator_mood);
        indicatorSleep = view.findViewById(R.id.indicator_sleep);
        indicatorAcademic = view.findViewById(R.id.indicator_academic);

        // Set Sleep as initial tab to match your image
        updateTabs("sleep");
        loadTabFragment(new SleepTrendsFragment());

        containerMood.setOnClickListener(v -> {
            updateTabs("mood");
            loadTabFragment(new MoodTrendsFragment());
        });
        
        containerSleep.setOnClickListener(v -> {
            updateTabs("sleep");
            loadTabFragment(new SleepTrendsFragment());
        });
        
        containerAcademic.setOnClickListener(v -> {
            updateTabs("academic");
            loadTabFragment(new AcademicTrendsFragment());
        });

        return view;
    }

    private void updateTabs(String activeTab) {
        // Reset all
        tabMood.setTextColor(Color.parseColor("#787885"));
        tabSleep.setTextColor(Color.parseColor("#787885"));
        tabAcademic.setTextColor(Color.parseColor("#787885"));
        
        indicatorMood.setVisibility(View.INVISIBLE);
        indicatorSleep.setVisibility(View.INVISIBLE);
        indicatorAcademic.setVisibility(View.INVISIBLE);

        // Activate one
        switch (activeTab) {
            case "mood":
                tabMood.setTextColor(Color.BLACK);
                indicatorMood.setVisibility(View.VISIBLE);
                break;
            case "sleep":
                tabSleep.setTextColor(Color.BLACK);
                indicatorSleep.setVisibility(View.VISIBLE);
                break;
            case "academic":
                tabAcademic.setTextColor(Color.BLACK);
                indicatorAcademic.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loadTabFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.insights_tab_container, fragment);
        transaction.commit();
    }
}