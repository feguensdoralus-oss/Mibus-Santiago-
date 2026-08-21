package com.example

import com.example.data.TransitRepository
import com.example.data.api.MockBipApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  private val mockApi = MockBipApiService()

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun mockBipApi_validStandardCard_returnsSuccess() = runBlocking {
    val response = mockApi.getBipBalance("12345678", simulateLatency = false)
    assertEquals(200, response.statusCode)
    assertNotNull(response.data)
    assertTrue(response.data!!.isValid)
    assertEquals("12345678", response.data!!.cardNumber)
    assertEquals(4560, response.data!!.balance)
    assertEquals("Estándar", response.data!!.cardType)
  }

  @Test
  fun mockBipApi_invalidCardNumber_returnsError() = runBlocking {
    val response = mockApi.getBipBalance("123", simulateLatency = false)
    assertEquals(400, response.statusCode)
    assertNotNull(response.data)
    assertFalse(response.data!!.isValid)
  }

  @Test
  fun mockBipApi_studentCard_returnsTneProfile() = runBlocking {
    val response = mockApi.getBipBalance("99887766", simulateLatency = false)
    assertEquals(200, response.statusCode)
    assertNotNull(response.data)
    assertTrue(response.data!!.isValid)
    assertEquals("Pase Escolar TNE", response.data!!.cardType)
    assertEquals(3250, response.data!!.balance)
  }

  @Test
  fun formatClp_formatsCorrectly() {
    val formatted = TransitRepository.formatClp(4560)
    assertTrue(formatted.contains("4.560") || formatted.contains("4,560") || formatted.contains("4560"))
  }
}

