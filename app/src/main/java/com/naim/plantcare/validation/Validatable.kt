package com.naim.plantcare.validation

interface Validatable {
    fun validationRules(): List<ValidationRule>

    fun validateInputs(): Boolean {
        var allValid = true
        validationRules().forEach { rule ->
            if (!rule.validate()) allValid = false
        }
        return allValid
    }

    fun clearValidationErrors() {
        validationRules().forEach { it.layout.error = null }
    }

    companion object {
        val PLANT_NAME_REGEX = Regex("^[\\p{L}0-9][\\p{L}0-9 .,'()-]{1,39}$")

        val WATERING_DAYS_REGEX = Regex("^([1-9]|[1-8][0-9]|90)$")

        fun isValid(text: String, regex: Regex, allowEmpty: Boolean): Boolean =
            if (text.isEmpty()) allowEmpty else regex.matches(text)
    }
}
