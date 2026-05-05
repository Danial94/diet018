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
    var troubleshootDetailsList = mutableStateListOf<Pair<String, String>>()
    var auditLogs = mutableStateListOf<String>()
    var statusLogs = mutableStateListOf<String>()
    var linkedObjects = mutableStateListOf<LinkedModel>()
    var isLoaded = mutableStateOf(false)
    var percentage = mutableIntStateOf(0)
    var deviceRisk   = mutableStateOf("UNKNOWN")   // computed from active threat severities
    var sdkVersion   = mutableStateOf("—")
    var lastScanTime = mutableStateOf("—")

    var showAlert = mutableStateOf(false)
    // All threats currently detected via the "alert" linked-function label.
    // Threats are added on detection and removed on mitigation.
    var alertThreats = mutableStateListOf<ThreatModel>()

    companion object {
        var shared = ZDefendManager()
    }

    private val sessionId = java.util.UUID.randomUUID().toString().take(8)
    private var lastDeviceStatus: ZDeviceStatus? = null
    private var deviceStatusRegistration: ZDeviceStatusRegistration? = null
    // Map label → registration so individual deregister is possible
    private var registeredFunctions: LinkedHashMap<String, ZLinkedFunctionRegistrationV2> = LinkedHashMap()
    // All Compose-state mutations from SDK callbacks must run on the main thread to
    // prevent race conditions when the SDK fires callbacks from a background thread.
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    fun getThreats() {
        threats.clear()
        lastDeviceStatus?.allThreats?.forEach { addThreat(it) }
        auditLogs.add("ZDefendManager - getThreats()")
    }

    fun addThreat(threat: ZDefendThreat) {
        threats.add(ThreatModel(
            threat.uuid,
            threat.localizedName,
            threat.severity.name,
            threat.isMitigated,
            threat.localizedDetails,
            threat.localizedResolution))
    }

    fun getPolicies() {
        policies.clear()
        lastDeviceStatus?.policies?.forEach { p ->
            policies.add(PolicyModel(p.policyHash, p.policyName, p.policyType.toString(), p.downloadDate.toString()))
        }

        auditLogs.add("ZDefendManager - getPolicies()")
    }

    fun initializeZDefendApi() {
        deviceStatusRegistration = ZDefend.addDeviceStatusCallback(this)
        ZDefendTroubleshoot.getZLog(this)
        ZDefendTroubleshoot.getTroubleshootDetails(this)
        auditLogs.add("ZDefendManager - initializeZDefendApi()")
    }

    fun preregisterLinkedFunction() {
        deregisterAllLinkedFunction()
        registerLinkedFunction("readonly")
        registerLinkedFunction("malware")
        registerLinkedFunction("disabled")
        registerLinkedFunction("device")
        registerLinkedFunction("alert")
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
        registeredFunctions[input] = ZDefend.registerLinkedFunction(input, ::onLinkedFunction, ::onMitigateFunction)
        if (linkedObjects.none { it.id == input }) {
            linkedObjects.add(LinkedModel(input, input, "", ArrayList()))
        }
        auditLogs.add("ZDefendManager - registerLinkedFunction($input)")
    }

    fun deregisterLinkedFunction(label: String) {
        registeredFunctions[label]?.deregister()
        registeredFunctions.remove(label)
        linkedObjects.removeAll { it.id == label }
        auditLogs.add("ZDefendManager - deregisterLinkedFunction($label)")
    }

    fun deregisterAllLinkedFunction() {
        registeredFunctions.values.forEach { it.deregister() }
        registeredFunctions.clear()
        linkedObjects.clear()
        auditLogs.add("ZDefendManager - deregisterAllLinkedFunction()")
    }

    // ── Shared helper: map event threats → models and update the linked object ──
    // isMitigation=false → accumulate (merge new threats in); true → remove mitigated threats
    //
    // IMPORTANT: The ZDefend SDK fires onLinkedFunction from a background thread and may
    // call it multiple times in quick succession.  If we read-modify-write linkedObjects
    // directly on the callback thread we get a classic TOCTOU race:
    //   Call-1 reads existing (threats=[])
    //   Call-2 reads existing (threats=[] — Call-1 hasn't committed yet)
    //   Call-1 writes threats=[A]
    //   Call-2 writes threats=[B]  ← overwrites A, last writer wins
    //
    // Posting to the main-thread Handler serialises every mutation so each call
    // always sees the latest committed state before it writes.
    private fun applyLinkedEvent(event: ZLinkedFunctionEvent, isMitigation: Boolean = false) {
        // Snapshot relatedThreats off the callback thread (safe to read here)
        val eventModels = event.relatedThreats.map { t ->
            ThreatModel(t.uuid, t.localizedName, t.severity.toString(),
                        t.isMitigated, t.localizedDetails, t.localizedResolution)
        }
        val label     = event.label
        val eventType = event.eventType.toString()

        mainHandler.post {
            val idx = linkedObjects.indexOfFirst { it.id == label }
            if (idx < 0) return@post

            val existing = linkedObjects[idx]

            val updatedThreats = if (isMitigation) {
                // Remove only the threats that were just mitigated
                val mitigatedIds = eventModels.map { it.id }.toSet()
                existing.threats.filter { it.id !in mitigatedIds }
            } else {
                // Accumulate: merge new/updated threats into the existing list.
                // Because we are now on the main thread and every write goes through
                // here, the read of existing.threats is always up-to-date.
                val merged = existing.threats.toMutableList()
                eventModels.forEach { new ->
                    val existingIdx = merged.indexOfFirst { it.id == new.id }
                    if (existingIdx >= 0) merged[existingIdx] = new else merged.add(new)
                }
                merged
            }

            // Replace the whole object so Compose detects the change via SnapshotStateList
            linkedObjects[idx] = existing.copy(
                eventType = eventType,
                threats   = updatedThreats
            )

            // ── "alert" label: maintain the dedicated alertThreats list ──
            if (label == "alert") {
                if (isMitigation) {
                    val mitigatedIds = eventModels.map { it.id }.toSet()
                    alertThreats.removeAll { it.id in mitigatedIds }
                    if (alertThreats.isEmpty()) showAlert.value = false
                } else {
                    eventModels.forEach { new ->
                        val existingIdx = alertThreats.indexOfFirst { it.id == new.id }
                        if (existingIdx >= 0) alertThreats[existingIdx] = new else alertThreats.add(new)
                    }
                    showAlert.value = true
                }
            }
        }
    }

    // Function runs when the first threat of the same label is being detected
    // Label : high_threat -> Malware - first detection - RUN
    // Label : high_threat -> Screen Sharing - second detection - NO ACTION
    // Label : low_threat -> Screen Sharing - first detection - RUN
    private fun onLinkedFunction(event: ZLinkedFunctionEvent) {
        applyLinkedEvent(event, isMitigation = false)
        auditLogs.add("ZDefendManager - onLinkedFunction(${event.label})")
    }

    // Function runs when the last threat of the same label is being mitigated
    private fun onMitigateFunction(event: ZLinkedFunctionEvent) {
        applyLinkedEvent(event, isMitigation = true)
        auditLogs.add("ZDefendManager - onMitigateFunction(${event.label})")
    }

    // Run Function on all life cycle, onStart, onResume, onAppBackground
    override fun onDeviceStatus(deviceStatus: ZDeviceStatus) {
        if (deviceStatus.loginStatus == ZLoginStatus.LOGGED_IN) {
            percentage.intValue = deviceStatus.initialScanProgressPercentage
            if (deviceStatus.initialScanProgressPercentage >= 100 && !isLoaded.value) {
                isLoaded.value = true
                preregisterLinkedFunction()
            }
        }

        val log = buildString {
            append("OnDeviceStatus: ").append(deviceStatus.loginStatus.name)
            append("\nDatetime: ").append(deviceStatus.statusDate)
            if (deviceStatus.loginStatus != ZLoginStatus.LOGGED_IN) {
                append("\nLast login error: ").append(deviceStatus.loginLastError.name)
            }
        }

        lastDeviceStatus = deviceStatus
        lastScanTime.value = try {
            java.text.SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale.getDefault())
                .format(deviceStatus.statusDate)
        } catch (_: Exception) { deviceStatus.statusDate.toString() }
        auditLogs.add("onDeviceStatus: $log")

        // Compute device risk from highest active-threat severity
        deviceRisk.value = when {
            deviceStatus.activeThreats.any { it.severity.name == "CRITICAL" } -> "CRITICAL"
            deviceStatus.activeThreats.any { it.severity.name == "HIGH" }     -> "HIGH"
            deviceStatus.activeThreats.any { it.severity.name == "MEDIUM" }   -> "MEDIUM"
            deviceStatus.activeThreats.any { it.severity.name == "LOW" }      -> "LOW"
            deviceStatus.activeThreats.isEmpty()                               -> "SECURE"
            else                                                               -> "SECURE"
        }

        // Keep threats and policies live — refresh on every device-status update
        getThreats()
        getPolicies()

        // ── Alert-screen sync ──────────────────────────────────────────────────
        // onLinkedFunction fires only when the "alert" label first becomes active
        // (0 → 1+ threats).  It will NOT fire for a second threat installed while
        // the label is already active (e.g. new malware via ADB while app is open).
        //
        // Rule:
        //   • If the "alert" label is already active (alertThreats non-empty),
        //     add any brand-new threats from this status cycle so they appear on
        //     the AlertScreen alongside the existing ones.
        //   • Remove threats that were just mitigated (handles partial mitigations
        //     that never reach onMitigateFunction).
        //   • Re-show AlertScreen (false → true) if there are still active threats,
        //     covers app-resume and post-dismiss scenarios.
        if (alertThreats.isNotEmpty()) {
            deviceStatus.activeNewThreats.forEach { t ->
                if (alertThreats.none { it.id == t.uuid }) {
                    alertThreats.add(
                        ThreatModel(t.uuid, t.localizedName, t.severity.toString(),
                                    t.isMitigated, t.localizedDetails, t.localizedResolution)
                    )
                }
            }
        }
        deviceStatus.mitigatedNewThreats.forEach { t ->
            alertThreats.removeAll { it.id == t.uuid }
        }
        // Keep the "alert" LinkedModel in sync so the Linked Function page
        // also reflects threats added/removed via onDeviceStatus
        val alertIdx = linkedObjects.indexOfFirst { it.id == "alert" }
        if (alertIdx >= 0) {
            linkedObjects[alertIdx] = linkedObjects[alertIdx].copy(threats = alertThreats.toList())
        }
        if (alertThreats.isNotEmpty() && !showAlert.value) {
            showAlert.value = true
        }

        deviceStatus.activeNewThreats.forEach { threat ->
            statusLogs.add("New Threats (${threat.internalThreatID}): ${threat.localizedName}\n${threat.appName}\n")
        }
        deviceStatus.mitigatedNewThreats.forEach { threat ->
            statusLogs.add("New Mitigation (${threat.internalThreatID}): ${threat.localizedName}\n${threat.appName}\n")
        }

        ZDefend.setTrackingIds("diet018", sessionId)
    }

    override fun onZLog(message: String) {
        this.troubleshootLogs.value = "onZLog(): $message"
        auditLogs.add("ZDefendManager - onZLog()")
    }

    override fun onTroubleshootDetails(messages: JSONArray?) {
        troubleshootDetailsList.clear()
        try {
            messages?.let {
                for (idx in 0 until it.length()) {
                    val entry = it.getJSONObject(idx)
                    if (entry["val"] is String) {
                        val key   = entry.getString("key")
                        val value = entry.getString("val")
                        troubleshootDetailsList.add(Pair(key, value))
                        // Auto-detect SDK version from troubleshoot details
                        if (sdkVersion.value == "—" &&
                            key.contains("sdk",     ignoreCase = true) &&
                            key.contains("version", ignoreCase = true)) {
                            sdkVersion.value = value
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            troubleshootDetailsList.add(Pair("error", e.message ?: "JSONException"))
        }
        auditLogs.add("ZDefendManager - onTroubleshootDetails()")
    }

    fun refreshTroubleshoot() {
        ZDefendTroubleshoot.getZLog(this)
        ZDefendTroubleshoot.getTroubleshootDetails(this)
        auditLogs.add("ZDefendManager - refreshTroubleshoot()")
    }

    fun simulateThreats(input: Int) {
        ZDefendDeveloper.simulateTestThreat(input, null)
        auditLogs.add("ZDefendManager - simulateThreats($input)")
    }

    fun mitigateSimulatedThreats() {
        ZDefendDeveloper.mitigateSimulatedThreats()
        auditLogs.add("ZDefendManager - mitigateThreats()")
    }
}