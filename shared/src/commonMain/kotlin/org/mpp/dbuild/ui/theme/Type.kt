package org.mpp.dbuild.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import pesmaricaeditor.shared.generated.resources.Res
import pesmaricaeditor.shared.generated.resources.roboto_it_v
import pesmaricaeditor.shared.generated.resources.roboto_mono_it_v
import pesmaricaeditor.shared.generated.resources.roboto_mono_v
import pesmaricaeditor.shared.generated.resources.roboto_v


enum class MonoFF{
    RobotoMono
}

//val jbrMono = FontFamily(
//    Font(R.font.jbr_r, FontWeight.Normal),
//    Font(R.font.jbr_i, FontWeight.Normal, FontStyle.Italic),
//    Font(R.font.jbr_sb, FontWeight.SemiBold),
//    Font(R.font.jbr_sbi, FontWeight.SemiBold, FontStyle.Italic),
//    Font(R.font.jbr_b, FontWeight.Bold),
//    Font(R.font.jbr_bi, FontWeight.Bold, FontStyle.Italic),
//)

val robotoMono: FontFamily
    @Composable get() = FontFamily(
        // Navadna (Regular) teža
        Font(resource = Res.font.roboto_mono_v, weight = FontWeight.Normal, style = FontStyle.Normal),
        Font(resource = Res.font.roboto_mono_it_v, weight = FontWeight.Normal, style = FontStyle.Italic),

        // Polkrepka (SemiBold) teža
        Font(resource = Res.font.roboto_mono_v, weight = FontWeight.SemiBold, style = FontStyle.Normal),
        Font(resource = Res.font.roboto_mono_it_v, weight = FontWeight.SemiBold, style = FontStyle.Italic),

        // Krepka (Bold) teža
        Font(resource = Res.font.roboto_mono_v, weight = FontWeight.Bold, style = FontStyle.Normal),
        Font(resource = Res.font.roboto_mono_it_v, weight = FontWeight.Bold, style = FontStyle.Italic),
    )

val robotoBff: FontFamily
    @Composable get() = FontFamily(
    Font(Res.font.roboto_v, FontWeight.Normal),
    Font(Res.font.roboto_it_v, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.roboto_v, FontWeight.SemiBold),
    Font(Res.font.roboto_it_v, FontWeight.SemiBold, FontStyle.Italic),
    Font(Res.font.roboto_v, FontWeight.Bold),
    Font(Res.font.roboto_it_v, FontWeight.Bold, FontStyle.Italic),
)
val robotoDff: FontFamily
    @Composable get() = FontFamily(
    Font(Res.font.roboto_v, FontWeight.Bold),
    Font(Res.font.roboto_it_v, FontWeight.Bold, FontStyle.Italic),
)

@Composable
fun getMonoFontFamily(which: MonoFF): FontFamily{
    return when(which){
        MonoFF.RobotoMono -> robotoMono
    }
}


val bodyFontFamily: FontFamily @Composable get() = robotoBff
val displayFontFamily: FontFamily @Composable get() = robotoDff

// Default Material 3 typography values
val baseline = Typography()

val typography: Typography @Composable get() = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)