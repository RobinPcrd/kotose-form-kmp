/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.github.robinpcrd.kotose.form.compose.fields

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.robinpcrd.kotose.form.R
import io.github.robinpcrd.kotose.form.compose.data.Country
import io.github.robinpcrd.kotose.form.fields.PhoneCountryPickerPresenter
import io.github.robinpcrd.kotose.form.fields.PhoneNumberField
import io.github.robinpcrd.kotose.resources.getString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal actual fun rememberDefaultPhoneCountryPickerPresenter(): PhoneCountryPickerPresenter? =
    LocalContext.current.findActivity().let { activity ->
        remember(activity) {
            activity?.let { PhoneCountryPickerPresenter(it) }
        }
    }

internal actual fun showPhoneCountryPicker(
    field: PhoneNumberField,
    presenter: PhoneCountryPickerPresenter,
) {
    val dialog = BottomSheetDialog(presenter.activity)
    dialog.behavior.skipCollapsed = true
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    val content = PhoneCountryPickerContent(
        context = dialog.context,
        field = field,
        onCountrySelected = { country ->
            field.selectCountry(country)
            dialog.dismiss()
        }
    )
    dialog.setContentView(content)
    dialog.setOnShowListener {
        dialog.configureEdgeToEdge()
    }
    dialog.show()
}

@Suppress("DEPRECATION")
private fun BottomSheetDialog.configureEdgeToEdge() {
    window?.let { window ->
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }
    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
        bottomSheet.fitsSystemWindows = false
        (bottomSheet as? ViewGroup)?.clipToPadding = false
        bottomSheet.setPadding(0, 0, 0, 0)
        ViewCompat.setOnApplyWindowInsetsListener(bottomSheet) { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }
        ViewCompat.requestApplyInsets(bottomSheet)
    }
}

private fun PhoneCountryPickerContent(
    context: Context,
    field: PhoneNumberField,
    onCountrySelected: (Country) -> Unit,
): View {
    return ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent {
            PhoneCountryPickerSheet(
                field = field,
                onCountrySelected = onCountrySelected
            )
        }
    }
}

@Composable
private fun PhoneCountryPickerSheet(
    field: PhoneNumberField,
    onCountrySelected: (Country) -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val data by field.valueState
    var query by remember { mutableStateOf("") }
    val normalizedQuery = remember(query) { query.trim().lowercase() }
    val countries = remember(
        field,
        field.setOfAllowedRegion,
        data?.country?.region,
        normalizedQuery,
        configuration,
    ) {
        field.selectableCountries()
            .filter { country -> country.matchesQuery(context, normalizedQuery) }
            .toImmutableList()
    }
    val colors = rememberPhoneCountryPickerColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colors.sheet,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
    ) {
        DragHandle(colors = colors)
        CountrySearchField(
            query = query,
            colors = colors,
            onQueryChange = { query = it }
        )
        CountryList(
            modifier = Modifier.weight(1f),
            countries = countries,
            selectedRegion = data?.country?.region,
            colors = colors,
            onCountrySelected = onCountrySelected
        )
    }
}

@Composable
private fun DragHandle(colors: PhoneCountryPickerColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .background(
                    color = colors.onSurfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
private fun CountrySearchField(
    query: String,
    colors: PhoneCountryPickerColors,
    onQueryChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val placeholder = stringResource(R.string.country_search_placeholder)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .focusRequester(focusRequester),
        textStyle = LocalTextStyle.current.merge(
            TextStyle(
                color = colors.onSurface,
                fontSize = 18.sp
            )
        ),
        singleLine = true,
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = colors.searchContainer,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.kotose_ic_search_24),
                    contentDescription = stringResource(R.string.search),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isBlank()) {
                        Text(
                            text = placeholder,
                            color = colors.onSurfaceVariant,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun CountryList(
    modifier: Modifier,
    countries: ImmutableList<Country>,
    selectedRegion: String?,
    colors: PhoneCountryPickerColors,
    onCountrySelected: (Country) -> Unit,
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        items(
            items = countries,
            key = { country -> country.region }
        ) { country ->
            CountryRow(
                country = country,
                countryName = country.displayName(context),
                selected = country.region == selectedRegion,
                colors = colors,
                onClick = { onCountrySelected(country) }
            )
        }
    }
}

@Composable
private fun CountryRow(
    country: Country,
    countryName: String,
    selected: Boolean,
    colors: PhoneCountryPickerColors,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val rowColor = if (selected) colors.selectedRow else colors.row
    val nameWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
    val codeWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(rowColor)
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else Color.Transparent,
                shape = shape
            )
            .clickable(onClick = onClick)
            .height(52.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country.emojiFlag,
            fontSize = 22.sp,
            modifier = Modifier.width(34.dp)
        )
        Text(
            text = countryName,
            color = colors.onSurface,
            fontSize = 16.sp,
            fontWeight = nameWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        Text(
            text = "+${country.code}",
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            fontSize = 15.sp,
            fontWeight = codeWeight,
            maxLines = 1,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun rememberPhoneCountryPickerColors(): PhoneCountryPickerColors {
    val context = LocalContext.current
    return remember(context) {
        val sheet = Color(context.resolveColor(android.R.attr.colorBackground))
        val primary = Color(context.resolveColor(android.R.attr.colorAccent))
        val onSurface = Color(context.resolveColor(android.R.attr.textColorPrimary))
        val onSurfaceVariant = Color(context.resolveColor(android.R.attr.textColorSecondary))

        PhoneCountryPickerColors(
            sheet = sheet,
            primary = primary,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            searchContainer = blendColors(sheet, onSurfaceVariant, 0.07f),
            row = blendColors(sheet, onSurfaceVariant, 0.07f),
            selectedRow = blendColors(sheet, primary, 0.14f)
        )
    }
}

private data class PhoneCountryPickerColors(
    val sheet: Color,
    val primary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val searchContainer: Color,
    val row: Color,
    val selectedRow: Color,
)

private fun Country.matchesQuery(context: Context, normalizedQuery: String): Boolean {
    if (normalizedQuery.isBlank()) {
        return true
    }

    val dialCode = "+$code"
    val dialCodeWithoutPrefix = code.toString()
    return displayName(context).lowercase().contains(normalizedQuery) ||
        region.lowercase().contains(normalizedQuery) ||
        dialCode.contains(normalizedQuery) ||
        dialCodeWithoutPrefix.contains(normalizedQuery.removePrefix("+"))
}

private fun Country.displayName(context: Context): String {
    return name.getString(context).orEmpty().ifBlank { region }
}

@ColorInt
private fun Context.resolveColor(attribute: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attribute, typedValue, true)
    return if (typedValue.resourceId != 0) {
        ContextCompat.getColor(this, typedValue.resourceId)
    } else {
        typedValue.data
    }
}

private fun blendColors(
    background: Color,
    foreground: Color,
    foregroundAlpha: Float,
): Color {
    val alpha = foregroundAlpha.coerceIn(0f, 1f)
    val inverseAlpha = 1f - alpha
    return Color(
        red = background.red * inverseAlpha + foreground.red * alpha,
        green = background.green * inverseAlpha + foreground.green * alpha,
        blue = background.blue * inverseAlpha + foreground.blue * alpha,
        alpha = background.alpha
    )
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
