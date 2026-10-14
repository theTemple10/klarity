package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.model.Product
import com.example.model.VerificationStatus
import org.json.JSONArray

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val barcodeGtin: String,
    val brandName: String,
    val productName: String,
    val isLocalBrand: Boolean,
    val nafdacNumber: String,
    val batchRegex: String,
    val ingredients: String, // JSON array string
    val imageUrl: String = "",
    val category: String = "Personal Care & Skincare",
    val description: String = ""
) {
    fun toProduct(): Product {
        val ingredientList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(ingredients)
            for (i in 0 until jsonArray.length()) {
                ingredientList.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {
            ingredientList.addAll(ingredients.split(",").map { it.trim() })
        }
        return Product(
            id = id,
            barcodeGtin = barcodeGtin,
            brandName = brandName,
            productName = productName,
            isLocalBrand = isLocalBrand,
            nafdacNumber = nafdacNumber,
            batchRegex = batchRegex,
            ingredients = ingredientList,
            imageUrl = imageUrl,
            category = category,
            description = description
        )
    }
}

@Entity(tableName = "scan_logs")
data class ScanLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: String?,
    val barcode: String,
    val brandName: String,
    val productName: String,
    val batchCodeScanned: String,
    val stateLocation: String, // e.g. "Lagos", "Kano", "Abuja"
    val status: String, // "GREEN", "YELLOW", "RED"
    val statusReason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "counterfeit_reports")
data class CounterfeitReportEntity(
    @PrimaryKey val id: String,
    val productName: String,
    val brandName: String,
    val batchCode: String,
    val marketName: String,
    val city: String,
    val sellerDetails: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val skinType: String,
    val isAcneProne: Boolean,
    val hasHyperpigmentation: Boolean,
    val isSensitive: Boolean,
    val climateZone: String,
    val allergens: String // comma-separated
)

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return JSONArray(list).toString()
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(value)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {
            list.addAll(value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
        return list
    }
}
