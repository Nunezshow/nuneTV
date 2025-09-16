package com.nunetv.iptv.fragments

import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import com.nunetv.iptv.R
import com.nunetv.iptv.adapters.EpgProgramPresenter
import com.nunetv.iptv.model.EpgProgram
import java.util.ArrayList

class EpgGridFragment : VerticalGridSupportFragment() {

    private val adapter = ArrayObjectAdapter(EpgProgramPresenter())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.live_tv)
        gridPresenter = VerticalGridPresenter().apply {
            numberOfColumns = NUM_COLUMNS
            shadowEnabled = false
        }
        setAdapter(adapter)
        val programs = arguments?.getParcelableArrayList<EpgProgram>(ARG_PROGRAMS).orEmpty()
        updatePrograms(programs)
    }

    fun updatePrograms(programs: List<EpgProgram>) {
        adapter.clear()
        programs.sortedBy { it.startTime }.forEach { adapter.add(it) }
        if (adapter.size() == 0) {
            adapter.add(
                EpgProgram(
                    channelId = "placeholder",
                    title = getString(R.string.no_channels),
                    description = getString(R.string.loading),
                    startTime = System.currentTimeMillis(),
                    endTime = System.currentTimeMillis()
                )
            )
        }
    }

    companion object {
        private const val ARG_PROGRAMS = "programs"
        private const val NUM_COLUMNS = 2

        fun newInstance(programs: List<EpgProgram>): EpgGridFragment {
            return EpgGridFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PROGRAMS, ArrayList(programs))
                }
            }
        }
    }
}
