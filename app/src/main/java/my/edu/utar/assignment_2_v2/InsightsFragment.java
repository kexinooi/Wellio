package my.edu.utar.assignment_2_v2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class InsightsFragment extends Fragment {

    private View indicatorMood, indicatorSleep, indicatorAcademic;
    private View containerMood, containerSleep, containerAcademic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

        containerMood = view.findViewById(R.id.tab_mood);
        containerSleep = view.findViewById(R.id.tab_sleep);
        containerAcademic = view.findViewById(R.id.tab_academic);

        indicatorMood = view.findViewById(R.id.indicator_mood);
        indicatorSleep = view.findViewById(R.id.indicator_sleep);
        indicatorAcademic = view.findViewById(R.id.indicator_academic);

        // Default selected tab
        updateTabs("mood");
        loadTabFragment(new MoodTrendsFragment());

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
        int activeColor = ContextCompat.getColor(requireContext(), R.color.text_black);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_grey_light);

        // Reset all tabs
        indicatorMood.setVisibility(View.INVISIBLE);
        indicatorSleep.setVisibility(View.INVISIBLE);
        indicatorAcademic.setVisibility(View.INVISIBLE);

        // Activate selected tab
        switch (activeTab) {
            case "mood":
                indicatorMood.setVisibility(View.VISIBLE);
                break;

            case "sleep":
                indicatorSleep.setVisibility(View.VISIBLE);
                break;

            case "academic":
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
