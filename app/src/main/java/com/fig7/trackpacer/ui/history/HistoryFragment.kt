package com.fig7.trackpacer.ui.history

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fig7.trackpacer.R
import com.fig7.trackpacer.PastActivity
import com.fig7.trackpacer.data.HistoryModel
import com.fig7.trackpacer.databinding.FragmentHistoryBinding
import com.fig7.trackpacer.data.ResultData
import com.fig7.trackpacer.dialog.DeleteDialog
import java.text.DateFormat

class HistoryFragment: Fragment () {
    private var binding: FragmentHistoryBinding? = null
    private val historyModel: HistoryModel by activityViewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val historyView = binding!!

        val historyList    = historyView.historyList
        val historyManager = historyModel.historyManager

        historyList.setContent {
            Column (modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)) {
                Divider(color = Color.Black)

                LazyColumn {
                    itemsIndexed(historyManager.historyList) { resultIndex, resultData ->
                        val df = DateFormat.getDateInstance(DateFormat.SHORT)
                        val shortRunDate = df.format(resultData.runDate)

                        Row(modifier = Modifier
                            .combinedClickable(onClick = { launchHistoryActivity(resultData) }, onLongClick = { deleteHistory(resultIndex) })
                            .padding(horizontal = 1.dp, vertical = 16.dp)) {

                            Text(text = resultData.runDist, fontSize = 16.sp,
                                 modifier = Modifier
                                     .weight(0.25F, true)
                                     .align(Alignment.CenterVertically))

                            Text(text = shortRunDate, fontSize = 14.sp, textAlign = TextAlign.Center,
                                 modifier = Modifier
                                     .weight(0.25F, true)
                                     .align(Alignment.CenterVertically))

                            Text(text = resultData.actualTimeStr, fontSize = 14.sp, textAlign = TextAlign.Center,
                                 modifier = Modifier
                                     .weight(0.25F, true)
                                     .align(Alignment.CenterVertically))

                            Text(text = resultData.actualPaceStr + "/km", fontSize = 14.sp, textAlign = TextAlign.Center,
                                 modifier = Modifier
                                     .weight(0.25F, true)
                                     .align(Alignment.CenterVertically))

                            Text(text = " > ", fontSize = 14.sp)
                        }

                        Divider(color = Color.Black)
                    }
                }
            }
        }

        return historyView.root
    }

    private fun launchHistoryActivity(resultData: ResultData) {
        val resultBundle = Bundle()
        resultBundle.putParcelable("resultParcel", resultData)

        val intent = Intent(requireContext(), PastActivity::class.java)
        intent.putExtras(resultBundle)

        startActivity(intent)

    }

    private fun deleteHistory(resultIndex: Int) {
        val dialog = DeleteDialog.newDialog("Delete pacing", "Are you sure you want to delete this run?", "DELETE_HISTORY_DIALOG")
        dialog.show(childFragmentManager, "DELETE_HISTORY_DIALOG")

        childFragmentManager.setFragmentResultListener("DELETE_HISTORY_DIALOG", this) { _: String, bundle: Bundle ->
            val resultVal = bundle.getBoolean("DeleteResult")
            if(!resultVal) {
                return@setFragmentResultListener
            }

            val historyManager = historyModel.historyManager
            historyManager.deleteHistory(resultIndex)
        }
    }

    override fun onResume() {
        super.onResume()

        val historyView = binding!!
        val context = requireContext()

        val phoneIcon = historyView.historyPhoneStatus
        val phonePermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        phoneIcon.setImageDrawable(AppCompatResources.getDrawable(context, if (phonePermission) R.drawable.baseline_phone_20 else R.drawable.baseline_phone_locked_20))

        val delaySetting = historyView.historyDelaySetting
        delaySetting.setText(R.string.start_delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
