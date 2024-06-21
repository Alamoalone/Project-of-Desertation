package com.fitstir.fitstirapp.ui.settings.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.fitstir.fitstirapp.R;
import com.fitstir.fitstirapp.databinding.FragmentProfileBinding;
import com.fitstir.fitstirapp.ui.settings.SettingsViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textUserIdProfile;
        settingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Add additions here
        ImageView profileImage = binding.profileImage;
        profileImage.setImageBitmap(settingsViewModel.getAvatar().getValue());

        TextView name = binding.textName;
        TextView age = binding.textAge;
        TextView height_ft = binding.textHeightFt;
        TextView height_in = binding.textHeightIn;
        TextView weight = binding.textWeight;
        TextView email = binding.textEmail;

        name.setText(settingsViewModel.getName().getValue());
        String tAge = settingsViewModel.getAge().getValue() + " years old";
        age.setText(tAge);
        String tHeightFeet = settingsViewModel.getHeightInFeet().getValue() + " feet ";
        height_ft.setText(tHeightFeet);
        String tHeightInches = settingsViewModel.getHeightInInches().getValue() + " inches";
        height_in.setText(tHeightInches);
        String tWeight = settingsViewModel.getWeight().getValue() + " lbs";
        weight.setText(tWeight);
        email.setText(settingsViewModel.getEmail().getValue());

        auth = FirebaseAuth.getInstance();
        String user = auth.getCurrentUser().getUid();
        reference =  FirebaseDatabase.getInstance().getReference("Users");
        reference.child(user).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        String fullName = String.valueOf(snapshot.child("fullname").getValue());
                        String email = String.valueOf(snapshot.child("email").getValue());
                        String age = String.valueOf(snapshot.child("age").getValue());
                        String height_ft = String.valueOf(snapshot.child("height_ft").getValue());
                        String height_in = String.valueOf(snapshot.child("height_in").getValue());
                        String weight = String.valueOf(snapshot.child("_Weight").getValue());

                        settingsViewModel.setName(fullName);
                        settingsViewModel.setEmail(email);
                        settingsViewModel.setAge(Integer.parseInt(age));
                        settingsViewModel.setHeightInFeet(Integer.parseInt(height_ft));
                        settingsViewModel.setHeightInInches(Integer.parseInt(height_in));
                        settingsViewModel.setWeight(Integer.parseInt(weight));

                        binding.textName.setText(fullName);
                        binding.textEmail.setText(email);
                        String tAge = settingsViewModel.getAge().getValue() + " years old";
                        binding.textAge.setText(tAge);
                        String tHeightFeet = settingsViewModel.getHeightInFeet().getValue() + " feet ";
                        binding.textHeightFt.setText(tHeightFeet);
                        String tHeightInches = settingsViewModel.getHeightInInches().getValue() + " inches";
                        binding.textHeightIn.setText(tHeightInches);
                        String tWeight = settingsViewModel.getWeight().getValue() + " lbs";
                        binding.textWeight.setText(tWeight);

                    }
                }
            }
        });


        CardView editButton = binding.editbuttonCardViewProfile;
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Navigation.findNavController(root).navigate(R.id.action_navigation_profile_to_navigation_edit_profile);
            }
        });

        // End

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}