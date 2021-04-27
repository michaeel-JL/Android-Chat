package com.example.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.MensajesActivity;
import com.example.chatapp.R;
import com.example.chatapp.pojos.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class AdapterChatLista extends RecyclerView.Adapter<AdapterChatLista.viewHolderAdapterChatList> {

    List<Users> userList;
    Context context;
    SharedPreferences mPref;


    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database =  FirebaseDatabase.getInstance();

    public AdapterChatLista(List<Users> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolderAdapterChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatlista, parent, false);
        viewHolderAdapterChatList holder = new viewHolderAdapterChatList(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolderAdapterChatList holder, int position) {
        Users userss = userList.get(position);

        final Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        holder.tv_usuario.setText(userss.getNombre());
        Glide.with(context).load(userss.getFoto()).into(holder.img_user);

        DatabaseReference ref_mis_solicitudes = database.getReference("Solicitudes").child(user.getUid());
        ref_mis_solicitudes.child(userss.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String estado = snapshot.child("estado").getValue(String.class);

                if (snapshot.exists()){
                    if (estado.equals("amigos")){
                        holder.cardView.setVisibility(View.VISIBLE);
                    }else{
                        holder.cardView.setVisibility(View.GONE);
                    }
                }else{
                    holder.cardView.setRadius(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Calendar c = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        DatabaseReference ref_Estado = database.getReference("Estado").child(userss.getId());
        ref_Estado.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String estado = snapshot.child("estado").getValue(String.class);
                String fecha = snapshot.child("fecha").getValue(String.class);
                String hora = snapshot.child("hora").getValue(String.class);

                if (snapshot.exists()){
                    if (estado.equals("conectado")){
                        holder.tv_conectado.setVisibility(View.VISIBLE);
                        holder.icon_conectado.setVisibility(View.VISIBLE);
                        holder.tv_desconectado.setVisibility(View.GONE);
                        holder.icon_desconectado.setVisibility(View.GONE);
                    }else{
                        holder.tv_conectado.setVisibility(View.GONE);
                        holder.icon_conectado.setVisibility(View.GONE);
                        holder.tv_desconectado.setVisibility(View.VISIBLE);
                        holder.icon_desconectado.setVisibility(View.VISIBLE);


                        if(fecha.equals(dateFormat.format(c.getTime()))){
                            holder.tv_desconectado.setText("últ.vez hoy a las" + hora);
                        }else{
                            holder.tv_desconectado.setText("últ.vez " + fecha + " a las " + hora);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPref = view.getContext().getSharedPreferences("usuario_sp",Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = mPref.edit();

                final DatabaseReference ref = database.getReference("Solicitudes").child(user.getUid()).child(userss.getId()).child("idchat");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String id_unico = snapshot.getValue(String.class);
                        if(snapshot.exists()){
                            Intent intent = new Intent(view.getContext(), MensajesActivity.class);
                            intent.putExtra("nombre", userss.getNombre());
                            intent.putExtra("img_user", userss.getFoto());
                            intent.putExtra("id_user", userss.getId());
                            intent.putExtra("id_unico", id_unico);
                            editor.putString("usuario_sp",userss.getId());
                            editor.apply();

                            view.getContext().startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }
        });


    }//Fin del onbindViewHolder

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class viewHolderAdapterChatList extends RecyclerView.ViewHolder {
        TextView tv_usuario;
        ImageView img_user;
        CardView cardView;
        TextView tv_conectado, tv_desconectado;
        ImageView icon_conectado, icon_desconectado;

        public viewHolderAdapterChatList(@NonNull View itemView) {
            super(itemView);
            tv_usuario = itemView.findViewById(R.id.tv_user);
            img_user = itemView.findViewById(R.id.img_user);
            cardView = itemView.findViewById(R.id.cardiew);
            tv_conectado = itemView.findViewById(R.id.tv_conectado);
            tv_desconectado = itemView.findViewById(R.id.tv_desconectado);
            icon_conectado = itemView.findViewById(R.id.icon_conectado);
            icon_desconectado = itemView.findViewById(R.id.icon_desconectado);
        }
    }
}
