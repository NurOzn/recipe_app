package com.melikenurozun.recipe_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import com.melikenurozun.recipe_app.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)
val PlayfairDisplayFont = GoogleFont("Playfair Display")
val InterFont = GoogleFont("Inter")

val FontFamilyPlayfair = FontFamily(
    Font(googleFont = PlayfairDisplayFont, fontProvider = provider),
    Font(googleFont = PlayfairDisplayFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = PlayfairDisplayFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = PlayfairDisplayFont, fontProvider = provider, weight = FontWeight.SemiBold)
)

val FontFamilyInter = FontFamily(
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.SemiBold)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamilyInter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = CharcoalBlack
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamilyInter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = WarmGray
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamilyInter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = WarmGray
    ),

    titleLarge = TextStyle(
        fontFamily = FontFamilyPlayfair,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = CharcoalBlack
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamilyPlayfair,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
        color = CharcoalBlack
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamilyPlayfair,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = CharcoalBlack
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamilyPlayfair,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-1).sp,
        color = CharcoalBlack
    ),

    labelLarge = TextStyle(
        fontFamily = FontFamilyInter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamilyInter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = WarmGray
    )
)