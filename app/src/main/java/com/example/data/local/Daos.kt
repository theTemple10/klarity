package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcodeGtin = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE REPLACE(REPLACE(UPPER(nafdacNumber), '-', ''), ' ', '') = REPLACE(REPLACE(UPPER(:nafdac), '-', ''), ' ', '') LIMIT 1")
    suspend fun getProductByNafdac(nafdac: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcodeGtin = :barcode OR REPLACE(REPLACE(UPPER(nafdacNumber), '-', ''), ' ', '') = REPLACE(REPLACE(UPPER(:nafdac), '-', ''), ' ', '') LIMIT 1")
    suspend fun getProductByBarcodeOrNafdac(barcode: String, nafdac: String): ProductEntity?

    @Query("SELECT * FROM products WHERE brandName LIKE '%' || :query || '%' OR productName LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)
}

@Dao
interface ScanLogDao {
    @Query("SELECT * FROM scan_logs ORDER BY timestamp DESC")
    fun getAllScanLogs(): Flow<List<ScanLogEntity>>

    @Query("SELECT * FROM scan_logs WHERE batchCodeScanned = :batchCode ORDER BY timestamp DESC")
    suspend fun getLogsForBatch(batchCode: String): List<ScanLogEntity>

    @Query("SELECT COUNT(*) FROM scan_logs WHERE batchCodeScanned = :batchCode AND timestamp > :sinceTime")
    suspend fun getRecentScanCountForBatch(batchCode: String, sinceTime: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanLog(scanLog: ScanLogEntity): Long
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM counterfeit_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<CounterfeitReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: CounterfeitReportEntity)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)
}
