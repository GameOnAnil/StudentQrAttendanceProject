package com.gameonanil.qrattendenceproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gameonanil.qrattendenceproject.databinding.AttendanceListBinding
import com.gameonanil.qrattendenceproject.model.Student
import com.gameonanil.qrattendenceproject.model.Users

class ManageUserAdapter(private val context: Context,
                        private val userList: List<Users>,
                        private val listener: OnUserClickListener
): RecyclerView.Adapter<ManageUserAdapter.ManageUserHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageUserHolder {
        val view = AttendanceListBinding.inflate(LayoutInflater.from(context), parent, false)
        return ManageUserHolder(view)
    }

    override fun onBindViewHolder(holder: ManageUserHolder, position: Int) {
        holder.bindTo(userList[position])
    }

    override fun getItemCount() = userList.size

    inner class ManageUserHolder(private val binding: AttendanceListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        fun bindTo(user: Users) {
            binding.apply {
                tvUserName.text = user.username.toString()
            }
            binding.editList.setOnClickListener {
                val position = adapterPosition
                if (position !=RecyclerView.NO_POSITION){
                    listener.handleDeleteClicked(position)
                }
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            val user = userList[position]
            if (position !=RecyclerView.NO_POSITION){
                listener.handleItemClicked(position,user)
            }
        }

    }

    interface OnUserClickListener {
        fun handleItemClicked(position: Int,user:Users)
        fun handleDeleteClicked(position: Int)
    }


}