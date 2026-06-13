package com.example.fitbook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ContactsActivity extends AppCompatActivity {

    private WebView webMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        findViewById(R.id.btnContactsBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvContactsTitle)).setText(R.string.client_contacts_title);
        ((TextView) findViewById(R.id.tvContactsSubtitle)).setText(R.string.client_contacts_subtitle);

        MaterialCardView btnCall = findViewById(R.id.btnCall);
        MaterialCardView btnVk = findViewById(R.id.btnVk);
        MaterialCardView btnMax = findViewById(R.id.btnMax);
        MaterialCardView btnChat = findViewById(R.id.btnChat);
        webMap = findViewById(R.id.webMap);

        btnCall.setOnClickListener(v -> openDialer(getString(R.string.client_contacts_phone)));
        btnVk.setOnClickListener(v -> openUrl(getString(R.string.client_contacts_vk_url)));
        btnMax.setOnClickListener(v -> openUrl(getString(R.string.client_contacts_max_url)));
        btnChat.setOnClickListener(v -> openUrl(getString(R.string.client_contacts_chat_url)));

        setupMap();
    }

    private void setupMap() {
        WebSettings settings = webMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webMap.setWebViewClient(new WebViewClient());
        webMap.loadUrl(getString(R.string.client_contacts_map_url));
    }

    private void openDialer(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.replace(" ", "").replace("(", "").replace(")", "").replace("-", "")));
        startActivity(intent);
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
