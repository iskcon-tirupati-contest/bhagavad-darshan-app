package com.iskcon.bhagavaddarshan.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.data.SubscriptionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val state: String = "Andhra Pradesh",
    val phone: String = "",
    val plan: SubscriptionPlan = SubscriptionPlan.ONE_YEAR,
    val paymentRef: String = "",
    val collectorName: String = "",
    val notes: String = "",
    val error: String? = null,
    val savedReceiptNo: Long? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    val subscriptions: StateFlow<List<Subscription>> =
        repository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expiring: StateFlow<List<Subscription>> =
        repository.observeExpiringSoon(30)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val searchResults: StateFlow<List<Subscription>> =
        _query.flatMapLatest { repository.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(RegisterFormState())
    val form = _form.asStateFlow()

    private val _selected = MutableStateFlow<Subscription?>(null)
    val selected = _selected.asStateFlow()

    fun setQuery(value: String) {
        _query.value = value
    }

    fun updateForm(transform: (RegisterFormState) -> RegisterFormState) {
        _form.value = transform(_form.value).copy(error = null, savedReceiptNo = null)
    }

    fun resetForm() {
        _form.value = RegisterFormState()
    }

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _selected.value = repository.getById(id)
        }
    }

    fun saveRegistration(onSuccess: (Long) -> Unit) {
        val f = _form.value
        val error = validate(f)
        if (error != null) {
            _form.value = f.copy(error = error)
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
                plan = f.plan,
                paymentRef = f.paymentRef,
                collectorName = f.collectorName,
                notes = f.notes
            )
            val saved = repository.getById(id)
            _form.value = f.copy(savedReceiptNo = saved?.receiptNo, error = null)
            onSuccess(id)
        }
    }

    fun markActive(id: Long) {
        viewModelScope.launch {
            val current = repository.getById(id) ?: return@launch
            repository.update(current.copy(status = Subscription.Status.ACTIVE))
            _selected.value = repository.getById(id)
        }
    }

    fun delete(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.delete(id)
            onDone()
        }
    }

    private fun validate(f: RegisterFormState): String? {
        if (f.name.isBlank()) return "Name is required"
        if (f.phone.length < 10) return "Enter a valid 10-digit phone"
        if (f.pincode.isNotBlank() && f.pincode.length != 6) return "Pincode must be 6 digits"
        if (f.villageTown.isBlank() && f.district.isBlank()) {
            return "Enter village/town or district"
        }
        return null
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
