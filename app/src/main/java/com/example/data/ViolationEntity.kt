package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.SpeedViolationRecord
import com.example.model.VehicleType

@Entity(tableName = "speed_violations")
data class ViolationEntity(
    @PrimaryKey val id: String,
    val junctionId: String,
    val junctionName: String,
    val plateNumber: String,
    val vehicleTypeName: String,
    val speedKmh: Float,
    val speedLimitKmh: Int,
    val fineAmount: Int,
    val timestamp: Long,
    val status: String
) {
    fun toDomain(): SpeedViolationRecord {
        val type = try {
            VehicleType.valueOf(vehicleTypeName)
        } catch (e: Exception) {
            VehicleType.CAR
        }
        return SpeedViolationRecord(
            id = id,
            junctionId = junctionId,
            junctionName = junctionName,
            plateNumber = plateNumber,
            vehicleType = type,
            speedKmh = speedKmh,
            speedLimitKmh = speedLimitKmh,
            fineAmount = fineAmount,
            timestamp = timestamp,
            status = status
        )
    }

    companion object {
        fun fromDomain(record: SpeedViolationRecord): ViolationEntity {
            return ViolationEntity(
                id = record.id,
                junctionId = record.junctionId,
                junctionName = record.junctionName,
                plateNumber = record.plateNumber,
                vehicleTypeName = record.vehicleType.name,
                speedKmh = record.speedKmh,
                speedLimitKmh = record.speedLimitKmh,
                fineAmount = record.fineAmount,
                timestamp = record.timestamp,
                status = record.status
            )
        }
    }
}
