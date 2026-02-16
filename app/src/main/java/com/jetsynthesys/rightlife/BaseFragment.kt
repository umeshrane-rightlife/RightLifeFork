package com.jetsynthesys.rightlife

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.databinding.FragmentBaseBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils.showCustomToast
import com.jetsynthesys.rightlife.ui.utility.Utils.showNewDesignToast
import java.io.IOException

open class BaseFragment : Fragment() {
    // Use a nullable backing property for the binding
    private var _binding: FragmentBaseBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    protected val baseBinding get() = _binding!!

    lateinit var sharedPreferenceManager: SharedPreferenceManager
    lateinit var apiService: ApiService
    lateinit var apiServiceFastApi: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize things that don't require a View, e.g., managers, services
        sharedPreferenceManager = SharedPreferenceManager.getInstance(requireContext())
        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        apiServiceFastApi = ApiClient.getAIClient().create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBaseBinding.inflate(inflater, container, false)
        return baseBinding.root
    }

    fun handleNoInternetView(e: Throwable) {
        if (!isAdded || context == null) return
        requireActivity().runOnUiThread {
            when (e) {
                is java.net.SocketTimeoutException -> Log.e("Error", e.message ?: "Timeout")
                is IOException -> e.message?.let { showCustomToast(requireActivity(),it) }
                else -> e.message?.let { showCustomToast(requireActivity(),it) }
            }
        }
    }

    // IMPORTANT: Clear the binding reference in onDestroyView to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}