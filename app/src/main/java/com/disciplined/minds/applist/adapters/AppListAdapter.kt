package com.disciplined.minds.applist.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.disciplined.minds.R
import com.disciplined.minds.applist.AppInfo


class AppListAdapter(private val installApp: List<AppInfo>, var itemEditListener: (Int) -> Unit) : RecyclerView.Adapter<AppListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(installApp[position], itemEditListener)
    }

    override fun getItemCount(): Int {
        return installApp.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(appInfo: AppInfo, itemEditListener: (Int) -> Unit) {
            val tvAppName = itemView.findViewById<AppCompatTextView>(R.id.tvAppName) as AppCompatTextView
            val ivAppIcon = itemView.findViewById<AppCompatImageView>(R.id.ivAppIcon) as AppCompatImageView
            val ivLock = itemView.findViewById<AppCompatImageView>(R.id.ivLock) as AppCompatImageView

            tvAppName.text = appInfo.applicationName
            ivAppIcon.setImageDrawable(appInfo.applicationIcon)

            if (appInfo.isOpen!!) {
                ivLock.setImageResource(R.drawable.ic_unlock)
            } else {
                ivLock.setImageResource(R.drawable.ic_lock)
            }

            ivLock.setOnClickListener {
                itemEditListener(adapterPosition)
            }
        }

    }
}