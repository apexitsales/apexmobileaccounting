package com.apexit.accounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ApexTheme { AppNavigation() } }
    }
}

@Composable
fun ApexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Color(0xFFD71920), secondary = Color(0xFF111827)),
        content = content
    )
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    Scaffold(bottomBar = {
        NavigationBar {
            NavigationBarItem(selected = false, onClick = { nav.navigate("dashboard") }, icon = {}, label = { Text("Home") })
            NavigationBarItem(selected = false, onClick = { nav.navigate("customers") }, icon = {}, label = { Text("Customers") })
            NavigationBarItem(selected = false, onClick = { nav.navigate("documents") }, icon = {}, label = { Text("Documents") })
            NavigationBarItem(selected = false, onClick = { nav.navigate("settings") }, icon = {}, label = { Text("Settings") })
        }
    }) { padding ->
        NavHost(nav, "dashboard", Modifier.padding(padding)) {
            composable("dashboard") { DashboardScreen(nav) }
            composable("customers") { CustomersScreen() }
            composable("documents") { DocumentsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
fun DashboardScreen(nav: androidx.navigation.NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as ApexAccountingApp
    var counts by remember { mutableStateOf(Triple(0, 0, 0.0)) }
    LaunchedEffect(Unit) {
        val customers = app.database.customers().getAll()
        val documents = app.database.documents().getAll()
        counts = Triple(customers.size, documents.size, documents.sumOf { it.total })
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Apex Accounting", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Customers", counts.first.toString(), Modifier.weight(1f))
            StatCard("Documents", counts.second.toString(), Modifier.weight(1f))
        }
        StatCard("Document value", "%.2f".format(counts.third), Modifier.fillMaxWidth())
        Button(onClick = { nav.navigate("customers") }, modifier = Modifier.fillMaxWidth()) { Text("Add / manage customers") }
        Button(onClick = { nav.navigate("documents") }, modifier = Modifier.fillMaxWidth()) { Text("Create documents") }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) { Column(Modifier.padding(16.dp)) { Text(title); Text(value, style = MaterialTheme.typography.titleLarge) } }
}

@Composable
fun CustomersScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ApexAccountingApp
    val scope = rememberCoroutineScope()
    var customers by remember { mutableStateOf(emptyList<Customer>()) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    suspend fun refresh() { customers = app.database.customers().getAll() }
    LaunchedEffect(Unit) { refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Customers", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            if (name.isNotBlank()) scope.launch {
                app.database.customers().insert(Customer(name = name.trim(), email = email.trim()))
                name = ""; email = ""; refresh()
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Save customer") }
        LazyColumn { items(customers) { customer -> ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Column(Modifier.padding(12.dp)) { Text(customer.name, fontWeight = FontWeight.Bold); Text(customer.email) } } } }
    }
}

@Composable
fun DocumentsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ApexAccountingApp
    val scope = rememberCoroutineScope()
    var documents by remember { mutableStateOf(emptyList<Document>()) }
    var type by remember { mutableStateOf("Invoice") }
    var total by remember { mutableStateOf("0") }

    suspend fun refresh() { documents = app.database.documents().getAll() }
    LaunchedEffect(Unit) { refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quotes & invoices", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Quote", "Invoice", "Statement").forEach { name -> FilterChip(selected = type == name, onClick = { type = name }, label = { Text(name) }) }
        }
        OutlinedTextField(total, { total = it }, label = { Text("Total") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            scope.launch {
                val customer = app.database.customers().getAll().firstOrNull()
                    ?: app.database.customers().insert(Customer(name = "Walk-in customer")).let { app.database.customers().getById(it) }
                if (customer != null) {
                    val value = total.toDoubleOrNull() ?: 0.0
                    val company = app.database.company().get() ?: CompanyProfile().also { app.database.company().save(it) }
                    val number = when (type) { "Quote" -> company.quotePrefix; "Statement" -> company.statementPrefix; else -> company.invoicePrefix } + "-" + (System.currentTimeMillis().toString().takeLast(6))
                    val document = Document(type = type, number = number, customerId = customer.id, date = java.time.LocalDate.now().toString(), dueDate = java.time.LocalDate.now().plusDays(30).toString(), subtotal = value, tax = 0.0, total = value)
                    app.database.documents().insert(document)
                    refresh()
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Create $type") }
        LazyColumn { items(documents) { document -> ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Column(Modifier.padding(12.dp)) {
            Text("${document.type} ${document.number}", fontWeight = FontWeight.Bold)
            Text("Status: ${document.status}")
            Text("Total: %.2f".format(document.total))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    scope.launch {
                        val customer = app.database.customers().getById(document.customerId) ?: return@launch
                        val company = app.database.company().get() ?: CompanyProfile()
                        val uri = DocumentPdf.create(context, company, document, customer)
                        ShareTools.emailPdf(context, uri, "${document.type} ${document.number}")
                    }
                }) { Text("PDF / Email") }
            }
        } } } }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ApexAccountingApp
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("Apex IT Solutions") }
    var prefix by remember { mutableStateOf("INV") }
    LaunchedEffect(Unit) {
        app.database.company().get()?.let { name = it.name; prefix = it.invoicePrefix }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Company & document settings", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(name, { name = it }, label = { Text("Company name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(prefix, { prefix = it }, label = { Text("Invoice prefix") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { scope.launch {
            val existing = app.database.company().get()
            app.database.company().save((existing ?: CompanyProfile()).copy(name = name, invoicePrefix = prefix))
        } }, modifier = Modifier.fillMaxWidth()) { Text("Save settings") }
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
            Text("Printing", fontWeight = FontWeight.Bold)
            Text("A4/PDF output is ready.")
            Text("Bluetooth 80 mm ESC/POS support is included; pair a printer in Android settings.")
            Text("Bluetooth status: " + if (ThermalPrinter.hasBluetooth(context)) "enabled" else "disabled")
        } }
    }
}
