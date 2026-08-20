package com.iskcon.bhagavaddarshan.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iskcon.bhagavaddarshan.data.IndianStates
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.data.SubscriptionRepository
import com.iskcon.bhagavaddarshan.payment.BookRedeemCalculator
import com.iskcon.bhagavaddarshan.util.FormValidators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RegisterFormState(
    val name: String = "",
    val houseNo: String = "",
    val street: String = "",
    val villageTown: String = "",
    val mandal: String = "",
    val district: String = "",
    val pincode: String = "",
    val state: String = IndianStates.DEFAULT,
    val phone: String = "",
    val plan: SubscriptionPlan = SubscriptionPlan.ONE_YEAR,
    /** Book sale amount for redeem mode. */
    val bookSaleAmount: String = "",
    val error: String? = null,
    val savedReceiptNo: Long? = null
)

private data class ListScope(
    val agentId: Long?,
    val isAdmin: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _scope = MutableStateFlow(ListScope(agentId = null, isAdmin = true))

    /**
     * Restrict list/search flows to an agent, or show all when [isAdmin] is true.
     */
    fun setScope(agentId: Long?, isAdmin: Boolean) {
        _scope.value = ListScope(agentId = agentId, isAdmin = isAdmin)
        viewModelScope.launch {
            val aid = if (isAdmin) null else agentId
            repository.refreshAll(agentId = aid)
            repository.refreshPending(agentId = aid)
            repository.refreshExpiring(30)
        }
    }

    fun refreshLists() {
        val scope = _scope.value
        viewModelScope.launch {
            val aid = if (scope.isAdmin) null else scope.agentId
            repository.refreshAll(agentId = aid, query = _query.value)
            repository.refreshPending(agentId = aid)
            repository.refreshExpiring(30)
        }
    }

    val subscriptions: StateFlow<List<Subscription>> =
        _scope.flatMapLatest { scope ->
            when {
                scope.isAdmin || scope.agentId == null || scope.agentId <= 0L ->
                    repository.observeAll()
                else -> repository.observeByAgent(scope.agentId)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Alias used by Customers tab. */
    val scopedSubscriptions: StateFlow<List<Subscription>> = subscriptions

    val pending: StateFlow<List<Subscription>> =
        repository.observePending()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingScoped: StateFlow<List<Subscription>> =
        _scope.flatMapLatest { scope ->
            when {
                scope.isAdmin || scope.agentId == null || scope.agentId <= 0L ->
                    repository.observePending()
                else -> repository.observePendingForAgent(scope.agentId)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _pendingTick = MutableStateFlow(0)
    fun refreshPending() {
        _pendingTick.value = _pendingTick.value + 1
        refreshLists()
    }

    val expiring: StateFlow<List<Subscription>> =
        repository.observeExpiringSoon(30)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val searchResults: StateFlow<List<Subscription>> =
        combine(_query, _scope) { q, scope -> q to scope }
            .flatMapLatest { (q, scope) ->
                when {
                    scope.isAdmin || scope.agentId == null || scope.agentId <= 0L ->
                        repository.searchAll(q)
                    else -> repository.searchForAgent(scope.agentId, q)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(RegisterFormState())
    val form = _form.asStateFlow()

    private val _selected = MutableStateFlow<Subscription?>(null)
    val selected = _selected.asStateFlow()

    fun setQuery(value: String) {
        _query.value = value
        refreshLists()
    }

    fun updateForm(transform: (RegisterFormState) -> RegisterFormState) {
        _form.value = transform(_form.value).copy(savedReceiptNo = null)
    }

    fun resetForm() {
        _form.value = RegisterFormState()
    }

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _selected.value = repository.getById(id)
        }
    }

    fun validateRegistrationForm(forBookRedeem: Boolean = false): String? {
        val f = _form.value
        FormValidators.name(f.name)?.let { return it }
        FormValidators.phone(f.phone)?.let { return it }
        FormValidators.pincode(f.pincode)?.let { return it }
        if (f.villageTown.isBlank() && f.district.isBlank()) {
            return "Enter village/town or district"
        }
        if (forBookRedeem) {
            val amt = f.bookSaleAmount.toIntOrNull() ?: 0
            if (!BookRedeemCalculator.isEligible(amt)) {
                return "Book sale must be at least ₹1000 (₹1000 = 1 month)"
            }
        }
        return null
    }

    fun saveRegistration(
        collectorName: String,
        agentId: Long,
        paymentRef: String = "",
        paymentMethod: String = "",
        paymentProofPath: String = "",
        bookSaleAmount: Int = 0,
        planMonthsOverride: Int? = null,
        source: String = Subscription.Source.COLLECTOR,
        registeredBy: String = collectorName.ifBlank { "self" },
        onSuccess: (Long) -> Unit = {}
    ) {
        val f = _form.value
        val err = validateRegistrationForm(bookSaleAmount > 0)
        if (err != null) {
            _form.value = f.copy(error = err)
            return
        }
        viewModelScope.launch {
            val id = repository.register(
                name = f.name,
                houseNo = f.houseNo,
                street = f.street,
                villageTown = f.villageTown,
                mandal = f.mandal,
                district = f.district,
                pincode = f.pincode,
                state = f.state,
                phone = f.phone,
                plan = if (bookSaleAmount > 0) null else f.plan,
                planMonths = planMonthsOverride
                    ?: if (bookSaleAmount > 0) BookRedeemCalculator.monthsForSale(bookSaleAmount)
                    else f.plan.years * 12,
                bookSaleAmount = bookSaleAmount,
                paymentRef = paymentRef,
                paymentMethod = paymentMethod,
                paymentProofPath = paymentProofPath,
                collectorName = collectorName,
                agentId = agentId,
                registeredBy = registeredBy,
                source = source
            )
            val saved = repository.getById(id)
            _form.value = f.copy(savedReceiptNo = saved?.receiptNo, error = null)
            onSuccess(id)
        }
    }

    fun saveRegistrationAfterPayment(
        paymentId: String,
        collectorName: String,
        agentId: Long,
        paymentMethod: String,
        paymentProofPath: String = "",
        bookSaleAmount: Int = 0,
        planMonthsOverride: Int? = null,
        source: String = Subscription.Source.COLLECTOR,
        registeredBy: String = collectorName.ifBlank { "self" },
        onSuccess: (Long) -> Unit = {}
    ) {
        saveRegistration(
            collectorName = collectorName,
            agentId = agentId,
            paymentRef = paymentId,
            paymentMethod = paymentMethod,
            paymentProofPath = paymentProofPath,
            bookSaleAmount = bookSaleAmount,
            planMonthsOverride = planMonthsOverride,
            source = source,
            registeredBy = registeredBy,
            onSuccess = onSuccess
        )
    }

    fun markActive(id: Long) {
        viewModelScope.launch {
            val current = repository.getById(id) ?: return@launch
            repository.update(current.copy(status = Subscription.Status.ACTIVE))
            _selected.value = repository.getById(id)
        }
    }

    fun markPaidWithRef(
        id: Long,
        paymentId: String,
        paymentMethod: String = "",
        proofPath: String = "",
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.markPaid(id, paymentId, paymentMethod, proofPath)
            _selected.value = repository.getById(id)
            onDone()
        }
    }

    fun delete(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.delete(id)
            onDone()
        }
    }
}

class SubscriptionViewModelFactory(
    private val repository: SubscriptionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
            return SubscriptionViewModel(repository) as T
        }
        error("Unknown ViewModel: ${modelClass.name}")
    }
}
