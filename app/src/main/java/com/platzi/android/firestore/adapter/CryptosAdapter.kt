package com.platzi.android.firestore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.platzi.android.firestore.R
import com.platzi.android.firestore.databinding.CryptoRowBinding
import com.platzi.android.firestore.model.Crypto

class CryptosAdapter(var cryptosAdapterListener: CryptosAdapterListener) :
    RecyclerView.Adapter<CryptosAdapter.ViewHolder>() {
    var cryptosList: List<Crypto> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.crypto_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val crypto = cryptosList[position]
        Glide.with(holder.itemView.context)
            .load(crypto.imageUrl)
            .into(holder.bin.image)
        holder.bin.nameTextView.text = crypto.name
        holder.bin.availableTextView.text = holder.itemView.context.getString(R.string.available_message, crypto.available.toString())
        holder.bin.buyButton.setOnClickListener {
            cryptosAdapterListener.onBuyCryptoClicked(crypto)
        }
    }

    override fun getItemCount() = cryptosList.size

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var bin = CryptoRowBinding.bind(view)
    }

}