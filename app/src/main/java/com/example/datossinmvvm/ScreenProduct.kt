package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenProduct() {
    val context = LocalContext.current
    var productIdText by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    val dataProducts = remember { mutableStateOf("") }

    val db = remember { crearAppDatabase(context) }
    val dao = db.productDao()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CRUD de Productos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0066CC),
                    titleContentColor = Color.White
                ),
                actions = {
                    // Acciones en AppBar: Crear y Listar
                    TextButton(
                        onClick = {
                            val price = productPrice.toDoubleOrNull() ?: 0.0
                            val product = Product(name = productName, price = price)
                            coroutineScope.launch {
                                agregarProducto(product, dao)
                                dataProducts.value = getProductos(dao)
                            }
                            productName = ""
                            productPrice = ""
                        }
                    ) {
                        Text("Agregar", color = Color.White)
                    }
                    TextButton(
                        onClick = {
                            coroutineScope.launch { dataProducts.value = getProductos(dao) }
                        }
                    ) {
                        Text("Listar", color = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            TextField(
                value = productIdText,
                onValueChange = { productIdText = it },
                label = { Text("ID del Producto (para Editar/Eliminar)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Nombre del Producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = productPrice,
                onValueChange = { productPrice = it },
                label = { Text("Precio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Botones de actualización y eliminación
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val id = productIdText.toIntOrNull() ?: 0
                        val price = productPrice.toDoubleOrNull() ?: 0.0
                        if (id > 0) {
                            val product = Product(id = id, name = productName, price = price)
                            coroutineScope.launch {
                                actualizarProducto(product, dao)
                                dataProducts.value = getProductos(dao)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Actualizar por ID")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val id = productIdText.toIntOrNull() ?: 0
                        if (id > 0) {
                            coroutineScope.launch {
                                eliminarProductoPorId(id, dao)
                                dataProducts.value = getProductos(dao)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Eliminar por ID")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(text = dataProducts.value, fontSize = 18.sp)
        }
    }
}

fun crearAppDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "product_db"
    ).build()
}

suspend fun getProductos(dao: ProductDao): String = withContext(Dispatchers.IO) {
    var rpta = ""
    val products = dao.getAll()
    products.forEach { p ->
        rpta += "[ID: ${p.id}] ${p.name} - S/ ${p.price}\n"
    }
    rpta
}

suspend fun agregarProducto(product: Product, dao: ProductDao) = withContext(Dispatchers.IO) {
    try {
        dao.insert(product)
    } catch (e: Exception) {
        Log.e("Product", "Error insert: ${e.message}")
    }
}

suspend fun actualizarProducto(product: Product, dao: ProductDao) = withContext(Dispatchers.IO) {
    try {
        dao.update(product)
    } catch (e: Exception) {
        Log.e("Product", "Error update: ${e.message}")
    }
}

suspend fun eliminarProductoPorId(id: Int, dao: ProductDao) = withContext(Dispatchers.IO) {
    try {
        val products = dao.getAll()
        val target = products.find { it.id == id }
        if (target != null) {
            dao.delete(target)
        }
    } catch (e: Exception) {
        Log.e("Product", "Error delete: ${e.message}")
    }
}