package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ViolationEntity
import com.example.model.DetectedVehicle
import com.example.model.LaneDirection
import com.example.model.SpeedViolationRecord
import com.example.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CityFlow", appName)
  }

  @Test
  fun `verify vehicle classifications have pcu weights`() {
    val busPcu = VehicleType.BUS.pcuWeight
    val carPcu = VehicleType.CAR.pcuWeight
    val bikePcu = VehicleType.BIKE.pcuWeight
    val lorryPcu = VehicleType.LORRY.pcuWeight

    assertTrue(busPcu > carPcu)
    assertTrue(lorryPcu > carPcu)
    assertTrue(bikePcu < carPcu)
    assertNotNull(VehicleType.AUTO)
  }

  @Test
  fun `verify speed violation record entity roundtrip`() {
    val record = SpeedViolationRecord(
      id = "vio-1",
      junctionId = "j1",
      junctionName = "MG Road & Cyber Highway",
      plateNumber = "DL-01-AB-1234",
      vehicleType = VehicleType.CAR,
      speedKmh = 72f,
      speedLimitKmh = 50,
      fineAmount = 1500,
      timestamp = 1000L,
      status = "ISSUED"
    )

    val entity = ViolationEntity.fromDomain(record)
    assertEquals("vio-1", entity.id)
    assertEquals("CAR", entity.vehicleTypeName)

    val domain = entity.toDomain()
    assertEquals(record.plateNumber, domain.plateNumber)
    assertEquals(record.speedKmh, domain.speedKmh, 0.01f)
    assertEquals(record.vehicleType, domain.vehicleType)
  }

  @Test
  fun `verify detected vehicle violation logged flag defaults to false`() {
    val vehicle = DetectedVehicle(
      id = "v-1",
      plateNumber = "KA-04-1234",
      type = VehicleType.BIKE,
      speedKmh = 65f,
      isSpeeding = true,
      lane = LaneDirection.NORTH_SOUTH,
      progress = 0.3f,
      laneIndex = 0
    )

    assertFalse(vehicle.violationLogged)
    val updated = vehicle.copy(violationLogged = true)
    assertTrue(updated.violationLogged)
  }
}
