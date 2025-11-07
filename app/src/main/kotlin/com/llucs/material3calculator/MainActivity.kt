package com.llucs.material3calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.llucs.material3calculator.ui.theme.Material3CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Material3CalculatorTheme {
                CalculatorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            CalculatorBottomBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "calculator",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("calculator") { CalculatorScreen() }
            composable("about") { AboutScreen() }
        }
    }
}

@Composable
fun CalculatorBottomBar(navController: NavController) {
    NavigationBar {
        val currentRoute by navController.currentBackStackEntryAsState()
        val route = currentRoute?.destination?.route ?: "calculator"

        NavigationBarItem(
            selected = route == "calculator",
            onClick = { navController.navigate("calculator") { popUpTo(navController.graph.startDestinationId) } },
            icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
            label = { Text("Calculadora") }
        )
        NavigationBarItem(
            selected = route == "about",
            onClick = { navController.navigate("about") { popUpTo(navController.graph.startDestinationId) } },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("Sobre") }
        )
    }
}

@Composable
fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var waitingForOperand by remember { mutableStateOf(false) }

    val onButtonClick: (String) -> Unit = { value ->
        when (value) {
            "C" -> {
                display = "0"
                operand1 = null
                operator = null
                waitingForOperand = false
            }
            "⌫" -> {
                display = if (display.length > 1) display.dropLast(1) else "0"
            }
            "+", "-", "×", "÷" -> {
                operand1 = display.toDoubleOrNull() ?: 0.0
                operator = value
                waitingForOperand = true
            }
            "=" -> {
                val operand2 = display.toDoubleOrNull() ?: 0.0
                operand1?.let { op1 ->
                    operator?.let { op ->
                        val result = when (op) {
                            "+" -> op1 + operand2
                            "-" -> op1 - operand2
                            "×" -> op1 * operand2
                            "÷" -> if (operand2 != 0.0) op1 / operand2 else Double.NaN
                            else -> 0.0
                        }
                        display = if (result.isNaN()) "Erro" else result.toString().removeSuffix(".0")
                        operand1 = null
                        operator = null
                        waitingForOperand = true
                    }
                }
            }
            else -> {
                if (waitingForOperand || display == "0" || display == "Erro") {
                    display = value
                    waitingForOperand = false
                } else {
                    display += value
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp) // Reduzido o espaçamento para caber melhor na tela
    ) {
        // Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Text(
                text = display,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false
            )
        }

        Spacer(modifier = Modifier.height(8.dp)) // Espaçamento extra entre display e botões

        // Buttons Grid
        val buttons = listOf(
            listOf("C", "⌫", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { label ->
                    val weight = if (label == "0") 2f else 1f
                    CalculatorButton(
                        label = label,
                        onClick = { onButtonClick(label) },
                        modifier = Modifier.weight(weight)
                    )
                }
                // Adiciona o botão de igual na última linha se não estiver lá
                if (row.size < 4 && row.last() != "=") {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
        label = "buttonScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            label in listOf("+", "-", "×", "÷", "=") -> MaterialTheme.colorScheme.primary
            label in listOf("C", "⌫") -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(200),
        label = "buttonColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            label in listOf("+", "-", "×", "÷", "=") -> MaterialTheme.colorScheme.onPrimary
            label in listOf("C", "⌫") -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200),
        label = "contentColor"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .height(72.dp),
        shape = MaterialTheme.shapes.large,
        color = backgroundColor,
        contentColor = contentColor,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Calculate,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Calculadora Material 3",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Versão 1.0.0",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Desenvolvedor",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Llucs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Feito com ❤️ usando Jetpack Compose e Material 3 Expressive",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
