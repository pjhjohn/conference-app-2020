package io.github.droidkaigi.confsched2020.session.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerFragment
import io.github.droidkaigi.confsched2020.di.PageScope
import io.github.droidkaigi.confsched2020.ext.assistedActivityViewModels
import io.github.droidkaigi.confsched2020.model.SessionPage
import io.github.droidkaigi.confsched2020.session.R
import io.github.droidkaigi.confsched2020.session.databinding.FragmentMainSessionsBinding
import io.github.droidkaigi.confsched2020.session.ui.MainSessionsFragmentDirections.actionSessionToSearchSessions
import io.github.droidkaigi.confsched2020.session.ui.item.SessionItem
import io.github.droidkaigi.confsched2020.session.ui.viewmodel.SessionsViewModel
import io.github.droidkaigi.confsched2020.system.ui.viewmodel.SystemViewModel
import io.github.droidkaigi.confsched2020.util.ProgressTimeLatch
import javax.inject.Inject
import javax.inject.Provider

class MainSessionsFragment : DaggerFragment() {

    private lateinit var binding: FragmentMainSessionsBinding

    @Inject
    lateinit var sessionsViewModelProvider: Provider<SessionsViewModel>
    private val sessionsViewModel: SessionsViewModel by assistedActivityViewModels {
        sessionsViewModelProvider.get()
    }
    @Inject
    lateinit var systemViewModelProvider: Provider<SystemViewModel>
    private val systemViewModel: SystemViewModel by assistedActivityViewModels {
        systemViewModelProvider.get()
    }

    @Inject
    lateinit var sessionItemFactory: SessionItem.Factory

    private lateinit var progressTimeLatch: ProgressTimeLatch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main_sessions,
            container,
            false
        )
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSessionPager()
    }

    private fun setupSessionPager() {
        val tabLayoutMediator = TabLayoutMediator(
            binding.sessionsTabLayout,
            binding.sessionsViewpager
        ) { tab, position ->
            tab.text = SessionPage.pages[position].title
        }
        // TODO: apply margin design
//        binding.sessionsViewpager.pageMargin =
//            resources.getDimensionPixelSize(R.dimen.session_pager_horizontal_padding)
        progressTimeLatch = ProgressTimeLatch { showProgress ->
            binding.sessionsProgressBar.isVisible = showProgress
        }.apply {
            loading = true
        }
        sessionsViewModel.uiModel.observe(viewLifecycleOwner) { uiModel: SessionsViewModel.UiModel ->
            progressTimeLatch.loading = uiModel.isLoading
        }
        binding.sessionsViewpager.adapter = object : FragmentStateAdapter(
            this
        ) {
            override fun getItemCount(): Int = SessionPage.pages.size

            override fun createFragment(position: Int): Fragment {
               return SessionsFragment.newInstance(
                    SessionsFragmentArgs
                        .Builder(position)
                        .build()
                )
            }
        }

        binding.sessionsTabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                    tab?.let {
                        //                        sessionPagesActionCreator.reselectTab(SessionPage.pages[it.position])
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) { // no-op
                }

                override fun onTabSelected(tab: TabLayout.Tab?) { // no-op
                }
            })

        // TODO: implement currentItem logic
//        val jstNow = DateTime.now().toOffset(9.hours)
//        if (jstNow.yearInt == 2019 && jstNow.month1 == 2 && jstNow.dayOfMonth == 8) {
//            binding.sessionsViewpager.currentItem = 1
//        }
        tabLayoutMediator.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sessions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.session_search -> {
                findNavController().navigate(actionSessionToSearchSessions())
                return false
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

@Module
abstract class MainSessionsFragmentModule {
    @ContributesAndroidInjector(modules = [SessionsFragmentModule::class])
    abstract fun contributeSessionPageFragment(): SessionsFragment

    @Module
    companion object {
        @PageScope
        @JvmStatic
        @Provides
        fun providesLifecycleOwnerLiveData(
            mainSessionsFragment: MainSessionsFragment
        ): LiveData<LifecycleOwner> {
            return mainSessionsFragment.viewLifecycleOwnerLiveData
        }
    }
}
