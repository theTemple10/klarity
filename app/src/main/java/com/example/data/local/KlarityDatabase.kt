package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

@Database(
    entities = [ProductEntity::class, ScanLogEntity::class, CounterfeitReportEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KlarityDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun scanLogDao(): ScanLogDao
    abstract fun reportDao(): ReportDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: KlarityDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): KlarityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KlarityDatabase::class.java,
                    "klarity_database"
                )
                    .addCallback(KlarityDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class KlarityDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.productDao(), database.scanLogDao(), database.userProfileDao())
                }
            }
        }

        suspend fun populateInitialData(
            productDao: ProductDao,
            scanLogDao: ScanLogDao,
            userProfileDao: UserProfileDao
        ) {
            // Seed Authentic Cosmetics Ledger (Global & Nigerian Local Brands)
            val initialProducts = listOf(
                ProductEntity(
                    id = "prod_nivea_even_glow",
                    barcodeGtin = "4005900940567",
                    brandName = "Nivea",
                    productName = "Radiant & Beauty Even Glow Body Lotion",
                    isLocalBrand = false,
                    nafdacNumber = "A2-4412",
                    batchRegex = "^[0-9]{8}$",
                    ingredients = JSONArray(listOf(
                        "Aqua", "Glycerin", "Paraffinum Liquidum", "Cetearyl Alcohol",
                        "Glyceryl Stearate", "Niacinamide", "Pearl Extract",
                        "Sodium Ascorbyl Phosphate", "Dimethicone", "Ethylhexylglycerin"
                    )).toString(),
                    category = "Body Care & Even Tone",
                    description = "Formulated with 95% pure Pearl Extract and Niacinamide to enhance melanin-rich skin tone."
                ),
                ProductEntity(
                    id = "prod_cerave_moisturizer",
                    barcodeGtin = "3337875597203",
                    brandName = "CeraVe",
                    productName = "Daily Moisturizing Lotion",
                    isLocalBrand = false,
                    nafdacNumber = "02-1988",
                    batchRegex = "^[0-9]{2}[A-Z][0-9]{3}$",
                    ingredients = JSONArray(listOf(
                        "Aqua / Water", "Glycerin", "Caprylic/Capric Triglyceride",
                        "Cetearyl Alcohol", "Cetyl Alcohol", "Ceramide NP",
                        "Ceramide AP", "Ceramide EOP", "Phytosphingosine",
                        "Cholesterol", "Hyaluronic Acid"
                    )).toString(),
                    category = "Moisturizers & Barrier Repair",
                    description = "Essential ceramides 1, 3, 6-II to restore the protective skin barrier."
                ),
                ProductEntity(
                    id = "prod_shea_origin_butter",
                    barcodeGtin = "6151101928014",
                    brandName = "Shea Origin",
                    productName = "Pure Cold-Pressed Virgin Shea Butter",
                    isLocalBrand = true,
                    nafdacNumber = "A4-2281",
                    batchRegex = "^SO-[A-Z]{2}-[0-9]{4}$",
                    ingredients = JSONArray(listOf(
                        "Pure Unrefined Butyrospermum Parkii (Shea Butter)",
                        "Tocopherol (Vitamin E)"
                    )).toString(),
                    category = "Natural Nigerian Botanicals",
                    description = "Handcrafted in Oyo State, Nigeria. 100% natural, unrefined grade-A shea butter."
                ),
                ProductEntity(
                    id = "prod_arami_glow_oil",
                    barcodeGtin = "6151108239019",
                    brandName = "Arami Essentials",
                    productName = "Glow Oil Body & Face Treatment",
                    isLocalBrand = true,
                    nafdacNumber = "B4-1102",
                    batchRegex = "^AE[0-9]{6}[A-Z]$",
                    ingredients = JSONArray(listOf(
                        "Simmondsia Chinensis (Jojoba) Seed Oil",
                        "Helianthus Annuus Seed Oil",
                        "Rosa Canina Fruit Oil",
                        "Citrus Aurantium Dulcis Peel Oil"
                    )).toString(),
                    category = "Oils & Serums",
                    description = "Clean African botanical formula for a luminous finish without clogging pores."
                ),
                ProductEntity(
                    id = "prod_dudu_osun_soap",
                    barcodeGtin = "6151100010017",
                    brandName = "Dudu-Osun",
                    productName = "Original African Black Soap",
                    isLocalBrand = true,
                    nafdacNumber = "02-0051",
                    batchRegex = "^DO-[0-9]{6}$",
                    ingredients = JSONArray(listOf(
                        "Pure Honey", "Shea Butter", "Osun (Camwood)",
                        "Palm Kernel Oil", "Cocoa Pod Ash", "Palm Bunch Ash",
                        "Aloe Vera", "Lime Juice", "Water"
                    )).toString(),
                    category = "Traditional Cleansers",
                    description = "Authentic Nigerian black soap enriched with camwood and soothing aloe."
                ),
                ProductEntity(
                    id = "prod_neutrogena_hydro",
                    barcodeGtin = "3574661309811",
                    brandName = "Neutrogena",
                    productName = "Hydro Boost Water Gel Face Moisturizer",
                    isLocalBrand = false,
                    nafdacNumber = "A2-7719",
                    batchRegex = "^LOT[0-9]{6}[A-Z0-9]?$",
                    ingredients = JSONArray(listOf(
                        "Water", "Dimethicone", "Glycerin",
                        "Sodium Hyaluronate", "Ethylhexylglycerin", "Polyacrylamide"
                    )).toString(),
                    category = "Hydration Gel",
                    description = "Oil-free hyaluronic acid hydration for humid and warm weather."
                ),
                ProductEntity(
                    id = "prod_fair_white_lotion",
                    barcodeGtin = "3442151240112",
                    brandName = "Fair & White",
                    productName = "Gold Revitalizing Body Lotion",
                    isLocalBrand = false,
                    nafdacNumber = "A2-3390",
                    batchRegex = "^FW-[0-9]{7}$",
                    ingredients = JSONArray(listOf(
                        "Aqua (Water)", "Paraffinum Liquidum", "Glycerin",
                        "Arbutin", "Glycolic Acid", "Parfum (Fragrance)"
                    )).toString(),
                    category = "Exfoliating Body Care",
                    description = "Gentle AHA brightening formula approved by NAFDAC (free from hydroquinone)."
                )
            )
            productDao.insertProducts(initialProducts)

            // Seed user profile with default Oily / Acne-Prone & Melanin-Rich profile
            userProfileDao.saveUserProfile(
                UserProfileEntity(
                    id = 1,
                    skinType = "OILY",
                    isAcneProne = true,
                    hasHyperpigmentation = true,
                    isSensitive = false,
                    climateZone = "Tropical Humid (Lagos / Southern Nigeria)",
                    allergens = "Artificial Fragrance, Parabens"
                )
            )
        }
    }
}
