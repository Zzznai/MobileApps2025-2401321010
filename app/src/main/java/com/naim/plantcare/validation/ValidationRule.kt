package com.naim.plantcare.validation

import com.google.android.material.textfield.TextInputLayout

data class ValidationRule(
    val layout: TextInputLayout,
    val regex: Regex,
    val errorMessage: String,
    val allowEmpty: Boolean = true
) {
    fun validate(): Boolean {
        val text = layout.editText?.text?.toString()?.trim().orEmpty()
        val valid = Validatable.isValid(text, regex, allowEmpty)
        layout.error = if (valid) null else errorMessage
        return valid
    }
}
