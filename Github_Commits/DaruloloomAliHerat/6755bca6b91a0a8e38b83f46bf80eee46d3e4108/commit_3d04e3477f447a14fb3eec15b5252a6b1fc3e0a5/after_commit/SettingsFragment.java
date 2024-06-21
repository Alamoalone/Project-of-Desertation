package com.ehsanhaidary.arefmorady;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class SettingsFragment extends Fragment {


    ImageView telegram, facebook, adminTelegram, adminGmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        telegram = view.findViewById(R.id.telegram);
        facebook = view.findViewById(R.id.facebook);
        adminTelegram = view.findViewById(R.id.adminTelegram);
        adminGmail = view.findViewById(R.id.adminGmail);

        telegram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://t.me/daruloloomAliHerat");
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://www.facebook.com/darululoomaaliherat");
            }
        });
        adminTelegram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://t.me/Ehsanullahhaidary");
            }
        });
        adminGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "ehsanullahhaidary@gmail.com", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(Intent.createChooser(intent, "بازکردن در:"));
    }
}