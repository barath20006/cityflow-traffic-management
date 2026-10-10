package com.example.data

import com.example.model.SpeedViolationRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrafficRepository(private val violationDao: ViolationDao) {

    val allViolations: Flow<List<SpeedViolationRecord>> =
        violationDao.getAllViolations().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun recordViolation(record: SpeedViolationRecord) {
        violationDao.insertViolation(ViolationEntity.fromDomain(record))
    }

    suspend fun updateViolationStatus(id: String, status: String) {
        violationDao.updateStatus(id, status)
    }

    suspend fun dismissViolation(id: String) {
        violationDao.deleteById(id)
    }

    suspend fun clearViolations() {
        violationDao.clearAll()
    }
}
