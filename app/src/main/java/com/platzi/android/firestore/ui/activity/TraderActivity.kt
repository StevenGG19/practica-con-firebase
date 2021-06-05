package com.platzi.android.firestore.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.R
import com.platzi.android.firestore.adapter.CryptosAdapter
import com.platzi.android.firestore.adapter.CryptosAdapterListener
import com.platzi.android.firestore.databinding.ActivityTraderBinding
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.network.Callback
import com.platzi.android.firestore.network.FirestoreService
import java.lang.Exception


/**
 * @author Santiago Carrillo
 * 2/14/19.
 */
class TraderActivity : AppCompatActivity(), CryptosAdapterListener {
    private lateinit var firestoreService: FirestoreService
    private lateinit var bin: ActivityTraderBinding
    private val cryptosAdapter = CryptosAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bin = ActivityTraderBinding.inflate(layoutInflater)
        setContentView(bin.root)

        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
        configureRecyclerView()
        loadCryptos()

        bin.fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.generating_new_cryptos), Snackbar.LENGTH_SHORT)
                .setAction("Info", null).show()
        }

    }

    private fun configureRecyclerView() {
        bin.recyclerView.setHasFixedSize(true)
        bin.recyclerView.layoutManager = LinearLayoutManager(this)
        bin.recyclerView.adapter = cryptosAdapter
    }

    private fun loadCryptos() {
        firestoreService.getCryptos(object : Callback<List<Crypto>> {
            override fun onSuccess(result: List<Crypto>?) {
                this@TraderActivity.runOnUiThread {
                    cryptosAdapter.cryptosList = result!!
                    cryptosAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailed(exception: Exception) {
            }

        })
    }

    fun showGeneralServerErrorMessage() {
        Snackbar.make(bin.fab, getString(R.string.error_while_connecting_to_the_server), Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    override fun onBuyCryptoClicked(crypto: Crypto) {
    }
}