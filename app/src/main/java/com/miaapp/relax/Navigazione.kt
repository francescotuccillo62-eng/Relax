package com.miaapp.relax

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** Nomi delle schermate (costanti, per non sbagliare a scriverli) */
object Destinazioni {
    const val HOME = "home"
    const val LASCIAPASSARE = "lasciapassare"
    const val REGISTRO = "registro"
    const val INFO = "info"
    const val PROGRAMMAZIONE = "programmazione"

    // 🧪 TEMPORANEO: rotte per test. RIMUOVERE IN PRODUZIONE
    const val TEST_ANIMAZIONE = "testAnimazione"
    const val TEST_DUE = "testDue"
}

/** Contenitore principale della navigazione tra schermate */
@Composable
fun AppNavigazione(innerPadding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinazioni.HOME
    ) {
        // ───── Schermata Home ─────
        composable(Destinazioni.HOME) {
            SchermataHome(
                onVaiALasciapassare = {
                    navController.navigate(Destinazioni.LASCIAPASSARE)
                },
                onVaiAlRegistro = {
                    navController.navigate(Destinazioni.REGISTRO)
                },
                onVaiAInfo = {
                    navController.navigate(Destinazioni.INFO)
                },
                onVaiAProgrammazione = {
                    navController.navigate(Destinazioni.PROGRAMMAZIONE)
                },
                onVaiATestDue = {
                    navController.navigate(Destinazioni.TEST_DUE)
                }
            )
        }

        // ───── Schermata Lasciapassare ─────
        composable(Destinazioni.LASCIAPASSARE) {
            SchermataLasciapassare(
                innerPadding = innerPadding,
                onTornaIndietro = {
                    navController.popBackStack()
                }
            )
        }

        // ───── Schermata Registro blocchi ─────
        composable(Destinazioni.REGISTRO) {
            SchermataRegistro(
                innerPadding = innerPadding,
                onTornaIndietro = {
                    navController.popBackStack()
                }
            )
        }

        // ───── Schermata Info ─────
        composable(Destinazioni.INFO) {
            SchermataInfo(
                innerPadding = innerPadding,
                onTornaIndietro = {
                    navController.popBackStack()
                }
            )
        }

        // ───── Schermata Programmazione oraria ─────
        composable(Destinazioni.PROGRAMMAZIONE) {
            SchermataProgrammazione(
                innerPadding = innerPadding,
                onTornaIndietro = {
                    navController.popBackStack()
                }
            )
        }
        composable(Destinazioni.TEST_DUE) {
            SchermataTestDue()
        }
    }
    // 🧪 FINE BLOCCO TEMPORANEO
}