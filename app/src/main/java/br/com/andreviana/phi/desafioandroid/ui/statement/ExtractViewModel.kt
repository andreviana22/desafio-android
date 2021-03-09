package br.com.andreviana.phi.desafioandroid.ui.statement

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import br.com.andreviana.phi.desafioandroid.data.repository.StatementRepository

class ExtractViewModel
@ViewModelInject constructor(
        private val statementRepository: StatementRepository
) : ViewModel() {

    fun getBalance() = liveData {
        emitSource(statementRepository.fetchBalance().asLiveData())
    }

    fun getStatement(limit: String, offset: String) = liveData {
        emitSource(statementRepository.fetchStatement(limit, offset).asLiveData())
    }
}