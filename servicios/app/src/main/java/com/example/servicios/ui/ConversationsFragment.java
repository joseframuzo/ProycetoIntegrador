package com.example.servicios.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.servicios.R;

public class ConversationsFragment extends Fragment {

    private EditText edtConvId;
    private Button btnOpen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_conversations, container, false);

        edtConvId = v.findViewById(R.id.edtConvId);
        btnOpen = v.findViewById(R.id.btnOpenConv);

        btnOpen.setOnClickListener(view -> {
            String id = edtConvId.getText().toString().trim();
            if (TextUtils.isEmpty(id)) {
                Toast.makeText(getContext(), "Ingresa un conversation_id", Toast.LENGTH_SHORT).show();
                return;
            }
            openChat(id);
        });

        return v;
    }

    private void openChat(@NonNull String conversationId) {
        Intent i = new Intent(requireContext(), ChatActivity.class);
        i.putExtra("CONV_ID", conversationId);
        startActivity(i);
    }
}
