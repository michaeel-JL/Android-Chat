package com.example.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.chatapp.pojos.Solicitudes;
import com.example.chatapp.pojos.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.viewHolderAdapter> {

    List<Users> userList;
    Context context;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database =  FirebaseDatabase.getInstance();


    SharedPreferences mPref;

    public UsersAdapter(List<Users> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolderAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_usuarios, parent, false);
        viewHolderAdapter holder = new viewHolderAdapter(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolderAdapter holder, int position) {
        Users userss = userList.get(position);

        final Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        Glide.with(context).load(userss.getFoto()).into(holder.img_user);
        holder.tv_usuario.setText(userss.getNombre());

        //Si el usuario que está recorriendo es igual al que tenemos lo oculta
        //Reciclable
        if (userss.getId().equals(user.getUid())){
            holder.cardView.setVisibility(View.GONE);
        }else{
            holder.cardView.setVisibility(View.VISIBLE);
        }

        //Crea la rama SOLICITUDES en fbs con un id y dentro de ese id los demas usuarios con su estado.
        DatabaseReference ref_mis_botones = database.getReference("Solicitudes").child(user.getUid());
        ref_mis_botones.child(userss.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String estado = snapshot.child("estado").getValue(String.class);
                if (snapshot.exists()){
                    if (estado.equals("enviado")){
                        holder.send.setVisibility(View.VISIBLE);
                        holder.amigos.setVisibility(View.GONE);
                        holder.tengoSolicitud.setVisibility(View.GONE);
                        holder.add.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);
                    }

                    if (estado.equals("amigos")){
                        holder.send.setVisibility(View.GONE);
                        holder.amigos.setVisibility(View.VISIBLE);
                        holder.tengoSolicitud.setVisibility(View.GONE);
                        holder.add.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);
                    }

                    if (estado.equals("solicitud")){
                        holder.send.setVisibility(View.GONE);
                        holder.amigos.setVisibility(View.GONE);
                        holder.tengoSolicitud.setVisibility(View.VISIBLE);
                        holder.add.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);
                    }

                }else{
                    holder.send.setVisibility(View.GONE);
                    holder.amigos.setVisibility(View.GONE);
                    holder.tengoSolicitud.setVisibility(View.GONE);
                    holder.add.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Boton de agregar amigo
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Primer registro
                final DatabaseReference A = database.getReference("Solicitudes").child(user.getUid());
                A.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Solicitudes sol = new Solicitudes("enviado", "");

                        A.child(userss.getId()).setValue(sol);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Segundo registro
                final DatabaseReference B = database.getReference("Solicitudes").child(userss.getId());
                B.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Solicitudes sol = new Solicitudes("solicitud", "");

                        B.child(user.getUid()).setValue(sol);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Cogemos al usuario en el que presionamos
                DatabaseReference count = database.getReference("Contador").child(userss.getId());
                count.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Integer val = snapshot.getValue(Integer.class);
                            if (val==0){
                                count.setValue(1);
                            }else{
                                count.setValue(val+1);
                            }
                        }else {
                            count.setValue(1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                vibrator.vibrate(300);


            }//Fin del onClick
        });//Fin SETonClick

        //Boton de aceptar solicitud de amgio
        holder.tengoSolicitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String idchat = ref_mis_botones.push().getKey();

                final DatabaseReference A = database.getReference("Solicitudes").child(userss.getId()).child(user.getUid());

                A.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Solicitudes sol = new Solicitudes("amigos", idchat);
                        A.setValue(sol);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                final DatabaseReference B = database.getReference("Solicitudes").child(user.getUid()).child(userss.getId());
                B.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Solicitudes sol = new Solicitudes("amigos", idchat);
                        B.setValue(sol);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                vibrator.vibrate(300);

            }
        });

        //Boton de amigos - Nos envia al chat
        holder.amigos.setOnClickListener(new View.OnClickListener() {
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


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class viewHolderAdapter extends RecyclerView.ViewHolder {
        TextView tv_usuario;
        ImageView img_user;
        CardView cardView;
        Button add, send, amigos, tengoSolicitud;
        ProgressBar progressBar;



        public viewHolderAdapter(@NonNull View itemView) {
            super(itemView);

            tv_usuario = itemView.findViewById(R.id.tv_user);
            img_user = itemView.findViewById(R.id.img_user);
            cardView = itemView.findViewById(R.id.cardiew);
            send = itemView.findViewById(R.id.btn_send);
            add = itemView.findViewById(R.id.btn_add);
            amigos = itemView.findViewById(R.id.btn_amigos);
            tengoSolicitud = itemView.findViewById(R.id.btn_tengoSolicitud);
            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }
}
