package com.example.servicios.ui;

import android.os.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.servicios.R;
import com.example.servicios.api.ApiClient;
import com.example.servicios.api.ApiService;
import com.example.servicios.data.SessionManager;
import com.example.servicios.models.Message;

import java.util.*;
import retrofit2.*;

public class ChatActivity extends AppCompatActivity {
    RecyclerView rv;
    EditText edtMsg;
    ImageButton btnSend;
    ChatAdapter adapter;
    ApiService api;
    SessionManager session;
    String convId;

    Handler handler = new Handler(Looper.getMainLooper());
    Runnable poller = new Runnable() {
        @Override public void run() {
            load();
            handler.postDelayed(this, 3000); // 3s
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_chat);
        api = ApiClient.get().create(ApiService.class);
        session = new SessionManager(this);

        convId = getIntent().getStringExtra("CONV_ID");

        rv = findViewById(R.id.rvChat);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(new ArrayList<>(), session.userId());
        rv.setAdapter(adapter);

        edtMsg = findViewById(R.id.edtMsg);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> send());

        poller.run();
    }

    void load() {
        api.listMessages(convId, 100).enqueue(new Callback<List<Message>>() {
            @Override public void onResponse(Call<List<Message>> call, Response<List<Message>> r) {
                if (r.isSuccessful() && r.body()!=null) {
                    adapter.setData(r.body());
                    rv.scrollToPosition(Math.max(0, adapter.getItemCount()-1));
                }
            }
            @Override public void onFailure(Call<List<Message>> call, Throwable t) {}
        });
    }

    void send() {
        String body = edtMsg.getText().toString().trim();
        if (body.isEmpty()) return;
        Map<String, String> b = new HashMap<>();
        b.put("conversation_id", convId);
        b.put("sender_id", session.userId());
        b.put("body", body);
        api.sendMessage(b).enqueue(new Callback<Message>() {
            @Override public void onResponse(Call<Message> call, Response<Message> r) {
                edtMsg.setText("");
                load();
            }
            @Override public void onFailure(Call<Message> call, Throwable t) {}
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(poller);
    }

    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
        List<Message> data; String myId;
        ChatAdapter(List<Message> d, String myId){ data=d; this.myId=myId; }
        void setData(List<Message> d){ data=d; notifyDataSetChanged(); }
        @Override public VH onCreateViewHolder(android.view.ViewGroup p, int v) {
            return new VH(android.view.LayoutInflater.from(p.getContext()).inflate(R.layout.item_message, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            Message m = data.get(pos);
            boolean mine = m.sender_id!=null && m.sender_id.equals(myId);
            h.txt.setText((mine ? "Yo: " : "Ellx: ") + m.body);
        }
        @Override public int getItemCount(){ return data==null?0:data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView txt; VH(android.view.View v){ super(v); txt = v.findViewById(R.id.txtMsg); }
        }
    }
}
