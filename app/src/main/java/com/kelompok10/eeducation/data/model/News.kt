package com.kelompok10.eeducation.data.model

import org.json.JSONObject

data class News(
    val id: Int,
    val judul: String,
    val penulis: String,
    val tanggal: String,
    val kategori: String,
    val ringkasan: String,
    val isiBerita: String,
    val tag: List<String>
) {
    companion object {
        fun fromJson(json: JSONObject): News {
            // Parse tags array
            val tagArray = json.getJSONArray("tag")
            val tags = mutableListOf<String>()
            for (i in 0 until tagArray.length()) {
                tags.add(tagArray.getString(i))
            }
            
            return News(
                id = json.getInt("id"),
                judul = json.getString("judul"),
                penulis = json.getString("penulis"),
                tanggal = json.getString("tanggal"),
                kategori = json.getString("kategori"),
                ringkasan = json.getString("ringkasan"),
                isiBerita = json.getString("isi_berita"),
                tag = tags
            )
        }
    }
}
