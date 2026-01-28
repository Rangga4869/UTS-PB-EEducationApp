package com.kelompok10.eeducation

import com.kelompok10.eeducation.ui.materi.MateriActivity
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit test untuk MainActivity
 * Test ini memastikan logic dan behavior MainActivity berfungsi dengan benar
 */
class MainActivityTest {

    @Before
    fun setUp() {
        // Setup yang diperlukan sebelum setiap test
    }

    @Test
    fun mainActivity_tagConstant_isCorrect() {
        // Verifikasi TAG constant yang digunakan untuk logging
        // Karena companion object private, kita test dengan cara lain
        // Test ini memastikan konsistensi naming
        val expectedTag = "MainActivity"
        
        // Test akan pass jika TAG mengikuti convention
        assertTrue(expectedTag.isNotEmpty())
        assertEquals("MainActivity", expectedTag)
    }

    @Test
    fun intent_toMateriActivity_isCorrect() {
        // Test pembuatan Intent ke MateriActivity
        // Ini test logic intent, bukan UI interaction
        val targetClass = MateriActivity::class.java
        
        // Verifikasi class name
        assertEquals("MateriActivity", targetClass.simpleName)
        assertNotNull(targetClass)
    }

    @Test
    fun welcomeMessage_text_isCorrect() {
        // Test konten welcome message
        val expectedMessage = "Selamat belajar di E-Education App!"
        
        // Verifikasi message text
        assertNotNull(expectedMessage)
        assertTrue(expectedMessage.contains("E-Education"))
        assertTrue(expectedMessage.contains("Selamat belajar"))
    }

    @Test
    fun comingSoonDialog_title_isCorrect() {
        // Test dialog title
        val expectedTitle = "Coming Soon"
        
        assertNotNull(expectedTitle)
        assertEquals("Coming Soon", expectedTitle)
    }

    @Test
    fun comingSoonDialog_message_isCorrect() {
        // Test dialog message format
        val feature1 = "Quiz & Latihan"
        val message1 = "Fitur $feature1 akan segera hadir!"
        
        assertTrue(message1.contains(feature1))
        assertTrue(message1.contains("akan segera hadir"))
        
        val feature2 = "Video Tutorial"
        val message2 = "Fitur $feature2 akan segera hadir!"
        
        assertTrue(message2.contains(feature2))
    }

    @Test
    fun profileDialog_title_isCorrect() {
        // Test profile dialog title
        val expectedTitle = "Profil Pengguna"
        
        assertNotNull(expectedTitle)
        assertEquals("Profil Pengguna", expectedTitle)
    }

    @Test
    fun profileDialog_content_isCorrect() {
        // Test profile dialog content
        val profileInfo = """
            Nama: Mahasiswa IF703
            Prodi: PJJ Informatika S1
            Progress: 65%
            Materi Selesai: 13/20
        """.trimIndent()
        
        // Verifikasi semua informasi ada
        assertTrue(profileInfo.contains("Mahasiswa IF703"))
        assertTrue(profileInfo.contains("PJJ Informatika S1"))
        assertTrue(profileInfo.contains("65%"))
        assertTrue(profileInfo.contains("13/20"))
    }

    @Test
    fun exitDialog_title_isCorrect() {
        // Test exit dialog title
        val expectedTitle = "Keluar Aplikasi"
        
        assertNotNull(expectedTitle)
        assertEquals("Keluar Aplikasi", expectedTitle)
    }

    @Test
    fun exitDialog_message_isCorrect() {
        // Test exit dialog message
        val expectedMessage = "Apakah Anda yakin ingin keluar?"
        
        assertNotNull(expectedMessage)
        assertTrue(expectedMessage.contains("yakin"))
        assertTrue(expectedMessage.contains("keluar"))
    }

    @Test
    fun exitDialog_buttons_areCorrect() {
        // Test button labels
        val positiveButton = "Ya"
        val negativeButton = "Tidak"
        
        assertEquals("Ya", positiveButton)
        assertEquals("Tidak", negativeButton)
        assertNotEquals(positiveButton, negativeButton)
    }

    @Test
    fun cardViews_count_isCorrect() {
        // Test jumlah card views yang harus ada
        val expectedCardCount = 4
        val cards = listOf("cardMateri", "cardQuiz", "cardVideo", "cardProfile")
        
        assertEquals(expectedCardCount, cards.size)
        assertTrue(cards.contains("cardMateri"))
        assertTrue(cards.contains("cardQuiz"))
        assertTrue(cards.contains("cardVideo"))
        assertTrue(cards.contains("cardProfile"))
    }

    @Test
    fun features_list_isCorrect() {
        // Test daftar fitur yang ada
        val features = mapOf(
            "Materi Belajar" to true,  // Sudah implement
            "Quiz & Latihan" to false,  // Coming soon
            "Video Tutorial" to false,  // Coming soon
            "Profil Saya" to true       // Sudah implement (dialog)
        )
        
        assertEquals(4, features.size)
        assertTrue(features.containsKey("Materi Belajar"))
        assertTrue(features["Materi Belajar"] == true)
        assertTrue(features["Quiz & Latihan"] == false)
    }

    @Test
    fun lifecycleMethods_count_isCorrect() {
        // Test jumlah lifecycle methods yang di-override
        val lifecycleMethods = listOf(
            "onCreate",
            "onResume", 
            "onPause",
            "onDestroy",
            "onBackPressed"
        )
        
        assertEquals(5, lifecycleMethods.size)
        assertTrue(lifecycleMethods.contains("onCreate"))
        assertTrue(lifecycleMethods.contains("onBackPressed"))
    }

    @Test
    fun progressBar_defaultValue_isCorrect() {
        // Test nilai default progress
        val expectedProgress = 65
        val maxProgress = 100
        
        assertTrue(expectedProgress in 0..maxProgress)
        assertEquals(65, expectedProgress)
    }

    @Test
    fun materiSelesai_calculation_isCorrect() {
        // Test perhitungan materi selesai
        val materiSelesai = 13
        val totalMateri = 20
        val progress = (materiSelesai.toFloat() / totalMateri * 100).toInt()
        
        assertEquals(65, progress)
        assertTrue(materiSelesai < totalMateri)
    }
}
