package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.MonoCaption
import com.example.ui.viewmodel.AuthUiState

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmitLogin: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val semantic = LocalSemanticColors.current
    val focusManager = LocalFocusManager.current
    val primaryGlow = colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryGlow.copy(alpha = 0.22f),
                            primaryGlow.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.16f),
                        radius = size.maxDimension * 0.52f
                    ),
                    radius = size.maxDimension * 0.52f,
                    center = Offset(size.width * 0.5f, size.height * 0.16f)
                )
            }
            .safeDrawingPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Official DepositoDental Logo Medallion
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorScheme.primary,
                                colorScheme.secondary.copy(alpha = 0.45f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_deposito_dental),
                    contentDescription = stringResource(R.string.logo_content_description),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Security Protocol Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = colorScheme.primaryContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stringResource(R.string.login_security_badge),
                        style = MonoCaption,
                        color = colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = colorScheme.surface,
                border = BorderStroke(1.dp, colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.login_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.login_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedContainerColor = semantic.cardElevated,
                        unfocusedContainerColor = semantic.cardElevated,
                        errorContainerColor = semantic.cardElevated,
                        focusedLabelColor = colorScheme.primary,
                        cursorColor = colorScheme.primary
                    )

                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = onUsernameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input"),
                        enabled = !uiState.isLoading && !uiState.isLockedOut,
                        label = { Text(stringResource(R.string.login_username_label)) },
                        placeholder = { Text(stringResource(R.string.login_username_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = if (uiState.usernameError != null) colorScheme.error else colorScheme.primary
                            )
                        },
                        isError = uiState.usernameError != null,
                        supportingText = uiState.usernameError?.let { err ->
                            { Text(text = err, color = colorScheme.error) }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        enabled = !uiState.isLoading && !uiState.isLockedOut,
                        label = { Text(stringResource(R.string.login_password_label)) },
                        placeholder = { Text(stringResource(R.string.login_password_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (uiState.passwordError != null) colorScheme.error else colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = onTogglePasswordVisibility,
                                modifier = Modifier.testTag("login_toggle_password")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) {
                                        Icons.Rounded.VisibilityOff
                                    } else {
                                        Icons.Rounded.Visibility
                                    },
                                    contentDescription = stringResource(
                                        if (uiState.isPasswordVisible) {
                                            R.string.login_hide_password
                                        } else {
                                            R.string.login_show_password
                                        }
                                    ),
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (uiState.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        isError = uiState.passwordError != null,
                        supportingText = uiState.passwordError?.let { err ->
                            { Text(text = err, color = colorScheme.error) }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onSubmitLogin()
                            }
                        )
                    )

                    // Error / Lockout Feedback
                    AnimatedVisibility(
                        visible = uiState.generalError != null || uiState.isLockedOut,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = semantic.dangerContainer,
                            border = BorderStroke(1.dp, semantic.danger.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = semantic.danger,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = if (uiState.isLockedOut) {
                                            stringResource(R.string.login_error_locked, uiState.lockoutSecondsRemaining)
                                        } else {
                                            uiState.generalError.orEmpty()
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (!uiState.isLockedOut && uiState.failedAttempts > 0) {
                                        Text(
                                            text = stringResource(
                                                R.string.login_attempts_remaining,
                                                uiState.remainingAttemptsBeforeLockout
                                            ),
                                            style = MonoCaption,
                                            color = semantic.warning
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onSubmitLogin()
                        },
                        enabled = !uiState.isLoading && !uiState.isLockedOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("login_submit_button"),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                            disabledContainerColor = colorScheme.outline,
                            disabledContentColor = colorScheme.onSurfaceVariant
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Verificando…",
                                style = MaterialTheme.typography.labelLarge
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.login_button),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
