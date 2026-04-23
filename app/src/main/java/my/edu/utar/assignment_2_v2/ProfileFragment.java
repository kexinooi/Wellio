package my.edu.utar.assignment_2_v2;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private ImageView btnSettings;
    private ImageView imgProfileAvatar;
    private TextView tvProfileName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnSettings = view.findViewById(R.id.btn_settings);
        imgProfileAvatar = view.findViewById(R.id.img_profile_avatar);
        tvProfileName = view.findViewById(R.id.tv_profile_name);

        loadProfileData();

        btnSettings.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences("wellio_settings", 0);

        String fullName = prefs.getString("full_name", "Kexin");
        String imageUriString = prefs.getString("profile_image_uri", null);

        tvProfileName.setText(fullName);

        if (!TextUtils.isEmpty(imageUriString)) {
            imgProfileAvatar.setImageURI(Uri.parse(imageUriString));
        } else {
            imgProfileAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }
}