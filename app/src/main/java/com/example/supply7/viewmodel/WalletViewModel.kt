package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.Card
import com.example.supply7.data.WalletRepository
import kotlinx.coroutines.launch

class WalletViewModel : ViewModel() {
    private val repository = WalletRepository()

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    private val _cards = MutableLiveData<List<Card>>()
    val cards: LiveData<List<Card>> = _cards

    fun loadWalletData() {
        viewModelScope.launch {
            _balance.value = repository.getWalletBalance()
            _cards.value = repository.getCards()
        }
    }

    fun addMoney(amount: Double) {
        viewModelScope.launch {
            repository.addBalance(amount)
            _balance.value = repository.getWalletBalance()
        }
    }

    fun addCard(card: Card) {
        viewModelScope.launch {
            repository.addCard(card)
            _cards.value = repository.getCards()
        }
    }
}
