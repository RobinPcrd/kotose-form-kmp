/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

@file:OptIn(ExperimentalForeignApi::class)

package io.github.robinpcrd.kotose.form.compose.fields

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.robinpcrd.kotose.form.compose.data.Country
import io.github.robinpcrd.kotose.form.fields.PhoneCountryPickerPresenter
import io.github.robinpcrd.kotose.form.fields.PhoneNumberField
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSIndexPath
import platform.UIKit.NSTextAlignmentRight
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIFont
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIScrollViewKeyboardDismissMode
import platform.UIKit.UISearchBar
import platform.UIKit.UISearchBarDelegateProtocol
import platform.UIKit.UISearchBarStyle
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UISheetPresentationControllerDetentIdentifierLarge
import platform.UIKit.UITableView
import platform.UIKit.UITableViewCell
import platform.UIKit.UITableViewCellSelectionStyle
import platform.UIKit.UITableViewCellSeparatorStyle
import platform.UIKit.UITableViewCellStyle
import platform.UIKit.UITableViewDataSourceProtocol
import platform.UIKit.UITableViewDelegateProtocol
import platform.UIKit.UITableViewStyle
import platform.UIKit.UIViewController
import platform.UIKit.labelColor
import platform.UIKit.row
import platform.UIKit.secondaryLabelColor
import platform.UIKit.secondarySystemGroupedBackgroundColor
import platform.UIKit.sheetPresentationController
import platform.UIKit.systemBlueColor
import platform.UIKit.systemGroupedBackgroundColor
import platform.darwin.NSInteger
import platform.darwin.NSObject

@Composable
internal actual fun rememberDefaultPhoneCountryPickerPresenter(): PhoneCountryPickerPresenter? =
    LocalUIViewController.current.let { viewController ->
        remember(viewController) {
            PhoneCountryPickerPresenter(viewController)
        }
    }

internal actual fun showPhoneCountryPicker(
    field: PhoneNumberField,
    presenter: PhoneCountryPickerPresenter,
) {
    val picker = PhoneCountryPickerViewController(field).apply {
        modalPresentationStyle = UIModalPresentationPageSheet
        sheetPresentationController?.detents = listOf(
            UISheetPresentationControllerDetent.mediumDetent(),
            UISheetPresentationControllerDetent.largeDetent()
        )
        sheetPresentationController?.selectedDetentIdentifier =
            UISheetPresentationControllerDetentIdentifierLarge
    }

    presenter.viewController.presentViewController(
        viewControllerToPresent = picker,
        animated = true,
        completion = null
    )
}

private class PhoneCountryPickerViewController(
    private val field: PhoneNumberField,
) : UIViewController(nibName = null, bundle = null) {
    private val searchBar = UISearchBar()
    private val tableView = UITableView(
        frame = CGRectZero.readValue(),
        style = UITableViewStyle.UITableViewStyleInsetGrouped
    )
    private val dataSource = PhoneCountryDataSource(field)
    private val delegate = PhoneCountryDelegate(dataSource) { country ->
        field.selectCountry(country)
        dismissViewControllerAnimated(true, completion = null)
    }
    private val searchDelegate = PhoneCountrySearchDelegate { query ->
        dataSource.submitQuery(query)
        tableView.reloadData()
    }

    override fun viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.systemGroupedBackgroundColor
        definesPresentationContext = true

        searchBar.delegate = searchDelegate
        searchBar.searchBarStyle = UISearchBarStyle.UISearchBarStyleMinimal
        view.addSubview(searchBar)

        tableView.separatorStyle = UITableViewCellSeparatorStyle.UITableViewCellSeparatorStyleNone
        tableView.backgroundColor = UIColor.systemGroupedBackgroundColor
        tableView.contentInset = UIEdgeInsetsMake(2.0, 0.0, 0.0, 0.0)
        tableView.keyboardDismissMode = UIScrollViewKeyboardDismissMode.UIScrollViewKeyboardDismissModeOnDrag
        tableView.rowHeight = 52.0
        tableView.dataSource = dataSource
        tableView.delegate = delegate
        view.addSubview(tableView)
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        searchBar.becomeFirstResponder()
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        val bounds = view.bounds
        val safeAreaInsets = view.safeAreaInsets
        val width = bounds.useContents { size.width }
        val height = bounds.useContents { size.height }
        val topInset = safeAreaInsets.useContents { top }
        val bottomInset = safeAreaInsets.useContents { bottom }

        searchBar.setFrame(
            CGRectMake(
                x = 8.0,
                y = topInset + 4.0,
                width = width - 16.0,
                height = SearchHeight - 8.0
            )
        )
        tableView.setFrame(
            CGRectMake(
                x = 0.0,
                y = topInset + SearchHeight,
                width = width,
                height = height - topInset - SearchHeight
            )
        )
        tableView.scrollIndicatorInsets = UIEdgeInsetsMake(
            top = 0.0,
            left = 0.0,
            bottom = bottomInset,
            right = 0.0
        )
    }
}

