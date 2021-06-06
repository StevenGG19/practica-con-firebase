package com.platzi.android.firestore.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.R
import com.platzi.android.firestore.adapter.CryptosAdapter
import com.platzi.android.firestore.adapter.CryptosAdapterListener
import com.platzi.android.firestore.databinding.ActivityTraderBinding
import com.platzi.android.firestore.databinding.CoinInfoBinding
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User
import com.platzi.android.firestore.network.Callback
import com.platzi.android.firestore.network.FirestoreService
import com.platzi.android.firestore.network.RealtimeDataListener
import java.lang.Exception


/**
 * @author Santiago Carrillo
 * 2/14/19.
 */
class TraderActivity : AppCompatActivity(), CryptosAdapterListener {
    private lateinit var firestoreService: FirestoreService
    private lateinit var bin: ActivityTraderBinding
    private val cryptosAdapter = CryptosAdapter(this)
    private var username: String? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bin = ActivityTraderBinding.inflate(layoutInflater)
        setContentView(bin.root)

        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
        username = intent.extras!![USERNAME_KEY].toString()
        bin.usernameTextView.text = username

        configureRecyclerView()
        loadCryptos()

        bin.fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.generating_new_cryptos), Snackbar.LENGTH_SHORT)
                .setAction("Info", null).show()
            generateCryptoCurrenciesRandom()
        }

    }

    private fun generateCryptoCurrenciesRandom() {
        for(crypto in cryptosAdapter.cryptosList) {
            val amount = (1..10).random()
            crypto.available += amount
            firestoreService.updateCrytos(crypto)
        }
    }

    private fun configureRecyclerView() {
        bin.recyclerView.setHasFixedSize(true)
        bin.recyclerView.layoutManager = LinearLayoutManager(this)
        bin.recyclerView.adapter = cryptosAdapter
    }

    private fun loadCryptos() {
        firestoreService.getCryptos(object : Callback<List<Crypto>> {
            override fun onSuccess(cryptosList: List<Crypto>?) {
                firestoreService.findUserById(username!!, object : Callback<User>{
                    override fun onSuccess(result: User?) {
                        user = result
                        if (user!!.cryptoList == null) {
                            val userCryptos = mutableListOf<Crypto>()
                            for (crypto in cryptosList!!) {
                                val cryptoUser = Crypto()
                                cryptoUser.name = crypto.name
                                cryptoUser.available = crypto.available
                                cryptoUser.imageUrl = crypto.imageUrl
                                userCryptos.add(cryptoUser)
                            }
                            user!!.cryptoList = userCryptos
                            firestoreService.updateUser(user!!, null)
                        }
                        loadUserCryptos()
                        addRealtimeDatabaseListeners(user!!, cryptosList!!)
                    }

                    override fun onFailed(exception: Exception) {
                        showGeneralServerErrorMessage()
                    }

                })

                this@TraderActivity.runOnUiThread {
                    cryptosAdapter.cryptosList = cryptosList!!
                    cryptosAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailed(exception: Exception) {
                Log.e("TraderActivity", "Error", exception)
                showGeneralServerErrorMessage()
            }

        })
    }

    private fun addRealtimeDatabaseListeners(user: User, cryptosList: List<Crypto>) {
        firestoreService.listenForUpdates(user, object : RealtimeDataListener<User>{
            override fun onDataChange(updatedData: User) {
                this@TraderActivity.user = updatedData
                loadUserCryptos()
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }

        })

        firestoreService.listenForUpdates(cryptosList, object : RealtimeDataListener<Crypto>{
            override fun onDataChange(updatedData: Crypto) {
                var pos = 0
                for (crypto in cryptosAdapter.cryptosList) {
                    if (crypto.name == updatedData.name) {
                        crypto.available = updatedData.available
                        cryptosAdapter.notifyItemChanged(pos)
                    }
                    pos++
                }
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }

        })
    }

    private fun loadUserCryptos() {
       runOnUiThread {
           if (user != null && user!!.cryptoList != null) {
                bin.infoPanel.removeAllViews()
               for (crypto in user!!.cryptoList!!) {
                    addUserCrytoInfRow(crypto)
               }
           }
       }
    }

    private fun addUserCrytoInfRow(crypto: Crypto) {
        val view = LayoutInflater.from(this).inflate(R.layout.coin_info, bin.infoPanel, false)
        val binding = CoinInfoBinding.bind(view)
        binding.coinLabel.text = getString(R.string.coin_info, crypto.name, crypto.available.toString())
        Glide.with(this).load(crypto.imageUrl).into(binding.coinIcon)
        bin.infoPanel.addView(view)
    }

    fun showGeneralServerErrorMessage() {
        Snackbar.make(bin.fab, getString(R.string.error_while_connecting_to_the_server), Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    override fun onBuyCryptoClicked(crypto: Crypto) {
        if(crypto.available > 0) {
            for (cryptos in user!!.cryptoList!!) {
                if (cryptos.name == crypto.name) {
                    cryptos.available += 1
                    break
                }
            }
            crypto.available--
        }
        firestoreService.updateUser(user!!, null)
        firestoreService.updateCrytos(crypto)
    }
}