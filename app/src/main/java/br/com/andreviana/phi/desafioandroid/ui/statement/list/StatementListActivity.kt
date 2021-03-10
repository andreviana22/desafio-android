package br.com.andreviana.phi.desafioandroid.ui.statement.list

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.andreviana.phi.desafioandroid.R
import br.com.andreviana.phi.desafioandroid.data.common.Constants
import br.com.andreviana.phi.desafioandroid.data.common.DataState
import br.com.andreviana.phi.desafioandroid.databinding.ActivityStatementListBinding
import br.com.andreviana.phi.desafioandroid.util.helper.PreferencesHelper
import br.com.andreviana.phi.desafioandroid.util.ktx.convertCentsToReal
import br.com.andreviana.phi.desafioandroid.util.ktx.moneyFormat
import br.com.andreviana.phi.desafioandroid.util.ktx.navigationToStatementDetail
import br.com.andreviana.phi.desafioandroid.util.ktx.showToastLong
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class StatementListActivity : AppCompatActivity(), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    private val viewModel: StatementViewModel by viewModels()
    private val binding by lazy { ActivityStatementListBinding.inflate(layoutInflater) }
    private val preferences: PreferencesHelper by lazy { PreferencesHelper(applicationContext) }

    private val adapter: StatementAdapter by lazy { StatementAdapter() }
    private var mCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onRefresh()
        getMyBalance()
        setupUI()
    }

    private fun setupUI() {
        binding.swipeRefreshMoves.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.teal_custom_300
            )
        )
        binding.imageViewHideBalance.setOnClickListener(this)
        binding.imageViewShowBalance.setOnClickListener(this)
        binding.swipeRefreshMoves.setOnRefreshListener(this)
        binding.recyclerViewMoves.adapter = adapter
        checkVisibilityBalance()
        checkUiNightMode()
        binding.recyclerViewMoves.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (recyclerView.canScrollVertically(-1)) {
                    val scrollState = binding.recyclerViewMoves.scrollState
                    Timber.tag(TAG).i("SCROLL STATE: $scrollState ")
                }
            }
        })

        adapter.runOnItemClickListener { statementId ->
            navigationToStatementDetail(statementId)
        }
    }

    private fun checkVisibilityBalance() {
        if (preferences.getVisibilityBalance()) showBalance()
        else hideBalance()
    }

    private fun checkUiNightMode() {
        val mode =
            binding.root.context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        if (mode == Configuration.UI_MODE_NIGHT_YES) {
            binding.cardViewBalance.setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.grey_custom_900
                )
            )
        }
    }

    private fun showBalance() {
        preferences.setVisibilityBalance(true)
        binding.imageViewHideBalance.visibility = View.VISIBLE
        binding.textViewBalanceValue.visibility = View.VISIBLE
        binding.imageViewShowBalance.visibility = View.GONE
        binding.viewLine.visibility = View.GONE
    }

    private fun hideBalance() {
        preferences.setVisibilityBalance(false)
        binding.imageViewShowBalance.visibility = View.VISIBLE
        binding.viewLine.visibility = View.VISIBLE
        binding.imageViewHideBalance.visibility = View.GONE
        binding.textViewBalanceValue.visibility = View.GONE

    }

    private fun getMyStatement(limit: String, offset: String) {
        viewModel.getStatement(limit, offset).observe(this, { dataState ->
            when (dataState) {
                is DataState.Success -> {
                    adapter.addStatementItems(dataState.data.toHashSet())
                    //jobStartRequisition()
                }
                is DataState.Failure -> showToastLong(dataState.message)
                is DataState.Error -> {
                    showToastLong(Constants.GENERIC_ERROR)
                    Timber.e(dataState.throwable)
                }
            }
        })
    }

    private fun jobStartRequisition() {
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            delay(10000L)
            ++mCounter
            getMyStatement("10", mCounter.toString())
        }
    }

    private fun getMyBalance() {
        viewModel.getBalance().observe(this, { dataState ->
            when (dataState) {
                is DataState.Loading -> showProgress()
                is DataState.Success -> {
                    hideProgress()
                    binding.textViewBalanceValue.text =
                        convertCentsToReal(dataState.data.amount).moneyFormat()
                }

                is DataState.Failure -> {
                    hideProgress()
                    showToastLong(dataState.message)
                }
                is DataState.Error -> showToastLong(Constants.GENERIC_ERROR)

            }
        })
    }

    private fun showProgress() {
        binding.swipeRefreshMoves.isRefreshing = true
    }

    private fun hideProgress() {
        binding.swipeRefreshMoves.isRefreshing = false
    }


    override fun onRefresh() {
        getMyStatement("10", mCounter.toString())
        Timber.tag(TAG).i("ON_REFRESH")
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imageViewHideBalance -> hideBalance()
            R.id.imageViewShowBalance -> showBalance()
        }
    }

    companion object {
        private const val TAG = "StatementListActivity"
    }

}