private class PhoneCountryDataSource(
    private val field: PhoneNumberField,
) : NSObject(), UITableViewDataSourceProtocol {
    private var countries: List<Country> = field.selectableCountries()

    fun submitQuery(query: String) {
        val normalizedQuery = query.trim().lowercase()
        countries = field.selectableCountries()
            .filter { country -> country.matchesQuery(normalizedQuery) }
    }

    fun countryAt(index: Int): Country? {
        return countries.getOrNull(index)
    }

    override fun numberOfSectionsInTableView(tableView: UITableView): NSInteger {
        return 1
    }

    @ObjCSignatureOverride
    override fun tableView(
        tableView: UITableView,
        numberOfRowsInSection: NSInteger,
    ): NSInteger {
        return countries.size.toLong()
    }

    @ObjCSignatureOverride
    override fun tableView(
        tableView: UITableView,
        cellForRowAtIndexPath: NSIndexPath,
    ): UITableViewCell {
        val cell = tableView.dequeueReusableCellWithIdentifier(CountryCellIdentifier)
            ?: UITableViewCell(
                style = UITableViewCellStyle.UITableViewCellStyleValue1,
                reuseIdentifier = CountryCellIdentifier
            )
        val country = countries[cellForRowAtIndexPath.row.toInt()]
        val selected = country.region == field.value?.country?.region
        cell.textLabel?.text = "${country.emojiFlag} ${country.displayName()}"
        cell.detailTextLabel?.text = "+${country.code}"
        cell.detailTextLabel?.textAlignment = NSTextAlignmentRight
        cell.selectionStyle = UITableViewCellSelectionStyle.UITableViewCellSelectionStyleNone
        cell.backgroundColor =
            if (selected) {
                UIColor.systemBlueColor.colorWithAlphaComponent(0.12)
            } else {
                UIColor.secondarySystemGroupedBackgroundColor
            }
        cell.contentView.backgroundColor = UIColor.clearColor
        cell.contentView.layer.cornerRadius = 12.0
        cell.contentView.layer.masksToBounds = true
        cell.textLabel?.font =
            if (selected) UIFont.boldSystemFontOfSize(17.0) else UIFont.systemFontOfSize(17.0)
        cell.textLabel?.textColor = UIColor.labelColor
        cell.detailTextLabel?.font =
            if (selected) UIFont.boldSystemFontOfSize(15.0) else UIFont.systemFontOfSize(15.0)
        cell.detailTextLabel?.textColor = if (selected) UIColor.systemBlueColor else UIColor.secondaryLabelColor
        return cell
    }
}

private class PhoneCountryDelegate(
    private val dataSource: PhoneCountryDataSource,
    private val onCountrySelected: (Country) -> Unit,
) : NSObject(), UITableViewDelegateProtocol {
    @ObjCSignatureOverride
    override fun tableView(
        tableView: UITableView,
        didSelectRowAtIndexPath: NSIndexPath,
    ) {
        dataSource.countryAt(didSelectRowAtIndexPath.row.toInt())?.let(onCountrySelected)
    }
}

private class PhoneCountrySearchDelegate(
    private val onQueryChanged: (String) -> Unit,
) : NSObject(), UISearchBarDelegateProtocol {
    @ObjCSignatureOverride
    override fun searchBar(
        searchBar: UISearchBar,
        textDidChange: String,
    ) {
        onQueryChanged(textDidChange)
    }
}

private fun Country.matchesQuery(normalizedQuery: String): Boolean {
    if (normalizedQuery.isBlank()) {
        return true
    }

    val dialCode = "+$code"
    val dialCodeWithoutPrefix = code.toString()
    return displayName().lowercase().contains(normalizedQuery) ||
        region.lowercase().contains(normalizedQuery) ||
        dialCode.contains(normalizedQuery) ||
        dialCodeWithoutPrefix.contains(normalizedQuery.removePrefix("+"))
}

private fun Country.displayName(): String {
    return name.text.orEmpty().ifBlank { region }
}

private const val CountryCellIdentifier = "KotosePhoneCountryCell"
private const val SearchHeight = 58.0
