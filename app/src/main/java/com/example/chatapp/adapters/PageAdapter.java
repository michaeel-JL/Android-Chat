package com.example.chatapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.chatapp.fragments.chatsFragment;
import com.example.chatapp.fragments.mis_solicitudesFragment;
import com.example.chatapp.fragments.solicitudesFragment;
import com.example.chatapp.fragments.usuariosFragment;

public class PageAdapter extends FragmentStateAdapter {

    public PageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){
            case 0:
                return new usuariosFragment();
            case 1:
                return new chatsFragment();
            case 2:
                return new solicitudesFragment();
            case 3:
                return new mis_solicitudesFragment();
            default:
                return new usuariosFragment();
        }

    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
