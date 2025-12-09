package com.kelompok10.eeducation

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit test untuk data class Materi
 * Test ini memastikan bahwa model Materi berfungsi dengan benar
 */
class MateriTest {

    @Test
    fun materi_creation_isCorrect() {
        // Membuat object Materi
        val materi = Materi(
            icon = "📚",
            title = "Pengenalan Pemrograman",
            description = "Dasar-dasar pemrograman dan algoritma",
            duration = "15 menit",
            isCompleted = true
        )

        // Verifikasi semua property
        assertEquals("📚", materi.icon)
        assertEquals("Pengenalan Pemrograman", materi.title)
        assertEquals("Dasar-dasar pemrograman dan algoritma", materi.description)
        assertEquals("15 menit", materi.duration)
        assertTrue(materi.isCompleted)
    }

    @Test
    fun materi_defaultCompleted_isFalse() {
        // Membuat Materi tanpa set isCompleted
        val materi = Materi(
            icon = "💻",
            title = "Test Materi",
            description = "Test Description",
            duration = "10 menit"
        )

        // Verifikasi default value isCompleted adalah false
        assertFalse(materi.isCompleted)
    }

    @Test
    fun materi_withCompletedTrue_isCorrect() {
        // Membuat Materi dengan isCompleted = true
        val materi = Materi(
            icon = "🔄",
            title = "Struktur Kontrol",
            description = "If-else, switch, dan perulangan",
            duration = "25 menit",
            isCompleted = true
        )

        // Verifikasi isCompleted adalah true
        assertTrue(materi.isCompleted)
    }

    @Test
    fun materi_withCompletedFalse_isCorrect() {
        // Membuat Materi dengan isCompleted = false
        val materi = Materi(
            icon = "📦",
            title = "Array dan Collections",
            description = "Belajar array, list, dan struktur data",
            duration = "30 menit",
            isCompleted = false
        )

        // Verifikasi isCompleted adalah false
        assertFalse(materi.isCompleted)
    }

    @Test
    fun materi_equality_isCorrect() {
        // Data class harus memiliki equals() yang benar
        val materi1 = Materi("📚", "Title", "Desc", "10 menit", true)
        val materi2 = Materi("📚", "Title", "Desc", "10 menit", true)
        val materi3 = Materi("💻", "Other", "Desc", "10 menit", false)

        // Verifikasi equality
        assertEquals(materi1, materi2) // Sama
        assertNotEquals(materi1, materi3) // Berbeda
    }

    @Test
    fun materi_copy_isCorrect() {
        // Test copy function dari data class
        val original = Materi(
            icon = "⚙️",
            title = "Fungsi dan Method",
            description = "Membuat dan menggunakan fungsi",
            duration = "20 menit",
            isCompleted = false
        )

        // Copy dengan perubahan isCompleted
        val completed = original.copy(isCompleted = true)

        // Verifikasi
        assertFalse(original.isCompleted)
        assertTrue(completed.isCompleted)
        assertEquals(original.title, completed.title)
        assertEquals(original.icon, completed.icon)
    }

    @Test
    fun materi_toString_isCorrect() {
        // Test toString dari data class
        val materi = Materi(
            icon = "🎯",
            title = "OOP",
            description = "Object Oriented Programming",
            duration = "35 menit",
            isCompleted = true
        )

        val toString = materi.toString()

        // Verifikasi toString mengandung semua property
        assertTrue(toString.contains("OOP"))
        assertTrue(toString.contains("🎯"))
        assertTrue(toString.contains("35 menit"))
    }

    @Test
    fun materi_hashCode_isCorrect() {
        // Test hashCode consistency
        val materi1 = Materi("📱", "Android", "Components", "30 menit", false)
        val materi2 = Materi("📱", "Android", "Components", "30 menit", false)

        // Objects yang sama harus memiliki hashCode yang sama
        assertEquals(materi1.hashCode(), materi2.hashCode())
    }

    @Test
    fun materi_componentN_isCorrect() {
        // Test destructuring (component functions)
        val materi = Materi(
            icon = "🎨",
            title = "UI Design",
            description = "Layout dan Views",
            duration = "25 menit",
            isCompleted = true
        )

        val (icon, title, description, duration, isCompleted) = materi

        // Verifikasi destructuring
        assertEquals("🎨", icon)
        assertEquals("UI Design", title)
        assertEquals("Layout dan Views", description)
        assertEquals("25 menit", duration)
        assertTrue(isCompleted)
    }
}