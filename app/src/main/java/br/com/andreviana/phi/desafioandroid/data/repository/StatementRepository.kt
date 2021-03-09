package br.com.andreviana.phi.desafioandroid.data.repository

import br.com.andreviana.phi.desafioandroid.data.common.DataState
import br.com.andreviana.phi.desafioandroid.data.model.Balance
import br.com.andreviana.phi.desafioandroid.data.model.Statement
import br.com.andreviana.phi.desafioandroid.data.model.StatementDetail
import br.com.andreviana.phi.desafioandroid.data.network.source.StatementDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface StatementRepository {
    suspend fun fetchBalance(): Flow<DataState<Balance>>
    suspend fun fetchStatement(limit: String, offset: String): Flow<DataState<Statement>>
    suspend fun fetchStatementDetail(id: String): Flow<DataState<StatementDetail>>
}

class StatementRepositoryImpl @Inject constructor(
        private val statementDataSource: StatementDataSource
) : StatementRepository {
    override suspend fun fetchBalance(): Flow<DataState<Balance>> {
        return statementDataSource.getBalance()
    }

    override suspend fun fetchStatement(limit: String, offset: String): Flow<DataState<Statement>> {
        return statementDataSource.getStatement(limit, offset)
    }

    override suspend fun fetchStatementDetail(id: String): Flow<DataState<StatementDetail>> {
        return statementDataSource.getStatementDetail(id)
    }
}