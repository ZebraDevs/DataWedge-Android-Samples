package com.zebra.sample.multifragmentsample1.data.state

import com.zebra.sample.multifragmentsample1.data.models.DWScanner
import com.zebra.sample.multifragmentsample1.data.models.DWScannerSwitch
import com.zebra.sample.multifragmentsample1.data.models.DWOutputData
import com.zebra.sample.multifragmentsample1.data.models.DWProfileCreate
import com.zebra.sample.multifragmentsample1.data.models.DWProfileUpdate
import com.zebra.sample.multifragmentsample1.data.models.DWScannerState
import com.zebra.sample.multifragmentsample1.data.models.DWStatus

data class ScanViewState(
    val dwStatus: DWStatus?=null,
    val dwOutputData: DWOutputData?=null,
    val dwProfileCreate: DWProfileCreate?=null,
    val dwProfileUpdate: DWProfileUpdate?=null,
    val dwScannerState: DWScannerState?=null,
    val swScannerList: List<DWScanner>?=null,
    val dwScannerSwitch: DWScannerSwitch?=null,
)