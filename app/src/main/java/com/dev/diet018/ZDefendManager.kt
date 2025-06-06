package com.dev.diet018

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.zimperium.api.v5.ZDefend
import com.zimperium.api.v5.ZDefendDeveloper
import com.zimperium.api.v5.ZDefendThreat
import com.zimperium.api.v5.ZDefendTroubleshoot
import com.zimperium.api.v5.ZDefendTroubleshoot.TroubleshootDetailsCallback
import com.zimperium.api.v5.ZDefendTroubleshoot.ZLogCallback
import com.zimperium.api.v5.ZDeviceStatus
import com.zimperium.api.v5.ZDeviceStatusCallback
import com.zimperium.api.v5.ZDeviceStatusRegistration
import com.zimperium.api.v5.ZLinkedFunctionEvent
import com.zimperium.api.v5.ZLinkedFunctionRegistrationV2
import com.zimperium.api.v5.ZLoginStatus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class ThreatModel(
    val id: String,
    val name: String,
    val severity: String,
    val status: Boolean,
    val description: String,
    val resolution: String
)

data class PolicyModel(
    val id: String,
    val name: String,
    val type: String,
    val downloadDate: String
)

data class LinkedModel(
    val id: String,
    val label: String,
    var eventType: String,
    var threats: List<ThreatModel>
)

@SuppressLint("MutableCollectionMutableState")
class ZDefendManager : ZDeviceStatusCallback, ZLogCallback, TroubleshootDetailsCallback {
    var threats = mutableStateListOf<ThreatModel>()
    var policies = mutableStateListOf<PolicyModel>()
    var troubleshootLogs = mutableStateOf("")
    var troubleshootDetails = mutableStateOf("")
    var auditLogs = mutableStateListOf<String>()
    var linkedObjects = mutableStateListOf<LinkedModel>()
    var isLoaded = mutableStateOf(false)
    var percentage = mutableIntStateOf(0)

    companion object {
        var shared = ZDefendManager()
    }

    private var lastDeviceStatus: ZDeviceStatus? = null
    private var deviceStatusRegistration: ZDeviceStatusRegistration? = null
    private var registeredFunctions: ArrayList<ZLinkedFunctionRegistrationV2> = ArrayList()

    fun getThreats() {
        val threats = lastDeviceStatus?.allThreats
        this.threats.clear()

        if (threats != null) {
            for (threat in threats) {
                addThreat(threat)
            }
        }

        auditLogs.add("ZDefendManager - getThreats()")
    }

    fun getPolicies() {
        val policies = lastDeviceStatus?.policies
        this.policies.clear()

        if (policies != null) {
            for (policy in policies) {
                this.policies.add(PolicyModel(
                    policy.policyHash,
                    policy.policyName,
                    policy.policyType.toString(),
                    policy.downloadDate.toString())
                )
            }
        }

        auditLogs.add("ZDefendManager - getPolicies()")
    }

    fun initializeZDefendApi() {
        deviceStatusRegistration = ZDefend.addDeviceStatusCallback(this)
        ZDefendTroubleshoot.getZLog(this)
        ZDefendTroubleshoot.getTroubleshootDetails(this)
        auditLogs.add("ZDefendManager - initializeZDefendApi()")
    }

    fun deregisterZDefendApi() {
        deviceStatusRegistration?.deregister()
        auditLogs.add("ZDefendManager - deregisterZDefendApi()")
    }

    fun checkForUpdates() {
        ZDefend.checkForUpdates()
        auditLogs.add("ZDefendManager - checkForUpdates()")
    }

    fun registerLinkedFunction(input: String) {
        registeredFunctions.add(ZDefend.registerLinkedFunction(input, ::onLinkedFunction, ::onMitigateFunction))
        linkedObjects.add(LinkedModel(
            input,
            input,
            "",
            ArrayList()
        ))

        auditLogs.add("ZDefendManager - registerLinkedFunction($input)")
    }

    fun deregisterAllLinkedFunction() {
        for (function in registeredFunctions) {
            function.deregister()
        }

        registeredFunctions.clear()
        linkedObjects.clear()
        auditLogs.add("ZDefendManager - deregisterAllLinkedFunction()")
    }

    private fun onLinkedFunction(event: ZLinkedFunctionEvent) {
        val threats = event.relatedThreats.map { threat ->
            ThreatModel(
                threat.uuid,
                threat.localizedName,
                threat.severity.toString(),
                threat.isMitigated,
                threat.localizedDetails,
                threat.localizedResolution)
        }

        val linked = linkedObjects.firstOrNull { link -> link.id == event.label }
        linked?.eventType = event.eventType.toString()
        linked?.threats = threats

        auditLogs.add("ZDefendManager - onLinkedFunction(${event.label})")
    }

    private fun onMitigateFunction(event: ZLinkedFunctionEvent) {
        auditLogs.add("ZDefendManager - onMitigateFunction(${event.label})")
    }

    private fun addThreat(threat: ZDefendThreat) {
        threats.add(ThreatModel(
            threat.uuid,
            threat.localizedName,
            threat.severity.name,
            threat.isMitigated,
            threat.localizedDetails,
            threat.localizedResolution))
    }

    override fun onDeviceStatus(deviceStatus: ZDeviceStatus) {
        val logBuilder = StringBuilder()
        logBuilder.append("OnDeviceStatus: ").append(deviceStatus.loginStatus.name)
        logBuilder.append("\nDatetime: ").append(deviceStatus.statusDate)

        if (deviceStatus.loginStatus == ZLoginStatus.LOGGED_IN) {
            percentage.intValue = deviceStatus.initialScanProgressPercentage
            if (deviceStatus.initialScanProgressPercentage >= 100) {
                isLoaded.value = true
            }
        } else {
            logBuilder.append("\nLast login error: ").append(deviceStatus.loginLastError.name)
        }

        lastDeviceStatus = deviceStatus
        auditLogs.add("ZDefendManager - onDeviceStatus: $logBuilder")
    }

    override fun onZLog(message: String) {
        this.troubleshootLogs.value = "onZLog(): $message"
        auditLogs.add("ZDefendManager - onZLog()")
    }

    override fun onTroubleshootDetails(messages: JSONArray?) {
        val logBuilder = java.lang.StringBuilder()
        logBuilder.append("onTroubleshootDetails(): ")

        try {
            if (messages != null) {
                for (idx in 0 until messages.length()) {
                    val entry: JSONObject = messages.getJSONObject(idx)
                    if (entry["val"] is String) {
                        var value = entry.getString("val")
                        if (value.length > 250) value = value.substring(0, 20) + "..."
                        logBuilder.append("\n    ").append(entry["key"]).append(" : ").append(value)
                    }
                }
            }
        } catch (e: JSONException) {
            logBuilder.append("\n    exception: ").append(e)
        }

        this.troubleshootDetails.value = logBuilder.toString()
        auditLogs.add("ZDefendManager - onTroubleshootDetails()")
    }

    fun simulateThreats(input: Int) {
        ZDefendDeveloper.simulateTestThreat(input, null)
        auditLogs.add("ZDefendManager - simulateThreats($input)")
    }

    fun mitigateThreats() {
        ZDefendDeveloper.mitigateSimulatedThreats()
        auditLogs.add("ZDefendManager - mitigateThreats()")
    }
}