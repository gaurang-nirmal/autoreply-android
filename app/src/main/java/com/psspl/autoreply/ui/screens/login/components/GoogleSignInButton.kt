package com.psspl.autoreply.ui.screens.login.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.psspl.autoreply.ui.theme.AutoReplyTheme
import com.psspl.autoreply.ui.theme.Spacing

/**
 * A Google-branded sign-in button.
 *
 * Follows Google's Sign-In button brand guidelines:
 * - White/surface background with outlined border
 * - Google "G" lettermark in Google Blue (#4285F4)
 * - "Continue with Google" label
 * - 52 dp height (matches [PrimaryButton] in the design system)
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) MaterialTheme.colorScheme.outline
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        // Google "G" lettermark — bold, Google Blue, slightly larger than body text
        Text(
            text = "G",
            color = Color(0xFF4285F4),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            fontFamily = FontFamily.SansSerif,
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = "Continue with Google",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GoogleSignInButtonPreview() {
    AutoReplyTheme {
        GoogleSignInButton(onClick = {})
    }
}
