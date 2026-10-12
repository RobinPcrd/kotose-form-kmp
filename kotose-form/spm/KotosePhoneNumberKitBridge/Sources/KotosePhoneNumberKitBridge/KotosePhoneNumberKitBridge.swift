/*
* Copyright 2025-present Robin PICARD and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

import Foundation
import PhoneNumberKit

@objc(KotosePhoneParseResult)
public final class KotosePhoneParseResult: NSObject {
    @objc public let nationalNumber: String
    @objc public let region: String
    @objc public let countryCode: Int32

    @objc public init(nationalNumber: String, region: String, countryCode: Int32) {
        self.nationalNumber = nationalNumber
        self.region = region
        self.countryCode = countryCode
        super.init()
    }
}

@objc(KotosePhonePartialResult)
public final class KotosePhonePartialResult: NSObject {
    @objc public let nationalNumber: String
    @objc public let region: String
    @objc public let countryCode: Int32

    @objc public init(nationalNumber: String, region: String, countryCode: Int32) {
        self.nationalNumber = nationalNumber
        self.region = region
        self.countryCode = countryCode
        super.init()
    }
}

@objc(KotosePhoneNumberKitBridge)
public final class KotosePhoneNumberKitBridge: NSObject {
    private let utility = PhoneNumberUtility()

    @objc public override init() {
        super.init()
    }

    @objc public func isPossibleNumber(_ number: String, region: String) -> Bool {
        // Match libphonenumber's possibility check, which checks length rather than allocation patterns.
        let formatter = PartialFormatter(utility: utility, defaultRegion: region, maxDigits: nil)
        let nationalNumber = formatter.nationalNumber(from: number)
        guard !nationalNumber.isEmpty, nationalNumber.allSatisfy(\.isNumber) else { return false }
        let types: [PhoneNumberType] = [
            .fixedLine, .mobile, .pager, .personalNumber, .premiumRate,
            .sharedCost, .tollFree, .voicemail, .voip, .uan
        ]
        return types.contains { type in
            let national = utility.possiblePhoneNumberLengths(
                forCountry: formatter.currentRegion, phoneNumberType: type, lengthType: .national
            )
            let local = utility.possiblePhoneNumberLengths(
                forCountry: formatter.currentRegion, phoneNumberType: type, lengthType: .localOnly
            )
            return national.contains(nationalNumber.count) || local.contains(nationalNumber.count)
        }
    }

    @objc public func countryCodeForRegion(_ region: String) -> Int32 {
        guard let code = utility.countryCode(for: region) else {
            return 0
        }
        return Int32(code)
    }

    @objc public func regionCodeForCountryCode(_ code: Int32) -> String {
        utility.mainCountry(forCode: UInt64(code)) ?? "ZZ"
    }

    @objc public func parsePhoneNumber(_ phone: String, defaultRegion: String) -> KotosePhoneParseResult? {
        do {
            let parsed = try utility.parse(phone, withRegion: defaultRegion, ignoreType: true)
            let region = utility.getRegionCode(of: parsed) ?? parsed.regionID ?? defaultRegion
            return KotosePhoneParseResult(
                nationalNumber: String(parsed.nationalNumber),
                region: region,
                countryCode: Int32(parsed.countryCode)
            )
        } catch {
            return nil
        }
    }

    @objc public func parsePartialPhoneNumber(_ phone: String, defaultRegion: String) -> KotosePhonePartialResult? {
        let formatter = PartialFormatter(
            utility: utility,
            defaultRegion: defaultRegion,
            withPrefix: false
        )
        _ = formatter.formatPartial(phone)

        let region = formatter.currentRegion
        guard let code = utility.countryCode(for: region),
              let nationalNumber = nationalNumberFromInternationalInput(phone, countryCode: code)
        else {
            return nil
        }

        return KotosePhonePartialResult(
            nationalNumber: nationalNumber,
            region: region,
            countryCode: Int32(code)
        )
    }

    @objc public func formatPartial(_ rawNumber: String, defaultRegion: String) -> String {
        PartialFormatter(
            utility: utility,
            defaultRegion: defaultRegion,
            withPrefix: false
        ).formatPartial(rawNumber)
    }

    private func nationalNumberFromInternationalInput(_ phone: String, countryCode: UInt64) -> String? {
        let trimmedPhone = phone.trimmingCharacters(in: .whitespacesAndNewlines)
        guard trimmedPhone.hasPrefix("+") || trimmedPhone.hasPrefix("＋") else {
            return nil
        }

        let digits = trimmedPhone.filter(\.isNumber)
        let countryCodeText = String(countryCode)
        guard digits.hasPrefix(countryCodeText) else {
            return nil
        }

        return String(digits.dropFirst(countryCodeText.count))
    }
}
