package com.apexit.accounting

import androidx.room.*

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val taxNumber: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val number: String,
    val customerId: Long,
    val date: String,
    val dueDate: String,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val notes: String = "",
    val status: String = "Draft",
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "document_items")
data class DocumentItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val description: String,
    val quantity: Double,
    val unitPrice: Double,
    val taxRate: Double = 0.0
)

@Entity(tableName = "company")
data class CompanyProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Apex IT Solutions",
    val address: String = "",
    val email: String = "",
    val phone: String = "",
    val registrationNumber: String = "",
    val taxNumber: String = "",
    val quotePrefix: String = "QT",
    val invoicePrefix: String = "INV",
    val statementPrefix: String = "STM",
    val nextQuoteNumber: Int = 1,
    val nextInvoiceNumber: Int = 1,
    val nextStatementNumber: Int = 1,
    val primaryColor: String = "#D71920",
    val fontName: String = "sans-serif"
)

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name") suspend fun getAll(): List<Customer>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(customer: Customer): Long
    @Update suspend fun update(customer: Customer)
    @Query("SELECT * FROM customers WHERE id=:id") suspend fun getById(id: Long): Customer?
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY id DESC") suspend fun getAll(): List<Document>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(document: Document): Long
    @Update suspend fun update(document: Document)
    @Query("SELECT * FROM documents WHERE id=:id") suspend fun getById(id: Long): Document?
}

@Dao
interface ItemDao {
    @Insert suspend fun insertAll(items: List<DocumentItem>)
    @Query("SELECT * FROM document_items WHERE documentId=:documentId") suspend fun forDocument(documentId: Long): List<DocumentItem>
}

@Dao
interface CompanyDao {
    @Query("SELECT * FROM company WHERE id=1") suspend fun get(): CompanyProfile?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(profile: CompanyProfile)
}

@Database(entities = [Customer::class, Document::class, DocumentItem::class, CompanyProfile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customers(): CustomerDao
    abstract fun documents(): DocumentDao
    abstract fun items(): ItemDao
    abstract fun company(): CompanyDao
}
